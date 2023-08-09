package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.LensImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@JsonDeserialize(as = JsonUnionLens.class)
public class JsonUnionLens extends JsonLens {
    @Nonnull
    public final List<List<String>> unionRelations;

    @Nonnull
    public final String provenanceColumn;

    public final boolean makeDistinct;

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonUnionLens.class);

    @JsonCreator
    public JsonUnionLens(@JsonProperty("name") List<String> name,
                         @JsonProperty(value = "unionRelations", required = true) List<List<String>> unionRelations,
                         @JsonProperty(value = "provenanceColumn") String provenanceColumn,
                         @JsonProperty("makeDistinct") boolean makeDistinct,
                         @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                         @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                         @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
                         @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints,
                         @JsonProperty("iriSafeConstraints") IRISafeConstraints iriSafeConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, iriSafeConstraints);
        this.unionRelations = unionRelations;
        this.provenanceColumn = provenanceColumn;
        this.makeDistinct = makeDistinct;
    }

    @Override
    public Lens createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, dbParameters, parentCacheMetadataLookup);

        int maxParentLevel = extractMaxParentLevel(iq, dbParameters.getCoreSingletons());

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

        insertUniqueConstraints(relation, idFactory, uniqueConstraints == null ? List.of() : uniqueConstraints.added);

        insertFunctionalDependencies(relation, idFactory, otherFunctionalDependencies == null ? List.of() : otherFunctionalDependencies.added, dbParameters.getCoreSingletons());

        if (foreignKeys != null)
            insertForeignKeys(relation, metadataLookupForFK, foreignKeys.added);
    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(Lens lens, ImmutableList<Attribute> parentAttributes) {
        return ImmutableList.of();
    }

    protected ImmutableMap<String, Variable> extractProjectedVariables(
            DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup) throws MetadataExtractionException {
        VariableGenerator variableGenerator = dbParameters.getCoreSingletons().getCoreUtilsFactory().createVariableGenerator(ImmutableList.of());

        if (unionRelations.size() < 2)
            throw new MetadataExtractionException("At least two relations are expected");

        ImmutableList<NamedRelationDefinition> parents = getParentRelations(dbParameters, parentCacheMetadataLookup);
        NamedRelationDefinition firstParent = parents.get(0);

        if (parents.stream().skip(1).anyMatch(parent -> !areAllAttributesEqual(firstParent, parent)))
            throw new MetadataExtractionException("The relations provided to the union do not have equal attributes");

        ImmutableMap<String, Variable> projectedVariables = firstParent.getAttributes().stream()
                .map(Attribute::getID)
                .collect(ImmutableCollectors.toMap(QuotedID::getName, id -> variableGenerator.generateNewVariable(id.getName())));
        return projectedVariables;
    }

    private static boolean areAllAttributesEqual(NamedRelationDefinition a, NamedRelationDefinition b) {
        return a.getAttributes().stream().noneMatch(
                attributeA -> b.getAttributes().stream().noneMatch(
                        attributeB -> areAttributesEqual(attributeA, attributeB)))

                && b.getAttributes().stream().noneMatch(
                attributeB -> a.getAttributes().stream().noneMatch(
                        attributeA -> areAttributesEqual(attributeA, attributeB)));
    }

    private static boolean areAttributesEqual(Attribute a, Attribute b) {
        //Performs equality check on attributes based on their name and their data type.
        //Currently, data types must be exactly equal. Could be extended by allowing related types.
        return a.getID().getName().equals(b.getID().getName()) && a.getTermType().equals(b.getTermType());
    }

    //Translates the union relation names into their actual relation definitions
    private ImmutableList<NamedRelationDefinition> getParentRelations(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup) {

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        return this.unionRelations.stream().map(name -> {
                try {
                    return parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                            name.toArray(new String[0])));
                } catch (MetadataExtractionException e) {
                    throw new RuntimeException(e);
                }
            }).collect(ImmutableCollectors.toList());
    }

    private IQ createIQ(RelationID relationId, DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {


        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();

        //Get list of all projected variables (including provenance column, if applicable)
        ImmutableMap<String, Variable> projectedVariablesMap = this.extractProjectedVariables(dbParameters, parentCacheMetadataLookup);
        ImmutableSet<Variable> projectedVariables = ImmutableSet.copyOf(projectedVariablesMap.values());
        ImmutableSet<Variable> allProjectedVariables;
        if (includesProvenanceColumn()) {
            Variable provenanceVariable = termFactory.getVariable(getProvenanceColumn(quotedIDFactory));
            if (projectedVariables.contains(provenanceVariable))
                throw new MetadataExtractionException("The provenance column with the name " + provenanceVariable.getName()
                    + " cannot be added, because a column with this name already exists.");
            allProjectedVariables = Sets.union(projectedVariables, ImmutableSet.of(provenanceVariable)).immutableCopy();
        }
        else {
            allProjectedVariables = projectedVariables;
        }

        //Generate children as ExtensionalDataNodes
        ImmutableList<IQTree> children = getParentRelations(dbParameters, parentCacheMetadataLookup).stream()
                .map(child -> iqFactory.createExtensionalDataNode(child, IntStream.range(0, projectedVariablesMap.size()).boxed().collect(
                        ImmutableCollectors.toMap(
                                i -> i,
                                i -> projectedVariablesMap.get(child.getAttribute(i + 1).getID().getName()))
                ))).collect(ImmutableCollectors.toList());

        //Add provenance column to each child
        ImmutableList<IQTree> extendedChildren;
        if(includesProvenanceColumn()) {
            extendedChildren = children.stream().map(
                    child -> addConstantColumn(
                            termFactory.getVariable(getProvenanceColumn(quotedIDFactory)),
                            quotedIDFactory.createRelationID(unionRelations.get(children.indexOf(child)).toArray(new String[0]))
                                    .getComponents().reverse().stream().map(QuotedID::getName).collect(Collectors.joining(".")),
                            child,
                            coreSingletons
                    )
            ).collect(ImmutableCollectors.toList());
        } else {
            extendedChildren = children;
        }


        //Create union of children
        UnionNode union = iqFactory.createUnionNode(allProjectedVariables);
        IQTree iqTree = iqFactory.createNaryIQTree(union, extendedChildren);

        if (this.makeDistinct)
            iqTree = iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), iqTree);

        NotYetTypedEqualityTransformer notYetTypedEqualityTransformer = coreSingletons.getNotYetTypedEqualityTransformer();
        IQTree transformedTree = notYetTypedEqualityTransformer.transform(iqTree);

        IQTree finalTree = addIRISafeConstraints(transformedTree, dbParameters);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, allProjectedVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, ImmutableList.copyOf(allProjectedVariables));

        //normalization will be done later on when unique constraint information from parents is available
        return iqFactory.createIQ(projectionAtom, finalTree);
    }

    private IQTree addConstantColumn(Variable variable, String value, IQTree child, CoreSingletons coreSingletons) {
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        ConstructionSubstitutionNormalizer substitutionNormalizer = coreSingletons.getConstructionSubstitutionNormalizer();
        TermFactory termFactory = coreSingletons.getTermFactory();

        DBConstant provenanceValue = termFactory.getDBStringConstant(value);
        Substitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(variable, provenanceValue);
        ImmutableSet<Variable> allProjectedVariables = Sets.union(child.getKnownVariables(), ImmutableSet.of(variable)).immutableCopy();
        ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization =
                substitutionNormalizer.normalizeSubstitution(substitution,
                        allProjectedVariables);
        ConstructionNode constructionNode = normalization.generateTopConstructionNode().get();
        return iqFactory.createUnaryIQTree(constructionNode, child);
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

    protected void insertUniqueConstraints(Lens relation, QuotedIDFactory idFactory,
                                           List<AddUniqueConstraints> addUniqueConstraints)
            throws MetadataExtractionException {

        List<AddUniqueConstraints> allAddUniqueConstraints = Stream.concat(
                    addUniqueConstraints.stream(),
                    inferUniqueConstraints(relation).stream()
                ).collect(Collectors.toList());

        for (AddUniqueConstraints addUC : allAddUniqueConstraints) {
            if (addUC.isPrimaryKey != null && addUC.isPrimaryKey) LOGGER.warn("Primary key set in the view file for " + addUC.name);
            FunctionalDependency.Builder builder = UniqueConstraint.builder(relation, addUC.name);
            JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
            builder.build();
        }

    }

    private ImmutableList<AddUniqueConstraints> inferUniqueConstraints(Lens relation) {

        IQTree iqTree = relation.getIQ().normalizeForOptimization().getTree();
        ImmutableSet<ImmutableSet<Variable>> constraints = iqTree.inferUniqueConstraints();

        DistinctVariableOnlyDataAtom projectedAtom = relation.getIQ().getProjectionAtom();

        ImmutableMap<Variable, QuotedID> variableIds = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(
                        a -> projectedAtom.getTerm(a.getIndex() - 1),
                        Attribute::getID));

        return constraints.stream()
                .map(vs -> new AddUniqueConstraints(
                        UUID.randomUUID().toString(),
                        vs.stream()
                                .map(v -> Optional.ofNullable(variableIds.get(v))
                                        .orElseThrow(() -> new MinorOntopInternalBugException(
                                                "The variables of the unique constraints should be projected")))
                                .map(QuotedID::getSQLRendering)
                                .collect(ImmutableCollectors.toList()),
                        // PK by default false
                        false
                ))
                .collect(ImmutableCollectors.toList());
    }

    private void insertFunctionalDependencies(NamedRelationDefinition relation,
                                              QuotedIDFactory idFactory,
                                              List<AddFunctionalDependency> addFunctionalDependencies,
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

    private void insertForeignKeys(NamedRelationDefinition relation, MetadataLookup lookup, List<AddForeignKey> addForeignKeys)
            throws MetadataExtractionException {

        for (AddForeignKey fk : addForeignKeys) {
            insertForeignKey(relation, lookup, fk);
        }
    }

    protected void insertForeignKey(NamedRelationDefinition relation, MetadataLookup lookup, AddForeignKey addForeignKey) throws MetadataExtractionException {

        QuotedIDFactory idFactory = lookup.getQuotedIDFactory();

        RelationID targetRelationId = JsonMetadata.deserializeRelationID(idFactory, addForeignKey.to.relation);
        NamedRelationDefinition targetRelation;
        try {
            targetRelation = lookup.getRelation(targetRelationId);
        }
        catch (MetadataExtractionException e) {
            LOGGER.info("Cannot find relation {} for FK {}", targetRelationId, addForeignKey.name);
            return;
        }


        int columnCount = addForeignKey.to.columns.size();
        if (addForeignKey.from.size() != columnCount)
            throw new MetadataExtractionException("Not the same number of from and to columns in FK definition");

        try {
            ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(addForeignKey.name, relation, targetRelation);
            for (int i = 0; i < columnCount; i++) {
                builder.add(
                        idFactory.createAttributeID(addForeignKey.from.get(i)),
                        idFactory.createAttributeID(addForeignKey.to.columns.get(i)));
            }
            builder.build();
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private boolean includesProvenanceColumn() {
        return provenanceColumn != null && !provenanceColumn.isEmpty();
    }

    private String getProvenanceColumn(QuotedIDFactory quotedIDFactory) {
        return quotedIDFactory.createAttributeID(this.provenanceColumn).getName();
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
