
package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

//    @Override
//    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
//            throws MetadataExtractionException {
//
//        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
//        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));
//
//        NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
//                baseRelation.toArray(new String[0])));
//
//
//        IQ iq = createIQ(relationId, parentDefinition, dbParameters);
//
//        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
//        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);
//
//        return new OntopViewDefinitionImpl(
//                ImmutableList.of(relationId),
//                attributeBuilder,
//                iq,
//                parentDefinition instanceof OntopViewDefinition ?
//                        ((OntopViewDefinition) parentDefinition).getLevel() + 1
//                        : 1,
//                dbParameters.getCoreSingletons());
//
//    }

//    @Override
//    public void insertIntegrityConstraints(NamedRelationDefinition relation,
//                                           ImmutableList<NamedRelationDefinition> baseRelations,
//                                           MetadataLookup metadataLookupForFK) throws MetadataExtractionException {
//
//        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();
//
//        insertUniqueConstraints(relation, idFactory, uniqueConstraints.added, baseRelations);
//
//        insertFunctionalDependencies(relation, idFactory, otherFunctionalDependencies.added, baseRelations);
//
//        for (AddForeignKey fk : foreignKeys.added) {
//            insertForeignKeys(relation, metadataLookupForFK, fk);
//        }
//    }


    protected IQ createIQ(RelationID relationId, ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap, DBParameters dbParameters)
            throws MetadataExtractionException {

        if(parentDefinitionMap.size() != 1) {
            throw "A nested view should have exactly one parent";
        }
        return createIQ(
                relationId,
                parentDefinitionMap.entrySet().stream().findFirst().get().getKey(),
                dbParameters
        );
    }

    protected IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition, DBParameters dbParameters)
            throws MetadataExtractionException {

        CoreSingletons cs = dbParameters.getCoreSingletons();

        IntermediateQueryFactory iqFactory = cs.getIQFactory();
        AtomFactory atomFactory = cs.getAtomFactory();
//        CoreUtilsFactory coreUtilsFactory = coreSingletons.getCoreUtilsFactory();
        VariableGenerator variableGenerator = cs.getCoreUtilsFactory().createVariableGenerator(ImmutableSet.of());

        ImmutableMap<Integer, String> parentAttributeMap = buildParentIndex2AttributeMap(parentDefinition);
        ImmutableMap<String, Variable> parentVariableMap = buildParentAttribute2VariableMap(parentAttributeMap, variableGenerator);


//        ImmutableMap<String, Variable> parentAttribute2VarMap = createAttribute2VarMap(
////                columns.kept,
////                flattenedColumn,
//                parentDefinition,
//                variableGenerator
////                cs
//        );

        Optional<Variable> positionVariable = (position == null)?
                Optional.empty():
                Optional.ofNullable(variableGenerator.generateNewVariable(position));


        ImmutableSet<Variable> retainedVariables = columns.kept.stream()
                .map(c -> parentVariableMap.get(c))
                .collect(ImmutableCollectors.toSet());

        Variable flattenedColumnVariable = variableGenerator.generateNewVariable(this.flattenedColumn);

        Variable flattenOutputVariable = variableGenerator.generateNewVariable("O");

        ImmutableSubstitution<ImmutableTerm> extractionSubstitution = getExtractionSubstitution(
                flattenOutputVariable,
                positionVariable,
                buildVar2ExtractedColumnMap(variableGenerator),
                cs
        );

        ImmutableSubstitution<ImmutableTerm> castSubstitution = getCastSubstitution(
                extractionSubstitution,
                cs
        );


        ImmutableList<Variable> projectedVariables = ImmutableList.copyOf(union(retainedVariables, castSubstitution.getImmutableMap().keySet()));

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), cs);

        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        ConstructionNode castConstructionNode = iqFactory.createConstructionNode(ImmutableSet.copyOf(projectedVariables), castSubstitution);

        ConstructionNode extractionConstructionNode = iqFactory.createConstructionNode(
                getExtractionOutputVariables(retainedVariables, extractionSubstitution),
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
                        castConstructionNode,
                        iqFactory.createUnaryIQTree(
                                extractionConstructionNode,
                                iqFactory.createUnaryIQTree(
                                        flattennode,
                                        dataNode
                                ))));
    }

    private ImmutableMap<String, Variable> buildParentAttribute2VariableMap(ImmutableMap<Integer, String> parentAttributeMap,
                                                                            VariableGenerator variableGenerator) {
        return parentAttributeMap.values().stream()
                .collect(ImmutableCollectors.toMap(
                        s -> s,
                        s -> variableGenerator.generateNewVariable(s)
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


    private Optional<Variable> getPositionVariable(VariableGenerator variableGenerator) {
        return (position == null)?
                Optional.empty():
                Optional.of(
                        variableGenerator.generateNewVariable(position)
                );

    }



    private ImmutableList<Variable> getProjectedVariables(ImmutableMap<Integer, Variable> parentArgumentMap, ImmutableSubstitution<ImmutableTerm> substitution) {
       return  ImmutableList.<Variable>builder()
                .addAll(parentArgumentMap.values())
                .addAll(substitution.getDomain())
               .build();
    }

//        return iqFactory.createIQ(
//                projectionAtom,
//                iqFactory.createUnaryIQTree()
//                .normalizeForOptimization();
//
//        ExtensionalDataNode parentDataNode = iqFactory.createExtensionalDataNode(parentDefinition, parentArgumentMap);
//
//
//
//        IQTree iqTree = iqFactory.createUnaryIQTree(
//                constructionNode,
//                parentDataNode);

//        Map<String, Variable> parentArgumentMap = createParentArgumentMap(
//                columns.kept,
//                flattenedColumn,
//                parentDefinition,
//                coreSingletons.getCoreUtilsFactory()
//        );

//        ImmutableSet<Variable> extractedAttributeVariables = columns.extracted.stream()
//                .map(a -> a.name)
//                .map(attributeName -> normalizeAttributeName(attributeName, quotedIdFactory))
//                .map(termFactory::getVariable)
//                .collect(ImmutableCollectors.toSet());
//
//
//        Variable flattenedAttributeVar = getFlattenedAttributeVar
//
//        ImmutableSet<String> keptColumnNames = columns.kept.stream()
//                .collect(ImmutableCollectors.toSet());
//
//        ImmutableList<Variable> projectedVariables = extractRelationVariables(extractedAttributeVariables, keptColumnNames,
//                parentDefinition, termFactory);
//
//        ImmutableMap<Integer, Variable> parentArgumentMap = createParentArgumentMap(extractedAttributeVariables, parentDefinition,
//                coreSingletons.getCoreUtilsFactory());
//



    private ImmutableList<Variable> extractRelationVariables(ImmutableSet<Variable> addedVariables,
                                                             ImmutableSet<String> keptColumnNames,
                                                             NamedRelationDefinition parentDefinition,
                                                             TermFactory termFactory) {

        ImmutableList<Variable> inheritedVariableStream = parentDefinition.getAttributes().stream()
                .map(a -> a.getID().getName())
                .filter(n -> keptColumnNames.contains(n))
                .map(termFactory::getVariable)
                .collect(ImmutableCollectors.toList());

        return Stream.concat(
                addedVariables.stream(),
                inheritedVariableStream.stream())
                .collect(ImmutableCollectors.toList());
    }


//    private ImmutableMap<String, Variable> createParentArgumentMap(List<String> kept,
//                                                                   String flattenedColumn,
//                                                                   NamedRelationDefinition parentDefinition,
//                                                                   VariableGenerator variableGenerator,
//                                                                   CoreSingletons cs) {
//
//    }




    private ImmutableMap<String, Variable> createAttribute2VarMap(NamedRelationDefinition parentDefinition,
                                                                  VariableGenerator variableGenerator) {

        ImmutableList<Attribute> parentAttributes = parentDefinition.getAttributes();

        return parentAttributes.stream()
                .map(a -> a.getID().getName())
                .collect(ImmutableCollectors.toMap(
                        n -> n,
                        n -> variableGenerator.generateNewVariable(n)
                ));
    }

    private ImmutableSubstitution<ImmutableTerm> getExtractionSubstitution(Variable flattenOutputVariable,
                                                                           Optional<Variable> positionVariable,
                                                                           ImmutableMap<Variable, ExtractedColumn> extractColumnsMap,
                                                                           CoreSingletons cs){

        ImmutableMap.Builder<Variable, ImmutableTerm> builder = ImmutableMap.<Variable, ImmutableTerm>builder()
                .putAll(
                        extractColumnsMap.entrySet().stream()
                                .collect(ImmutableCollectors.toMap(
                                        c -> c.getKey(),
                                        c -> getExtractFromJSONFunctionalTerm(flattenOutputVariable, c.getValue().key, cs)
                                )));
        positionVariable
                .ifPresent(p -> builder.put(flattenOutputVariable, getPositionInJSONArrayFunctionalTerm(positionVariable.get(), cs)));

        return cs.getSubstitutionFactory().getSubstitution(builder.build());
    }


    private ImmutableFunctionalTerm getExtractFromJSONFunctionalTerm(Variable var, List<String> key, CoreSingletons cs) {
        DBConstant path = buildJSONPath(key, cs);
        return cs.getTermFactory().getImmutableFunctionalTerm(
                cs.getDBFunctionsymbolFactory().getDBRetrieveJSONElementFunctionSymbol(),
                var,
                path
        );
    }

    private DBConstant buildJSONPath(List<String> key, CoreSingletons cs) {
        return cs.getTermFactory().getDBConstant(
                cs.getDBFunctionsymbolFactory().serializeJSONPath(key),
                cs.getTypeFactory().getDBTypeFactory().getDBStringType()
        );
    }

    private ImmutableTerm getPositionInJSONArrayFunctionalTerm(Variable var, CoreSingletons cs) {
        return cs.getTermFactory()
                .getImmutableFunctionalTerm(
                        cs.getDBFunctionsymbolFactory().getDBPositionInJSONArrayFunctionSymbol(),
                        var
                );

    }

//    private ConstructionNode createConstructionNode(ImmutableList<Variable> projectedVariables,
//                                                    NamedRelationDefinition parentDefinition,
//                                                    ImmutableMap<Integer, Variable> parentArgumentMap,
//                                                    DBParameters dbParameters) throws MetadataExtractionException {
//
//        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();
//        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
//        TermFactory termFactory = coreSingletons.getTermFactory();
//        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
//        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();
//
//        ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap = parentArgumentMap.entrySet().stream()
//                .collect(ImmutableCollectors.toMap(
//                        e -> new QualifiedAttributeID(null, parentDefinition.getAttributes().get(e.getKey()).getID()),
//                        Map.Entry::getValue));
//
//
//        ImmutableMap.Builder<Variable, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
//        for (AddColumns a : columns.added) {
//            Variable v = termFactory.getVariable(normalizeAttributeName(a.name, quotedIdFactory));
//            try {
//                ImmutableTerm value = extractExpression(a.expression, parentAttributeMap, quotedIdFactory, coreSingletons);
//                substitutionMapBuilder.put(v, value);
//            } catch (JSQLParserException e) {
//                throw new MetadataExtractionException("Unsupported expression for " + a.name + " in " + name + ":\n" + e);
//            }
//        }
//
//        return iqFactory.createConstructionNode(
//                ImmutableSet.copyOf(projectedVariables),
//                substitutionFactory.getSubstitution(substitutionMapBuilder.build()));
//    }

//    private ImmutableTerm extractExpression(String partialExpression,
//                                            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
//                                            QuotedIDFactory quotedIdFactory, CoreSingletons coreSingletons) throws JSQLParserException {
//        String sqlQuery = "SELECT " + partialExpression + " FROM fakeTable";
//        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
//        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
//        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
//        net.sf.jsqlparser.expression.Expression exp = ((SelectExpressionItem) si).getExpression();
//        return parser.parseTerm(exp, new RAExpressionAttributes(parentAttributeMap, null));
//    }


//    @Override
//    public void insertIntegrityConstraints(OntopViewDefinition relation,
//                                           ImmutableList<NamedRelationDefinition> baseRelations,
//                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {
//
//        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();
//
//        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
//
//        insertUniqueConstraints(relation, idFactory,
//                (uniqueConstraints != null) ? uniqueConstraints.added : ImmutableList.of(),
//                baseRelations, coreSingletons);
//
//        insertFunctionalDependencies(relation, idFactory,
//                (otherFunctionalDependencies != null) ? otherFunctionalDependencies.added : ImmutableList.of(),
//                baseRelations);
//
//        insertForeignKeys(relation, metadataLookupForFK,
//                (foreignKeys != null) ? foreignKeys.added : ImmutableList.of(),
//                baseRelations);
//    }

    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(OntopViewDefinition ontopViewDefinition, ImmutableList<Attribute> parentAttributes) {
    }

    @Override
    protected ImmutableMap<NamedRelationDefinition, String> extractParentDefinitions(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup) throws MetadataExtractionException {
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
}
