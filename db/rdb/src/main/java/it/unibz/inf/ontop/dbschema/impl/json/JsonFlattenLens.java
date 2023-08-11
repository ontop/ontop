
package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.LensImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@JsonDeserialize(as = JsonFlattenLens.class)
public class JsonFlattenLens extends JsonBasicOrJoinOrNestedLens {

    @Nonnull
    public final Columns columns;
    @Nonnull
    public final List<String> baseRelation;
    @Nonnull
    private final FlattenedColumn flattenedColumn;

    public final UniqueConstraints uniqueConstraints;

    public final OtherFunctionalDependencies otherFunctionalDependencies;

    public final ForeignKeys foreignKeys;

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonFlattenLens.class);

    @JsonCreator
    public JsonFlattenLens(
            @JsonProperty("name") List<String> name,
            @JsonProperty("baseRelation") List<String> baseRelation,
            @JsonProperty("flattenedColumn") FlattenedColumn flattenedColumn,
            @JsonProperty("columns") Columns columns,
            @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
            @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
            @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
            @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints,
            @JsonProperty("iriSafeConstraints") IRISafeConstraints iriSafeConstraints
    ) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, iriSafeConstraints);
        this.columns = columns;
        this.baseRelation = baseRelation;
        this.flattenedColumn = flattenedColumn;
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
    }

    @Override
    public Lens createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        ViewDefinitionCreator creator = new ViewDefinitionCreator(dbParameters);

        NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(creator.getRelationID(baseRelation));

        int parentLevel = (parentDefinition instanceof Lens)
                ? ((Lens) parentDefinition).getLevel()
                : 0;

        RelationID relationId = creator.getRelationID(name);
        IQ iq = creator.createIQ(relationId, parentDefinition);

        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new LensImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                parentLevel + 1,
                dbParameters.getCoreSingletons());
    }

    private class ViewDefinitionCreator {

        final DBParameters dbParameters;
        final QuotedIDFactory quotedIDFactory;
        final IntermediateQueryFactory iqFactory;
        final CoreUtilsFactory coreUtilsFactory;
        final SubstitutionFactory substitutionFactory;
        final AtomFactory atomFactory;
        final TermFactory termFactory;
        final DBTypeFactory dbTypeFactory;
        final CoreSingletons coreSingletons;

        ViewDefinitionCreator(DBParameters dbParameters) {
            this.dbParameters = dbParameters;
            quotedIDFactory = dbParameters.getQuotedIDFactory();
            coreSingletons = dbParameters.getCoreSingletons();
            iqFactory = coreSingletons.getIQFactory();
            coreUtilsFactory = coreSingletons.getCoreUtilsFactory();
            substitutionFactory = coreSingletons.getSubstitutionFactory();
            atomFactory = coreSingletons.getAtomFactory();
            termFactory = coreSingletons.getTermFactory();
            dbTypeFactory = dbParameters.getDBTypeFactory();
        }

        RelationID getRelationID(List<String> components) {
            return quotedIDFactory.createRelationID(components.toArray(new String[0]));
        }

        IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition) throws MetadataExtractionException {

            VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());

            ImmutableList<Attribute> attributes = parentDefinition.getAttributes();
            ImmutableMap<Integer, String> parentAttributeMap = IntStream.range(0, attributes.size()).boxed()
                    .collect(ImmutableCollectors.toMap(i -> i, i -> attributes.get(i).getID().getName()));

            ImmutableMap<String, Variable> parentVariableMap = parentAttributeMap.values().stream()
                    .collect(ImmutableCollectors.toMap(s -> s, variableGenerator::generateNewVariable));

            Optional<Variable> indexVariable = Optional.ofNullable(columns.position)
                    .map(p -> variableGenerator.generateNewVariable(normalizeAttributeName(columns.position, quotedIDFactory)));

            Variable flattenedVariable = Optional.ofNullable(parentVariableMap.get(normalizeAttributeName(flattenedColumn.name, quotedIDFactory)))
                    .orElseThrow(() -> new MetadataExtractionException("The flattened column " + flattenedColumn.name + " is not present in the base relation"));

            DBTermType flattenedDBType = dbTypeFactory.getDBTermType(flattenedColumn.datatype);
            Variable flattenedIfArrayVariable = variableGenerator.generateNewVariableFromVar(flattenedVariable);
            Variable flattenOutputVariable = variableGenerator.generateNewVariable(normalizeAttributeName(columns.newColumn, quotedIDFactory));

            ImmutableSet<Variable> projectedVariables = computeRetainedVariables(parentVariableMap, indexVariable, flattenOutputVariable);

            AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);

            DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, ImmutableList.copyOf(projectedVariables));

            ConstructionNode constructionNode = iqFactory.createConstructionNode(projectedVariables);

            FilterNode filterNode = iqFactory.createFilterNode(termFactory.getDBIsNotNull(flattenOutputVariable));

            FlattenNode flattennode = iqFactory.createFlattenNode(flattenOutputVariable, flattenedDBType.getCategory() == DBTermType.Category.ARRAY ? flattenedVariable : flattenedIfArrayVariable, indexVariable, flattenedDBType);

            ExtensionalDataNode dataNode = iqFactory.createExtensionalDataNode(parentDefinition, compose(parentAttributeMap, parentVariableMap));

            if (flattenedDBType.getCategory() == DBTermType.Category.ARRAY) {
                //If we use an array type, we do not need to add a filter to check if it really is an array.
                IQTree treeBeforeSafenessInfo = iqFactory.createUnaryIQTree(constructionNode,
                                iqFactory.createUnaryIQTree(flattennode, dataNode));

                return iqFactory.createIQ(projectionAtom, addIRISafeConstraints(treeBeforeSafenessInfo, dbParameters));
            }
            else {
                //If we use a json type or similar, we first need to check if the item is a valid array.
                ConstructionNode checkArrayConstructionNode = iqFactory.createConstructionNode(
                        Sets.union(dataNode.getVariables(), ImmutableSet.of(flattenedIfArrayVariable)).immutableCopy(),
                        substitutionFactory.<ImmutableTerm>getSubstitution(
                                flattenedIfArrayVariable,
                                termFactory.getIfElseNull(termFactory.getDBIsArray(flattenedDBType, flattenedVariable), flattenedVariable)));


                IQTree treeBeforeSafenessInfo = iqFactory.createUnaryIQTree(constructionNode,
                        iqFactory.createUnaryIQTree(filterNode,
                                iqFactory.createUnaryIQTree(flattennode,
                                        iqFactory.createUnaryIQTree(checkArrayConstructionNode, dataNode))));

                return iqFactory.createIQ(projectionAtom, addIRISafeConstraints(treeBeforeSafenessInfo, dbParameters));
            }
        }

        private ImmutableSet<Variable> computeRetainedVariables(ImmutableMap<String, Variable> parentVariableMap, Optional<Variable> positionVariable, Variable outputVariable) throws MetadataExtractionException {

            ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
            for (String keptColumn : columns.kept) {
                String normalizedName = normalizeAttributeName(keptColumn, quotedIDFactory);
                Variable var = Optional.ofNullable(parentVariableMap.get(normalizedName))
                        .orElseThrow(() -> new MetadataExtractionException("Kept column " + normalizedName + " not found in base view definition"));
                builder.add(var);
            }
            positionVariable.ifPresent(builder::add);
            builder.add(outputVariable);
            return builder.build();
        }


        private ImmutableMap<Integer, ? extends VariableOrGroundTerm> compose(ImmutableMap<Integer, String> map1, ImmutableMap<String, Variable> map2) {
            return map1.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> map2.get(e.getValue())));
        }

    }
    @Override
    public void insertIntegrityConstraints(Lens relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        CoreSingletons cs = dbParameters.getCoreSingletons();

        if (baseRelations.size() != 1) {
            throw new MetadataExtractionException("A nested view should have exactly one base relation");
        }
        NamedRelationDefinition baseRelation = baseRelations.get(0);

        insertUniqueConstraints(
                relation,
                idFactory,
                Optional.ofNullable(uniqueConstraints).map(u -> u.added).orElseGet(ImmutableList::of),
                // No UC can be inherited as such from the parent.
                ImmutableList.of(),
                cs);

        ImmutableSet<QuotedID> addedColumns = Stream.concat(
                        Optional.ofNullable(columns.position).stream(),
                        Stream.of(columns.newColumn))
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<QuotedID> keptColumns = columns.kept.stream()
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<QuotedID> hiddenColumns = baseRelation.getAttributes().stream()
                .map(Attribute::getID)
                .filter(d -> !keptColumns.contains(d))
                .collect(ImmutableCollectors.toSet());

        /*
         * FDs declared as such in the parent relation are inherited similarly to Join views.
         * UCs declared in the parent relation may be added as FDs.
         */
        insertFunctionalDependencies(
                relation,
                idFactory,
                hiddenColumns,
                addedColumns,
                Optional.ofNullable(otherFunctionalDependencies).map(d -> d.added).orElseGet(ImmutableList::of),
                inferFDsFromParentUCs(keptColumns, baseRelation),
                baseRelations,
                dbParameters.getCoreSingletons());

        insertForeignKeys(relation, metadataLookupForFK,
                Stream.concat(
                            Optional.ofNullable(foreignKeys).map(f -> f.added).orElseGet(ImmutableList::of).stream(),
                                inferForeignKeysFromParentUCs(keptColumns, baseRelation).stream()
                        )
                        .collect(ImmutableCollectors.toList()),
                baseRelations);
    }

    private ImmutableList<AddForeignKey> inferForeignKeysFromParentUCs(ImmutableSet<QuotedID> keptColumns, NamedRelationDefinition baseRelation) {

        return baseRelation.getUniqueConstraints().stream()
                .map(UniqueConstraint::getAttributes)
                .map(attributes -> attributes.stream()
                        .map(Attribute::getID)
                        .collect(ImmutableCollectors.toSet()))
                .map(attributes -> getInferredForeignKey(attributes, keptColumns))
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toList());
    }

    private Optional<AddForeignKey> getInferredForeignKey(ImmutableSet<QuotedID> ucs, ImmutableSet<QuotedID> keptColumns) {
        if (keptColumns.containsAll(ucs)) {
            return Optional.of(new AddForeignKey(
                    UUID.randomUUID().toString(),
                    ucs.stream()
                            .map(QuotedID::getSQLRendering)
                            .collect(ImmutableCollectors.toList()),
                    new ForeignKeyPart(baseRelation, ucs.stream()
                            .map(QuotedID::getSQLRendering)
                            .collect(ImmutableCollectors.toList()))));
        }
        return Optional.empty();
    }


    private ImmutableList<FunctionalDependencyConstruct> inferFDsFromParentUCs(ImmutableSet<QuotedID> keptColumns, NamedRelationDefinition baseRelation) {

        return baseRelation.getUniqueConstraints().stream()
                .map(UniqueConstraint::getAttributes)
                .map(attributes -> attributes.stream()
                        .map(Attribute::getID)
                        .collect(ImmutableCollectors.toSet()))
                .map(attributes -> getInferredFD(attributes, keptColumns))
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toList());
    }

    private Optional<FunctionalDependencyConstruct> getInferredFD(ImmutableSet<QuotedID> determinants, ImmutableSet<QuotedID> keptColumns) {
        if (keptColumns.containsAll(determinants)) {
            ImmutableSet<QuotedID> difference = Sets.difference(keptColumns, determinants).immutableCopy();
            if (!difference.isEmpty()) {
                return Optional.of(new FunctionalDependencyConstruct(determinants, difference));
            }
        }
        return Optional.empty();
    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(Lens lens,
                                                                                    ImmutableList<Attribute> parentAttributes) {
        return ImmutableList.of();
    }

    @JsonPropertyOrder({
            "kept",
            "new",
            "position",
    })
    private static class Columns extends JsonOpenObject {
        @Nonnull
        public final List<String> kept;
        @Nonnull
        public final String newColumn;

        public final String position;

        @JsonCreator
        public Columns(@JsonProperty("kept") List<String> kept,
                       @JsonProperty("new") String newColumn,
                       @JsonProperty("position") String position) {
            this.kept = kept;
            this.newColumn = newColumn;
            this.position = position;
        }
    }

    @JsonPropertyOrder({
            "name",
            "datatype",
    })
    private static class FlattenedColumn extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String datatype;

        @JsonCreator
        public FlattenedColumn(@JsonProperty("name") String name,
                               @JsonProperty("datatype") String datatype) {
            this.name = name;
            this.datatype = datatype;
        }
    }
}
