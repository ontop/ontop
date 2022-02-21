
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
    public final List<String> baseRelation;
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
            @JsonProperty("baseRelation") List<String> baseRelation,
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

    @Override
    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        NamedRelationDefinition parentDefinition = extractParentDefinition(dbParameters, parentCacheMetadataLookup);

        Integer parentLevel = (parentDefinition instanceof OntopViewDefinition)?
                ((OntopViewDefinition) parentDefinition).getLevel():
                0;

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, parentDefinition, dbParameters);

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
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

    protected IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition, DBParameters dbParameters) {

        CoreSingletons cs = dbParameters.getCoreSingletons();
        IntermediateQueryFactory iqFactory = cs.getIQFactory();
        VariableGenerator variableGenerator = cs.getCoreUtilsFactory().createVariableGenerator(ImmutableSet.of());

        ImmutableMap<Integer, String> parentAttributeMap = buildParentIndex2AttributeMap(parentDefinition);
        ImmutableMap<String, Variable> parentVariableMap = buildParentAttribute2VariableMap(parentAttributeMap, variableGenerator);

        Optional<Variable> positionVariable = (position == null)?
                Optional.empty():
                Optional.ofNullable(variableGenerator.generateNewVariable(position));

        ImmutableSet<Variable> retainedVariables = computeRetainedVariables(parentVariableMap, positionVariable, dbParameters.getQuotedIDFactory());

        Variable flattenedColumnVariable = variableGenerator.generateNewVariable(this.flattenedColumn);

        Variable flattenOutputVariable = variableGenerator.generateNewVariable("O");

        ImmutableSubstitution<ImmutableTerm> extractionSubstitution = getExtractionSubstitution(
                flattenOutputVariable,
                buildVar2ExtractedColumnMap(variableGenerator),
                cs,
                dbParameters.getDBTypeFactory(),
                dbParameters.getQuotedIDFactory()
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

    private ImmutableSet<Variable> computeRetainedVariables(ImmutableMap<String, Variable> parentVariableMap, Optional<Variable> positionVariable,
                                                            QuotedIDFactory quotedIDFactory) {
        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
        builder.addAll(
                columns.kept.stream()
                        .map(k -> getVarForAttribute(k, parentVariableMap, quotedIDFactory)).iterator()
        );
        positionVariable.ifPresent(p -> builder.add(p));
        return builder.build();
    }

    private Variable getVarForAttribute(String name, ImmutableMap<String, Variable> parentVariableMap, QuotedIDFactory idFactory) {
        String normalizedName = normalizeAttributeName(name, idFactory);
        Variable var = parentVariableMap.get(normalizedName);
        if (var == null){
            throw new InvalidOntopViewException("Kept column "+normalizedName+" not found in base view definition");
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
                                                                           ImmutableMap<Variable, ExtractedColumn> extractColumnsMap,
                                                                           CoreSingletons cs, DBTypeFactory typeFactory,
                                                                           QuotedIDFactory idFactory){

        return cs.getSubstitutionFactory().getSubstitution(
                extractColumnsMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> getCheckDatatypeExtractAndCastFromJson(
                                        flattenOutputVariable,
                                        getPathAsDBConstants(e.getValue().key, cs.getTermFactory()),
                                        getTermTypeCategoryFor(
                                                e.getValue().datatype,
                                                normalizeAttributeName(
                                                        e.getValue().name,
                                                        idFactory
                                                )),
                                        cs,
                                        typeFactory
                                ))));
    }

    private ImmutableList<DBConstant> getPathAsDBConstants(List<String> path, TermFactory termFactory) {
        return (path == null)?
                ImmutableList.of(termFactory.getDBStringConstant("")):
                path.stream()
                        .map(termFactory::getDBStringConstant)
                        .collect(ImmutableCollectors.toList());
    }

    private DBTermType.Category getTermTypeCategoryFor(String datatype, String columnName) {
        switch (datatype.toLowerCase()){
            case "boolean":
                return DBTermType.Category.BOOLEAN;
            case "text":
                return DBTermType.Category.STRING;
            case "number":
                return DBTermType.Category.FLOAT_DOUBLE;
        }
        throw new InvalidOntopViewException("Incorrect datatype " + datatype + " for column " + columnName +
                "\n One of \"boolean\", \"text\" or \"number\" is expected");
    }


    private ImmutableFunctionalTerm getCheckDatatypeExtractAndCastFromJson(Variable var, ImmutableList<DBConstant> path, DBTermType.Category expectedDBType,
                                                                           CoreSingletons cs, DBTypeFactory dbTypeFactory) {
        TermFactory termFactory = cs.getTermFactory();
        return termFactory.getIfElseNull(
                        getDatatypeCondition(expectedDBType, path, var, cs),
                        getExtractAndCastFromJson(path, expectedDBType, var, cs, dbTypeFactory)
        );
    }

    private ImmutableExpression getDatatypeCondition(DBTermType.Category expectedType, ImmutableList<DBConstant> path, Variable var, CoreSingletons cs) {
        switch (expectedType){
            case BOOLEAN:
                return cs.getTermFactory().getDBJsonIsBoolean(path, var);
            case STRING:
                return cs.getTermFactory().getDBJsonIsString(path, var);
            case INTEGER:
            case FLOAT_DOUBLE:
            case DECIMAL:
                return cs.getTermFactory().getDBJsonIsNumeric(path, var);
        }
        throw new JsonNestedViewException("Unexpected DBType category");
    }

    private ImmutableFunctionalTerm getExtractAndCastFromJson(ImmutableList<DBConstant> path, DBTermType.Category dbTypeCategory,
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
            case INTEGER:
            case FLOAT_DOUBLE:
            case DECIMAL:
                return typeFactory.getDBDoubleType();
        }
        throw new JsonNestedViewException("Unexpected datatype category");
    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(OntopViewDefinition ontopViewDefinition,
                                                                                    ImmutableList<Attribute> parentAttributes) {
        return ImmutableList.of();
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
            throw new JsonNestedViewException("A nested view should have exactly one parent");
        }
        return baseRelations.get(0).getAttributes().stream()
                .map(Attribute::getID)
                .collect(ImmutableCollectors.toSet());
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

    protected static class JsonNestedViewException extends OntopInternalBugException {

        protected JsonNestedViewException(String message) {
            super(message);
        }
    }

    private static class InvalidOntopViewException extends RuntimeException {
        public InvalidOntopViewException(String message) {
            super(message);
        }
    }
}
