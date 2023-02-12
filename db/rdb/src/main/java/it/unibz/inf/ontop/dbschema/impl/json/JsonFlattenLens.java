
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
import it.unibz.inf.ontop.substitution.Substitution;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.JSON;

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

        NamedRelationDefinition parentDefinition = creator.extractParentDefinition(parentCacheMetadataLookup);

        int parentLevel = (parentDefinition instanceof Lens)?
                ((Lens) parentDefinition).getLevel():
                0;

        RelationID relationId = dbParameters.getQuotedIDFactory().createRelationID(name.toArray(new String[0]));

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
        final CoreSingletons coreSingletons;
        final QuotedIDFactory quotedIDFactory;
        final IntermediateQueryFactory iqFactory;
        final CoreUtilsFactory coreUtilsFactory;
        final SubstitutionFactory substitutionFactory;
        final AtomFactory atomFactory;
        final TermFactory termFactory;
        final DBTypeFactory dbTypeFactory;

        ViewDefinitionCreator(DBParameters dbParameters) {
            this.dbParameters = dbParameters;
            quotedIDFactory = dbParameters.getQuotedIDFactory();
            coreSingletons = dbParameters.getCoreSingletons();
            iqFactory = coreSingletons.getIQFactory();
            coreUtilsFactory = coreSingletons.getCoreUtilsFactory();
            substitutionFactory = coreSingletons.getSubstitutionFactory();
            atomFactory = coreSingletons.getAtomFactory();
            termFactory = coreSingletons.getTermFactory();
            dbTypeFactory =  dbParameters.getDBTypeFactory();
        }

        NamedRelationDefinition extractParentDefinition(MetadataLookup parentCacheMetadataLookup) throws MetadataExtractionException {
            return parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(baseRelation.toArray(new String[0])));
        }

        IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition) throws MetadataExtractionException {

            VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());

            ImmutableList<Attribute> attributes = parentDefinition.getAttributes();
            ImmutableMap<Integer, QuotedID> parentAttributeMap = IntStream.range(0, attributes.size()).boxed()
                    .collect(ImmutableCollectors.toMap(i -> i, i -> attributes.get(i).getID()));

            ImmutableMap<String, Variable> parentVariableMap = parentAttributeMap.values().stream()
                    .map(QuotedID::getName)
                    .collect(ImmutableCollectors.toMap(s -> s, variableGenerator::generateNewVariable));

            Optional<Variable> indexVariable = Optional.ofNullable(columns.position)
                    .map(p -> normalizeAttributeName(p, quotedIDFactory))
                    .map(p -> variableGenerator.generateNewVariable());

            ImmutableSet<Variable> retainedVariables = computeRetainedVariables(parentVariableMap, indexVariable);

            Variable flattenedVariable = parentVariableMap.get(normalizeAttributeName(flattenedColumn.name, quotedIDFactory));
            if (flattenedVariable == null) {
                throw new MetadataExtractionException("The flattened column " + flattenedColumn.name + " is not present in the base relation");
            }

            DBTermType flattenedDBType = dbTypeFactory.getDBTermType(flattenedColumn.datatype);

            Variable flattenedIfArrayVariable = variableGenerator.generateNewVariableFromVar(flattenedVariable);
            Variable flattenOutputVariable = variableGenerator.generateNewVariable("O");

            Substitution<ImmutableTerm> extractionSubstitution = substitutionFactory.getSubstitutionThrowsExceptions(
                    columns.extracted,
                    c -> variableGenerator.generateNewVariable(normalizeAttributeName(c.name, quotedIDFactory)),
                    c -> getCheckDatatypeExtractAndCastFromJson(
                            flattenOutputVariable, flattenedDBType, getPath(c), c.datatype, c.name));

            ImmutableSet<Variable> projectedVariables = Sets.union(retainedVariables, extractionSubstitution.getDomain()).immutableCopy();

            AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);

            DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, ImmutableList.copyOf(projectedVariables));

            ConstructionNode extractionConstructionNode = iqFactory.createConstructionNode(projectedVariables, extractionSubstitution);

            FilterNode filterNode = iqFactory.createFilterNode(termFactory.getDBIsNotNull(flattenOutputVariable));

            FlattenNode flattennode = iqFactory.createFlattenNode(
                    flattenOutputVariable,
                    flattenedIfArrayVariable,
                    indexVariable,
                    flattenedDBType);

            ExtensionalDataNode dataNode = iqFactory.createExtensionalDataNode(parentDefinition, compose(parentAttributeMap, parentVariableMap));

            ImmutableSet<Variable> subtreeVars = dataNode.getVariables();

            ConstructionNode checkArrayConstructionNode = iqFactory.createConstructionNode(
                    Sets.union(subtreeVars, ImmutableSet.of(flattenedIfArrayVariable)).immutableCopy(),
                    substitutionFactory.<ImmutableTerm>getSubstitution(
                            flattenedIfArrayVariable,
                            termFactory.getIfElseNull(termFactory.getDBIsArray(flattenedDBType, flattenedVariable), flattenedVariable)));


            IQTree treeBeforeSafenessInfo = iqFactory.createUnaryIQTree(extractionConstructionNode,
                    iqFactory.createUnaryIQTree(filterNode,
                            iqFactory.createUnaryIQTree(flattennode,
                                    iqFactory.createUnaryIQTree(checkArrayConstructionNode, dataNode))));

            return iqFactory.createIQ(projectionAtom, addIRISafeConstraints(treeBeforeSafenessInfo, dbParameters));
        }

        ImmutableSet<Variable> computeRetainedVariables(ImmutableMap<String, Variable> parentVariableMap, Optional<Variable> positionVariable) throws MetadataExtractionException {

            ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
            for(String keptColumn : columns.kept) {
                String normalizedName = normalizeAttributeName(keptColumn, quotedIDFactory);
                Variable var = parentVariableMap.get(normalizedName);
                if (var == null) {
                    throw new MetadataExtractionException("Kept column " + normalizedName + " not found in base view definition");
                }
                builder.add(var);
            }
            positionVariable.ifPresent(builder::add);
            return builder.build();
        }

        ImmutableMap<Integer,? extends VariableOrGroundTerm> compose(ImmutableMap<Integer, QuotedID> map1, ImmutableMap<String, Variable> map2) {
            return map1.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> map2.get(e.getValue().getName())));
        }

        ImmutableList<String> getPath(ExtractedColumn col) {
            return col.key == null ?
                    ImmutableList.of():
                    ImmutableList.copyOf(col.key);
        }

        /**
         * If no expected DB type is specified, then do not cast the value (leave it as a JSON value)
         */
        private ImmutableFunctionalTerm getCheckDatatypeExtractAndCastFromJson(Variable sourceVar, DBTermType flattenedDBType,
                                                                               ImmutableList<String> path,
                                                                               String datatypeString, String columnName)
                throws MetadataExtractionException {

            DBTermType termType = dbTypeFactory.getDBTermType(datatypeString);

            ImmutableFunctionalTerm cast = getCast(termType, sourceVar, path);

            if (termType.getCategory() == JSON) {
                return cast;
            }
            return termFactory.getIfElseNull(
                    getDatatypeCondition(flattenedDBType, termFactory.getDBJsonElement(sourceVar, path), termType, columnName),
                    cast);
        }


        ImmutableFunctionalTerm getCast(DBTermType columnTermType, Variable sourceVar, ImmutableList<String> path) {

            ImmutableFunctionalTerm retrieveEltAsText = termFactory.getDBJsonElementAsText(sourceVar, path);

            // TODO: consider the input type as well (could enable more simplification, e.g. no cast when same datatype)
            return termFactory.getDBCastFunctionalTerm(columnTermType, retrieveEltAsText);
        }



        ImmutableExpression getDatatypeCondition(DBTermType flattenedDBType, ImmutableFunctionalTerm arg,
                                                         DBTermType columnTermType, String columnName) throws MetadataExtractionException {

            switch (columnTermType.getCategory()) {
                case BOOLEAN:
                    return termFactory.getDBJsonIsBoolean(flattenedDBType, arg);
                case INTEGER:
                case FLOAT_DOUBLE:
                case DECIMAL:
                    return termFactory.getDBJsonIsNumber(flattenedDBType, arg);
                case ARRAY:
                    // TODO: remove this restriction
                    throw new MetadataExtractionException(
                            "Array datatypes are currently not supported for extracted column from a flatten lens. Column: "
                                    + columnName);
                case STRING:
                    // By default, treat it as a string
                default:
                    return termFactory.getDBJsonIsScalar(flattenedDBType, arg);
            }
        }
    }





    @Override
    public void insertIntegrityConstraints(Lens relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory quotedIDFactory = metadataLookupForFK.getQuotedIDFactory();
        CoreSingletons cs = dbParameters.getCoreSingletons();

        if (baseRelations.size() != 1) {
            throw new MetadataExtractionException("A nested view should have exactly one base relation");
        }
        NamedRelationDefinition baseRelation = baseRelations.get(0);

        insertUniqueConstraints(
                relation,
                quotedIDFactory,
                (uniqueConstraints != null) ? uniqueConstraints.added : ImmutableList.of(),
                /*
                 * No UC can be inherited as such from the parent.
                 */
                ImmutableList.of(),
                cs
        );

        ImmutableSet<QuotedID> addedColumns = Stream.concat(
                        Stream.of(columns.position),
                        columns.extracted.stream().map(a -> a.name))
                .map(quotedIDFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<QuotedID> keptColumns = columns.kept.stream()
                .map(quotedIDFactory::createAttributeID)
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
                quotedIDFactory,
                hiddenColumns,
                addedColumns,
                (otherFunctionalDependencies != null) ? otherFunctionalDependencies.added : ImmutableList.of(),
                inferFDsFromParentUCs(keptColumns, baseRelation),
                baseRelations
        );

        insertForeignKeys(relation, metadataLookupForFK,
                (foreignKeys != null) ? foreignKeys.added : ImmutableList.of(),
                baseRelations);
    }

    private ImmutableList<FunctionalDependencyConstruct> inferFDsFromParentUCs(ImmutableSet<QuotedID> keptColumns, NamedRelationDefinition baseRelation) {

        return baseRelation.getUniqueConstraints().stream()
                        .map(UniqueConstraint::getAttributes)
                        .map(attributes -> attributes.stream()
                                .map(Attribute::getID)
                                .collect(ImmutableCollectors.toSet()))
                        .map(attributes -> getInferredFD(attributes, keptColumns))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(ImmutableCollectors.toList());
    }

    private Optional<FunctionalDependencyConstruct> getInferredFD(ImmutableSet<QuotedID> determinants, ImmutableSet<QuotedID> keptColumns) {
        if (keptColumns.containsAll(determinants)) {
            ImmutableSet<QuotedID> difference = Sets.difference(keptColumns, determinants).immutableCopy();
            if(!difference.isEmpty()) {
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
            "extracted",
            "position",
    })
    private static class Columns extends JsonOpenObject {
        @Nonnull
        public final List<String> kept;
        @Nonnull
        public final List<ExtractedColumn> extracted;

        public final String position;

        @JsonCreator
        public Columns(@JsonProperty("kept") List<String> kept,
                       @JsonProperty("extracted") List<ExtractedColumn> extracted,
                       @JsonProperty("position") String position
        ) {
            this.kept = kept;
            this.extracted = extracted;
            this.position = position;
        }
    }

    @JsonPropertyOrder({
            "name",
            "datatype",
            "key",
    })
    private static class ExtractedColumn extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String datatype;

        public final List<String> key;

        @JsonCreator
        public ExtractedColumn(@JsonProperty("name") String name,
                               @JsonProperty("datatype") String datatype,
                               @JsonProperty("key") List<String> key) {
            this.name = name;
            this.datatype = datatype;
            this.key = key;
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
