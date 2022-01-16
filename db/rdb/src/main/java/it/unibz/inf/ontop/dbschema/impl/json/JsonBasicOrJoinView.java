package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.iq.visit.impl.RelationExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.ExpressionParser;
import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
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
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class JsonBasicOrJoinView extends JsonView {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonBasicOrJoinView.class);

    @Nonnull
    public final Columns columns;

    @Nonnull
    public final String filterExpression;

    protected JsonBasicOrJoinView(List<String> name, @Nullable UniqueConstraints uniqueConstraints,
                                  @Nullable OtherFunctionalDependencies otherFunctionalDependencies,
                                  @Nullable ForeignKeys foreignKeys, @Nullable NonNullConstraints nonNullConstraints,
                                  Columns columns, String filterExpression) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints);
        this.columns = columns;
        this.filterExpression = filterExpression;
    }

    @Override
    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap = extractParentDefinitions(dbParameters, parentCacheMetadataLookup);

        Integer maxParentLevel = parentDefinitionMap.keySet().stream()
                .map(r -> (r instanceof OntopViewDefinition)
                        ? ((OntopViewDefinition) r).getLevel()
                        : 0)
                .reduce(0, Math::max, Math::max);

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, parentDefinitionMap, dbParameters);

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new OntopViewDefinitionImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                maxParentLevel + 1,
                dbParameters.getCoreSingletons());
    }

    @Override
    public void insertIntegrityConstraints(OntopViewDefinition relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        insertUniqueConstraints(relation, idFactory,
                (uniqueConstraints != null) ? uniqueConstraints.added : ImmutableList.of(),
                baseRelations, coreSingletons);

        insertFunctionalDependencies(relation, idFactory,
                (otherFunctionalDependencies != null) ? otherFunctionalDependencies.added : ImmutableList.of(),
                baseRelations);

        insertForeignKeys(relation, metadataLookupForFK,
                (foreignKeys != null) ? foreignKeys.added : ImmutableList.of(),
                baseRelations);
    }

    private IQ createIQ(RelationID relationId, ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap, DBParameters dbParameters)
            throws MetadataExtractionException {

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        ImmutableSet<Variable> addedVariables = columns.added.stream()
                .map(a -> a.name)
                .map(attributeName -> normalizeAttributeName(attributeName, quotedIdFactory))
                .map(termFactory::getVariable)
                .collect(ImmutableCollectors.toSet());

        ImmutableList<Variable> projectedVariables = extractRelationVariables(addedVariables, columns.hidden, parentDefinitionMap,
                dbParameters);

        ImmutableTable<NamedRelationDefinition, Integer, Variable> parentArgumentTable = createParentArgumentTable(
                addedVariables, parentDefinitionMap, coreSingletons.getCoreUtilsFactory());

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap = extractParentAttributeMap(
                parentDefinitionMap, parentArgumentTable, quotedIdFactory);

        ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization =
                createConstructionSubstitution(projectedVariables, parentAttributeMap, dbParameters);

        IQTree parentTree = createParentTree(parentArgumentTable, iqFactory);

        ConstructionNode constructionNode = normalization.generateTopConstructionNode()
                // In case, we reintroduce a ConstructionNode to get rid of unnecessary variables from the parent relation
                // It may be eliminated by the IQ normalization
                .orElseGet(() -> iqFactory.createConstructionNode(ImmutableSet.copyOf(projectedVariables)));

        ImmutableList<ImmutableExpression> filterConditions;
        try {
            filterConditions = filterExpression != null && !filterExpression.isEmpty()
                    ? extractFilter(parentAttributeMap, quotedIdFactory, coreSingletons)
                    : ImmutableList.of();
        } catch (JSQLParserException e) {
            throw new MetadataExtractionException("Unsupported filter expression for " + ":\n" + e);
        }

        IQTree updatedParentDataNode = filterConditions.stream()
                .reduce(termFactory::getConjunction)
                .map(iqFactory::createFilterNode)
                .map(f -> updateParentDataNode(normalization,
                        iqFactory.createUnaryIQTree(f, parentTree)))
                .orElse(updateParentDataNode(normalization, parentTree));

        IQTree iqTree = iqFactory.createUnaryIQTree(constructionNode, updatedParentDataNode);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        return iqFactory.createIQ(projectionAtom, iqTree)
                .normalizeForOptimization();
    }

    abstract protected ImmutableMap<NamedRelationDefinition, String> extractParentDefinitions(DBParameters dbParameters,
                                                                                              MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException;

    private IQTree createParentTree(ImmutableTable<NamedRelationDefinition, Integer, Variable> parentArgumentTable,
                                    IntermediateQueryFactory iqFactory) throws MetadataExtractionException {
        ImmutableList<IQTree> parents = parentArgumentTable.rowKeySet().stream()
                .map(d -> iqFactory.createExtensionalDataNode(d, parentArgumentTable.row(d)))
                .collect(ImmutableCollectors.toList());

        switch (parents.size()) {
            case 0:
                throw new MetadataExtractionException("At least one base relation was expected");
            case 1:
                return parents.get(0);
            default:
                return iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(),
                        parents);
        }
    }

    private ImmutableList<Variable> extractRelationVariables(ImmutableSet<Variable> addedVariables, List<String> hidden,
                                                             ImmutableMap<NamedRelationDefinition, String> parentDefinitions, DBParameters dbParameters) {
        TermFactory termFactory = dbParameters.getCoreSingletons().getTermFactory();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        ImmutableList<String> hiddenColumnNames = hidden.stream()
                .map(attributeName -> normalizeAttributeName(attributeName, quotedIdFactory))
                .collect(ImmutableCollectors.toList());

        ImmutableList<Variable> inheritedVariableStream = parentDefinitions.entrySet().stream()
                .flatMap(e -> e.getKey().getAttributes().stream()
                        .map(a -> e.getValue() + a.getID().getName()))
                .filter(n -> !hiddenColumnNames.contains(n))
                .map(termFactory::getVariable)
                .filter(v -> !addedVariables.contains(v))
                .collect(ImmutableCollectors.toList());

        return Stream.concat(
                        addedVariables.stream(),
                        inheritedVariableStream.stream())
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableTable<NamedRelationDefinition, Integer, Variable> createParentArgumentTable(ImmutableSet<Variable> addedVariables,
                                                                                                 ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap,
                                                                                                 CoreUtilsFactory coreUtilsFactory) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(addedVariables);

        // NB: the non-necessary variables will be pruned out by normalizing the IQ
        return parentDefinitionMap.entrySet().stream()
                .flatMap(e -> {
                    ImmutableList<Attribute> parentAttributes = e.getKey().getAttributes();
                    return IntStream.range(0, parentAttributes.size())
                            .mapToObj(i -> Tables.immutableCell(e.getKey(), i, variableGenerator.generateNewVariable(
                                    e.getValue() + parentAttributes.get(i).getID().getName())));
                })
                .collect(ImmutableCollectors.toTable());
    }

    private IQTree updateParentDataNode(ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization,
                                        IQTree parentIQTree) {

        return normalization.updateChild(parentIQTree);
    }

    private ImmutableMap<QualifiedAttributeID, ImmutableTerm> extractParentAttributeMap(
            ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap,
            ImmutableTable<NamedRelationDefinition, Integer, Variable> parentArgumentTable,
            QuotedIDFactory quotedIdFactory) throws MetadataExtractionException {

        ImmutableMap<QualifiedAttributeID, Collection<Variable>> map = parentArgumentTable.cellSet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        c -> new QualifiedAttributeID(null,
                                quotedIdFactory.createAttributeID(
                                        quotedIdFactory.getIDQuotationString() +
                                                // Prefix
                                                parentDefinitionMap.get(c.getRowKey()) +
                                                // Original column name
                                                c.getRowKey().getAttributes().get(c.getColumnKey()).getID().getName() +
                                                quotedIdFactory.getIDQuotationString())),
                        Table.Cell::getValue
                )).asMap();

        ImmutableSet<QualifiedAttributeID> conflictingAttributeIds = map.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());

        if (!conflictingAttributeIds.isEmpty())
            throw new ConflictingVariableInJoinViewException(conflictingAttributeIds);

        return map.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().iterator().next()
                ));

    }

    private ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization createConstructionSubstitution(
            ImmutableList<Variable> projectedVariables,
            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
            DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();
        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
        TermFactory termFactory = coreSingletons.getTermFactory();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();



        ImmutableMap.Builder<Variable, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
        for (AddColumns a : columns.added) {
            Variable v = termFactory.getVariable(normalizeAttributeName(a.name, quotedIdFactory));
            try {
                ImmutableTerm value = extractExpression(a.expression, parentAttributeMap, quotedIdFactory, coreSingletons);
                substitutionMapBuilder.put(v, value);
            } catch (JSQLParserException | UnsupportedSelectQueryRuntimeException e) {
                throw new UnsupportedAddedColumnExpressionException("Unsupported expression for " + a.name + " in " + name + ":\n" + e, e);
            }
        }

        ConstructionSubstitutionNormalizer substitutionNormalizer = dbParameters.getCoreSingletons()
                .getConstructionSubstitutionNormalizer();

        return substitutionNormalizer.normalizeSubstitution(
                substitutionFactory.getSubstitution(substitutionMapBuilder.build()),
                ImmutableSet.copyOf(projectedVariables));
    }

    private ImmutableTerm extractExpression(String partialExpression,
                                            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
                                            QuotedIDFactory quotedIdFactory, CoreSingletons coreSingletons)
            throws JSQLParserException, UnsupportedSelectQueryRuntimeException {
        String sqlQuery = "SELECT " + partialExpression + " FROM fakeTable";
        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        net.sf.jsqlparser.expression.Expression exp = ((SelectExpressionItem) si).getExpression();
        return parser.parseTerm(exp, new RAExpressionAttributes(parentAttributeMap, null));
    }

    private ImmutableList<ImmutableExpression> extractFilter(ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
                                                             QuotedIDFactory quotedIdFactory,
                                                             CoreSingletons coreSingletons) throws JSQLParserException {
        String sqlQuery = "SELECT * FROM fakeTable WHERE " + filterExpression;
        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
        PlainSelect plainSelect = ((PlainSelect) ((Select) statement).getSelectBody());
        return plainSelect.getWhere() == null
                ? ImmutableList.of()
                : parser.parseBooleanExpression(plainSelect.getWhere(), new RAExpressionAttributes(parentAttributeMap, null));
    }


    protected String normalizeAttributeName(String attributeName, QuotedIDFactory quotedIdFactory) {
        return quotedIdFactory.createAttributeID(attributeName).getName();
    }

    protected AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryViewPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .mapToObj(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    private void insertUniqueConstraints(OntopViewDefinition relation, QuotedIDFactory idFactory,
                                         List<AddUniqueConstraints> addUniqueConstraints,
                                         ImmutableList<NamedRelationDefinition> baseRelations,
                                         CoreSingletons coreSingletons)
            throws MetadataExtractionException {


        List<AddUniqueConstraints> list = extractUniqueConstraints(relation, addUniqueConstraints, baseRelations, idFactory,
                coreSingletons);

        for (AddUniqueConstraints addUC : list) {
            if (addUC.isPrimaryKey != null && addUC.isPrimaryKey) LOGGER.warn("Primary key set in the view file for " + addUC.name);

            FunctionalDependency.Builder builder = UniqueConstraint.builder(relation, addUC.name);

            JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
            builder.build();
        }
    }

    private List<AddUniqueConstraints> extractUniqueConstraints(OntopViewDefinition relation, List<AddUniqueConstraints> addUniqueConstraints,
                                                                ImmutableList<NamedRelationDefinition> baseRelations,
                                                                QuotedIDFactory idFactory, CoreSingletons coreSingletons) {

        // List of constraints added
        ImmutableList<QuotedID> addedConstraintsColumns = (uniqueConstraints == null)
                ? ImmutableList.of()
                : uniqueConstraints.added.stream()
                .map(a -> a.determinants)
                .flatMap(Collection::stream)
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toList());

        // Filter inherited constraints
        ImmutableList<AddUniqueConstraints> inheritedConstraints = inferInheritedUniqueConstraints(relation, baseRelations,
                // TODO: refactor this parameter (should preserve composite UCs)
                addedConstraintsColumns, idFactory, coreSingletons);

        // Throw a warning if duplicate unique constraints are added
        if (!(addUniqueConstraints.stream())
                .allMatch(new HashSet<>()::add))
            LOGGER.warn("Duplicate unique constraints found in the viewfile");


        // Return full list of added and inherited constraints, remove duplicates based on constraint attribute
        return Stream.concat(addUniqueConstraints.stream(), inheritedConstraints.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Inferred from the tree
     */
    protected ImmutableList<AddUniqueConstraints> inferInheritedUniqueConstraints(OntopViewDefinition relation,
                                                                                  ImmutableList<NamedRelationDefinition> baseRelations,
                                                                                  ImmutableList<QuotedID> addedConstraintsColumns,
                                                                                  QuotedIDFactory idFactory,
                                                                                  CoreSingletons coreSingletons) {
        IQ relationIQ = relation.getIQ();

        NotYetTypedEqualityTransformer eqTransformer = coreSingletons.getNotYetTypedEqualityTransformer();
        IQTree tree = eqTransformer.transform(relationIQ.getTree())
                .normalizeForOptimization(relationIQ.getVariableGenerator());

        ImmutableSet<ImmutableSet<Variable>> variableUniqueConstraints = tree.inferUniqueConstraints();

        ImmutableList<Attribute> attributes = relation.getAttributes();
        DistinctVariableOnlyDataAtom projectedAtom = relationIQ.getProjectionAtom();

        ImmutableMap<Variable, QuotedID> variableIds = IntStream.range(0, attributes.size())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        projectedAtom::getTerm,
                        i -> attributes.get(i).getID()
                ));

        return variableUniqueConstraints.stream()
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


    /**
     * Infer functional dependencies from the parent
     *
     * TODO: for joins, we could convert the non-inherited unique constraints into general functional dependencies.
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
     *
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
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(String.format(
                    "Cannot find attribute %s for Functional Dependency %s", e.getAttributeID(), fdConstruct));
        }
        builder.build();
    }

    protected void insertForeignKeys(OntopViewDefinition relation, MetadataLookup lookup,
                                     List<AddForeignKey> addForeignKeys,
                                     ImmutableList<NamedRelationDefinition> baseRelations)
            throws MetadataExtractionException {

        List<AddForeignKey> list = extractForeignKeys(relation, addForeignKeys, baseRelations);

        for (AddForeignKey fk : list) {
            insertForeignKey(relation, lookup, fk);
        }
    }

    private ImmutableList<AddForeignKey> extractForeignKeys(OntopViewDefinition relation, List<AddForeignKey> addForeignKeys,
                                                   ImmutableList<NamedRelationDefinition> baseRelations) {
        return Stream.concat(
                    addForeignKeys.stream(),
                    inferForeignKeys(relation, baseRelations))
                .distinct()
                .collect(ImmutableCollectors.toList());
    }

    /**
     * TODO: add FKs towards the base relations
     */
    protected Stream<AddForeignKey> inferForeignKeys(OntopViewDefinition relation,
                                                     ImmutableList<NamedRelationDefinition> baseRelations) {
        return baseRelations.stream()
                .flatMap(p -> inferForeignKeysFromParent(relation, p));
    }

    protected Stream<AddForeignKey> inferForeignKeysFromParent(OntopViewDefinition relation,
                                                               NamedRelationDefinition baseRelation) {
        return baseRelation.getForeignKeys().stream()
                .flatMap(fk -> getDerivedFromParentAttributes(
                        relation,
                        fk.getComponents().stream()
                                .map(ForeignKeyConstraint.Component::getAttribute)
                                .collect(ImmutableCollectors.toList())).stream()
                        .map(as -> new AddForeignKey(
                                UUID.randomUUID().toString(),
                                JsonMetadata.serializeAttributeList(as.stream()),
                                new ForeignKeyPart(
                                        JsonMetadata.serializeRelationID(fk.getReferencedRelation().getID()),
                                        JsonMetadata.serializeAttributeList(
                                        fk.getComponents().stream()
                                                .map(ForeignKeyConstraint.Component::getReferencedAttribute))))));

    }

    protected void insertForeignKey(NamedRelationDefinition relation, MetadataLookup lookup, AddForeignKey addForeignKey) throws MetadataExtractionException {

        RelationID targetRelationId = JsonMetadata.deserializeRelationID(lookup.getQuotedIDFactory(), addForeignKey.to.relation);
        NamedRelationDefinition targetRelation;
        try {
            targetRelation = lookup.getRelation(targetRelationId);
        }
        // If the target relation has not
        catch (MetadataExtractionException e) {
            LOGGER.info("Cannot find relation {} for FK {}", targetRelationId, addForeignKey.name);
            return ;
        }

        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(addForeignKey.name, relation, targetRelation);

        int columnCount = addForeignKey.to.columns.size();
        if (addForeignKey.from.size() != columnCount)
            throw new MetadataExtractionException("Not the same number of from and to columns in FK definition");

        try {
            for (int i=0; i < columnCount; i++ ) {
                builder.add(
                        lookup.getQuotedIDFactory().createAttributeID(addForeignKey.from.get(i)),
                        lookup.getQuotedIDFactory().createAttributeID(addForeignKey.to.columns.get(i)));
            }
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }

        builder.build();
    }

    /**
     * Parent attributes are expected to all come from the same parent
     */
    protected ImmutableList<ImmutableList<Attribute>> getDerivedFromParentAttributes(
            OntopViewDefinition ontopViewDefinition, ImmutableList<Attribute> parentAttributes) {
        IQ viewIQ = ontopViewDefinition.getIQ();

        ImmutableList<RelationDefinition> parentRelations = parentAttributes.stream()
                .map(Attribute::getRelation)
                .distinct()
                .collect(ImmutableCollectors.toList());

        RelationDefinition parentRelation;
        switch (parentRelations.size()) {
            case 0:
                return ImmutableList.of();
            case 1:
                parentRelation = parentRelations.get(0);
                break;
            default:
                throw new MinorOntopInternalBugException("Was expecting all the attributes to come from the same parent");
        }

        Optional<ExtensionalDataNode> optionalParentNode = viewIQ.getTree()
                .acceptVisitor(new RelationExtractor())
                .filter(n -> n.getRelationDefinition().equals(parentRelation))
                .findAny();

        if (!optionalParentNode.isPresent())
            // TODO: log a warning
            return ImmutableList.of();

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> parentNodeArgumentMap = optionalParentNode.get().getArgumentMap();

        ImmutableList.Builder<Variable> parentVariableBuilder = ImmutableList.builder();
        for (Attribute parentAttribute : parentAttributes) {
            ImmutableTerm argument = parentNodeArgumentMap.get(parentAttribute.getIndex() - 1);
            if (!(argument instanceof Variable))
                // Unused or filtering column (the latter should not happen)
                return ImmutableList.of();
            parentVariableBuilder.add((Variable) argument);
        }

        ImmutableList<Variable> parentVariables = parentVariableBuilder.build();
        ImmutableList<Variable> projectedVariables = viewIQ.getProjectionAtom().getArguments();

        ImmutableList<Integer> parentVariableIndexes = parentVariables.stream()
                .map(projectedVariables::indexOf)
                .collect(ImmutableCollectors.toList());

        if (parentVariableIndexes.stream().anyMatch(i -> i < 0))
            // Non-projected parent variable
            return ImmutableList.of();

        return ImmutableList.of(
                parentVariableIndexes.stream()
                        .map(i -> ontopViewDefinition.getAttribute(i + 1))
                        .collect(ImmutableCollectors.toList()));
    }

    protected static class Columns extends JsonOpenObject {
        @Nonnull
        public final List<AddColumns> added;
        @Nonnull
        public final List<String> hidden;

        @JsonCreator
        public Columns(@JsonProperty("added") List<AddColumns> added,
                       @JsonProperty("hidden") List<String> hidden) {
            this.added = added;
            this.hidden = hidden;
        }
    }

    protected static class AddColumns extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String expression;


        @JsonCreator
        public AddColumns(@JsonProperty("name") String name,
                          @JsonProperty("expression") String expression) {
            this.name = name;
            this.expression = expression;
        }
    }
}
