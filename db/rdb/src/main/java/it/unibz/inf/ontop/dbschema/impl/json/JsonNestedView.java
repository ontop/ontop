
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
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
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

@JsonDeserialize(as = JsonNestedView.class)
public class JsonNestedView extends JsonBasicOrJoinOrNestedView {

    @Nonnull
    public final Columns columns;
    @Nonnull
    public final String baseRelation;
    @Nonnull
    private final String flattenedColumn;

    private final String position;

    public final UniqueConstraints uniqueConstraints;

    public final OtherFunctionalDependencies otherFunctionalDependencies;

    public final ForeignKeys foreignKeys;

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonNestedView.class);

    @JsonCreator
    public JsonNestedView(
            @JsonProperty("name") List<String> name,
            @JsonProperty("baseRelation") String baseRelation,
            @JsonProperty("flattenedColumn") String flattenedColumn,
            @JsonProperty("position") String position,
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
        this.position = position;
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
    }


    protected IQ createIQ(RelationID relationId, ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap, DBParameters dbParameters) {

        if(parentDefinitionMap.size() != 1) {
            throw new JSONNestedViewException("A nested view should have exactly one parent");
        }
        return createIQ(
                relationId,
                parentDefinitionMap.entrySet().stream().findFirst().get().getKey(),
                dbParameters
        );
    }

    protected IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition, DBParameters dbParameters) {

        CoreSingletons cs = dbParameters.getCoreSingletons();
        IntermediateQueryFactory iqFactory = cs.getIQFactory();
        VariableGenerator variableGenerator = cs.getCoreUtilsFactory().createVariableGenerator(ImmutableSet.of());

        ImmutableMap<Integer, String> parentAttributeMap = buildParentIndex2AttributeMap(parentDefinition);
        ImmutableMap<String, Variable> parentVariableMap = buildParentAttribute2VariableMap(parentAttributeMap, variableGenerator);

        Optional<Variable> positionVariable = (position == null)?
                Optional.empty():
                Optional.ofNullable(variableGenerator.generateNewVariable(position));

        ImmutableSet<Variable> retainedVariables = columns.kept.stream()
                .map(parentVariableMap::get)
                .collect(ImmutableCollectors.toSet());

        Variable flattenedColumnVariable = variableGenerator.generateNewVariable(this.flattenedColumn);

        Variable flattenOutputVariable = variableGenerator.generateNewVariable("O");

        ImmutableSubstitution<ImmutableTerm> extractionSubstitution = getExtractionSubstitution(
                flattenOutputVariable,
                positionVariable,
                buildVar2ExtractedColumnMap(variableGenerator),
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

        FlattenNode flattennode = iqFactory.createFlattenNode(
                flattenOutputVariable,
                flattenedColumnVariable,
                positionVariable,
                true
        );

        ExtensionalDataNode dataNode = iqFactory.createExtensionalDataNode(parentDefinition, compose(parentAttributeMap, parentVariableMap));

        return iqFactory.createIQ(projectionAtom,
                iqFactory.createUnaryIQTree(
                        extractionConstructionNode,
                        iqFactory.createUnaryIQTree(
                                flattennode,
                                dataNode
                        )));
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
                        variableGenerator::generateNewVariable
                ));
    }

    private ImmutableMap<Integer, String> buildParentIndex2AttributeMap(NamedRelationDefinition parentRelation) {
        ImmutableList<Attribute> attributes = parentRelation.getAttributes();
        return IntStream.range(0, attributes.size()).boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        i -> attributes.get(i).getID().getName()
                ));
    }

    private ImmutableMap<Variable, ExtractedColumn> buildVar2ExtractedColumnMap(VariableGenerator variableGenerator) {

        return  columns.extracted.stream()
                .collect(ImmutableCollectors.toMap(
                        c -> variableGenerator.generateNewVariable(c.name),
                        c -> c
                ));
    }

    private ImmutableSubstitution<ImmutableTerm> getExtractionSubstitution(Variable flattenOutputVariable,
                                                                           Optional<Variable> positionVariable,
                                                                           ImmutableMap<Variable, ExtractedColumn> extractColumnsMap,
                                                                           CoreSingletons cs, DBTypeFactory dbTypeFactory){

        ImmutableMap.Builder<Variable, ImmutableTerm> builder = ImmutableMap.<Variable, ImmutableTerm>builder()
                .putAll(
                        extractColumnsMap.entrySet().stream()
                                .collect(ImmutableCollectors.toMap(
                                        Map.Entry::getKey,
                                        e -> getCheckDatatypeExtractAndCastFromJson(
                                                flattenOutputVariable,
                                                ImmutableList.copyOf(e.getValue().key),
                                                getTermTypeCategoryFor(e.getValue().datatype, e.getValue().name),
                                                cs,
                                                dbTypeFactory
                                        )
                                )));
        positionVariable
                .ifPresent(p -> builder.put(flattenOutputVariable, getPositionInJSONArrayFunctionalTerm(positionVariable.get(), cs)));

        return cs.getSubstitutionFactory().getSubstitution(builder.build());
    }

    private DBTermType.Category getTermTypeCategoryFor(String datatype, String columnName) {
        switch (datatype.toLowerCase()){
            case "boolean":
                return DBTermType.Category.BOOLEAN;
            case "string":
                return DBTermType.Category.STRING;
            case "number":
                return DBTermType.Category.FLOAT_DOUBLE;
        }
        throw new InvalidOntopViewException("Incorrect datatype " + datatype + " for column " + columnName +
                "\n One of \"boolean\", \"string\" or \"number\" is expected");
    }


    private ImmutableFunctionalTerm getCheckDatatypeExtractAndCastFromJson(Variable var, ImmutableList<String> path, DBTermType.Category expectedDBType,
                                                                           CoreSingletons cs, DBTypeFactory dbTypeFactory) {
        TermFactory termFactory = cs.getTermFactory();
        return termFactory.getIfElseNull(
                        getDatatypeCondition(expectedDBType, path, var, cs),
                        getExtractAndCastFromJson(path, expectedDBType, var, cs, dbTypeFactory)
        );
    }

    private ImmutableExpression getDatatypeCondition(DBTermType.Category expectedType, ImmutableList<String> path, Variable var, CoreSingletons cs) {
        return cs.getTermFactory().getDBJsonElementHasType(
                path,
                expectedType,
                var
        );
    }

    private ImmutableFunctionalTerm getExtractAndCastFromJson(ImmutableList<String> path, DBTermType.Category dbTypeCategory,
                                                              Variable var, CoreSingletons cs, DBTypeFactory dbTypeFactory) {
        TermFactory termFactory = cs.getTermFactory();

        return termFactory.getDBCastFunctionalTerm(
                getDefaultDBType(dbTypeCategory, dbTypeFactory),
                termFactory.getDBJsonElement(
                        path,
                        var
                ));
    }

    private DBTermType getDefaultDBType(DBTermType.Category dbTypeCategory, DBTypeFactory typeFactory) {
        switch (dbTypeCategory){
            case BOOLEAN:
                return typeFactory.getDBBooleanType();
            case STRING:
                return typeFactory.getDBStringType();
            case FLOAT_DOUBLE:
                return typeFactory.getDBDoubleType();
        }
        throw new JSONNestedViewException("Unexpected datatype category");
    }

    private ImmutableTerm getPositionInJSONArrayFunctionalTerm(Variable var, CoreSingletons cs) {
        return cs.getTermFactory().getDBPositionInJsonArray(var);
    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(OntopViewDefinition ontopViewDefinition,
                                                                                    ImmutableList<Attribute> parentAttributes) {
        return ImmutableList.of();
    }

    @Override
    protected ImmutableMap<NamedRelationDefinition, String> extractParentDefinitions(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {
            return ImmutableMap.of(extractParentDefinition(dbParameters, parentCacheMetadataLookup), baseRelation);
    }

    @Override
    protected ImmutableSet<QuotedID> getAddedColumns(QuotedIDFactory idFactory) {

        ImmutableSet.Builder<QuotedID> builder =  ImmutableSet.<QuotedID>builder().addAll(
                columns.extracted.stream()
                        .map(e -> e.name)
                        .map(idFactory::createAttributeID)
                        .iterator()
        );
        if(position != null){
            builder.add(idFactory.createAttributeID(position));
        }
        return builder.build();
    }

    @Override
    protected ImmutableSet<QuotedID> getHiddenColumns(ImmutableList<NamedRelationDefinition> baseRelations, QuotedIDFactory idFactory) {
        if(baseRelations.size() != 1) {
            throw new JSONNestedViewException("A nested view should have exactly one parent");
        }
        return baseRelations.get(0).getAttributes().stream()
                .map(Attribute::getID)
                .collect(ImmutableCollectors.toSet());
    }

    private NamedRelationDefinition extractParentDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {
        return parentCacheMetadataLookup.getRelation(dbParameters.getQuotedIDFactory().createRelationID(baseRelation));
    }

//    @Override
//    protected List<String> getAddedColumns() {
//        ImmutableList.Builder<String> builder =  ImmutableList.<String>builder()
//                .addAll(
//                        columns.extracted.stream()
//                                .map(e -> e.name)
//                                .iterator()
//                );
//        if(position != null){
//            builder.add(position);
//        }
//        return builder.build();
//    }
//
//    @Override
//    protected List<String> getHiddenColumns(ImmutableList<NamedRelationDefinition> baseRelations) {
//        if(baseRelations.size() != 1) {
//            throw new JSONNestedViewException("A nested view should have exactly one parent");
//        }
//        return baseRelations.get(0)
//                .
//    }


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

    protected static class JSONNestedViewException extends OntopInternalBugException {

        protected JSONNestedViewException (String message) {
            super(message);
        }
    }

    private static class InvalidOntopViewException extends RuntimeException {
        public InvalidOntopViewException(String message) {
            super(message);
        }
    }
}
