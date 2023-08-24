package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.LensImpl;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;

@JsonDeserialize(as = JsonSQLLens.class)
public class JsonSQLLens extends JsonLens {
    @Nonnull
    public final String query;

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonSQLLens.class);

    @JsonCreator
    public JsonSQLLens(@JsonProperty("name") List<String> name,
                       @JsonProperty("query") String query,
                       @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                       @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                       @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
                       @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints,
                       @JsonProperty("iriSafeConstraints") IRISafeConstraints iriSafeConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, iriSafeConstraints);
        this.query = query;
    }

    @Override
    public Lens createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, dbParameters, parentCacheMetadataLookup);

        int maxParentLevel = extractMaxParentLevel(iq, dbParameters.getCoreSingletons());

        if (maxParentLevel > 0)
            LOGGER.warn("It is dangerous to build SQLViewDefinitions above OntopViewDefinitions, " +
                    "because the view definition will fail if the SQL query cannot be parsed by Ontop");

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new LensImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                maxParentLevel + 1,
                dbParameters.getCoreSingletons());
    }

    private int extractMaxParentLevel(IQ iq, CoreSingletons coreSingletons) {
        LevelExtractor transformer = new LevelExtractor(coreSingletons);
        // Side-effect (cheap but good enough implementation)
        transformer.transform(iq.getTree());
        return transformer.getMaxLevel();
    }

    @Override
    public void insertIntegrityConstraints(Lens relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {
        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        if (uniqueConstraints != null)
            insertUniqueConstraints(relation, idFactory, uniqueConstraints.added);

        if (otherFunctionalDependencies != null)
            insertFunctionalDependencies(relation, idFactory, otherFunctionalDependencies.added, dbParameters.getCoreSingletons());

    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(Lens lens, ImmutableList<Attribute> parentAttributes) {
        return ImmutableList.of();
    }


    private IQ createIQ(RelationID relationId, DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();
        ConstructionSubstitutionNormalizer substitutionNormalizer = coreSingletons.getConstructionSubstitutionNormalizer();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();

        IQTree initialChild;
        RAExpression raExpression;
        try {
            SQLQueryParser sq = new SQLQueryParser(coreSingletons);
            raExpression = sq.getRAExpression(query, parentCacheMetadataLookup);
            initialChild = sq.convert(raExpression);
        }
        catch (InvalidQueryException e) {
            throw new MetadataExtractionException("Unsupported expression for " + ":\n" + e);
        }

        Substitution<ImmutableTerm> ascendingSubstitution = raExpression.getUnqualifiedAttributes().entrySet().stream()
                .collect(substitutionFactory.toSubstitution(
                        e -> termFactory.getVariable(e.getKey().getName()),
                        Map.Entry::getValue));

        ImmutableSet<Variable> projectedVariables = ascendingSubstitution.getDomain();

        VariableGenerator variableGenerator = coreSingletons.getCoreUtilsFactory().createVariableGenerator(
                Sets.union(initialChild.getKnownVariables(), projectedVariables));

        ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(ascendingSubstitution, projectedVariables);

        IQTree updatedChild = normalization.updateChild(initialChild, variableGenerator);

        IQTree iqTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(projectedVariables, normalization.getNormalizedSubstitution()),
                updatedChild);

        NotYetTypedEqualityTransformer notYetTypedEqualityTransformer = coreSingletons.getNotYetTypedEqualityTransformer();
        IQTree transformedTree = notYetTypedEqualityTransformer.transform(iqTree);

        IQTree finalTree = addIRISafeConstraints(transformedTree, dbParameters);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, ImmutableList.copyOf(projectedVariables));

        return iqFactory.createIQ(projectionAtom, finalTree)
                .normalizeForOptimization();
    }

    private AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryLensPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .mapToObj(i -> dbRootType)
                        .collect(ImmutableCollectors.toList()));
    }

    private void insertUniqueConstraints(NamedRelationDefinition relation,
                                         QuotedIDFactory idFactory,
                                         List<JsonSQLLens.AddUniqueConstraints> addUniqueConstraints)
            throws MetadataExtractionException {

        for (JsonSQLLens.AddUniqueConstraints addUC : addUniqueConstraints) {
            if (addUC.isPrimaryKey != null && addUC.isPrimaryKey) LOGGER.warn("Primary key set in the view file for " + addUC.name);
            FunctionalDependency.Builder builder = UniqueConstraint.builder(relation, addUC.name);
            JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
            builder.build();
        }
    }

    private void insertFunctionalDependencies(NamedRelationDefinition relation,
                                              QuotedIDFactory idFactory,
                                              List<JsonSQLLens.AddFunctionalDependency> addFunctionalDependencies,
                                              CoreSingletons coreSingletons)
            throws MetadataExtractionException {

        try {
            insertTransitiveFunctionalDependencies(
                    addFunctionalDependencies.stream()
                        .map(jsonFD -> new FunctionalDependencyConstruct(
                            jsonFD.determinants.stream()
                                    .map(idFactory::createAttributeID)
                                    .collect(ImmutableCollectors.toSet()),
                            jsonFD.dependents.stream()
                                    .map(idFactory::createAttributeID)
                                    .collect(ImmutableCollectors.toSet())))
                        .collect(ImmutableCollectors.toSet()),
                    relation, coreSingletons);
        } catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(String.format(
                    "Cannot find attribute %s for Functional Dependency.", e.getAttributeID()));
        }

    }

    private static class LevelExtractor extends DefaultRecursiveIQTreeVisitingTransformer {
        // Non-final
        int maxLevel;

        public int getMaxLevel() {
            return maxLevel;
        }

        public LevelExtractor(CoreSingletons coreSingletons) {
            super(coreSingletons);
            maxLevel = 0;
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            RelationDefinition parentRelation = dataNode.getRelationDefinition();
            int level = (parentRelation instanceof Lens)
                    ? ((Lens) parentRelation).getLevel()
                    : 0;
            maxLevel = Math.max(maxLevel, level);
            return dataNode;
        }
    }
}
