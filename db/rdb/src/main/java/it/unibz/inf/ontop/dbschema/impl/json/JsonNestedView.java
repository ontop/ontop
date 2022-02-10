
package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.ExpressionParser;
import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@JsonPropertyOrder({
        "relations"
})
@JsonDeserialize(as = JsonBasicView.class)
public class JsonNestedView extends JsonView {
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

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                baseRelation.toArray(new String[0])));


        IQ iq = createIQ(relationId, parentDefinition, dbParameters);

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new OntopViewDefinitionImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                parentDefinition instanceof OntopViewDefinition ?
                        ((OntopViewDefinition) parentDefinition).getLevel() + 1
                        : 1,
                dbParameters.getCoreSingletons());

    }

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


    private IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition, DBParameters dbParameters) {

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();
        CoreUtilsFactory coreUtilsFactory = coreSingletons.getCoreUtilsFactory();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());

        ImmutableMap<Integer, Variable> parentArgumentMap = createParentArgumentMap(
                columns.kept,
                flattenedColumn,
                parentDefinition,
                coreSingletons.getCoreUtilsFactory(),
                variableGenerator
        );

        Variable flattenedColumnVariable = variableGenerator.generateNewVariable(this.flattenedColumn);

        Variable flattenOutputVariable = variableGenerator.generateNewVariable("O");

        ImmutableMap<Variable, ExtractedColumn> extractedColumnsMap = columns.extracted.stream()
                .collect(ImmutableCollectors.toMap(
                        c -> variableGenerator.generateNewVariable(c.name),
                        c -> c
                ));

        Optional<Variable> positionVariable = (position == null)?
                Optional.empty():
                Optional.ofNullable(variableGenerator.generateNewVariable(position));

        ImmutableSubstitution<ImmutableTerm> checkIfArraySubstitution = getCheckIfArraySubstitution(
                flattenedColumnVariable,
                coreSingletons
        );

        ImmutableSubstitution<ImmutableTerm> columnExtractionSubstitution = getExtractionSubstitution(
                flattenOutputVariable,
                positionVariable,
                extractedColumnsMap,
                coreSingletons
        );

        ImmutableSubstitution<ImmutableTerm> castSubstitution = getCastSubstitution(

                variableGenerator,
                coreSingletons
        );


        ImmutableList<Variable> projectedVariables = getProjectedVariables(parentArgumentMap, columnExtractionSubstitution);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);

        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        ConstructionNode columnExtractionConstructionNode = iqFactory.createConstructionNode(ImmutableSet.copyOf(projectedVariables), columnExtractionSubstitution);

        FlattenNode flattennode = iqFactory.createFlattenNode(
                flattenOutputVariable,
                flattenedColumnVariable,
                positionVariable,
                true
        );

        ExtensionalDataNode dataNode = iqFactory.createExtensionalDataNode(parentDefinition, parentArgumentMap);

        return iqFactory.createIQ(projectionAtom,
                iqFactory.createUnaryIQTree(
                        columnExtractionConstructionNode,
                        iqFactory.createUnaryIQTree(
                                flattennode,
                                dataNode
                )));
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

    private String normalizeAttributeName(String attributeName, QuotedIDFactory quotedIdFactory) {
        return quotedIdFactory.createAttributeID(attributeName).getName();
    }

    private AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryViewPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    private ImmutableMap<Integer, Variable> createParentArgumentMap(ImmutableSet<Variable> addedVariables,
                                                                    NamedRelationDefinition parentDefinition,
                                                                    CoreUtilsFactory coreUtilsFactory) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(addedVariables);

        ImmutableList<Attribute> parentAttributes = parentDefinition.getAttributes();

        // NB: the non-necessary variables will be pruned out by normalizing the IQ
        return IntStream.range(0, parentAttributes.size())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        i -> variableGenerator.generateNewVariable(
                                parentAttributes.get(i).getID().getName())));

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
                                        c -> getExtractFromJSONFunctionalTerm(c.getKey(), c.getValue().key, cs)
                                )));
        positionVariable
                .ifPresent(p -> builder.put(p, getPositionInJSONArrayFunctionalTerm(positionVariable.get(), cs)));

        return cs.getSubstitutionFactory().getSubstitution(builder.build());
    }


    private ImmutableFunctionalTerm getExtractFromJSONFunctionalTerm(Variable var, List<String> key, CoreSingletons cs) {
        DBConstant path = buildJSONPath(key, cs);
        return cs.getTermFactory().getImmutableFunctionalTerm(
                cs.getDBFunctionsymbolFactory().getDBRetrieveJSONElementFunctionSymbol(),
                path,
                var
        );
    }

    private DBConstant buildJSONPath(List<String> key, CoreSingletons cs) {
        return cs.getTermFactory().getDBConstant(
                cs.getDBFunctionsymbolFactory().serializeJSONPath(key),
                cs.getTypeFactory().getDBTypeFactory().getDBStringType()
        );
    }



    private ImmutableTerm getPositionInJSONArrayFunctionalTerm(Variable positionVar, CoreSingletons cs) {
        return cs.getTermFactory()
                .getImmutableFunctionalTerm(
                        cs.getDBFunctionsymbolFactory().getDBPositionInJSONArrayFunctionSymbol(),

                )

    }


    private ConstructionNode createConstructionNode(ImmutableList<Variable> projectedVariables,
                                                    NamedRelationDefinition parentDefinition,
                                                    ImmutableMap<Integer, Variable> parentArgumentMap,
                                                    DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();
        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap = parentArgumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> new QualifiedAttributeID(null, parentDefinition.getAttributes().get(e.getKey()).getID()),
                        Map.Entry::getValue));


        ImmutableMap.Builder<Variable, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
        for (AddColumns a : columns.added) {
            Variable v = termFactory.getVariable(normalizeAttributeName(a.name, quotedIdFactory));
            try {
                ImmutableTerm value = extractExpression(a.expression, parentAttributeMap, quotedIdFactory, coreSingletons);
                substitutionMapBuilder.put(v, value);
            } catch (JSQLParserException e) {
                throw new MetadataExtractionException("Unsupported expression for " + a.name + " in " + name + ":\n" + e);
            }
        }

        return iqFactory.createConstructionNode(
                ImmutableSet.copyOf(projectedVariables),
                substitutionFactory.getSubstitution(substitutionMapBuilder.build()));
    }

    private ImmutableTerm extractExpression(String partialExpression,
                                            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
                                            QuotedIDFactory quotedIdFactory, CoreSingletons coreSingletons) throws JSQLParserException {
        String sqlQuery = "SELECT " + partialExpression + " FROM fakeTable";
        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        net.sf.jsqlparser.expression.Expression exp = ((SelectExpressionItem) si).getExpression();
        return parser.parseTerm(exp, new RAExpressionAttributes(parentAttributeMap, null));
    }

    private RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, DBParameters dbParameters) throws MetadataExtractionException {
        UniqueTermTypeExtractor uniqueTermTypeExtractor = dbParameters.getCoreSingletons().getUniqueTermTypeExtractor();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();
        IQTree iqTree = iq.getTree();

        RawQuotedIDFactory rawQuotedIqFactory = new RawQuotedIDFactory(quotedIdFactory);

        for (Variable v : iqTree.getVariables()) {
            builder.addAttribute(rawQuotedIqFactory.createAttributeID(v.getName()),
                    (DBTermType) uniqueTermTypeExtractor.extractUniqueTermType(v, iqTree)
                            // TODO: give the name of the view
                            .orElseThrow(() -> new MetadataExtractionException("No type inferred for " + v + " in " + iq)),
                    iqTree.getVariableNullability().isPossiblyNullable(v));
        }
        return builder;
    }

    private void insertUniqueConstraints(NamedRelationDefinition relation,
                                         QuotedIDFactory idFactory,
                                         List<AddUniqueConstraints> addUniqueConstraints,
                                         ImmutableList<NamedRelationDefinition> baseRelations)
            throws MetadataExtractionException {


        List<AddUniqueConstraints> list = extractUniqueConstraints(addUniqueConstraints, baseRelations, idFactory);

        for (AddUniqueConstraints addUC : list) {
            FunctionalDependency.Builder builder = addUC.isPrimaryKey
                    ? UniqueConstraint.primaryKeyBuilder(relation, addUC.name)
                    : UniqueConstraint.builder(relation, addUC.name);
            JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
            builder.build();
        }
    }

    /**
     * Infer unique constraints from the parent
     */
    private List<AddUniqueConstraints> extractUniqueConstraints(List<AddUniqueConstraints> addUniqueConstraints,
                                                                ImmutableList<NamedRelationDefinition> baseRelations,
                                                                QuotedIDFactory idFactory) {

        // List of constraints added
        ImmutableList<QuotedID> addedConstraintsColumns = uniqueConstraints.added.stream()
                .map(a -> a.determinants)
                .flatMap(Collection::stream)
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toList());


        // List of added columns
        ImmutableList<QuotedID> addedNewColumns = columns.added.stream()
                .map(a -> idFactory.createAttributeID(a.name))
                .collect(ImmutableCollectors.toList());

        // List of hidden columns
        ImmutableList<QuotedID> hiddenColumnNames = columns.hidden.stream()
                .map(a -> idFactory.createAttributeID(a))
                .collect(ImmutableCollectors.toList());

        // Filter inherited constraints
        ImmutableList<UniqueConstraint> inheritedConstraints = baseRelations.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .filter(c -> c.getAttributes().stream()
                        .map(Attribute::getID)
                        .noneMatch(addedConstraintsColumns::contains))
                .filter(c -> c.getAttributes().stream()
                        .map(Attribute::getID)
                        .noneMatch(addedNewColumns::contains))
                .filter(c -> c.getAttributes().stream()
                        .map(Attribute::getID)
                        .noneMatch(hiddenColumnNames::contains))
                .collect(ImmutableCollectors.toList());

        // Create unique constraints
        List<AddUniqueConstraints> inferredUniqueConstraints = inheritedConstraints.stream()
                .map(i -> new AddUniqueConstraints(
                        i.getName(),
                        i.getDeterminants().stream()
                                .map(c -> c.getID().getSQLRendering())
                                .collect(Collectors.toList()),
                        // If no PK defined in added columns keep parent PK
                        i.isPrimaryKey() && addUniqueConstraints.stream()
                                .noneMatch(k -> k.isPrimaryKey)
                ))
                .collect(Collectors.toList());

        // Throw a warning if duplicate unique constraints are added
        if (!(addUniqueConstraints.stream())
                .allMatch(new HashSet<>()::add))
            LOGGER.warn("Duplicate unique constraints found in the viewfile");


        // Return full list of added and inherited constraints, remove duplicates based on constraint attribute
        return Stream.concat(addUniqueConstraints.stream(), inferredUniqueConstraints.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Infer functional dependencies from the parent
     */
    private void insertFunctionalDependencies(NamedRelationDefinition relation,
                                              QuotedIDFactory idFactory,
                                              List<AddFunctionalDependency> addFunctionalDependencies,
                                              ImmutableList<NamedRelationDefinition> baseRelations)
            throws MetadataExtractionException {

        ImmutableSet<QuotedID> hiddenColumns = columns.hidden.stream()
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<QuotedID> addedColumns = columns.added.stream()
                .map(a -> a.name)
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        Stream<FunctionalDependencyConstruct> inheritedFDConstructs = baseRelations.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .filter(f -> canFDBeInherited(f, hiddenColumns, addedColumns))
                .map(f -> new FunctionalDependencyConstruct(
                        f.getDeterminants().stream()
                                .map(Attribute::getID)
                                .collect(ImmutableCollectors.toSet()),
                        f.getDependents().stream()
                                .map(Attribute::getID)
                                .flatMap(d -> extractNewDependents(d, addedColumns, hiddenColumns))
                                .collect(ImmutableCollectors.toSet())));

        Stream<FunctionalDependencyConstruct> declaredFdDependencies = addFunctionalDependencies.stream()
                .map(jsonFD -> new FunctionalDependencyConstruct(
                        jsonFD.determinants.stream()
                                .map(idFactory::createAttributeID)
                                .collect(ImmutableCollectors.toSet()),
                        jsonFD.dependents.stream()
                                .map(idFactory::createAttributeID)
                                .collect(ImmutableCollectors.toSet())));

        ImmutableMultimap<ImmutableSet<QuotedID>, FunctionalDependencyConstruct> fdMultimap = Stream.concat(declaredFdDependencies, inheritedFDConstructs)
                .collect(ImmutableCollectors.toMultimap(
                        FunctionalDependencyConstruct::getDeterminants,
                        fd -> fd));

        ImmutableSet<FunctionalDependencyConstruct> fdConstructs = fdMultimap.asMap().values().stream()
                .map(fds -> fds.stream()
                        .reduce((f1, f2) -> f1.merge(f2)
                                .orElseThrow(() -> new MinorOntopInternalBugException(
                                        "Should be mergeable as they are having the same determinants")))
                        // Guaranteed by the multimap structure
                        .get())
                .collect(ImmutableCollectors.toSet());

        // Insert the FDs
        for (FunctionalDependencyConstruct fdConstruct : fdConstructs) {
            addFunctionalDependency(relation, idFactory, fdConstruct);
        }
    }

    /**
     * At the moment, only consider mirror attribute (same as in the parent)
     * <p>
     * TODO: enrich the stream so as to include all derived attributes id
     */
    private Stream<QuotedID> extractNewDependents(QuotedID parentDependentId,
                                                  ImmutableSet<QuotedID> addedColumns,
                                                  ImmutableSet<QuotedID> hiddenColumns) {
        return (addedColumns.contains(parentDependentId) || hiddenColumns.contains(parentDependentId))
                ? Stream.empty()
                : Stream.of(parentDependentId);
    }

    /**
     * The selecting criteria could be relaxed in the future
     */
    private boolean canFDBeInherited(FunctionalDependency fd, ImmutableSet<QuotedID> hiddenColumns,
                                     ImmutableSet<QuotedID> addedColumns) {
        return fd.getDeterminants().stream()
                .map(Attribute::getID)
                .noneMatch(d -> hiddenColumns.contains(d) || addedColumns.contains(d));
    }

    private void addFunctionalDependency(NamedRelationDefinition viewDefinition,
                                         QuotedIDFactory idFactory,
                                         FunctionalDependencyConstruct fdConstruct) throws MetadataExtractionException {

        FunctionalDependency.Builder builder = FunctionalDependency.defaultBuilder(viewDefinition);

        try {
            for (QuotedID determinant : fdConstruct.getDeterminants())
                builder.addDeterminant(determinant);

            for (QuotedID dependent : fdConstruct.getDependents())
                builder.addDependent(dependent);
        } catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(String.format(
                    "Cannot find attribute %s for Functional Dependency %s", e.getAttributeID(), fdConstruct));
        }
        builder.build();
    }

    public void insertForeignKeys(NamedRelationDefinition relation, MetadataLookup lookup, AddForeignKey addForeignKey) throws MetadataExtractionException {

        RelationID targetRelationId = JsonMetadata.deserializeRelationID(lookup.getQuotedIDFactory(), addForeignKey.to.relation);
        NamedRelationDefinition targetRelation;
        try {
            targetRelation = lookup.getRelation(targetRelationId);
        }
        // If the target relation has not
        catch (MetadataExtractionException e) {
            LOGGER.info("Cannot find relation {} for FK {}", targetRelationId, addForeignKey.name);
            return;
        }

        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(addForeignKey.name, relation, targetRelation);

        try {
            for (String column : addForeignKey.to.columns)
                builder.add(
                        lookup.getQuotedIDFactory().createAttributeID(addForeignKey.from),
                        lookup.getQuotedIDFactory().createAttributeID(column));
        } catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }

        builder.build();
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
            "added"
    })

    @JsonPropertyOrder({
            "name",
            "determinants",
            "isPrimaryKey"
    })
    private static class AddUniqueConstraints extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final List<String> determinants;
        @Nonnull
        public final Boolean isPrimaryKey;


        @JsonCreator
        public AddUniqueConstraints(@JsonProperty("name") String name,
                                    @JsonProperty("determinants") List<String> determinants,
                                    @JsonProperty("isPrimaryKey") Boolean isPrimaryKey) {
            this.name = name;
            this.determinants = determinants;
            this.isPrimaryKey = isPrimaryKey;
        }

        /*
         * Ovverride equals method to ensure we can check for object equality
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AddUniqueConstraints other = (AddUniqueConstraints) obj;
            return Objects.equals(determinants, other.determinants);
        }

        /*
         * Ovverride hashCode method to ensure we can check for object equality
         */
        @Override
        public int hashCode() {
            return Objects.hash(determinants);
        }
    }

    @JsonPropertyOrder({
            "determinants",
            "dependents"
    })
    private static class AddFunctionalDependency extends JsonOpenObject {
        @Nonnull
        public final List<String> determinants;
        @Nonnull
        public final List<String> dependents;

        public AddFunctionalDependency(@JsonProperty("determinants") List<String> determinants,
                                       @JsonProperty("dependents") List<String> dependents) {
            this.determinants = determinants;
            this.dependents = dependents;
        }

        /*
         * Ovverride equals method to ensure we can check for object equality
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AddFunctionalDependency other = (AddFunctionalDependency) obj;
            return Objects.equals(ImmutableMap.of(determinants, dependents),
                    ImmutableMap.of(other.determinants, other.dependents));
        }

        /*
         * Ovverride hashCode method to ensure we can check for object equality
         */
        @Override
        public int hashCode() {
            return Objects.hash(ImmutableMap.of(determinants, dependents));
        }
    }

    @JsonPropertyOrder({
            "determinants",
            "dependents"
    })
    private static class AddForeignKey extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String from;
        @Nonnull
        public final ForeignKeyPart to;

        public AddForeignKey(@JsonProperty("name") String name,
                             @JsonProperty("from") String from,
                             @JsonProperty("to") ForeignKeyPart to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }
    }

    @JsonPropertyOrder({
            "relation",
            "columns"
    })
    public static class ForeignKeyPart extends JsonOpenObject {
        public final List<String> relation;
        public final List<String> columns;

        @JsonCreator
        public ForeignKeyPart(@JsonProperty("relation") List<String> relation,
                              @JsonProperty("columns") List<String> columns) {
            this.relation = relation;
            this.columns = columns;
        }
    }
}
