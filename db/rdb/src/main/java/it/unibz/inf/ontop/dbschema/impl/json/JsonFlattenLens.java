
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
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.JSON;

@JsonDeserialize(as = JsonFlattenLens.class)
public class JsonFlattenLens extends JsonBasicOrJoinOrNestedView {

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
            @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints
    ) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints);
        this.columns = columns;
        this.baseRelation = baseRelation;
        this.flattenedColumn = flattenedColumn;
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
    }

    @Override
    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        NamedRelationDefinition parentDefinition = extractParentDefinition(dbParameters, parentCacheMetadataLookup);

        int parentLevel = (parentDefinition instanceof OntopViewDefinition)?
                ((OntopViewDefinition) parentDefinition).getLevel():
                0;

        RelationID relationId = dbParameters.getQuotedIDFactory().createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, parentDefinition, dbParameters);

        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new OntopViewDefinitionImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                parentLevel + 1,
                dbParameters.getCoreSingletons());
    }

    private NamedRelationDefinition extractParentDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup) throws MetadataExtractionException {
            QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
            return parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                    baseRelation.toArray(new String[0])));
    }

    protected IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition, DBParameters dbParameters) throws MetadataExtractionException {

        CoreSingletons cs = dbParameters.getCoreSingletons();
        IntermediateQueryFactory iqFactory = cs.getIQFactory();
        VariableGenerator variableGenerator = cs.getCoreUtilsFactory().createVariableGenerator(ImmutableSet.of());
        QuotedIDFactory idFactory = dbParameters.getQuotedIDFactory();
        TermFactory termFactory = cs.getTermFactory();

        ImmutableMap<Integer, String> parentAttributeMap = buildParentIndex2AttributeMap(parentDefinition);
        ImmutableMap<String, Variable> parentVariableMap = buildParentAttribute2VariableMap(parentAttributeMap, variableGenerator);

        Optional<Variable> indexVariable = (columns.position == null)?
                Optional.empty():
                Optional.ofNullable(variableGenerator.generateNewVariable(normalizeAttributeName(
                        columns.position,
                        idFactory
                )));


        ImmutableSet<Variable> retainedVariables = computeRetainedVariables(parentVariableMap, indexVariable, idFactory);

        Variable flattenedVariable = parentVariableMap.get(normalizeAttributeName(flattenedColumn.name, idFactory));
        DBTermType flattenedDBType = dbParameters.getDBTypeFactory().getDBTermType(flattenedColumn.datatype);

        if(flattenedVariable == null){
            throw new MetadataExtractionException("The flattened column "+ flattenedColumn.name + " is not present in the base relation");
        }

        Variable flattenedJsonVariable = variableGenerator.generateNewVariableFromVar(flattenedVariable);
        Variable flattenOutputVariable = variableGenerator.generateNewVariable("O");

        ImmutableSubstitution<ImmutableTerm> extractionSubstitution = getExtractionSubstitution(
                flattenOutputVariable,
                flattenedDBType,
                buildVar2ExtractedColumnMap(variableGenerator, idFactory),
                cs,
                dbParameters.getDBTypeFactory()
        );

        ImmutableList<Variable> projectedVariables = ImmutableList.copyOf(union(retainedVariables, extractionSubstitution.getImmutableMap().keySet()));

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), cs);

        DistinctVariableOnlyDataAtom projectionAtom = cs.getAtomFactory().getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        ConstructionNode extractionConstructionNode = iqFactory.createConstructionNode(
                union(
                        retainedVariables,
                        extractionSubstitution.getDomain()
                ),
                extractionSubstitution
        );

        FilterNode filterNode = iqFactory.createFilterNode(
                termFactory.getDBIsNotNull(flattenOutputVariable)
        );

        FlattenNode flattennode = iqFactory.createFlattenNode(
                flattenOutputVariable,
                flattenedJsonVariable,
                indexVariable,
                flattenedDBType
        );

        ExtensionalDataNode dataNode = iqFactory.createExtensionalDataNode(parentDefinition, compose(parentAttributeMap, parentVariableMap));

        ConstructionNode checkArrayConstructionNode = iqFactory.createConstructionNode(
                getProjectedVars(dataNode.getVariables(), flattenedJsonVariable),
                getCheckIfArraySubstitution(
                        flattenedVariable,
                        flattenedDBType,
                        flattenedJsonVariable,
                        cs
                ));

        return iqFactory.createIQ(projectionAtom,
                iqFactory.createUnaryIQTree(
                        extractionConstructionNode,
                        iqFactory.createUnaryIQTree(
                                filterNode,
                                iqFactory.createUnaryIQTree(
                                        flattennode,
                                        iqFactory.createUnaryIQTree(
                                                checkArrayConstructionNode,
                                                dataNode
                                        )))));
    }

    private ImmutableSet<Variable> getProjectedVars(ImmutableSet<Variable> subtreeVars, Variable freshVar) {
        return ImmutableSet.<Variable>builder()
                .addAll(subtreeVars)
                .add(freshVar)
                .build();
    }

    private ImmutableSet<Variable> computeRetainedVariables(ImmutableMap<String, Variable> parentVariableMap, Optional<Variable> positionVariable,
                                                            QuotedIDFactory quotedIDFactory) throws MetadataExtractionException {
        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
        for(String keptColumn : columns.kept) {
            builder.add(getVarForAttribute(keptColumn, parentVariableMap, quotedIDFactory));
        }
        positionVariable.ifPresent(builder::add);
        return builder.build();
    }

    private Variable getVarForAttribute(String name, ImmutableMap<String, Variable> parentVariableMap, QuotedIDFactory idFactory) throws MetadataExtractionException {
        String normalizedName = normalizeAttributeName(name, idFactory);
        Variable var = parentVariableMap.get(normalizedName);
        if (var == null){
            throw new MetadataExtractionException("Kept column "+normalizedName+" not found in base view definition");
        }
        return var;
    }


    private ImmutableMap<Integer,? extends VariableOrGroundTerm> compose(ImmutableMap<Integer, String> map1, ImmutableMap<String, Variable> map2) {
        return map1.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> map2.get(e.getValue())
                ));
    }

    private ImmutableSet<Variable> union(ImmutableSet<Variable> s1, ImmutableSet<Variable> s2) {
        return Sets.union(s1, s2).immutableCopy();
    }

    private ImmutableMap<String, Variable> buildParentAttribute2VariableMap(ImmutableMap<Integer, String> parentAttributeMap,
                                                                            VariableGenerator variableGenerator) {
        return parentAttributeMap.values().stream()
                .collect(ImmutableCollectors.toMap(
                        s -> s,
                        variableGenerator::generateNewVariable));
    }

    private ImmutableMap<Integer, String> buildParentIndex2AttributeMap(NamedRelationDefinition parentRelation) {
        ImmutableList<Attribute> attributes = parentRelation.getAttributes();
        return IntStream.range(0, attributes.size()).boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        i -> attributes.get(i).getID().getName()
                ));
    }

    private ImmutableMap<Variable, ExtractedColumn> buildVar2ExtractedColumnMap(VariableGenerator variableGenerator, QuotedIDFactory idFactory) {

        return  columns.extracted.stream()
                .collect(ImmutableCollectors.toMap(
                        c -> variableGenerator.generateNewVariable(
                                                normalizeAttributeName(
                                                        c.name,
                                                        idFactory
                                                )),
                        c -> c
                ));
    }

    private ImmutableSubstitution<ImmutableTerm> getExtractionSubstitution(Variable flattenOutputVariable,
                                                                           DBTermType flattenedDBType,
                                                                           ImmutableMap<Variable, ExtractedColumn> extractColumnsMap,
                                                                           CoreSingletons cs, DBTypeFactory typeFactory)
            throws MetadataExtractionException {
        ImmutableMap.Builder<Variable, ImmutableTerm> builder = ImmutableMap.builder();
        for (Map.Entry<Variable, JsonFlattenLens.ExtractedColumn> entry : extractColumnsMap.entrySet()) {
            builder.put(entry.getKey(), getCheckDatatypeExtractAndCastFromJson(
                    flattenOutputVariable,
                    flattenedDBType,
                    getPath(entry.getValue()),
                    entry.getValue().datatype,
                    entry.getValue().name,
                    cs,
                    typeFactory
            ));
        }
        return cs.getSubstitutionFactory().getSubstitution(builder.build());
    }

    private ImmutableList<String> getPath(ExtractedColumn col) {
        return col.key == null ?
            ImmutableList.of():
            ImmutableList.copyOf(col.key);
    }


    private ImmutableSubstitution<ImmutableTerm> getCheckIfArraySubstitution(Variable flattenedVar, DBTermType dbType,
                                                                             Variable flattenedIfArrayVar, CoreSingletons cs){

        TermFactory termFactory = cs.getTermFactory();
        return cs.getSubstitutionFactory().getSubstitution(ImmutableMap.of(
                        flattenedIfArrayVar,
                        termFactory.getIfElseNull(
                                termFactory.getDBIsArray(dbType, flattenedVar),
                                flattenedVar
                        )));
    }


    /**
     * If no expected DB type is specified, then do not cast the value (leave it as a JSON value)
     */
    private ImmutableFunctionalTerm getCheckDatatypeExtractAndCastFromJson(Variable sourceVar, DBTermType flattenedDBType,
                                                                           ImmutableList<String> path,
                                                                           String datatypeString, String columnName,
                                                                           CoreSingletons cs, DBTypeFactory dbTypeFactory)
            throws MetadataExtractionException {
        TermFactory termFactory = cs.getTermFactory();

        DBTermType termType = dbTypeFactory.getDBTermType(datatypeString);

        ImmutableFunctionalTerm cast = getCast(termType, sourceVar, path, termFactory);

        if (termType.getCategory() == JSON) {
            return cast;
        }
        return termFactory.getIfElseNull(
                getDatatypeCondition(
                        flattenedDBType,
                        termFactory.getDBJsonElement(
                                sourceVar,
                                path
                        ),
                        termType,
                        columnName,
                        cs
                ),
                cast
        );
    }

    private ImmutableFunctionalTerm getCast(DBTermType columnTermType, Variable sourceVar, ImmutableList<String> path, TermFactory termFactory) {

        ImmutableFunctionalTerm retrieveEltAsText = termFactory.getDBJsonElementAsText(sourceVar, path);

        // TODO: consider the input type as well (could enable more simplification, e.g. no cast when same datatype)
        return termFactory.getDBCastFunctionalTerm(
                columnTermType,
                retrieveEltAsText
        );
    }

    private ImmutableExpression getDatatypeCondition(DBTermType flattenedDBType, ImmutableFunctionalTerm arg,
                                                     DBTermType columnTermType, String columnName, CoreSingletons cs)
            throws MetadataExtractionException {
        TermFactory termFactory = cs.getTermFactory();

        switch (columnTermType.getCategory()){
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

    @Override
    public void insertIntegrityConstraints(OntopViewDefinition relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        CoreSingletons cs = dbParameters.getCoreSingletons();

        if(baseRelations.size() != 1){
            throw new MetadataExtractionException("A nested view should have exactly one base relation");
        }
        NamedRelationDefinition baseRelation = baseRelations.get(0);

        insertUniqueConstraints(
                relation,
                idFactory,
                (uniqueConstraints != null) ? uniqueConstraints.added : ImmutableList.of(),
                /*
                 * No UC can be inherited as such from the parent.
                 */
                ImmutableList.of(),
                cs
        );

        ImmutableSet<QuotedID> addedColumns = getAddedColumns(idFactory);
        ImmutableSet<QuotedID> keptColumns = getKeptColumns(idFactory);
        ImmutableSet<QuotedID> hiddenColumns = getHiddenColumns(baseRelation, keptColumns);

        /*
         * FDs declared as such in the parent relation are inherited similarly to Join views.
         * UCs declared in the parent relation may be added as FDs.
         */
        insertFunctionalDependencies(
                relation,
                idFactory,
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
                        .map(this::toQuotedIDs)
                        .map(attributes -> getInferredFD(attributes, keptColumns))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(ImmutableCollectors.toList());
    }

    private Optional<FunctionalDependencyConstruct> getInferredFD(ImmutableSet<QuotedID> determinants, ImmutableSet<QuotedID> keptColumns) {
        if(keptColumns.containsAll(determinants)){
            ImmutableSet<QuotedID> difference = Sets.difference(keptColumns, determinants).immutableCopy();
            if(!difference.isEmpty()){
                return Optional.of(new FunctionalDependencyConstruct(determinants, difference));
            }
        }
        return Optional.empty();
    }

    private ImmutableSet<QuotedID> toQuotedIDs(ImmutableList<Attribute> attributes) {
        return attributes.stream()
                .map(Attribute::getID)
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSet<QuotedID> getAddedColumns(QuotedIDFactory idFactory) {
        ImmutableSet.Builder<QuotedID> builder = ImmutableSet.builder();
        if(columns.position != null) {
            builder.add(idFactory.createAttributeID(columns.position));
        }
        builder.addAll(
                columns.extracted.stream()
                .map(a -> a.name)
                .map(idFactory::createAttributeID)
                        .iterator()
        );
        return builder.build();
    }

    private ImmutableSet<QuotedID> getHiddenColumns(NamedRelationDefinition baseRelation, ImmutableSet<QuotedID> keptColumns) {
        return baseRelation.getAttributes().stream()
                .map(Attribute::getID)
                .filter(d -> !keptColumns.contains(d))
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSet<QuotedID> getKeptColumns(QuotedIDFactory idFactory) {
        return columns.kept.stream()
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(OntopViewDefinition ontopViewDefinition,
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
