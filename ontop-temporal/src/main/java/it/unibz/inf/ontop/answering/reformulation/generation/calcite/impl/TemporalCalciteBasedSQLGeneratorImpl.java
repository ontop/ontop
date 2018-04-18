package it.unibz.inf.ontop.answering.reformulation.generation.calcite.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.calcite.OntopJDBCSchema;
import it.unibz.inf.ontop.answering.reformulation.generation.calcite.TemporalCalciteBasedSQLGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.calcite.TemporalRelBuilder;
import it.unibz.inf.ontop.answering.reformulation.generation.utils.COL_TYPE;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.ParserViewDefinition;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MissingTemporalIntermediateQueryNodeException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.TemporalTranslationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.GroundTermRemovalFromDataNodeReshaper;
import it.unibz.inf.ontop.iq.optimizer.PullOutVariableOptimizer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.spec.mapping.transformer.RedundantTemporalCoalesceEliminator;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.temporal.iq.node.*;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.calcite.avatica.util.TimeUnit;
import org.apache.calcite.jdbc.*;
import org.apache.calcite.materialize.MaterializationKey;
import org.apache.calcite.materialize.MaterializationService;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeFamily;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.*;
import org.apache.calcite.util.DateString;
import org.apache.calcite.util.TimeString;
import org.apache.calcite.util.TimestampString;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.eclipse.rdf4j.model.Literal;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static it.unibz.inf.ontop.answering.reformulation.generation.utils.COL_TYPE.BNODE;
import static it.unibz.inf.ontop.answering.reformulation.generation.utils.COL_TYPE.OBJECT;
import static it.unibz.inf.ontop.answering.reformulation.generation.utils.COL_TYPE.UNSUPPORTED;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.utils.ImmutableCollectors.toList;
import static it.unibz.inf.ontop.utils.ImmutableCollectors.toMap;
import static it.unibz.inf.ontop.utils.ImmutableCollectors.toSet;
import static java.lang.Math.incrementExact;
import static java.lang.Math.toIntExact;
import static org.apache.calcite.rel.type.RelDataType.PRECISION_NOT_SPECIFIED;

public class TemporalCalciteBasedSQLGeneratorImpl implements TemporalCalciteBasedSQLGenerator {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TemporalCalciteBasedSQLGeneratorImpl.class);
    private final RelDataTypeFactory typeFactory;
    private final RexBuilder rexBuilder;
    private final OntopSQLCredentialSettings settings;
    private final PullOutVariableOptimizer pullOutVariableOptimizer;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final SimpleRDF rdfFactory;
    private final TemporalTranslationFactory temporalTranslationFactory;
    private final RedundantTemporalCoalesceEliminator tcEliminator;

    private RelToSqlConverter converter;
    private MaterializationService mService;
    private CalciteSchema calciteSchema;
    private SchemaPlus rootSchema = null;
    private FrameworkConfig calciteFrameworkConfig;
    private Planner planner;

    private SqlDialect dialect;
    private Stack<String> materializationStack = new Stack();
    private List<VariableOrGroundTerm> lastProjectedArguments = new ArrayList<>();

    private Map<String, MaterializationKey> materializationKeyMap;
    private int withClauseCounter = 0;
    List<String> temporalAliases = new ArrayList<>();

    @AssistedInject
    private TemporalCalciteBasedSQLGeneratorImpl(@Assisted DBMetadata metadata,
//                                    @Nullable IRIDictionary iriDictionary,
                                                 OntopSQLCredentialSettings coreSettings,
                                                 //JdbcTypeMapper jdbcTypeMapper,
                                                 PullOutVariableOptimizer pullOutVariableOptimizer,
                                                 TermFactory termFactory,
                                                 SubstitutionFactory substitutionFactory,
                                                 TemporalTranslationFactory temporalTranslationFactory, RedundantTemporalCoalesceEliminator tcEliminator) {

        this.pullOutVariableOptimizer = pullOutVariableOptimizer;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.temporalTranslationFactory = temporalTranslationFactory;
        this.tcEliminator = tcEliminator;
        //this.rootSchema = new OntopSchemaPlus(metadata);
        this.typeFactory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
        this.rdfFactory = new SimpleRDF();
        this.rexBuilder = new RexBuilder(typeFactory);
        this.settings = coreSettings;
        mService = MaterializationService.instance();
        materializationKeyMap = new LinkedHashMap<>();
        temporalAliases.addAll(Arrays.asList("bInc", "b", "e", "eInc"));
        try {
            this.rootSchema = configRootSchema(metadata);
            calciteFrameworkConfig = Frameworks.newConfigBuilder()
                    .defaultSchema(rootSchema)
                    .build();
            calciteSchema = CalciteSchema.from(calciteFrameworkConfig.getDefaultSchema());
            planner = Frameworks.getPlanner(calciteFrameworkConfig);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private SchemaPlus configRootSchema(DBMetadata metadata) throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.calcite.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:calcite:");
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();

        Connection jdbcConnection = DriverManager.getConnection(settings.getJdbcUrl(), settings.getJdbcUser(), settings.getJdbcPassword());
        dialect = new SqlDialectFactoryImpl().create(jdbcConnection.getMetaData());
        converter = new RelToSqlConverter(dialect);

        final Set<String> schemaNames = metadata.getDatabaseRelations()
                .stream()
                .map(databaseRelationDefinition -> databaseRelationDefinition.getID().getSchemaName())
                .collect(toSet());

        Map<String, Object> operand = ImmutableMap.<String, Object>builder()
                .put("jdbcUrl", settings.getJdbcUrl())
                .put("jdbcDriver", settings.getJdbcDriver().orElse(""))
                .put("jdbcUser", settings.getJdbcUser())
                .put("jdbcPassword", settings.getJdbcPassword())
                .build();

        ImmutableMap<RelationID, RelationDefinition> diffMap = getDiffMap(metadata);

        for (String schemaName : schemaNames) {
            final OntopJDBCSchema ontopJDBCSchema = OntopJDBCSchema.create(rootSchema, schemaName, operand, diffMap);
            rootSchema.add(schemaName, ontopJDBCSchema);
        }

        return rootSchema;
    }

    private ImmutableMap<RelationID, RelationDefinition> getDiffMap(DBMetadata metadata) {
        Map<RelationID, RelationDefinition> diffMap = new HashMap<>();
        ImmutableMap<RelationID, RelationDefinition> relationMap = metadata.copyRelations();

        for (RelationID rid : relationMap.keySet()) {
            if (metadata.copyTables().keySet().stream().noneMatch(k -> k.equals(rid))) {
                diffMap.put(rid, relationMap.get(rid));
            }
        }
        return ImmutableMap.copyOf(diffMap);
    }

    @Override
    public ExecutableQuery generateSourceQuery(IntermediateQuery intermediateQuery, ImmutableList<String> signature) {

        IntermediateQuery normalizedQuery = normalizeIQ(intermediateQuery);

        final TemporalRelBuilder relBuilder = TemporalRelBuilder.create(calciteFrameworkConfig);

        final NodeProcessor np = new NodeProcessor(normalizedQuery, relBuilder, signature);

        normalizedQuery.getRootNode().acceptVisitor(np);

        log.debug("Temporal Calcite Relational Algebra has been created.");

        String sql = np.getSQL();

        return new SQLExecutableQuery(sql, signature);
    }

    @Override
    public ExecutableQuery generateEmptyQuery(ImmutableList<String> signature) {
        return null;
    }

    private IntermediateQuery normalizeIQ(IntermediateQuery intermediateQuery) {

        IntermediateQuery groundTermFreeQuery = new GroundTermRemovalFromDataNodeReshaper()
                .optimize(intermediateQuery);
        log.debug("New query after removing ground terms: \n" + groundTermFreeQuery);

        IntermediateQuery queryAfterPullOut = pullOutVariableOptimizer.optimize(groundTermFreeQuery);
        log.debug("New query after pulling out equalities: \n" + queryAfterPullOut);

        return queryAfterPullOut;
    }

    @Override
    public TemporalCalciteBasedSQLGenerator clone(DBMetadata dbMetadata) {
        return temporalTranslationFactory.createSQLGenerator(dbMetadata);
    }

    public class NodeProcessor implements TemporalQueryNodeVisitor {

        private final IntermediateQuery query;
        private final TemporalRelBuilder relBuilder;
        private final ImmutableList<Variable> signature;
        private final ImmutableMap<Variable, Variable> IQ2answerVar;


        public NodeProcessor(IntermediateQuery query, TemporalRelBuilder relBuilder, ImmutableList<String> signature) {
            this.query = query;
            this.relBuilder = relBuilder;
            this.signature = signature.stream()
                    .map(termFactory::getVariable)
                    .collect(toList());
            IQ2answerVar = buildIQ2AnswerVArMap(this.signature);
        }

        private ImmutableMap<Variable, Variable> buildIQ2AnswerVArMap(ImmutableList<Variable> signature) {
            ImmutableList<Variable> queryVariables = query.getProjectionAtom().getArguments();

            return IntStream
                    .range(0, signature.size()).boxed()
                    .collect(toMap(
                            queryVariables::get,
                            signature::get
                    ));
        }

        @Override
        public void visit(ConstructionNode constructionNode) {
            // only one child
            query.getChildrenStream(constructionNode).forEach(n -> n.acceptVisitor(this));

            boolean isRoot = constructionNode.equals(query.getRootNode());
            ImmutableSubstitution<ImmutableTerm> substitution = getCnSubstitution(isRoot, constructionNode);

            if(!materializationStack.empty()){
                String table = materializationStack.pop();
                relBuilder.scan(table);
            }

            if (constructionNode.getVariables().isEmpty()) {
                // Only for ASK
                final RexNode trueAsX = relBuilder.alias(rexBuilder.makeLiteral(true), "x");
                relBuilder.project(ImmutableList.of(trueAsX));
            } else {
                Stream<Variable> variableStream;
                if (isRoot) {
                    // respects the order in the root node
                    variableStream = signature.stream();
                } else {
                    variableStream = constructionNode.getVariables().stream();
                }

                final ImmutableList<RexNode> project = variableStream
                        .flatMap(v -> getColumnsForVariable(v, substitution, isRoot))
                        .collect(toList());

                relBuilder.project(project);
            }

//            constructionNode.getOptionalModifiers().ifPresent(
//                    modifiers -> {
//                        if (modifiers.hasOrder()) {
//                            final ImmutableList<RexNode> orderByConditions = modifiers
//                                    .getSortConditions()
//                                    .stream()
//                                    .map(s -> {
//                                        final RexInputRef field = relBuilder.field(s.getVariable().getName());
//                                        return (s.getDirection() == DESCENDING) ?
//                                                relBuilder.desc(field) : field;
//                                    })
//                                    .collect(toList());
//                            relBuilder.sort(orderByConditions);
//                        }
//
//                        if (modifiers.hasLimit() || modifiers.hasOffset()) {
//                            relBuilder.limit((int) modifiers.getOffset(), (int) modifiers.getLimit());
//                        }
//
//                        if (modifiers.isDistinct()) {
//                            relBuilder.distinct();
//                        }
//                    }
//            );
        }

        private ImmutableSubstitution<ImmutableTerm> getCnSubstitution(boolean isRoot, ConstructionNode constructionNode) {
            ImmutableSubstitution<ImmutableTerm> substitution = constructionNode.getSubstitution();
            if (isRoot) {
                substitution = substitutionFactory.getSubstitution(
                        substitution.getImmutableMap().entrySet().stream()
                                .collect(toMap(
                                        e -> IQ2answerVar.get(e.getKey()),
                                        e -> e.getValue()
                                )));
            }
            return substitution;
        }

        private Stream<RexNode> getColumnsForVariable(Variable v, ImmutableSubstitution<ImmutableTerm> substitution, boolean isRoot) {
            //if (isRoot) {
            return Stream.of(
                    getTypeColumnForVariable(v, substitution),
                    getLangColumnForVariable(v, substitution),
                    getMainColumnForVariable(v, substitution));
//            } else {
//                return Stream.of(getMainColumnForVariable(v, substitution));
//            }
        }

        private RexNode getTypeColumnForVariable(Variable v, ImmutableSubstitution<ImmutableTerm> substitution) {
            ImmutableTerm def = v;

            if (substitution.getDomain().contains(v)) {
                def = substitution.get(v);
            }

            RexNode typeDefinition = rexBuilder.makeExactLiteral(new BigDecimal(OBJECT.getQuestCode()));
            int questCode;
            if (def instanceof Variable) {

                final List<String> fieldNames = relBuilder.peek().getRowType().getFieldNames();

                if (fieldNames.contains(((Variable) def).getName() + "Type")) {
                    typeDefinition = relBuilder.field(((Variable) def).getName() + "Type");
                }


            } else if (def instanceof Function) {
                final Predicate functionSymbol = ((Function) def).getFunctionSymbol();
                if (functionSymbol instanceof URITemplatePredicate) {
                    questCode = OBJECT.getQuestCode();
                } else if (functionSymbol.getName().equals("BNODE")) {
                    questCode = COL_TYPE.BNODE.getQuestCode();
                } else {
                    final String f = functionSymbol.getName();
                    questCode = COL_TYPE.getColType(rdfFactory.createIRI(f))
                            .getQuestCode();
                }
                typeDefinition = rexBuilder.makeExactLiteral(new BigDecimal(questCode));
            }


            return relBuilder.call(SqlStdOperatorTable.AS,
                    typeDefinition,
                    //rexBuilder.makeNullLiteral(typeFactory.createSqlType(SqlTypeName.VARCHAR)),
                    rexBuilder.makeLiteral(v.getName() + "Type"));
        }

        private RexNode getLangColumnForVariable(Variable v, ImmutableSubstitution<ImmutableTerm> substitution) {
            return relBuilder.call(SqlStdOperatorTable.AS,
                    // TODO
                    rexBuilder.makeNullLiteral(typeFactory.createSqlType(SqlTypeName.VARCHAR)),
                    rexBuilder.makeLiteral(v.getName() + "Lang"));
        }

        private RexNode getMainColumnForVariable(Variable v, ImmutableSubstitution<ImmutableTerm> substitution) {
            if (substitution.getDomain().contains(v)) {
                return relBuilder.alias(termToRexNode(substitution.get(v)), v.getName());
            } else {
                return relBuilder.field(v.getName());
            }
        }

        private RexNode termToRexNode(ImmutableTerm immutableTerm) {
            return termToRexNode(immutableTerm, 1);
        }


        @Override
        public void visit(UnionNode unionNode) {
            query.getChildrenStream(unionNode).forEach(n -> n.acceptVisitor(this));
            relBuilder.union(true, query.getChildren(unionNode).size());

            // WORKAROUND
            // Add an explict project in order to workaround a bug that calcite does not handle projection properly
//            final ImmutableList<RexNode> projectNodes = unionNode.getVariables()
//                    .stream()
//                    .flatMap(v -> getColumnsForVariable(v, EMPTY_SUBSTITUTION, false))
//                    .collect(toList());
//
//            relBuilder.project(projectNodes);
        }

        @Override
        public void visit(InnerJoinNode innerJoinNode) {
            boolean first = true;

            for (QueryNode queryNode : query.getChildren(innerJoinNode)) {
                queryNode.acceptVisitor(this);
                if (queryNode instanceof TemporalOperatorNode && !materializationStack.isEmpty()){
                    String clauseName = materializationStack.pop();
                    relBuilder.scan(clauseName);
                }
                if (!first) {
                    relBuilder.join(JoinRelType.INNER);
                }
                first = false;
            }

            innerJoinNode.getOptionalFilterCondition().ifPresent(filter -> {
                relBuilder.filter(expressionToRexNode(filter, 1));
            });
        }

        @Override
        public void visit(LeftJoinNode leftJoinNode) {
            // Left Join is always binary
            query.getChildrenStream(leftJoinNode).forEach(n -> n.acceptVisitor(this));

            if (leftJoinNode.getOptionalFilterCondition().isPresent()) {
                final ImmutableExpression expression = leftJoinNode.getOptionalFilterCondition().get();
                relBuilder.join(JoinRelType.LEFT, getJoinCondition(expression, 2));
            } else {
                relBuilder.join(JoinRelType.LEFT);
            }
        }

        private RexNode getJoinCondition(ImmutableExpression expression, int inputCount) {
            final RexNode condition = expressionToRexNode(expression, 2);

            // TODO: if condition is not boolean, convert to effective boolean
            // if(condition.getType())

            return condition;
        }

        @Override
        public void visit(FilterNode filterNode) {
            // only one child
            query.getChildrenStream(filterNode).forEach(n -> n.acceptVisitor(this));
            final ImmutableExpression filterCondition = filterNode.getFilterCondition();
            final RexNode rexNode = checkNotNull(expressionToRexNode(filterCondition, 1));

            if (!materializationStack.empty()) {
                String mName = materializationStack.pop();
                MaterializationKey key = materializationKeyMap.get(mName);
                relBuilder.scan(mService.checkValid(key).name);
            }

            relBuilder.filter(rexNode);
        }

        private RexNode expressionToRexNode(ImmutableExpression filterCondition, int inputCount) {
            final OperationPredicate functionSymbol = filterCondition.getFunctionSymbol();
            final ImmutableList<RexNode> args = filterCondition.getArguments()
                    .stream()
                    .map(a -> termToRexNode(a, inputCount))
                    .collect(toList());

            if (functionSymbol instanceof ExpressionOperation) {
                return getRexNode(filterCondition, (ExpressionOperation)functionSymbol, args);
            }
            notImplemented(filterCondition);
            // not reachable
            return null;
        }


        private ImmutableTerm filterOutTemporals(ImmutableExpression filterCondition, List<String> temporalArgs1, List<String> temporalArgs2){
            final OperationPredicate functionSymbol = filterCondition.getFunctionSymbol();

            if (functionSymbol.equals(AND)){
                ImmutableTerm arg1 = filterCondition.getArguments().get(0);
                arg1 = filterOut(arg1, temporalArgs1, temporalArgs2);

                ImmutableTerm arg2 = filterCondition.getArguments().get(1);
                arg2 = filterOut(arg2, temporalArgs1, temporalArgs2);

                if (arg1 == null){
                    if (arg2 == null)
                        return null;
                    else
                        return arg2;
                } else {
                    if (arg2 == null)
                        return arg1;
                    else return  filterCondition;
                }
            }


//            final ImmutableList<ImmutableTerm> args = filterCondition.getArguments()
//                    .stream()
//                    .map(t -> filterOut(t, temporalArgs1, temporalArgs2))
//                    .collect(toList());

            return filterCondition;
        }

        private ImmutableTerm filterOut(ImmutableTerm term, List<String> temporalArgs1, List<String> temporalArgs2){
            if (term instanceof Expression){
                if (((Expression) term).getFunctionSymbol().equals(EQ)){
                    Term arg1 = ((Expression) term).getTerms().get(0);
                    Term arg2 = ((Expression) term).getTerms().get(1);
                    if (arg1 instanceof Variable && arg2 instanceof Variable){
                        if ((temporalArgs1.contains(((Variable) arg1).getName()) || temporalArgs2.contains(((Variable) arg1).getName())) &&
                                (temporalArgs1.contains(((Variable) arg2).getName()) || temporalArgs2.contains(((Variable) arg2).getName()))){
                            return null;
                        }
                    }
                    return filterOutTemporals((ImmutableExpression) term, temporalArgs1, temporalArgs2);

                }
                return filterOutTemporals((ImmutableExpression)term, temporalArgs1, temporalArgs2);
            }
            return term;
        }

        private RexNode getRexNode(ImmutableExpression filterCondition,
                                   ExpressionOperation functionSymbol, ImmutableList<RexNode> args){
            switch (functionSymbol) {
                case MINUS:
                    return relBuilder.call(SqlStdOperatorTable.MINUS, args);
                case ADD:
                    return relBuilder.call(SqlStdOperatorTable.PLUS, args);
                case SUBTRACT:
                    return relBuilder.call(SqlStdOperatorTable.MINUS, args);
                case MULTIPLY:
                    return relBuilder.call(SqlStdOperatorTable.MULTIPLY, args);
                case DIVIDE:
                    return relBuilder.call(SqlStdOperatorTable.DIVIDE, args);
                case ABS:
                    return relBuilder.call(SqlStdOperatorTable.ABS, args);
                case ROUND:
                    return relBuilder.call(SqlStdOperatorTable.ROUND, args);
                case CEIL:
                    return relBuilder.call(SqlStdOperatorTable.CEIL, args);
                case FLOOR:
                    return relBuilder.call(SqlStdOperatorTable.FLOOR, args);
                case RAND:
                    return relBuilder.call(SqlStdOperatorTable.RAND, args);
                case AND:
                    return relBuilder.call(SqlStdOperatorTable.AND, args);
                case OR:
                    return relBuilder.call(SqlStdOperatorTable.OR, args);
                case NOT:
                    return relBuilder.call(SqlStdOperatorTable.NOT, args);
                case EQ:
                    return relBuilder.call(SqlStdOperatorTable.EQUALS, args);
                case NEQ:
                    return relBuilder.call(SqlStdOperatorTable.NOT_EQUALS, args);
                case GTE:
                    return relBuilder.call(SqlStdOperatorTable.GREATER_THAN_OR_EQUAL, args);
                case GT:
                    return relBuilder.call(SqlStdOperatorTable.GREATER_THAN, args);
                case LTE:
                    return relBuilder.call(SqlStdOperatorTable.LESS_THAN_OR_EQUAL, args);
                case LT:
                    return relBuilder.call(SqlStdOperatorTable.LESS_THAN, args);
                case IS_NULL:
                    return relBuilder.call(SqlStdOperatorTable.IS_NULL, args);
                case IS_NOT_NULL:
                    return relBuilder.call(SqlStdOperatorTable.IS_NOT_NULL, args);
                case IS_TRUE:
                    notImplemented(filterCondition);
                case STR_STARTS:
                    notImplemented(filterCondition);
                case STR_ENDS:
                    notImplemented(filterCondition);
                case CONTAINS:
                    notImplemented(filterCondition);
                case STRLEN:
                    return relBuilder.call(SqlStdOperatorTable.CHAR_LENGTH, args);
                case UCASE:
                    return relBuilder.call(SqlStdOperatorTable.UPPER, args);
                case LCASE:
                    return relBuilder.call(SqlStdOperatorTable.LOWER, args);
                case SUBSTR2:
                    notImplemented(filterCondition);
                case SUBSTR3:
                    notImplemented(filterCondition);
                case STRBEFORE:
                    notImplemented(filterCondition);
                case STRAFTER:
                    notImplemented(filterCondition);
                case REPLACE:
                    return relBuilder.call(SqlStdOperatorTable.REPLACE, args);
                case CONCAT:
                    return relBuilder.call(SqlStdOperatorTable.CONCAT, args);
                case ENCODE_FOR_URI:
                    break;
                case MD5:
                    notImplemented(filterCondition);
                case SHA1:
                    notImplemented(filterCondition);
                case SHA512:
                    notImplemented(filterCondition);
                case SHA256:
                    notImplemented(filterCondition);
                case NOW:
                    notImplemented(filterCondition);
                case YEAR:
                    notImplemented(filterCondition);
                case DAY:
                    notImplemented(filterCondition);
                case MONTH:
                    notImplemented(filterCondition);
                case HOURS:
                    notImplemented(filterCondition);
                case MINUTES:
                    notImplemented(filterCondition);
                case SECONDS:
                    notImplemented(filterCondition);
                case TZ:
                    notImplemented(filterCondition);
                case SPARQL_STR:
                    notImplemented(filterCondition);
                case SPARQL_DATATYPE:
                    notImplemented(filterCondition);
                case SPARQL_LANG:
                    notImplemented(filterCondition);
                case UUID:
                    notImplemented(filterCondition);
                case STRUUID:
                    notImplemented(filterCondition);
                case IS_LITERAL:
                    notImplemented(filterCondition);
                case IS_IRI:
                    notImplemented(filterCondition);
                case IS_BLANK:
                    notImplemented(filterCondition);
                case LANGMATCHES:
                    notImplemented(filterCondition);
                case REGEX:
                    notImplemented(filterCondition);
                case SQL_LIKE:
                    notImplemented(filterCondition);
                case QUEST_CAST:
                    assert args.size() == 2;
                    // TODO use the right type for casting
                    return relBuilder.cast(args.get(0), SqlTypeName.CHAR);
                case AVG:
                    return relBuilder.call(SqlStdOperatorTable.AVG, args);
                case SUM:
                    return relBuilder.call(SqlStdOperatorTable.SUM, args);
                case MAX:
                    return relBuilder.call(SqlStdOperatorTable.MAX, args);
                case MIN:
                    return relBuilder.call(SqlStdOperatorTable.MIN, args);
                case COUNT:
                    return relBuilder.call(SqlStdOperatorTable.COUNT, args);
            }
            return null;
        }

        private RexNode termToRexNode(Term a, int inputCount) {
            if (a instanceof Expression) {
                return expressionToRexNode((ImmutableExpression) a, inputCount);
            }
            // note that not all functions are expressions
            else if (a instanceof Function) {
                final Function a1 = (Function) a;
                final Predicate f = a1.getFunctionSymbol();
                if (f instanceof URITemplatePredicate) {
                    return getRexNodeTemplateFunction(a1);
                } else if (f.getName().equals("BNODE")) {
                    Term term = a1.getTerms().get(0);
                    return termToRexNode(term, inputCount);
                } else if (a1.isDataTypeFunction()) {
                    /*
                     * Case where we have a typing function in the head (this is the
                     * case for all literal columns
                     */
                    RexNode termStr = null;
                    int size = a1.getTerms().size();
                    if ((a1 instanceof Literal) || size > 2) {
                        //termStr = getSQLStringForTemplateFunction(ov, index);
                    } else {
                        Term term = a1.getTerms().get(0);
                        termStr = termToRexNode(term, inputCount);

                    }
                    return termStr;
                } else {
                    notImplemented(a);
                }
            } else if (a instanceof Variable) {
                return getField((Variable) a, inputCount);
            } else if (a instanceof Constant) {
                final Constant constant = (Constant) a;
                COL_TYPE colType = extractColType(constant.getType()).orElse(UNSUPPORTED);
                switch (colType) {
                    case UNSUPPORTED:
                        notImplemented(a);
                    case NULL:
                        return rexBuilder.makeNullLiteral(SqlTypeName.ANY);
                    case OBJECT:
                        notImplemented(a);
                    case BNODE:
                        notImplemented(a);
                        //case LITERAL:
                        //    return rexBuilder.makeLiteral(constant.getValue());
                        //case LITERAL_LANG:
                        //    notImplemented(a);
                    case DOUBLE:
                    case FLOAT:
                        notImplemented(a);
                    case STRING:
                        return rexBuilder.makeLiteral(constant.getValue());
                    case DATETIME:
                        notImplemented(a);
                    case BOOLEAN:
                        return rexBuilder.makeLiteral(Boolean.valueOf(constant.getValue()));
                    case DATE:
                        return rexBuilder.makeDateLiteral(new DateString(constant.getValue()));
                    case TIME:
                        return rexBuilder.makeTimeLiteral(new TimeString(constant.getValue()), PRECISION_NOT_SPECIFIED);
                    case YEAR:
                    case DECIMAL:
                    case INTEGER:
                    case LONG:
                        rexBuilder.makeExactLiteral(new BigDecimal(constant.getValue()));
                    case NEGATIVE_INTEGER:
                        rexBuilder.makeExactLiteral(new BigDecimal(constant.getValue()));
                    case NON_NEGATIVE_INTEGER:
                    case POSITIVE_INTEGER:
                    case NON_POSITIVE_INTEGER:
                    case INT:
                    case UNSIGNED_INT:
                        return rexBuilder.makeExactLiteral(new BigDecimal(constant.getValue()));
                    case DATETIME_STAMP:
                        return rexBuilder.makeTimestampLiteral(new TimestampString(constant.getValue()), PRECISION_NOT_SPECIFIED);
                }
            } else {
                notImplemented(a);
            }
            notImplemented(a);
            // not reachable
            return null;
        }

        private Optional<COL_TYPE> extractColType(TermType termType) {
            if (termType instanceof ObjectRDFType) {
                COL_TYPE colType = ((ObjectRDFType) termType).isBlankNode() ? BNODE : OBJECT;
                return Optional.of(colType);
            } else if (termType instanceof RDFDatatype) {
                return Optional.of(COL_TYPE.getColType(((RDFDatatype) termType).getIRI()));
            } else
                return Optional.empty();
        }


        private RexInputRef getField(Variable a, int inputCount) {
            // final RelNode relNode = relBuilder.peek(2);
            String fieldName = a.getName();

            if (inputCount == 1) {
                return relBuilder.field(a.getName());
            }

            for (int inputOrdinal = 0; inputOrdinal < inputCount; inputOrdinal++) {
                final List<String> fieldNames = relBuilder.peek(inputCount, inputOrdinal)
                        .getRowType().getFieldNames();

                int i = fieldNames.indexOf(fieldName);
                if (i >= 0) {
                    return relBuilder.field(inputCount, inputOrdinal, i);
                }
            }

            throw new IllegalArgumentException("field [" + a.getName() + "] not found ");
        }

        @Override
        public void visit(IntensionalDataNode intensionalDataNode) {
            throw new UnsupportedOperationException("not implemented");
        }

        private RelNode convertSqlNodeToRelNode(String sqlString) {

            Planner planner = Frameworks.getPlanner(calciteFrameworkConfig);

            //sqlString = "SELECT \"sensor_id\" FROM (SELECT \"sensor_id\", LAG(\"sensor_id\") OVER() AS lag_sensor_id  FROM \"public\".\"tb_sensors\" WHERE \"sensor_type\" = 0) SUBQUERY";

            RelNode relNode = null;
            try {
                SqlNode parsedNode = planner.parse(sqlString);

                SqlNode validatedSqlNode = planner.validate(parsedNode);
                relNode = planner.rel(validatedSqlNode).project();

//                RelToSqlConverter converter = new RelToSqlConverter(dialect);
//                SqlNode sqlNode = converter.visitChild(0, relNode).asStatement();


            } catch (ValidationException | RelConversionException | SqlParseException e) {
                e.printStackTrace();
            }
            return relNode;
        }

        @Override
        public void visit(ExtensionalDataNode extensionalDataNode) {
            final DataAtom projectionAtom = extensionalDataNode.getProjectionAtom();
            final ImmutableList arguments = projectionAtom.getArguments();

            final String predicateName = projectionAtom.getPredicate().getName();

            if (relBuilder.getOntopViews().containsKey(predicateName)) {
                if (!materializationKeyMap.containsKey(predicateName)) {
                    String sqlString = ((ParserViewDefinition) relBuilder.getOntopViews().get(predicateName)).getStatement();
                    MaterializationKey key = mService.defineMaterialization(calciteSchema, null,
                            sqlString, calciteSchema.path(null), predicateName, true, false);
                    materializationKeyMap.putIfAbsent(predicateName, key);
                }
                relBuilder.scan(predicateName);
                projectArguments(arguments);
            } else {
                relBuilder.scan(predicateName.split("\\."));
                projectArguments(arguments);
            }
        }

        private void projectArguments(ImmutableList arguments){
            ImmutableList<RexNode> rexNodes = IntStream
                    .range(0, arguments.size())
                    .mapToObj(relBuilder::field)
                    .collect(toList());

            final ImmutableList<String> names = IntStream
                    .range(0, arguments.size())
                    .mapToObj(i -> arguments.get(i).toString())
                    .collect(toList());

            // "force: true" is important to workaround a bug of calcite, otherwise, the renaming might be ignored
            relBuilder.project(rexNodes, names, true);
            lastProjectedArguments.clear();
            lastProjectedArguments.addAll(arguments);
        }

        private void projectArguments2(ImmutableList <VariableOrGroundTerm> arguments){

            ImmutableList<RexNode> rexNodes = arguments.stream()
                    .map(arg -> relBuilder.field(arg.toString()))
                    .collect(toList());

            final ImmutableList<String> names = IntStream
                    .range(0, arguments.size())
                    .mapToObj(i -> arguments.get(i).toString())
                    .collect(toList());

            // "force: true" is important to workaround a bug of calcite, otherwise, the renaming might be ignored
            relBuilder.project(rexNodes, names, true);
            lastProjectedArguments.clear();
            lastProjectedArguments.addAll(arguments);
        }

        @Override
        public void visit(EmptyNode emptyNode) {
            throw new UnsupportedOperationException(emptyNode.getClass().getName() + " not implemented");
        }

        @Override
        public void visit(TrueNode trueNode) {
            throw new UnsupportedOperationException(trueNode.getClass().getName() + " not implemented");
        }

        @Override
        public void visit(DistinctNode distinctNode) {
            query.getChildrenStream(distinctNode).forEach(n -> n.acceptVisitor(this));
            relBuilder.distinct();
        }

        @Override
        public void visit(SliceNode sliceNode) {
            query.getChildrenStream(sliceNode).forEach(n -> n.acceptVisitor(this));
            relBuilder.limit((int) sliceNode.getOffset(), toIntExact(sliceNode.getLimit().get()));
        }

        @Override
        public void visit(OrderByNode orderByNode) {
            query.getChildrenStream(orderByNode).forEach(n -> n.acceptVisitor(this));
            final ImmutableList<RexNode> orderByConditions = orderByNode.getComparators()
                    .stream()
                    .map(s -> {
                        final RexInputRef field = relBuilder.field(((Variable) s.getTerm()).getName());
                        return (!s.isAscending()) ?
                                relBuilder.desc(field) : field;
                    })
                    .collect(toList());
            relBuilder.sort(orderByConditions);
        }


        RexNode getRexNodeTemplateFunction(Function ov) {
            /*
             * The first inner term determines the form of the result
             */
            Term t = ov.getTerms().get(0);

            String literalValue = "";

            if (t instanceof ValueConstant || t instanceof BNode) {
                /*
                 * The function is actually a template. The first parameter is a
                 * string of the form http://.../.../ or empty "{}" with place holders of the form
                 * {}. The rest are variables or constants that should be put in
                 * place of the place holders. We need to tokenize and form the
                 * CONCAT
                 */
                if (t instanceof BNode) {
                    //TODO: why getValue and not getName(). Change coming from v1.
                    literalValue = ((BNode) t).getName();
                } else {
                    literalValue = ((ValueConstant) t).getValue();
                }

                Predicate pred = ov.getFunctionSymbol();

                String template = trimLiteral(literalValue);

                String[] split = template.split("[{][}]");

                List<RexNode> vex = new ArrayList<>();
                if (split.length > 0 && !split[0].isEmpty()) {
                    vex.add(rexBuilder.makeLiteral(split[0]));
                }

                /*
                 * New we concat the rest of the function, note that if there is
                 * only 1 element there is nothing to concatenate
                 */
                if (ov.getTerms().size() > 1) {
                    int size = ov.getTerms().size();
                    if (pred == ExpressionOperation.IS_LITERAL) {
                        size--;
                    }
                    for (int termIndex = 1; termIndex < size; termIndex++) {
                        Term currentTerm = ov.getTerms().get(termIndex);
                        RexNode repl = termToRexNode(currentTerm, 1);
                        // TODO: replaces for IRI-safe version of string
                        if (repl.getType().getFamily() != SqlTypeFamily.CHARACTER) {
                            repl = relBuilder.cast(repl, SqlTypeName.CHAR);
                        }

                        vex.add(repl);
                        if (termIndex < split.length) {
                            final RexLiteral e = rexBuilder.makeLiteral(split[termIndex]);
                            vex.add(e);
                        }
                    }
                }


                if (vex.size() == 1) {
                    return vex.get(0);
                } else {
                    RexNode toReturn = null;
                    boolean first = true;
                    for (RexNode rn : vex) {
                        if (first) {
                            toReturn = rn;
                        } else {
                            toReturn = relBuilder.call(SqlStdOperatorTable.CONCAT, toReturn, rn);
                        }
                        first = false;
                    }
                    return toReturn;
                    //return relBuilder.call(SqlStdOperatorTable.CONCAT, vex);
                }
//                String[] params = new String[vex.size()];
//                int i = 0;
//                for (String param : vex) {
//                    params[i] = param;
//                    i += 1;
//                }
//                return getStringConcatenation(params);

            } else if (t instanceof Variable) {
                /*
                 * The function is of the form uri(x), we need to simply return the
                 * value of X
                 */
                return relBuilder.field(((Variable) t).getName());
            } else if (t instanceof URIConstant) {
                /*
                 * The function is of the form uri("http://some.uri/"), i.e., a
                 * concrete URI, we return the string representing that URI.
                 */
                return rexBuilder.makeLiteral(((URIConstant) t).getValue());
            }
            /*
             * Complex first argument: treats it as a string and ignore other arguments
             */
            else {
                /*
                 * The function is for example of the form uri(CONCAT("string",x)),we simply return the value from the database.
                 */
                throw new UnsupportedOperationException("not implemented");
                //return getSQLString(t, index, false);
            }
        }

        public String getSQL() {

            //SqlDialect dialect = SqlDialect.DatabaseProduct.H2.getDialect();
            final RelNode node = relBuilder.build();
            final SqlNode sqlNode = converter.visitChild(0, node).asStatement();
            String sqlStr =  sqlNode.toSqlString(dialect).getSql();
            //sqlStr = sqlStr.replaceAll("FROM\t\".*\"", "FROM\t.*");
            String with = appendWithClauses();
            sqlStr = with + "\n\n" + sqlStr;
            return  sqlStr;
        }

        private String appendWithClauses(){
            if (!materializationKeyMap.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("WITH ");
                for (MaterializationKey key : materializationKeyMap.values()) {
                    CalciteSchema.TableEntry tableEntry = mService.checkValid(key);
                    stringBuilder.append("\"");
                    stringBuilder.append(tableEntry.name);
                    stringBuilder.append("\"");
                    stringBuilder.append(" AS (\n");
                    String sql = tableEntry.sqls.get(0);
                    stringBuilder.append(sql);
                    stringBuilder.append("),\n");
                }
                return stringBuilder.substring(0, stringBuilder.toString().length()-2);
            }
            return "";
        }

        @Override
        public void visit(TemporalJoinNode temporalJoinNode) {

            // temporal joins must be normalized before the SQL generation step.
            // a normalized temporal join node can only have two nodes.
            QueryNode queryNode1 = query.getChildren(temporalJoinNode).get(0);
            queryNode1.acceptVisitor(this);
            Set <NonGroundTerm>keySet1 = getKeySet();
            List<VariableOrGroundTerm> argumentsToProject = new ArrayList<>(lastProjectedArguments);
            int size = lastProjectedArguments.size();
            String bInc1 = lastProjectedArguments.get(size - 4).toString();
            String b1 = lastProjectedArguments.get(size - 3).toString();
            String e1 = lastProjectedArguments.get(size - 2).toString();
            String eInc1 = lastProjectedArguments.get(size - 1).toString();
            List <String> temporalArgs1 = Arrays.asList(bInc1, b1, e1, eInc1);
            if (materializationStack.empty()){
                materialize();
            }
            String viewName1 = materializationStack.pop();

            QueryNode queryNode2 = query.getChildren(temporalJoinNode).get(1);
            queryNode2.acceptVisitor(this);
            size = lastProjectedArguments.size();
            String bInc2 = lastProjectedArguments.get(size - 4).toString();
            String b2 = lastProjectedArguments.get(size - 3).toString();
            String e2 = lastProjectedArguments.get(size - 2).toString();
            String eInc2 = lastProjectedArguments.get(size - 1).toString();
            List <String> temporalArgs2 = Arrays.asList(bInc2, b2, e2, eInc2);
            if (materializationStack.empty()){
                materialize();
            }
            String viewName2 = materializationStack.pop();

            relBuilder.scan(viewName1);
            relBuilder.scan(viewName2);
            relBuilder.join(JoinRelType.INNER);

            RexNode bInc1F = relBuilder.field(bInc1);
            RexNode b1F = relBuilder.field(b1);
            RexNode e1F = relBuilder.field(e1);
            RexNode eInc1F = relBuilder.field(eInc1);

            RexNode bInc2F = relBuilder.field(bInc2);
            RexNode b2F = relBuilder.field(b2);
            RexNode e2F = relBuilder.field(e2);
            RexNode eInc2F = relBuilder.field(eInc2);

            //begin
            RexNode c1_1 = rexBuilder.makeCall(SqlStdOperatorTable.GREATER_THAN, b1F, b2F);
            RexNode c1_2 = rexBuilder.makeCall(SqlStdOperatorTable.GREATER_THAN, e2F, b1F);
            RexNode w1_1 = rexBuilder.makeCall(SqlStdOperatorTable.AND, c1_1, c1_2);
            RexNode t1_1 = b1F;

            RexNode c1_3 = rexBuilder.makeCall(SqlStdOperatorTable.GREATER_THAN, b2F, b1F);
            RexNode c1_4 = rexBuilder.makeCall(SqlStdOperatorTable.GREATER_THAN, e1F, b2F);
            RexNode w1_2 = rexBuilder.makeCall(SqlStdOperatorTable.AND, c1_3, c1_4);
            RexNode t1_2 = b2F;

            RexNode w1_3 = rexBuilder.makeCall(SqlStdOperatorTable.EQUALS, b1F, b2F);
            RexNode t1_3 = b1F;
            RexNode case1 = relBuilder.alias(rexBuilder.makeCall(SqlStdOperatorTable.CASE, w1_1, t1_1, w1_2, t1_2, w1_3, t1_3, t1_3), b1);

            //begin Inc
            RexNode t3_1 = bInc1F;
            RexNode t3_2 = bInc2F;
            RexNode t3_3 = rexBuilder.makeCall(SqlStdOperatorTable.AND, bInc1F, bInc2F);
            RexNode case3 = relBuilder.alias(rexBuilder.makeCall(SqlStdOperatorTable.CASE, w1_1, t3_1, w1_2, t3_2, w1_3, t3_3, t3_3), bInc1);

            //end
            RexNode c2_1 = rexBuilder.makeCall(SqlStdOperatorTable.LESS_THAN, e1F, e2F);
            RexNode c2_2 = rexBuilder.makeCall(SqlStdOperatorTable.GREATER_THAN, e1F, b2F);
            RexNode w2_1 = rexBuilder.makeCall(SqlStdOperatorTable.AND, c2_1, c2_2);
            RexNode t2_1 = e1F;

            RexNode c2_3 = rexBuilder.makeCall(SqlStdOperatorTable.LESS_THAN, e2F, e1F);
            RexNode c2_4 = rexBuilder.makeCall(SqlStdOperatorTable.GREATER_THAN, e2F, b1F);
            RexNode w2_2 = rexBuilder.makeCall(SqlStdOperatorTable.AND, c2_3, c2_4);
            RexNode t2_2 = e2F;

            RexNode w2_3 = rexBuilder.makeCall(SqlStdOperatorTable.EQUALS, e1F, e2F);
            RexNode t2_3 = e1F;
            RexNode case2 = relBuilder.alias(rexBuilder.makeCall(SqlStdOperatorTable.CASE, w2_1, t2_1, w2_2, t2_2, w2_3, t2_3, t2_3), e1);

            //begin Inc
            RexNode t4_1 = eInc1F;
            RexNode t4_2 = eInc2F;
            RexNode t4_3 = rexBuilder.makeCall(SqlStdOperatorTable.AND, eInc1F, eInc2F);
            RexNode case4 = relBuilder.alias(rexBuilder.makeCall(SqlStdOperatorTable.CASE, w2_1, t4_1, w2_2, t4_2, w2_3, t4_3, t4_3), eInc1);

            RexNode joinRexNode = null;
            if (temporalJoinNode.getOptionalFilterCondition().isPresent()){
                ImmutableTerm filterCond = filterOutTemporals(temporalJoinNode.getOptionalFilterCondition().get(), temporalArgs1, temporalArgs2);
                if (filterCond != null)
                    joinRexNode = expressionToRexNode((ImmutableExpression)filterCond, 1);
            }

            RexNode or1 = rexBuilder.makeCall(SqlStdOperatorTable.OR, rexBuilder.makeCall(SqlStdOperatorTable.OR, w1_1, w1_2), w1_3);
            RexNode or2 = rexBuilder.makeCall(SqlStdOperatorTable.OR, rexBuilder.makeCall(SqlStdOperatorTable.OR, w2_1, w2_2), w2_3);
            RexNode and1 = rexBuilder.makeCall(SqlStdOperatorTable.AND, or1, or2);

            if (joinRexNode != null)
                relBuilder.filter(joinRexNode, and1);
            else
                relBuilder.filter(and1);

            List<RexNode> rexToProject = new ArrayList<>();
            rexToProject.addAll(keySet1.stream()
                    .map(k -> termToRexNode(k, 1))
                    .collect(toList()));
            rexToProject.addAll(Arrays.asList(case3, case1, case2, case4));
            relBuilder.project(rexToProject);

            lastProjectedArguments.clear();
            lastProjectedArguments.addAll(argumentsToProject);
            materialize();
        }

        private ImmutableList<String> getKeys(List<VariableOrGroundTerm> projectedArguments) {
            return projectedArguments.stream()
                    .filter(t -> t instanceof Variable)
                    .map(t-> (Variable)t)
                    .filter(v -> !temporalAliases.contains(v.getName()))
                    .map(Variable::getName)
                    .collect(ImmutableCollectors.toList());
        }

        private void createBoxView(TemporalRange range, SqlBinaryOperator operator){
            int size = lastProjectedArguments.size();
            String bInc = lastProjectedArguments.get(size - 4).toString();
            String b = lastProjectedArguments.get(size - 3).toString();
            String e = lastProjectedArguments.get(size - 2).toString();
            String eInc = lastProjectedArguments.get(size - 1).toString();

            if (!materializationStack.empty()){
                String viewName = materializationStack.pop();
                relBuilder.scan(viewName);
            }

            long beginInMs = range.getBegin().toMillis();
            long endInMs = range.getEnd().toMillis();

            RexLiteral beginInterval;
            RexNode begin = null;
            if (beginInMs > 0){
                beginInterval = rexBuilder.makeIntervalLiteral(new BigDecimal(beginInMs), getSQLIQualifier());
                RexNode n = rexBuilder.makeCall(operator, relBuilder.field(b), beginInterval);
                begin = relBuilder.alias(n, b);

            }

            RexLiteral endInterval;
            RexNode end = null;
            if (endInMs > 0){
                endInterval = rexBuilder.makeIntervalLiteral(new BigDecimal(endInMs), getSQLIQualifier());
                RexNode n = rexBuilder.makeCall(operator, relBuilder.field(e), endInterval);
                end = relBuilder.alias(n, e);

            }

            RexLiteral diffInterval;
            if (endInMs > 0 ){
                if (endInMs - beginInMs >= 0) {
                    diffInterval = rexBuilder.makeIntervalLiteral(new BigDecimal(endInMs - beginInMs), getSQLIQualifier());
                    RexNode n = rexBuilder.makeCall(SqlStdOperatorTable.MINUS, relBuilder.field(e), diffInterval);
                    RexNode x = rexBuilder.makeCall(SqlStdOperatorTable.GREATER_THAN_OR_EQUAL, n, relBuilder.field(b));
                    relBuilder.filter(x);
                }else {
                    throw new  IllegalArgumentException("start point of a time interval can not be bigger than the end point");
                }
            }

            if (begin == null)
                begin = relBuilder.field(b);
            if (end == null)
                end = relBuilder.field(e);
            //relBuilder.project(begin, end);
            RexNode bIncRex = relBuilder.field(bInc);
            RexNode eIncRex = relBuilder.field(eInc);

            List <RexNode> nodesToProject = getKeys(lastProjectedArguments).stream().map(relBuilder::field).collect(Collectors.toList());
            nodesToProject.addAll(Arrays.asList(bIncRex, begin, end, eIncRex));
            relBuilder.project(nodesToProject);
            materialize();
        }

        private void materialize(){
            RelNode node = relBuilder.build();
            final SqlNode sqlNode = converter.visitChild(0, node).asStatement();
            String sqlString = sqlNode.toSqlString(dialect).getSql();
            String clauseName = getNewWithClauseName();
            MaterializationKey key = mService.defineMaterialization(calciteSchema, null,
                    sqlString, calciteSchema.path(null), clauseName, true, true);
            materializationKeyMap.putIfAbsent(clauseName, key);
            materializationStack.push(clauseName);
        }

        private void createDiamondView(TemporalRange range, SqlBinaryOperator operator){
            int size = lastProjectedArguments.size();
            String bInc = lastProjectedArguments.get(size - 4).toString();
            String b = lastProjectedArguments.get(size - 3).toString();
            String e = lastProjectedArguments.get(size - 2).toString();
            String eInc = lastProjectedArguments.get(size - 1).toString();

            if (!materializationStack.empty()){
                String viewName = materializationStack.pop();
                relBuilder.scan(viewName);
            }

            long beginInMs = range.getBegin().toMillis();
            long endInMs = range.getEnd().toMillis();

            RexLiteral beginInterval;
            RexNode begin = null;
            if (endInMs > 0){
                beginInterval = rexBuilder.makeIntervalLiteral(new BigDecimal(endInMs), getSQLIQualifier());
                RexNode n = rexBuilder.makeCall(operator, relBuilder.field(b), beginInterval);
                begin = relBuilder.alias(n, b);
            }

            RexLiteral endInterval;
            RexNode end = null;
            if (beginInMs > 0){
                endInterval = rexBuilder.makeIntervalLiteral(new BigDecimal(beginInMs), getSQLIQualifier());
                RexNode n = rexBuilder.makeCall(operator, relBuilder.field(e), endInterval);
                end = relBuilder.alias(n, e);
            }

            if (endInMs - beginInMs < 0)
                throw new  IllegalArgumentException("start point of a time interval can not be bigger than the end point");

            if (begin == null)
                begin = relBuilder.field(b);
            if (end == null)
                end = relBuilder.field(e);
            //relBuilder.project(begin, end);
            RexNode bIncRex = relBuilder.field(bInc);
            RexNode eIncRex = relBuilder.field(eInc);

            List <RexNode> nodesToProject = getKeys(lastProjectedArguments).stream().map(relBuilder::field).collect(Collectors.toList());
            nodesToProject.addAll(Arrays.asList(bIncRex, begin, end, eIncRex));
            relBuilder.project(nodesToProject);
            materialize();
        }

        @Override
        public void visit(BoxMinusNode boxMinusNode) {
            query.getChildrenStream(boxMinusNode).forEach(n -> n.acceptVisitor(this));
            TemporalRange range = boxMinusNode.getRange();
            createBoxView(range, SqlStdOperatorTable.PLUS);
        }

        private SqlIntervalQualifier getSQLIQualifier() {
            //Calcite does not support millisecond precision
            return new SqlIntervalQualifier(TimeUnit.SECOND,
                    TimeUnit.SECOND, SqlParserPos.ZERO);
        }

        private boolean lastProjectedArgumentsContains(String s){
            return lastProjectedArguments.stream()
                    .filter(arg -> arg instanceof Variable)
                    .anyMatch(arg -> ((Variable)arg).getName().equals(s));
        }

        @Override
        public void visit(BoxPlusNode boxPlusNode) {
            query.getChildrenStream(boxPlusNode).forEach(n -> n.acceptVisitor(this));
            TemporalRange range = boxPlusNode.getRange();
            createBoxView(range, SqlStdOperatorTable.MINUS);
        }

        @Override
        public void visit(DiamondMinusNode diamondMinusNode) {
            query.getChildrenStream(diamondMinusNode).forEach(n -> n.acceptVisitor(this));

            TemporalRange range = diamondMinusNode.getRange();
            createDiamondView(range, SqlStdOperatorTable.PLUS);
        }

        @Override
        public void visit(DiamondPlusNode diamondPlusNode) {
            query.getChildrenStream(diamondPlusNode).forEach(n -> n.acceptVisitor(this));

            TemporalRange range = diamondPlusNode.getRange();
            createDiamondView(range, SqlStdOperatorTable.MINUS);
        }

        @Override
        public void visit(SinceNode sinceNode) {
            throw new UnsupportedOperationException(sinceNode.getClass().getName());
        }

        @Override
        public void visit(UntilNode untilNode) {
            throw new UnsupportedOperationException(untilNode.getClass().getName());
        }

        @Override
        public void visit(TemporalCoalesceNode temporalCoalesceNode) {
            query.getChildrenStream(temporalCoalesceNode).forEach(n -> n.acceptVisitor(this));
            //projectArguments(ImmutableList.copyOf(lastProjectedArguments));

            try {
                if (!(tcEliminator.isRedundantCoalesce(query, temporalCoalesceNode, query.getRootNode()))) {
                    // BUG: CALCITE-2057 causes the following if condition.
                    // In case of current node instance of TemporalCoalesceNode child node instance of ExtensionalDataNode,
                    // Calcite throws this exception.
                    QueryNode child = query.getFirstChild(temporalCoalesceNode).orElseThrow(() ->
                            new MissingTemporalIntermediateQueryNodeException("child of filter node is missing"));
                    if (!(child instanceof ExtensionalDataNode)) {
                        updateProjectedArguments(temporalCoalesceNode.getTerms(),
                                ImmutableList.copyOf(lastProjectedArguments.subList(lastProjectedArguments.size() - 4, lastProjectedArguments.size())));

                        RelNode node = relBuilder.peek();
                        if (!(node instanceof LogicalProject)) {
                            projectArguments2(ImmutableList.copyOf(lastProjectedArguments));
                        }

                        node = relBuilder.build();

                        final SqlNode sqlNode = converter.visitChild(0, node).asStatement();
                        String sqlString = sqlNode.toSqlString(dialect).getSql();
                        String clauseName = getNewWithClauseName();
                        MaterializationKey key = mService.defineMaterialization(calciteSchema, null,
                                sqlString, calciteSchema.path(null), clauseName, true, true);
                        materializationKeyMap.putIfAbsent(clauseName, key);


                        createCoalesceClauses(clauseName);
                    }
                }
                else {
                    System.out.println();
                }
            } catch (MissingTemporalIntermediateQueryNodeException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProjectedArguments(ImmutableSet<NonGroundTerm> keyTerms, List<VariableOrGroundTerm> temporalTerms){
        List <VariableOrGroundTerm> updatedList = new ArrayList(keyTerms.asList());
//        for(VariableOrGroundTerm arg : lastProjectedArguments){
//            if (keyTerms.contains(arg)){
//                updatedList.add(arg);
//            }
//        }
        //TODO: this is very ugly. Find a better way to do it.
        for (VariableOrGroundTerm term : temporalTerms) {
            if (!updatedList.contains(term)) {
                updatedList.add(term);
            }
        }

        lastProjectedArguments.clear();
        lastProjectedArguments.addAll(updatedList);
    }

    private  String getStrkeys(){
        String strKeys = "";
        //TODO: this is very ugly. Find a better way to do it
        if (lastProjectedArguments.size() > 4) {
            for (int i = 0; i < lastProjectedArguments.size() - 4; i ++) {
                Term term = lastProjectedArguments.get(i);
                if (term instanceof Variable) {
                    strKeys += "\"" + ((Variable) term).getName() + "\", ";
                } else {
                    throw new IllegalArgumentException(term.toString() + " is not a variable. Constants are not allowed in Temporal Coalesce.");
                }
            }
        }
        return strKeys.substring(0, strKeys.length()-2);
    }

    private ImmutableSet<NonGroundTerm> getKeySet(){
        Set <NonGroundTerm> keySet = new LinkedHashSet<>();
        //TODO: this is very ugly. Find a better way to do it
        if (lastProjectedArguments.size() > 4) {
            for (int i = 0; i < lastProjectedArguments.size() - 4; i ++) {
                VariableOrGroundTerm term = lastProjectedArguments.get(i);
                if (term instanceof Variable) {
                    keySet.add (((Variable) term));
                } else {
                    throw new IllegalArgumentException(term.toString() + " is not a variable. Constants are not allowed in Temporal Coalesce.");
                }
            }
        }
        return ImmutableSet.copyOf(keySet);
    }

    //TODO: replace string sqls with relNodes when you find a better implementation of temporal coalesce.
    private void createCoalesceClauses(String materializationName){

        String strKeys = getStrkeys();
        int size = lastProjectedArguments.size();
        String bInc = lastProjectedArguments.get(size - 4).toString();
        String b = lastProjectedArguments.get(size - 3).toString();
        String e = lastProjectedArguments.get(size - 2).toString();
        String eInc = lastProjectedArguments.get(size - 1).toString();

        String sql1 = String.format("SELECT 1 AS \"Start_ts\", 0 AS \"End_ts\", \"%s\" AS \"ts\", \"%s\" AS \"bInc\", \"%s\" AS \"eInc\", %s\t\n" +
                "FROM \"%s\" \n" +
                "UNION ALL\n" +
                "SELECT 0 AS \"Start_ts\", 1 AS \"End_ts\", \"%s\" AS \"ts\", \"%s\" AS \"bInc\", \"%s\" AS \"eInc\", %s\t\n" +
                "FROM \"%s\" ", b, bInc, eInc, strKeys, materializationName, e, bInc, eInc, strKeys, materializationName);


        String clauseName1 = createClause(sql1);

        String sql2 = String.format("SELECT \n" +
                "SUM(\"Start_ts\") OVER (PARTITION BY %s ORDER BY \"ts\", \"End_ts\" ROWS UNBOUNDED PRECEDING) AS \"Crt_Total_ts_1\",\n" +
                "SUM(\"End_ts\") OVER (PARTITION BY %s ORDER BY \"ts\", \"End_ts\" ROWS UNBOUNDED PRECEDING) AS \"Crt_Total_ts_2\",\n" +
                "SUM(\"Start_ts\") OVER (PARTITION BY %s ORDER BY \"ts\", \"End_ts\" ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) AS \"Prv_Total_ts_1\",\n" +
                "SUM(\"End_ts\") OVER (PARTITION BY %s ORDER BY \"ts\", \"End_ts\" ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) AS \"Prv_Total_ts_2\",\n" +
                "\"ts\", \"bInc\", \"eInc\", %s \n" +
                " FROM \"%s\"", strKeys, strKeys, strKeys, strKeys, strKeys, clauseName1);
        String clauseName2 = createClause(sql2);

        String sql3 = String.format("SELECT (\"Crt_Total_ts_1\" - \"Crt_Total_ts_2\") AS \"Crt_Total_ts\", " +
                "(\"Prv_Total_ts_1\" - \"Prv_Total_ts_2\") AS \"Prv_Total_ts\", \"ts\", \"bInc\", \"eInc\", %s \n" +
                "FROM \"%s\" \n" +
                "WHERE \"Crt_Total_ts_1\" = \"Crt_Total_ts_2\" OR \"Prv_Total_ts_1\" = \"Prv_Total_ts_2\" " +
                "OR \"Prv_Total_ts_1\" IS NULL OR \"Prv_Total_ts_2\" IS NULL", strKeys, clauseName2);
        String clauseName3 = createClause(sql3);

        String sql4 = String.format("SELECT %s, \"bInc\" AS \"%s\", \"prevTs\" AS \"%s\", \"ts\" AS \"%s\", \"eInc\" AS \"%s\" FROM (\n" +
                "SELECT %s, \"bInc\", LAG(\"ts\",1) OVER (PARTITION BY %s ORDER BY \"ts\", \"Crt_Total_ts\") As \"prevTs\",\n" +
                "\"ts\", \"eInc\", \"Crt_Total_ts\" \n" +
                "FROM \"%s\") F \n" +
                "WHERE \"Crt_Total_ts\" = 0", strKeys, bInc, b, e, eInc, strKeys, strKeys, clauseName3);
        String clauseName4 = createClause(sql4);
        materializationStack.push(clauseName4);

        //updateProjectedArguments(getKeySet(), temporalAliases.stream().map(termFactory::getVariable).collect(Collectors.toList()));

    }

    private String createClause(String sql){
        String clauseName = getNewWithClauseName();
        MaterializationKey key = mService.defineMaterialization(calciteSchema, null,
                sql, calciteSchema.path(null), clauseName, true, true);
        materializationKeyMap.putIfAbsent(clauseName, key);

        return clauseName;
    }

    private String getNewWithClauseName(){
        withClauseCounter++;
        return "C_" + (withClauseCounter - 1) ;
    }

    private static final Pattern pQuotes = Pattern.compile("[\"`\\['][^\\.]*[\"`\\]']");

    private static String trimLiteral(String string) {
        while (pQuotes.matcher(string).matches()) {
            string = string.substring(1, string.length() - 1);
        }
        return string;
    }

    public static void notImplemented(Object o) {
        throw new UnsupportedOperationException(o.toString() + " not implemented!");
    }

    private static void printResultSet(ResultSet resultSet) throws SQLException {
        final StringBuilder buf = new StringBuilder();
        while (resultSet.next()) {
            int n = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= n; i++) {
                buf.append(i > 1 ? "; " : "")
                        .append(resultSet.getMetaData().getColumnLabel(i))
                        .append("=")
                        .append(resultSet.getObject(i));
            }
            System.out.println(buf.toString());
            buf.setLength(0);
        }
    }


}
