package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
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
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;

@JsonDeserialize(as = JsonSQLView.class)
public class JsonSQLView extends JsonView {
    @Nonnull
    public final String query;

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonSQLView.class);

    @JsonCreator
    public JsonSQLView(@JsonProperty("name") List<String> name,
                       @JsonProperty("query") String query,
                       @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                       @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                       @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
                       @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints);
        this.query = query;
    }

    @Override
    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
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

        return new OntopViewDefinitionImpl(
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
    public void insertIntegrityConstraints(OntopViewDefinition relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {
        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        if (uniqueConstraints != null)
            insertUniqueConstraints(relation, idFactory, uniqueConstraints.added);

        if (otherFunctionalDependencies != null)
            insertFunctionalDependencies(relation, idFactory, otherFunctionalDependencies.added);

    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(OntopViewDefinition ontopViewDefinition, ImmutableList<Attribute> parentAttributes) {
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

        RAExpression2IQConverter raExpression2IQConverter = new RAExpression2IQConverter(coreSingletons);

        IQTree initialChild;
        RAExpression raExpression;
        try {
            raExpression = extractRAExpression(dbParameters, parentCacheMetadataLookup);
            initialChild = raExpression2IQConverter.convert(raExpression);
        } catch (JSQLParserException | UnsupportedSelectQueryException | InvalidQueryException e) {
            throw new MetadataExtractionException("Unsupported expression for " + ":\n" + e);
        }

        ImmutableMap<QualifiedAttributeID, Variable> attributeVariableMap = raExpression.getAttributes().asMap().keySet().stream()
                .collect(ImmutableCollectors.toMap(
                        k -> k,
                        k -> termFactory.getVariable(k.getAttribute().getName())
                ));

        ImmutableList<Variable> atomVariables = raExpression.getAttributes().asMap().keySet().stream()
                .map(attributeVariableMap::get)
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> projectedVariables = ImmutableSet.copyOf(atomVariables);

        if (atomVariables.size() != projectedVariables.size()) {
            ImmutableMultiset<Variable> multiSet = ImmutableMultiset.copyOf(atomVariables);
            ImmutableSet<Variable> conflictingVariables = multiSet.stream()
                    .filter(v -> multiSet.count(v) > 1)
                    .collect(ImmutableCollectors.toSet());

            throw new MetadataExtractionException("The following projected columns have multiple possible provenances: "
                    + conflictingVariables);
        }

        ImmutableMap<Variable, ImmutableTerm> ascendingSubstitutionMap = raExpression.getAttributes().asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> attributeVariableMap.get(e.getKey()),
                        Map.Entry::getValue));

        ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(
                substitutionFactory.getSubstitution(ascendingSubstitutionMap),
                projectedVariables);

        IQTree updatedChild = normalization.updateChild(initialChild);

        IQTree iqTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(projectedVariables, normalization.getNormalizedSubstitution()),
                updatedChild);

        NotYetTypedEqualityTransformer notYetTypedEqualityTransformer = coreSingletons.getNotYetTypedEqualityTransformer();
        IQTree transformedTree = notYetTypedEqualityTransformer.transform(iqTree);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, atomVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, atomVariables);

        return iqFactory.createIQ(projectionAtom, transformedTree)
                .normalizeForOptimization();
    }

    private RAExpression extractRAExpression(DBParameters dbParameters, MetadataLookup metadataLookup)
            throws JSQLParserException, UnsupportedSelectQueryException, InvalidQueryException, MetadataExtractionException {
        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
        SQLQueryParser sq = new SQLQueryParser(coreSingletons);
        return sq.getRAExpression(query, metadataLookup);
    }

    private AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryViewPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .mapToObj(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    private void insertUniqueConstraints(NamedRelationDefinition relation,
                                         QuotedIDFactory idFactory,
                                         List<JsonSQLView.AddUniqueConstraints> addUniqueConstraints)
            throws MetadataExtractionException {

        for (JsonSQLView.AddUniqueConstraints addUC : addUniqueConstraints) {
            if (addUC.isPrimaryKey != null && addUC.isPrimaryKey) LOGGER.warn("Primary key set in the view file for " + addUC.name);
            FunctionalDependency.Builder builder = UniqueConstraint.builder(relation, addUC.name);
            JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
            builder.build();
        }
    }

    private void insertFunctionalDependencies(NamedRelationDefinition relation,
                                              QuotedIDFactory idFactory,
                                              List<JsonSQLView.AddFunctionalDependency> addFunctionalDependencies)
            throws MetadataExtractionException {

        for (JsonSQLView.AddFunctionalDependency addFD : addFunctionalDependencies) {
            FunctionalDependency.Builder builder = FunctionalDependency.defaultBuilder(relation);

            try {
                JsonMetadata.deserializeAttributeList(idFactory, addFD.determinants, builder::addDeterminant);
                JsonMetadata.deserializeAttributeList(idFactory, addFD.dependents, builder::addDependent);
                builder.build();
            }
            catch (MetadataExtractionException e) {
                throw new MetadataExtractionException(String.format(
                        "Cannot find attribute for Functional Dependency %s", addFD.determinants));
            }
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
            int level = (parentRelation instanceof OntopViewDefinition)
                    ? ((OntopViewDefinition) parentRelation).getLevel()
                    : 0;
            maxLevel = Math.max(maxLevel, level);
            return dataNode;
        }
    }
}
