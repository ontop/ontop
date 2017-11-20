package it.unibz.inf.ontop.answering.reformulation.generation.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.google.common.base.Joiner;
import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLAdapterFactory;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl.DB2SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.utils.XsdDatatypeConverter;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.DatalogAlgebraOperatorPredicates;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopTypingException;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.OrderCondition;
import it.unibz.inf.ontop.iq.optimizer.GroundTermRemovalFromDataNodeReshaper;
import it.unibz.inf.ontop.iq.optimizer.PullOutVariableOptimizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BNodePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.impl.TermUtils;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.EncodeForURI;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE.*;

/**
 * This class generates an SQLExecutableQuery from the datalog program coming from the
 * unfolder.
 *
 * This class is NOT thread-safe (attributes values are query-dependent).
 * Thus, an instance of this class should NOT BE SHARED between QuestStatements but be DUPLICATED.
 *
 *
 * @author mrezk, mariano, guohui
 *
 */
public class OneShotSQLGeneratorEngine {

	/**
	 * Formatting template
	 */
    private static final String VIEW_PREFIX = "Q";
    private static final String VIEW_SUFFIX = "VIEW";
    private static final String VIEW_ANS_SUFFIX = "View";

    private static final String TYPE_SUFFIX = "QuestType";
    private static final String LANG_SUFFIX = "Lang";
    private static final String MAIN_COLUMN_SUFFIX = "";

	private static final String INDENT = "    ";

	private final RDBMetadata metadata;
	private final SQLDialectAdapter sqladapter;
	private final IntermediateQuery2DatalogTranslator iq2DatalogTranslator;

	private final boolean distinctResultSet;
	private final String replace1, replace2;

	@Nullable
	private final IRIDictionary uriRefIds;

	private final ImmutableMap<ExpressionOperation, String> operations;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OneShotSQLGeneratorEngine.class);
	private final JdbcTypeMapper jdbcTypeMapper;

	/**
	 * Mutable (query-dependent)
	 */
	private boolean isDistinct = false;
	private boolean isOrderBy = false;


	OneShotSQLGeneratorEngine(DBMetadata metadata,
							  IRIDictionary iriDictionary,
							  OntopReformulationSQLSettings settings,
							  JdbcTypeMapper jdbcTypeMapper,
							  IntermediateQuery2DatalogTranslator iq2DatalogTranslator) {

		String driverURI = settings.getJdbcDriver()
				.orElseGet(() -> {
					try {
						return DriverManager.getDriver(settings.getJdbcUrl()).getClass().getCanonicalName();
					}
					catch (SQLException e) {
						// TODO: find a better exception
						throw new RuntimeException("Impossible to get the JDBC driver. Reason: " + e.getMessage());
					}
				});

		if (!(metadata instanceof RDBMetadata)) {
			throw new IllegalArgumentException("Not a DBMetadata!");
		}

		this.metadata = (RDBMetadata)metadata;
		this.sqladapter = SQLAdapterFactory.getSQLDialectAdapter(driverURI,this.metadata.getDbmsVersion(), settings);
		this.operations = buildOperations(sqladapter);
		this.distinctResultSet = settings.isDistinctPostProcessingEnabled();
		this.iq2DatalogTranslator = iq2DatalogTranslator;

		if (settings.isIRISafeEncodingEnabled()) {
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for (Entry<String, String> e : EncodeForURI.TABLE.entrySet()) {
				sb1.append("REPLACE(");
				sb2.append(", '").append(e.getValue()).append("', '").append(e.getKey()).append("')");
			}
			replace1 = sb1.toString();
			replace2 = sb2.toString();
		}
		else {
			replace1 = replace2 = "";
		}

		this.uriRefIds = iriDictionary;
		this.jdbcTypeMapper = jdbcTypeMapper;
 	}

	/**
	 * For clone purposes only
	 */
	private OneShotSQLGeneratorEngine(RDBMetadata metadata, SQLDialectAdapter sqlAdapter,
                                      String replace1, String replace2, boolean distinctResultSet,
                                      IRIDictionary uriRefIds, JdbcTypeMapper jdbcTypeMapper,
                                      ImmutableMap<ExpressionOperation, String> operations,
									  IntermediateQuery2DatalogTranslator iq2DatalogTranslator) {
		this.metadata = metadata;
		this.sqladapter = sqlAdapter;
		this.operations = operations;
		this.replace1 = replace1;
		this.replace2 = replace2;
		this.distinctResultSet = distinctResultSet;
		this.uriRefIds = uriRefIds;
		this.jdbcTypeMapper = jdbcTypeMapper;
		this.iq2DatalogTranslator = iq2DatalogTranslator;
	}

	private static ImmutableMap<ExpressionOperation, String> buildOperations(SQLDialectAdapter sqladapter) {
		ImmutableMap.Builder<ExpressionOperation, String> builder = new ImmutableMap.Builder<ExpressionOperation, String>()
				.put(ExpressionOperation.ADD, "%s + %s")
				.put(ExpressionOperation.SUBTRACT, "%s - %s")
				.put(ExpressionOperation.MULTIPLY, "%s * %s")
				.put(ExpressionOperation.DIVIDE, "(1.0 * %s) / %s")
				.put(ExpressionOperation.ABS, "ABS(%s)")
				.put(ExpressionOperation.CEIL, sqladapter.ceil())
				.put(ExpressionOperation.FLOOR, "FLOOR(%s)")
				.put(ExpressionOperation.ROUND, sqladapter.round())
				.put(ExpressionOperation.RAND, sqladapter.rand())
				.put(ExpressionOperation.EQ, "%s = %s")
				.put(ExpressionOperation.NEQ, "%s <> %s")
				.put(ExpressionOperation.GT, "%s > %s")
				.put(ExpressionOperation.GTE, "%s >= %s")
				.put(ExpressionOperation.LT, "%s < %s")
				.put(ExpressionOperation.LTE, "%s <= %s")
				.put(ExpressionOperation.AND, "%s AND %s")
				.put(ExpressionOperation.OR, "%s OR %s")
				.put(ExpressionOperation.NOT, "NOT %s")
				.put(ExpressionOperation.IS_NULL, "%s IS NULL")
				.put(ExpressionOperation.IS_NOT_NULL, "%s IS NOT NULL")
				.put(ExpressionOperation.IS_TRUE, "%s IS TRUE")
				.put(ExpressionOperation.SQL_LIKE, "%s LIKE %s")
				.put(ExpressionOperation.STR_STARTS, sqladapter.strStartsOperator())
				.put(ExpressionOperation.STR_ENDS, sqladapter.strEndsOperator())
				.put(ExpressionOperation.CONTAINS, sqladapter.strContainsOperator())
				.put(ExpressionOperation.NOW, sqladapter.dateNow());

		try {
			builder.put(ExpressionOperation.STRUUID, sqladapter.strUuid());
		} catch (UnsupportedOperationException e) {
			// ignore
		}
		try {
			builder.put(ExpressionOperation.UUID, sqladapter.uuid());
		} catch (UnsupportedOperationException e) {
			// ignore
		}
		return builder.build();
	}

	/**
	 * SQLGenerator must not be shared between threads
	 * but CLONED.
	 *
	 * @return A cloned object without any query-dependent value
	 */
	@Override
	public OneShotSQLGeneratorEngine clone() {
		return new OneShotSQLGeneratorEngine(metadata, sqladapter,
				replace1, replace2, distinctResultSet, uriRefIds, jdbcTypeMapper, operations, iq2DatalogTranslator);
	}

	/**
	 * Generates and SQL query ready to be executed by Quest. Each query is a
	 * SELECT FROM WHERE query. To know more about each of these see the inner
	 * method descriptions. Observe that the SQL itself will be done by
	 * {@link #generateQuery}
	 *
	 */
	public SQLExecutableQuery generateSourceQuery(IntermediateQuery intermediateQuery, ImmutableList<String> signature)
			throws OntopReformulationException {

		IntermediateQuery normalizedQuery = normalizeIQ(intermediateQuery);

		DatalogProgram queryProgram = iq2DatalogTranslator.translate(normalizedQuery);

		normalizeProgram(queryProgram);

		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(queryProgram);
		Multimap<Predicate, CQIE> ruleIndex = depGraph.getRuleIndex();

		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();
		List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();

		MutableQueryModifiers queryModifiers = queryProgram.getQueryModifiers();
		isDistinct = queryModifiers.hasModifiers()
						&& queryModifiers.isDistinct();

		isOrderBy = queryModifiers.hasModifiers()
						&& !queryModifiers.getSortConditions().isEmpty();

		final String sqlQuery;
		String subquery = generateQuery(signature, ruleIndex, predicatesInBottomUp, extensionalPredicates);
		if (queryModifiers.hasModifiers()) {
			final String outerViewName = "SUB_QVIEW";

			//List<Variable> groupby = queryProgram.getQueryModifiers().getGroupConditions();
			// if (!groupby.isEmpty()) {
			// subquery += "\n" + sqladapter.sqlGroupBy(groupby, "") + " " +
			// havingStr + "\n";
			// }
			// List<OrderCondition> conditions =
			// query.getQueryModifiers().getSortConditions();

			long limit = queryModifiers.getLimit();
			long offset = queryModifiers.getOffset();
			List<OrderCondition> conditions = queryModifiers.getSortConditions();

			final String modifier;
			if (!conditions.isEmpty()) {
				modifier = sqladapter.sqlOrderByAndSlice(conditions, outerViewName, limit, offset) + "\n";
			}
			else if (limit != -1 || offset != -1) {
				modifier = sqladapter.sqlSlice(limit, offset) + "\n";
			}
			else {
				modifier = "";
			}

			sqlQuery = "SELECT *\n" +
					"FROM (\n" + subquery + "\n" + ") " + outerViewName + "\n" +
					modifier;
		}
		else {
			sqlQuery = subquery;
		}
		return new SQLExecutableQuery(sqlQuery, signature);
	}

	private IntermediateQuery normalizeIQ(IntermediateQuery intermediateQuery) {

		IntermediateQuery groundTermFreeQuery = new GroundTermRemovalFromDataNodeReshaper()
				.optimize(intermediateQuery);
		log.debug("New query after removing ground terms: \n" + groundTermFreeQuery);

		IntermediateQuery queryAfterPullOut = new PullOutVariableOptimizer().optimize(groundTermFreeQuery);
		log.debug("New query after pulling out equalities: \n" + queryAfterPullOut);

		return queryAfterPullOut;
	}


	/**
	 * Main method. Generates the full SQL query, taking into account
	 * limit/offset/order by. An important part of this program is
	 * {@link #generateQueryFromRules}
	 * that will create a view for every ans predicate in the Datalog input
	 * program.
	 *
	 * @param signature
	 *            The Select variables in the SPARQL query
	 * @param ruleIndex
	 *            The index that maps intentional predicates to its rules
	 * @param predicatesInBottomUp
	 *            The topologically ordered predicates in
	 *            <code> query </code>.
	 * @param extensionalPredicates
	 *            The predicates that are not defined by any rule in <code>
	 *            query </code>
	 * @return
	 */
	private String generateQuery(List<String> signature,
								 Multimap<Predicate, CQIE> ruleIndex,
								 List<Predicate> predicatesInBottomUp,
								 List<Predicate> extensionalPredicates) throws OntopReformulationException {

		TypeExtractor.TypeResults typeResults;
		try {
			typeResults = TypeExtractor.extractTypes(ruleIndex, predicatesInBottomUp, metadata);
			/*
			 * Currently, incompatible terms are treated as a reformulation error
			 */
		}
		catch (IncompatibleTermException e) {
			throw new OntopTypingException(e.getMessage());
		}

		ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap = typeResults.getTermTypeMap();
		ImmutableMap<Predicate, ImmutableList<COL_TYPE>> castTypeMap = typeResults.getCastTypeMap();

		/**
		 * ANS i > 1
		 */

		ImmutableMap.Builder<Predicate, ParserViewDefinition> subQueryDefinitionsBuilder = ImmutableMap.builder();
		Set<RelationID> usedAliases = new HashSet<>();
		// create a view for every ans predicate in the Datalog input program.
		int numPreds = predicatesInBottomUp.size();
		for (int i = 0; i < numPreds - 1; i++) {
			Predicate pred = predicatesInBottomUp.get(i);
			if (!extensionalPredicates.contains(pred)) {
				// extensional predicates are defined by DBs
				// so, we skip them
				/**
				 * handle the semantics of OPTIONAL when there
				 * are multiple mappings or Unions. It will take mappings of the form
				 * <ul>
				 * <li>Concept <- definition1</li>
				 * <li>Concept <- definition2</li>
				 * </ul>
				 * And will generate a view of the form
				 * <ul>
				 * <li>QConceptView = definition1 UNION definition2
				 * </ul>
				 * This view is stored in the <code>metadata </code>. See DBMetadata
				 *
				 * The idea is to use the view definition in the case of Union in the
				 * Optionals/LeftJoins
				 */

				// all have the same arity
				int size = ruleIndex.get(pred).iterator().next().getHead().getArity();
				// create signature
				ImmutableList.Builder<String> builder = ImmutableList.builder();
				for (int k = 0; k < size; k++) {
					builder.add("v" + k);
				}
				ImmutableList<SignatureVariable> s =
						createSignature(builder.build(), castTypeMap.get(pred));

				// Creates BODY of the view query
				String subquery = generateQueryFromRules(ruleIndex.get(pred), s, ruleIndex,
						subQueryDefinitionsBuilder.build(), termTypeMap, false);

				RelationID subqueryAlias = createAlias(pred.getName(), VIEW_ANS_SUFFIX, usedAliases);
				usedAliases.add(subqueryAlias);

				// creates a view outside the DBMetadata (specific to this sub-query)
				ParserViewDefinition view = new ParserViewDefinition(subqueryAlias, subquery);
				for (SignatureVariable var : s) {
					for (String alias : var.columnAliases) {
						view.addAttribute(new QualifiedAttributeID(subqueryAlias,
								metadata.getQuotedIDFactory().createAttributeID(alias)));
					}
				}
				subQueryDefinitionsBuilder.put(pred, view);
			}
		}

		/**
		 * ANS 1
		 */

		// This should be ans1, and the rules defining it.
		Predicate predAns1 = predicatesInBottomUp.get(numPreds - 1);
		ImmutableList<SignatureVariable> s = createSignature(signature, castTypeMap.get(predAns1));

		return generateQueryFromRules(ruleIndex.get(predAns1), s, ruleIndex,
				subQueryDefinitionsBuilder.build(), termTypeMap,
				isDistinct && !distinctResultSet);
	}




	/**
	 * Takes a collection of Datalog rules <code> cqs </code> and returns the SQL
	 * translation of the rules. It is a helper method for{@link #generateQuery}
	 *
	 * @param cqs
	 * @param signature
	 * @param ruleIndex
	 * @param subQueryDefinitions
	 * @param termTypeMap
	 * @param unionNoDuplicates
	 */
	private String generateQueryFromRules(Collection<CQIE> cqs,
										  ImmutableList<SignatureVariable> signature,
										  Multimap<Predicate, CQIE> ruleIndex,
										  ImmutableMap<Predicate, ParserViewDefinition> subQueryDefinitions,
										  ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
										  boolean unionNoDuplicates) {

		List<String> sqls = Lists.newArrayListWithExpectedSize(cqs.size());
		for (CQIE cq : cqs) {
		    /* Main loop, constructing the SPJ query for each CQ */
			QueryAliasIndex index = new QueryAliasIndex(cq, subQueryDefinitions, ruleIndex);

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			if (isDistinct && !distinctResultSet) {
				sb.append("DISTINCT ");
			}

			List<Term> terms = cq.getHead().getTerms();
			List<Optional<TermType>> termTypes = termTypeMap.get(cq);

			List<String> selectList = Lists.newArrayListWithCapacity(signature.size());
			for (int i = 0; i < signature.size(); i++) {
				selectList.add(getSelectClauseFragment(signature.get(i), terms.get(i),  termTypes.get(i), index));
			}

			if (selectList.isEmpty())
				sb.append("'true' AS x"); 				//Only for ASK
			else
				Joiner.on(", ").appendTo(sb, selectList);

			sb.append("\n FROM \n");
			List<String> tableDefinitions = getTableDefs(cq.getBody(), index, "");
			if (tableDefinitions.isEmpty()) {
				sb.append("(" + sqladapter.getDummyTable() + ") tdummy");
			}
			else {
				Joiner.on(",\n").appendTo(sb, tableDefinitions);
			}

			String conditions = getConditionsString(cq.getBody(), index, false, "");
			if (!conditions.isEmpty())
				sb.append("\nWHERE \n").append(conditions);

			List<String> groupBy = getGroupBy(cq.getBody(), index);
			if (!groupBy.isEmpty())
				sb.append("\nGROUP BY " + Joiner.on(", ").join(groupBy));

			List<Function> having = getHaving(cq.getBody());
			if (!having.isEmpty())
				sb.append("\nHAVING (" + Joiner.on(" AND ").join(getBooleanConditionsString(having, index)) + ") ");

			sqls.add(sb.toString());
		}
		return sqls.size() == 1
				? sqls.get(0)
				: "(" + Joiner.on(")\n " + (unionNoDuplicates ? "UNION" : "UNION ALL") + "\n (")
						.join(sqls) + ")";
	}


	private List<Function> convert(List<Term> terms) {
		return terms.stream().map(c -> (Function)c).collect(ImmutableCollectors.toList());
	}

	private List<Function> getHaving(List<Function> body) {
		for (Function atom : body) {
			if (atom.getFunctionSymbol() == DatalogAlgebraOperatorPredicates.SPARQL_HAVING) {
				return convert(atom.getTerms());
			}
		}
		return Collections.emptyList();
	}

	private List<String> getGroupBy(List<Function> body, QueryAliasIndex index) {
		List<String> groupReferences = Lists.newArrayList();
		for (Function atom : body) {
			if (atom.getFunctionSymbol() == DatalogAlgebraOperatorPredicates.SPARQL_GROUP) {
				for (Variable var : atom.getVariables()) {
					index.getColumnReferences(var).stream()
							.map(QualifiedAttributeID::getSQLRendering)
							.forEach(groupReferences::add);
				}
			}
		}
		return groupReferences;
	}


	/**
	 * Normalizations of the Datalog program requirend by the Datalog to SQL translator
	 *
	 * @param program
	 */
	private void normalizeProgram(DatalogProgram program) {
		for (CQIE rule : program.getRules()) {
			DatalogNormalizer.foldJoinTrees(rule);
			DatalogNormalizer.addMinimalEqualityToLeftJoin(rule);
		}
		log.debug("Program normalized for SQL translation: \n"+program);
	}


	private RelationID createAlias(String predicateName, String suffix, Collection<RelationID> alreadyAllocatedViewNames) {
		// Escapes view names.
		String safePredicateName = predicateName
				.replace('.', '_')
				.replace(':', '_')
				.replace('/', '_')
				.replace(' ', '_');
		String alias = sqladapter.nameView(VIEW_PREFIX, safePredicateName, suffix, alreadyAllocatedViewNames);
		return metadata.getQuotedIDFactory().createRelationID(null, alias);
	}

	/***
	 * Returns a string with boolean conditions formed with the boolean atoms
	 * found in the atoms list.
	 */
	private Set<String> getBooleanConditionsString(List<Function> atoms, QueryAliasIndex index) {
		Set<String> conditions = new LinkedHashSet<>();
		for (Function atom : atoms) {
			// Boolean expression
			if (atom.isOperation()) {
				String condition = getSQLCondition(atom, index);
				conditions.add(condition);
			}
			else if (atom.isDataTypeFunction()) {
				String condition = getSQLString(atom, index, false);
				conditions.add(condition);
			}
		}
		return conditions;
	}

	/***
	 * Returns the SQL for an atom representing an SQL condition (booleans).
	 */
	private String getSQLCondition(Function atom, QueryAliasIndex index) {
		Predicate functionSymbol = atom.getFunctionSymbol();
		if (functionSymbol.getArity() == 1) {
			// For unary boolean operators, e.g., NOT, IS NULL, IS NOT NULL.
			// added also for IS TRUE
			String expressionFormat = operations.get(functionSymbol);
			Term term = atom.getTerm(0);
			String column = getSQLString(term, index, false);
			if (expressionFormat.contains("NOT %s")) {
				// find data type of term and evaluate accordingly
				// int type = 8;
				if (term instanceof Function) {
					Function f = (Function) term;
					if (!f.isDataTypeFunction())
						return String.format(expressionFormat, column);
				}
				int type = getVariableDataType(term);
				switch (type) {
					case Types.INTEGER:
					case Types.BIGINT:
					case Types.DOUBLE:
					case Types.FLOAT:
						return String.format("%s = 0", column);
					case Types.BOOLEAN:
						return String.format("NOT %s", column);
					case Types.VARCHAR:
						return String.format("NOT LENGTH(%s) > 0", column);
					default:
						throw new UnsupportedOperationException("Unsupported type: " + type);
				}
			}
			if (expressionFormat.contains("IS TRUE")) {
				// find data type of term and evaluate accordingly
				int type = getVariableDataType(term);
				switch (type) {
					case Types.INTEGER:
					case Types.BIGINT:
					case Types.DOUBLE:
					case Types.FLOAT:
						return String.format("%s != 0", column);
					case Types.BOOLEAN:
						return String.format("%s", column);
					case Types.VARCHAR:
						return String.format("LENGTH(%s) > 0", column);
					default:
						throw new UnsupportedOperationException("Unsupported type: " + type);
				}
			}
			return String.format(expressionFormat, column);
		}
		else if (functionSymbol.getArity() == 2) {
			// For binary boolean operators, e.g., AND, OR, EQ, GT, LT, etc.
			String expressionFormat = operations.get(functionSymbol);
			Term left = atom.getTerm(0);
			Term right = atom.getTerm(1);
			String leftOp = getSQLString(left, index, true);
			String rightOp = getSQLString(right, index, true);

			return String.format("(" + expressionFormat + ")", leftOp, rightOp);
		}
		else {
			if (functionSymbol == ExpressionOperation.REGEX) {
				boolean caseinSensitive = false;
				boolean multiLine = false;
				boolean dotAllMode = false;
				if (atom.getArity() == 3) {
					String options = atom.getTerm(2).toString();
					caseinSensitive = options.contains("i");
					multiLine = options.contains("m");
					dotAllMode = options.contains("s");
				}
				Term p1 = atom.getTerm(0);
				Term p2 = atom.getTerm(1);

				String column = getSQLString(p1, index, false);
				String pattern = getSQLString(p2, index, false);
				String sqlRegex = sqladapter.sqlRegex(column, pattern, caseinSensitive,
						multiLine, dotAllMode);
				return sqlRegex;
			}
			else
				throw new RuntimeException("The builtin function "
						+ functionSymbol + " is not supported yet!");
		}
	}

	private List<String> getTableDefs(List<Function> atoms, QueryAliasIndex index, String indent) {
		/*
		 * We now collect the view definitions for each data atom each
		 * condition, and each each nested Join/LeftJoin
		 */
		List<String> tableDefinitions = Lists.newArrayListWithCapacity(atoms.size());
		for (Function a : atoms) {
			String definition = getTableDefinition(a, index, indent + INDENT);
			if (!definition.isEmpty()) {
				tableDefinitions.add(definition);
			}
		}
		return tableDefinitions;
	}

	/**
	 * Returns the table definition for these atoms. By default, a list of atoms
	 * represents JOIN or LEFT JOIN of all the atoms, left to right. All boolean
	 * atoms in the list are considered conditions in the ON clause of the JOIN.
	 *
	 * <p>
	 * If the list is a LeftJoin, then it can only have 2 data atoms, and it HAS
	 * to have 2 data atoms.
	 *
	 * <p>
	 * If process boolean operators is enabled, all boolean conditions will be
	 * added to the ON clause of the first JOIN.
	 *
	 * @param atoms
	 * @param index
	 *
	 * @return
	 */
	private String getTableDefinitions(List<Function> atoms,
									   QueryAliasIndex index, String JOIN_KEYWORD, boolean parenthesis,
									   String indent) {

		List<String> tableDefinitions = getTableDefs(atoms, index, indent);

		int size = tableDefinitions.size();
		if (size == 0) {
			throw new RuntimeException("Cannot generate definition for empty data");
		}
		if (size == 1) {
			return tableDefinitions.get(0);
		}

		String JOIN_WITH_PARENTHESIS = indent + indent + "%s\n" + indent + JOIN_KEYWORD + "\n" + indent + "(%s)" + indent;
		String JOIN_NO_PARENTHESIS = indent + indent + "%s\n" + indent + JOIN_KEYWORD + "\n" + indent + "%s" + indent;
		/*
		 * Now we generate the table definition: Join/LeftJoin
		 * (possibly nested if there are more than 2 table definitions in the
		 * current list) in case this method was called recursively.
		 *
		 * To form the JOIN we will cycle through each data definition,
		 * nesting the JOINs as we go. The conditions in the ON clause will
		 * go on the TOP level only.
		 */
		String currentJoin = String.format(parenthesis ? JOIN_WITH_PARENTHESIS : JOIN_NO_PARENTHESIS,
				tableDefinitions.get(size - 2), tableDefinitions.get(size - 1));

		for (int i = size - 3; i >= 0; i--) {
			currentJoin = String.format(JOIN_WITH_PARENTHESIS, tableDefinitions.get(i), currentJoin);
		}

	    // If there are ON conditions we add them now.
		String conditions = getConditionsString(atoms, index, true, indent);

		StringBuilder sb = new StringBuilder();
		sb.append(currentJoin).append(" ON\n").append(conditions).append("\n").append(indent);
		return sb.toString();
	}

	/**
	 * Returns the table definition for the given atom. If the atom is a simple
	 * table or view, then it returns the value as defined by the
	 * QueryAliasIndex. If the atom is a Join or Left Join, it will call
	 * getTableDefinitions on the nested term list.
	 */
	private String getTableDefinition(Function atom, QueryAliasIndex index, String indent) {

		if (atom.isOperation() || atom.isDataTypeFunction()) {
			// These don't participate in the FROM clause
			return "";
		}
		else if (atom.isAlgebraFunction()) {
			Predicate predicate = atom.getFunctionSymbol();

			if (predicate == DatalogAlgebraOperatorPredicates.SPARQL_GROUP) {
				return "";
			}

			if (predicate == DatalogAlgebraOperatorPredicates.SPARQL_JOIN ||
					predicate == DatalogAlgebraOperatorPredicates.SPARQL_LEFTJOIN) {

				boolean isLeftJoin = (predicate == DatalogAlgebraOperatorPredicates.SPARQL_LEFTJOIN);
				boolean parenthesis = false;

				int i = 0;
				for (Term term : atom.getTerms()) {
					if (((Function) term).isAlgebraFunction()) {
						//nested joins we need to add parenthesis later
						parenthesis = true;
					}
					else if (isLeftJoin && i == 1) {
						//in case of left join we  want to add the parenthesis
						// only for the right tables
						//we ignore nested joins from the left tables
						parenthesis = false;
					}
					i++;
				}
				String tableDefinitions =  getTableDefinitions(convert(atom.getTerms()), index,
						isLeftJoin ? "LEFT OUTER JOIN" : "JOIN", parenthesis, indent + INDENT);
				return tableDefinitions;
			}
		}

		/*
		 * This is a data atom
		 */
		return index.getViewDefinition(atom);
	}

	/**
	 * Generates all the conditions on the given atoms, e.g., shared variables
	 * and boolean conditions. This string can then be used to form a WHERE or
	 * an ON clause.
	 *
	 * <p>
	 * The method assumes that no variable in this list (or nested ones) referes
	 * to an upper level one.
	 */
	private String getConditionsString(List<Function> atoms,
									   QueryAliasIndex index, boolean processShared, String indent) {

		Set<String> conditions = new LinkedHashSet<>();

		// if (processShared)
		// guohui: After normalization, do we have shared variables?
		// TODO: should we remove this ??
		Set<String> conditionsSharedVariablesAndConstants =
				getConditionsSharedVariablesAndConstants(atoms, index, processShared);
		conditions.addAll(conditionsSharedVariablesAndConstants);

		Set<String> booleanConditions = getBooleanConditionsString(atoms, index);
		conditions.addAll(booleanConditions);

		return conditions.isEmpty() ? "" : indent + Joiner.on(" AND\n" + indent).join(conditions);
	}

	/**
	 * Returns the set of variables that participate data atoms (either in this
	 * atom directly or in nested ones). This will recursively collect the
	 * variables references in in this atom, exlcuding those on the right side
	 * of left joins.
	 *
	 * @param atom
	 * @return
	 */
	private Set<Variable> getVariableReferencesWithLeftJoin(Function atom) {
		if (atom.isDataFunction()) {
			Set<Variable> variables = new LinkedHashSet<>();
			TermUtils.addReferencedVariablesTo(variables, atom);
			return variables;
		}
		if (atom.isOperation()) {
			return Collections.emptySet();
		}
		if (atom.isDataTypeFunction()) {
			return Collections.emptySet();
		}
		/*
		 * we have an algebra operator (join or left join) if its a join, we need
		 * to collect all the variables of each nested atom., if its a left
		 * join, only of the first data/algebra atom (the left atom).
		 */
		boolean isLeftJoin = atom.getFunctionSymbol() == DatalogAlgebraOperatorPredicates.SPARQL_LEFTJOIN;
		boolean foundFirstDataAtom = false;

		Set<Variable> innerVariables = new LinkedHashSet<>();
		for (Term t : atom.getTerms()) {
			if (isLeftJoin && foundFirstDataAtom) {
				break;
			}
			Function asFunction = (Function) t;
			if (asFunction.isOperation()) {
				continue;
			}
			innerVariables
					.addAll(getVariableReferencesWithLeftJoin(asFunction));
			foundFirstDataAtom = true;
		}
		return innerVariables;
	}

	/**
	 * Returns a list of equality conditions that reflect the semantics of the
	 * shared variables in the list of atoms.
	 * <p>
	 * The method assumes that no variables are shared across deeper levels of
	 * nesting (through Join or LeftJoin atoms), it will not call itself
	 * recursively. Nor across upper levels.
	 *
	 * <p>
	 * When generating equalities recursively, we will also generate a minimal
	 * number of equalities. E.g., if we have A(x), Join(R(x,y), Join(R(y,
	 * x),B(x))
	 *
	 */
	private Set<String> getConditionsSharedVariablesAndConstants(
			List<Function> atoms, QueryAliasIndex index, boolean processShared) {

		Set<Variable> currentLevelVariables = new LinkedHashSet<>();
		if (processShared) {
			for (Function atom : atoms) {
				currentLevelVariables
						.addAll(getVariableReferencesWithLeftJoin(atom));
			}
		}

		/*
		 * For each variable we collect all the columns that shold be equated
		 * (due to repeated positions of the variable). then we form atoms of
		 * the form "COL1 = COL2"
		 */
		Set<String> equalities = new LinkedHashSet<>();
		for (Variable var : currentLevelVariables) {
			Set<QualifiedAttributeID> references = index.getColumnReferences(var);
			if (references.size() >= 2) {
				// if 1, tnen no need for equality
				Iterator<QualifiedAttributeID> referenceIterator = references.iterator();
				QualifiedAttributeID leftColumnReference = referenceIterator.next();
				while (referenceIterator.hasNext()) {
					QualifiedAttributeID rightColumnReference = referenceIterator.next();
					String equality = String.format("(%s = %s)",
							leftColumnReference.getSQLRendering(),
							rightColumnReference.getSQLRendering());
					equalities.add(equality);
					leftColumnReference = rightColumnReference;
				}
			}
		}

		for (Function atom : atoms) {
			if (atom.isDataFunction())  {
				for (int idx = 0; idx < atom.getArity(); idx++) {
					Term l = atom.getTerm(idx);
					if (l instanceof Constant) {
						String value = getSQLString(l, index, false);
						String columnReference = index.getColumnReference(atom, idx);
						equalities.add(String.format("(%s = %s)", columnReference, value));
					}
				}
			}
		}
		return equalities;
	}

	// return variable SQL data type
	private int getVariableDataType(Term term) {

		if (term instanceof Function) {
			Function f = (Function) term;
			if (f.isDataTypeFunction()) {
				Predicate p = f.getFunctionSymbol();
				COL_TYPE type = TYPE_FACTORY.getDatatype(p.getName()).get();
				return jdbcTypeMapper.getSQLType(type);
			}
			// Return varchar for unknown
			return Types.VARCHAR;
		}
		else if (term instanceof Variable) {
			throw new RuntimeException("Cannot return the SQL type for: " + term);
		}
		/**
		 * Boolean constant
		 */
		else if (term.equals(TermConstants.FALSE)
				 || term.equals(TermConstants.TRUE)) {
			return Types.BOOLEAN;
		}

		return Types.VARCHAR;
	}

	private static final class SignatureVariable {
		private final String name;
		private final ImmutableList<String> columnAliases;
		private final COL_TYPE castType;
		SignatureVariable(String name, ImmutableList<String> columnAliases, COL_TYPE castType) {
			this.name = name;
			this.columnAliases = columnAliases;
			this.castType = castType;
		}
	}

	/**
	 * produces the select clause of the sql query for the given CQIE
	 *
	 * @return the sql select clause
	 */
	private String getSelectClauseFragment(SignatureVariable var,
										   Term term,
										   Optional<TermType> termType,
										   QueryAliasIndex index) {
		/*
		 * Datatype for the main column (to which it is cast).
		 * Beware, it may defer the RDF datatype (the one of the type column).
		 *
		 * Why? Because most DBs (if not all) require the result table to have
		 * one datatype per column. If the sub-queries are producing results of different types,
		 * them there will be a difference between the type in the main column and the RDF one.
		 */
		String typeColumn = getTypeColumnForSELECT(term, index, termType);
		String langColumn = getLangColumnForSELECT(term, index, termType);
		String mainColumn = getMainColumnForSELECT(term, index, var.castType);

		return new StringBuffer().append("\n   ")
				.append(typeColumn).append(" AS ").append(var.columnAliases.get(0)).append(", ")
				.append(langColumn).append(" AS ").append(var.columnAliases.get(1)).append(", ")
				.append(mainColumn).append(" AS ").append(var.columnAliases.get(2))
				.toString();
	}

	private ImmutableList<SignatureVariable> createSignature(List<String> names, ImmutableList<COL_TYPE> castTypes) {
		/**
		 * Set that contains all the variable names created on the top query.
		 * It helps the dialect adapter to generate variable names according to its possible restrictions.
		 * Currently, this is needed for the Oracle adapter (max. length of 30 characters).
		 */
		Set<String> sqlVariableNames = new HashSet<>();
		ImmutableList.Builder<SignatureVariable> builder = ImmutableList.builder();
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);

			// Creates name names that satisfy the restrictions of the SQL dialect.
			String typeAlias = sqladapter.nameTopVariable(name, TYPE_SUFFIX, sqlVariableNames);
			sqlVariableNames.add(typeAlias);

			String langAlias = sqladapter.nameTopVariable(name, LANG_SUFFIX, sqlVariableNames);
			sqlVariableNames.add(langAlias);

			String mainAlias = sqladapter.nameTopVariable(name, MAIN_COLUMN_SUFFIX, sqlVariableNames);
			sqlVariableNames.add(mainAlias);

			builder.add(new SignatureVariable(name,
					ImmutableList.of(typeAlias, langAlias, mainAlias),
					castTypes.get(i)));
		}
		return builder.build();
	}

	private String getMainColumnForSELECT(Term ht, QueryAliasIndex index, COL_TYPE castDataType) {

		String mainColumn;
		if (ht instanceof URIConstant) {
			URIConstant uc = (URIConstant) ht;
			mainColumn = sqladapter.getSQLLexicalFormString(uc.getURI());
		}
		else if (ht == TermConstants.NULL) {
			/**
			 * TODO: we should not have to treat NULL as a special case!
			 * It is because this constant is currently
			 * a STRING!
			 */
			mainColumn = "NULL";
		}
		else if (ht instanceof ValueConstant) {
			mainColumn = getSQLLexicalForm((ValueConstant) ht);
		}
		else if (ht instanceof Variable) {
			mainColumn = getSQLString(ht, index, false);
		}
		else if (ht instanceof Function) {
			/*
			 * if it's a function we need to get the nested value if its a
			 * datatype function or we need to do the CONCAT if its URI(....).
			 */
			Function ov = (Function) ht;
			Predicate functionSymbol = ov.getFunctionSymbol();

			/*
			 * Adding the column(s) with the actual value(s)
			 */
			if (ov.isDataTypeFunction()) {
				/*
				 * Case where we have a typing function in the head (this is the
				 * case for all literal columns
				 */
				int size = ov.getTerms().size();
				if ((functionSymbol instanceof Literal) || size > 2) {
					mainColumn = getSQLStringForTemplateFunction(ov, index);
				}
				else {
					Term term = ov.getTerms().get(0);
					if (term instanceof ValueConstant) {
						mainColumn = getSQLLexicalForm((ValueConstant) term);
					}
					else {
						mainColumn = getSQLString(term, index, false);
					}
				}
			}
			else if (functionSymbol instanceof URITemplatePredicate) {
				// New template based URI building functions
				mainColumn = getSQLStringForTemplateFunction(ov, index);
			}
            else if (functionSymbol instanceof BNodePredicate) {
				// New template based BNODE building functions
				mainColumn = getSQLStringForTemplateFunction(ov, index);
			}
			else if (ov.isOperation()) {
				mainColumn = getSQLString(ov, index, false);
			}
			else
				throw new IllegalArgumentException(
						"Error generating SQL query. Found an invalid function during translation: " + ov);
		}
		else
			throw new RuntimeException("Cannot generate SELECT for term: " + ht);

		/*
		 * If the we have a column we need to still CAST to VARCHAR
		 */
		if (mainColumn.charAt(0) != '\'' && mainColumn.charAt(0) != '(' && castDataType != null) {
			mainColumn = sqladapter.sqlCast(mainColumn, jdbcTypeMapper.getSQLType(castDataType));
		}

		return mainColumn;
	}

	private String getLangColumnForSELECT(Term ht, QueryAliasIndex index, Optional<TermType> optionalTermType) {

		if (ht instanceof Variable) {
			return index.getLangColumn((Variable) ht)
					.map(QualifiedAttributeID::getSQLRendering)
					.orElse("NULL");
		}
		else {
			return optionalTermType
					.filter(t -> t.getColType() == LANG_STRING)
					.map(t -> t.getLanguageTagConstant()
								.map(tag -> "'" + tag.getFullString() + "'")
								.orElseGet(() -> t.getLanguageTagTerm()
										.map(tag -> getSQLString(tag, index, false))
										.orElseThrow(() -> new IllegalStateException(
												"Inconsistent term type: the language tag must be defined for any LANG_STRING"))))
					.orElse("NULL");
		}
    }

	/**
	 * Infers the type of a projected term.
	 *
	 * Note this type may differ from the one used for casting the main column (in some special cases).
	 * This type will appear as the RDF datatype.
	 *
	 * @param ht
	 * @param index Used when the term correspond to a column name
	 * @param optionalTermType
	 */
	private String getTypeColumnForSELECT(Term ht, QueryAliasIndex index, Optional<TermType> optionalTermType) {

		if (ht instanceof Variable) {
	        // Such variable does not hold this information, so we have to look
	        // at the database metadata.
			return index.getTypeColumn((Variable) ht)
					.map(QualifiedAttributeID::getSQLRendering)
					// By default, we assume that the variable is an IRI.
					.orElseGet(() -> String.format("%d", OBJECT.getQuestCode()));
		}
		else {
			COL_TYPE colType = optionalTermType
					.map(TermType::getColType)
					// By default, we apply the "most" general COL_TYPE
					.orElse(STRING);

			return String.format("%d", colType.getQuestCode());
		}
	}



	private String getSQLStringForTemplateFunction(Function ov, QueryAliasIndex index) {
		/*
		 * The first inner term determines the form of the result
		 */
		List<Term> terms = ov.getTerms();
		Term t = terms.get(0);

		if (t instanceof ValueConstant || t instanceof BNode) {
			/*
			 * The function is actually a template. The first parameter is a
			 * string of the form http://.../.../ or empty "{}" with place holders of the form
			 * {}. The rest are variables or constants that should be put in
			 * place of the place holders. We need to tokenize and form the
			 * CONCAT
			 */
			final String literalValue;
			if (t instanceof BNode) {
				//TODO: why getValue and not getName(). Change coming from v1.
				literalValue = ((BNode) t).getName();
			}
			else {
				literalValue = ((ValueConstant) t).getValue();
			}

			String template = trimLiteral(literalValue);
			String[] split = template.split("[{][}]");

			List<String> vex = new ArrayList<>();
			if (split.length > 0 && !split[0].isEmpty()) {
				vex.add(sqladapter.getSQLLexicalFormString(split[0]));
			}

			/*
			 * New we concat the rest of the function, note that if there is
			 * only 1 element there is nothing to concatenate
			 */
			int size = terms.size();
			if (size > 1) {
//				if (TYPE_FACTORY.isLiteral(pred)) {
//					size--;
//				}
				for (int termIndex = 1; termIndex < size; termIndex++) {
					Term currentTerm = terms.get(termIndex);
					String repl0 = getSQLString(currentTerm, index, false);
					String repl1 = isStringColType(currentTerm, index)
							? repl0
							: sqladapter.sqlCast(repl0, Types.VARCHAR);
					//empty place holders: the correct uri is in the column of DB no need to replace
					String repl = (split.length == 0) ? repl1 : replace1 + repl1 + replace2;
					vex.add(repl);
					if (termIndex < split.length) {
						vex.add(sqladapter.getSQLLexicalFormString(split[termIndex]));
					}
				}
			}

			if (vex.size() == 1) {
				return vex.get(0);
			}
			return getStringConcatenation(vex.toArray(new String[0]));
		}
		else if (t instanceof Variable) {
			/*
			 * The function is of the form uri(x), we need to simply return the
			 * value of X
			 */
			return getSQLString(t, index, false);
		}
		else if (t instanceof URIConstant) {
			/*
			 * The function is of the form uri("http://some.uri/"), i.e., a
			 * concrete URI, we return the string representing that URI.
			 */
			URIConstant uc = (URIConstant) t;
			return sqladapter.getSQLLexicalFormString(uc.getURI());
		}
		/**
		 * Complex first argument: treats it as a string and ignore other arguments
		 */
		else {
			/*
			 * The function is for example of the form uri(CONCAT("string",x)),we simply return the value from the database.
			 */
			return getSQLString(t, index, false);
		}
	}

	// TODO: move to SQLAdapter
	private String getStringConcatenation(String[] params) {
		String toReturn = sqladapter.strConcat(params);
		if (sqladapter instanceof DB2SQLDialectAdapter) {
			/*
			 * A work around to handle DB2 (>9.1) issue SQL0134N: Improper use
			 * of a string column, host variable, constant, or function name.
			 * http
			 * ://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic
			 * =%2Fcom.ibm.db2.luw.messages.sql.doc%2Fdoc%2Fmsql00134n.html
			 */
			if (isDistinct || isOrderBy) {
				return sqladapter.sqlCast(toReturn, Types.VARCHAR);
			}
		}
		return toReturn;
	}

	private boolean isStringColType(Term term, QueryAliasIndex index) {
		if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			if (functionSymbol instanceof URITemplatePredicate) {
				/*
				 * A URI function always returns a string, thus it is a string
				 * column type.
				 */
				return !hasIRIDictionary();
			}
			else {
				if (functionSymbol.getArity() == 1) {
					if (functionSymbol.getName().equals("Count")) {
						return false;
					}
					/*
					 * Update the term with the parent term's first parameter.
					 * Note: this method is confusing :(
					 */
					term = function.getTerm(0);
					return isStringColType(term, index);
				}
			}
		}
		else if (term instanceof Variable) {
			Set<QualifiedAttributeID> attrs = index.getColumnReferences((Variable) term);
			QualifiedAttributeID attr0 = attrs.iterator().next();

			RelationID relationId = attr0.getRelation();
			QuotedID colId = attr0.getAttribute();

			// Non-final TODO: understand
			String table = relationId.getTableName();
			if (relationId.getTableName().startsWith("QVIEW")) {
				for (Entry<Function, DataDefinition> e : index.dataDefinitions.entrySet()) {
					RelationID knownViewId = e.getValue().alias;
					if (knownViewId.equals(relationId)) {
						table = e.getKey().getFunctionSymbol().toString();
						break;
					}
				}
			}
			for (DatabaseRelationDefinition tabledef :  metadata.getDatabaseRelations()) {
				if (tabledef.getID().getTableName().equals(table)) {
					List<Attribute> attr = tabledef.getAttributes();
					for (Attribute a : attr) {
						if (a.getID().equals(colId)) {
							switch (a.getType()) {
								case Types.VARCHAR:
								case Types.CHAR:
								case Types.LONGNVARCHAR:
								case Types.LONGVARCHAR:
								case Types.NVARCHAR:
								case Types.NCHAR:
									return true;
								default:
									return false;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean hasIRIDictionary() {
		return uriRefIds != null;
	}

	private static final Pattern pQuotes = Pattern.compile("[\"`\\['][^\\.]*[\"`\\]']");

	private static String trimLiteral(String string) {
		while (pQuotes.matcher(string).matches()) {
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	/**
	 * Generates the SQL string that forms or retrieves the given term. The
	 * function takes as input either: a constant (value or URI), a variable, or
	 * a Function (i.e., uri(), eq(..), ISNULL(..), etc)).
	 * <p>
	 * If the input is a constant, it will return the SQL that generates the
	 * string representing that constant.
	 * <p>
	 * If its a variable, it returns the column references to the position where
	 * the variable first appears.
	 * <p>
	 * If its a function uri(..) it returns the SQL string concatenation that
	 * builds the result of uri(...)
	 * <p>
	 * If its a boolean comparison, it returns the corresponding SQL comparison.
	 */
	private String getSQLString(Term term, QueryAliasIndex index, boolean useBrackets) {

		if (term == null) {
			return "";
		}
		if (term instanceof ValueConstant) {
			ValueConstant ct = (ValueConstant) term;
			if (hasIRIDictionary()) {
				if (ct.getType() == OBJECT || ct.getType() == STRING) {
					int id = getUriid(ct.getValue());
					if (id >= 0)
						//return jdbcutil.getSQLLexicalForm(String.valueOf(id));
						return String.valueOf(id);
				}
			}
			return getSQLLexicalForm(ct);
		}
		else if (term instanceof URIConstant) {
			if (hasIRIDictionary()) {
				String uri = term.toString();
				int id = getUriid(uri);
				return sqladapter.getSQLLexicalFormString(String.valueOf(id));
			}
			URIConstant uc = (URIConstant) term;
			return sqladapter.getSQLLexicalFormString(uc.toString());
		}
		else if (term instanceof Variable) {
			Variable var = (Variable) term;
			Set<QualifiedAttributeID> posList = index.getColumnReferences(var);
			return posList.iterator().next().getSQLRendering();
		}

		/* If its not constant, or variable its a function */

		Function function = (Function) term;
		Predicate functionSymbol = function.getFunctionSymbol();
		int size = function.getTerms().size();

		if (function.isDataTypeFunction()) {
			if (functionSymbol.getType(0) == UNSUPPORTED) {
				throw new RuntimeException("Unsupported type in the query: " + function);
			}
			if (size == 1) {
				// atoms of the form integer(x)
				Term term1 = function.getTerm(0);
				return getSQLString(term1, index, false);
			}
			else {
				return getSQLStringForTemplateFunction(function, index);
			}
		}
		else if (operations.containsKey(functionSymbol)) {
			// atoms of the form EQ(x,y)
			String expressionFormat = operations.get(functionSymbol);
			if (function.getArity() == 1) {
				Term term1 = function.getTerm(0);
				// for unary functions, e.g., NOT, IS NULL, IS NOT NULL
				// also added for IS TRUE
				if (functionSymbol == ExpressionOperation.IS_TRUE) {
					// find data type of term and evaluate accordingly
					String column = getSQLString(term1, index, false);
					int type = getVariableDataType(term1);
					if (type == Types.INTEGER || type == Types.BIGINT || type == Types.DOUBLE || type == Types.FLOAT)
						return String.format("%s > 0", column);
					else if (type == Types.BOOLEAN)
						return String.format("%s", column);
					else if (type == Types.VARCHAR)
						return String.format("LENGTH(%s) > 0", column);
					return "1";
				}
				String op = getSQLString(term1, index, true);
				return String.format(expressionFormat, op);
			}
			else if (function.getArity() == 2) {
				// for binary functions, e.g., AND, OR, EQ, NEQ, GT etc.
				String leftOp = getSQLString(function.getTerm(0), index, true);
				String rightOp = getSQLString(function.getTerm(1), index, true);
				String result = String.format(expressionFormat, leftOp, rightOp);
				if (useBrackets)
					return String.format("(%s)", result);
				else
					return result;
			}
			else if (function.getArity() == 0) {
				return expressionFormat;
			}
			else
				throw new RuntimeException("Cannot translate boolean function: " + functionSymbol);
		}
		else if (functionSymbol == ExpressionOperation.REGEX) {
			boolean caseinSensitive = false;
			boolean multiLine = false;
			boolean dotAllMode = false;
			if (function.getArity() == 3) {
				String options = function.getTerm(2).toString();
				caseinSensitive = options.contains("i");
				multiLine = options.contains("m");
				dotAllMode = options.contains("s");
			}
			Term p1 = function.getTerm(0);
			Term p2 = function.getTerm(1);

			String column = getSQLString(p1, index, false);
			String pattern = getSQLString(p2, index, false);
			return sqladapter.sqlRegex(column, pattern, caseinSensitive, multiLine, dotAllMode);
		}
		else if (functionSymbol == ExpressionOperation.SPARQL_LANG) {
			Variable var = (Variable) function.getTerm(0);
			Set<QualifiedAttributeID> posList = index.getColumnReferences(var);

			// TODO: fix the hack
			String langC = posList.iterator().next().getSQLRendering();
			String langColumn = langC.replaceAll("`$", "Lang`");
			return langColumn;

			/**
			 * TODO: replace by a switch
			 */
		}
		else {
			if (functionSymbol == ExpressionOperation.QUEST_CAST) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				String datatype = ((Constant) function.getTerm(1)).getValue();
				int sqlDatatype = datatype.equals(XMLSchema.STRING.stringValue())
						? Types.VARCHAR
						: Types.LONGVARCHAR;
				return isStringColType(function, index) ? columnName : sqladapter.sqlCast(columnName, sqlDatatype);
			}
			else if (functionSymbol == ExpressionOperation.SPARQL_STR) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				return isStringColType(function, index) ? columnName : sqladapter.sqlCast(columnName, Types.VARCHAR);
			}
			else if (functionSymbol == ExpressionOperation.REPLACE) {
                String orig = getSQLString(function.getTerm(0), index, false);
                String out_str = getSQLString(function.getTerm(1), index, false);
                String in_str = getSQLString(function.getTerm(2), index, false);
                String result = sqladapter.strReplace(orig, out_str, in_str);
				// TODO: handle flags
                return result;
            }
            else if (functionSymbol == ExpressionOperation.CONCAT) {
                String left = getSQLString(function.getTerm(0), index, false);
                String right = getSQLString(function.getTerm(1), index, false);
                return sqladapter.strConcat(new String[]{left, right});
            }
			else if (functionSymbol == ExpressionOperation.STRLEN) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.strLength(literal);
			}
			else if (functionSymbol == ExpressionOperation.YEAR) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.dateYear(literal);
			}
			else if (functionSymbol == ExpressionOperation.MINUTES) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.dateMinutes(literal);
			}
			else if (functionSymbol == ExpressionOperation.DAY) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.dateDay(literal);
			}
			else if (functionSymbol == ExpressionOperation.MONTH) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.dateMonth(literal);
			}
			else if (functionSymbol == ExpressionOperation.SECONDS) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.dateSeconds(literal);
			}
			else if (functionSymbol == ExpressionOperation.HOURS) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.dateHours(literal);
			}
			else if (functionSymbol == ExpressionOperation.TZ) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.dateTZ(literal);
			}
			else if (functionSymbol == ExpressionOperation.ENCODE_FOR_URI) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.strEncodeForUri(literal);
			}
			else if (functionSymbol == ExpressionOperation.UCASE) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.strUcase(literal);
			}
			else if (functionSymbol == ExpressionOperation.MD5) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.MD5(literal);
			}
			else if (functionSymbol == ExpressionOperation.SHA1) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.SHA1(literal);
			}
			else if (functionSymbol == ExpressionOperation.SHA256) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.SHA256(literal);
			}
			else if (functionSymbol == ExpressionOperation.SHA512) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.SHA512(literal); //TODO FIX
			}
			else if (functionSymbol == ExpressionOperation.LCASE) {
				String literal = getSQLString(function.getTerm(0), index, false);
				return sqladapter.strLcase(literal);
			}
			else if (functionSymbol == ExpressionOperation.SUBSTR2) {
				String string = getSQLString(function.getTerm(0), index, false);
				String start = getSQLString(function.getTerm(1), index, false);
				return sqladapter.strSubstr(string, start);
			}
			else if (functionSymbol == ExpressionOperation.SUBSTR3) {
				String string = getSQLString(function.getTerm(0), index, false);
				String start = getSQLString(function.getTerm(1), index, false);
				String end = getSQLString(function.getTerm(2), index, false);
				return sqladapter.strSubstr(string, start, end);
			}
			else if (functionSymbol == ExpressionOperation.STRBEFORE) {
				String string = getSQLString(function.getTerm(0), index, false);
				String before = getSQLString(function.getTerm(1), index, false);
				return sqladapter.strBefore(string, before);
			}
			else if (functionSymbol == ExpressionOperation.STRAFTER) {
				String string = getSQLString(function.getTerm(0), index, false);
				String after = getSQLString(function.getTerm(1), index, false);
				return sqladapter.strAfter(string, after);
			}
			else if (functionSymbol == ExpressionOperation.COUNT) {
				if (function.getTerm(0).toString().equals("*")) {
					return "COUNT(*)";
				}
				String columnName = getSQLString(function.getTerm(0), index, false);
				//havingCond = true;
				return "COUNT(" + columnName + ")";
			}
			else if (functionSymbol == ExpressionOperation.AVG) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				//havingCond = true;
				return "AVG(" + columnName + ")";
			}
			else if (functionSymbol == ExpressionOperation.SUM) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				//havingCond = true;
				return "SUM(" + columnName + ")";
			}
		}

		/*
		 * The atom must be of the form uri("...", x, y)
		 */
		if (functionSymbol instanceof URITemplatePredicate
				|| functionSymbol instanceof BNodePredicate) {
			return getSQLStringForTemplateFunction(function, index);
		}

		throw new RuntimeException("Unexpected function in the query: " + functionSymbol);
	}

	/***
	 * Returns the valid SQL lexical form of rdf literals based on the current
	 * database and the datatype specified in the function predicate.
	 *
	 * <p>
	 * For example, if the function is xsd:boolean, and the current database is
	 * H2, the SQL lexical form would be for "true" "TRUE" (or any combination
	 * of lower and upper case) or "1" is always
	 *
	 * @param constant
	 * @return
	 */
	private String getSQLLexicalForm(ValueConstant constant) {
		switch (constant.getType()) {
			case BNODE:
			case OBJECT:
			case STRING:
				return sqladapter.getSQLLexicalFormString(constant.getValue());
			case BOOLEAN:
				boolean v = XsdDatatypeConverter.parseXsdBoolean(constant.getValue());
				return sqladapter.getSQLLexicalFormBoolean(v);
			case DATETIME:
				return sqladapter.getSQLLexicalFormDatetime(constant.getValue());
			case DATETIME_STAMP:
				return sqladapter.getSQLLexicalFormDatetimeStamp(constant.getValue());
			case DECIMAL:
			case DOUBLE:
			case INTEGER:
			case LONG:
			case FLOAT:
			case NON_POSITIVE_INTEGER:
			case INT:
			case UNSIGNED_INT:
			case NEGATIVE_INTEGER:
			case POSITIVE_INTEGER:
			case NON_NEGATIVE_INTEGER:
				return constant.getValue();
			case NULL:
				return "NULL";
			default:
				return "'" + constant.getValue() + "'";
		}
	}

	/***
	 * We look for the ID in the list of IDs, if its not there, we return -2,
	 * which we know will never appear on the DB. This is correct because if a
	 * constant appears in a query, and that constant was never inserted in the
	 * DB, the query must be empty (that atom), by putting -2 as id, we will
	 * enforce that.
	 *
	 * @param uri
	 * @return
	 */
	private int getUriid(String uri) {
		Integer id = uriRefIds.getId(uri);
		if (id != null)
			return id;
		return -2;
	}

	private static final class DataDefinition {
		private final RelationDefinition def;
		private final RelationID alias;
		DataDefinition(RelationID alias, RelationDefinition def) {
			this.alias = alias;
			this.def = def;
		}
		@Override
		public String toString() {
			return alias + " " + def;
		}
	}

	/**
	 * Utility class to resolve "database" atoms to view definitions ready to be
	 * used in a FROM clause, and variables, to column references defined over
	 * the existing view definitions of a query.
	 */
	public final class QueryAliasIndex {

		final Map<Function, DataDefinition> dataDefinitions = new HashMap<>();
		final Map<RelationID, RelationDefinition> dataDefinitionsById = new HashMap<>();
		final Map<Variable, Set<QualifiedAttributeID>> columnReferences = new HashMap<>();

		public QueryAliasIndex(CQIE query, ImmutableMap<Predicate, ParserViewDefinition> subQueryDefinitions, Multimap<Predicate, CQIE> ruleIndex) {
			for (Function atom : query.getBody()) {
				/*
				 * This will be called recursively if necessary
				 */
				generateViewsIndexVariables(atom, subQueryDefinitions, ruleIndex);
			}
		}

		/***
		 * We assiciate each atom to a view definition. This will be
		 * <p>
		 * "tablename" as "viewX" or
		 * <p>
		 * (some nested sql view) as "viewX"
		 *
		 * <p>
		 * View definitions are only done for data atoms. Join/LeftJoin and
		 * boolean atoms are not associated to view definitions.
		 *
		 * @param atom
		 * @param subQueryDefinitions
		 */
		private void generateViewsIndexVariables(Function atom,
												 Map<Predicate, ParserViewDefinition> subQueryDefinitions,
												 Multimap<Predicate, CQIE> ruleIndex) {
			if (atom.isOperation()) {
				return;
			}
			else if (atom.isAlgebraFunction()) {
				for (Term subatom : atom.getTerms()) {
					if (subatom instanceof Function) {
						generateViewsIndexVariables((Function) subatom, subQueryDefinitions, ruleIndex);
					}
				}
			}

			Predicate predicate = atom.getFunctionSymbol();
			RelationID tableId = Relation2Predicate.createRelationFromPredicateName(metadata.getQuotedIDFactory(),
					predicate);
			RelationDefinition def = metadata.getRelation(tableId);

			final RelationID relationId, relationAlias;
			if (def == null) {
				/*
				 * There is no definition for this atom, its not a database
				 * predicate. Check if it is an ans predicate and has a view:
				 */
				def = subQueryDefinitions.get(predicate);
				if (def == null) {
					return; // empty
				}
				else {
					relationId = relationAlias = def.getID();
				}
			}
			else {
				relationAlias = createAlias(predicate.getName(),
						VIEW_SUFFIX + dataDefinitions.size(),
						dataDefinitions.entrySet().stream()
								.map(e -> e.getValue().alias).collect(Collectors.toList()));
				relationId = tableId;
			}
			dataDefinitions.put(atom, new DataDefinition(relationAlias, def));
			dataDefinitionsById.put(relationId, def);

			for (int index = 0; index < atom.getTerms().size(); index++) {
				Term term = atom.getTerms().get(index);

				if (term instanceof Variable) {

					Set<QualifiedAttributeID> references = columnReferences.get(term);
					if (references == null) {
						references = new LinkedHashSet<>();
						columnReferences.put((Variable) term, references);
					}

					/*
					 * the index of attributes of the definition starts from 1
					 */
					final Attribute column;
					if (ruleIndex.containsKey(atom.getFunctionSymbol())) {
						// If I am here it means that it is not a database table
						// but a view from an Ans predicate
						column = def.getAttribute(3 * (index + 1));
					}
					else {
						column = def.getAttribute(index + 1);
					}

					QualifiedAttributeID qualifiedId = new QualifiedAttributeID(relationAlias, column.getID());
					references.add(qualifiedId);
				}
			}
		}

		/***
		 * Returns all the column aliases that correspond to this variable,
		 * across all the DATA atoms in the query (not algebra operators or
		 * boolean conditions.
		 *
		 * @param var
		 *            The variable we want the referenced columns.
		 */
		public Set<QualifiedAttributeID> getColumnReferences(Variable var) {
			Set<QualifiedAttributeID> attrs = columnReferences.get(var);
			if (attrs == null || attrs.size() == 0)
				throw new RuntimeException("Unbound variable found in WHERE clause: " + var);
			return attrs;
		}

		/***
		 * Generates the view definition, i.e., "tablename viewname".
		 */
		public String getViewDefinition(Function atom) {

			DataDefinition dd = dataDefinitions.get(atom);
			if (dd != null) {
				if (dd.def instanceof DatabaseRelationDefinition) {
					return sqladapter.sqlTableName(dd.def.getID().getSQLRendering(),
							dd.alias.getSQLRendering());
				}
				else if (dd.def instanceof ParserViewDefinition) {
					return String.format("(%s) %s", ((ParserViewDefinition) dd.def).getStatement(),
							dd.alias.getSQLRendering());
				}
				throw new RuntimeException("Impossible to get data definition for: " + atom + ", type: " + dd);
			}
			else if (atom.getArity() == 0) {
				 // Special case of nullary atoms
				return "(" + sqladapter.getDummyTable() + ") tdummy";
			}
			throw new RuntimeException(
						"Impossible to get data definition for: " + atom + ", type: " + dd);
		}

		public String getColumnReference(Function atom, int column) {
			DataDefinition dd = dataDefinitions.get(atom);
			QuotedID columnname = dd.def.getAttribute(column + 1).getID(); // indexes from 1
			return new QualifiedAttributeID(dd.alias, columnname).getSQLRendering();
		}

		public Optional<QualifiedAttributeID> getTypeColumn(Variable var) {
			return getNonMainColumn(var, -2);
		}

		public Optional<QualifiedAttributeID> getLangColumn(Variable var) {
			return getNonMainColumn(var, -1);
		}

		private Optional<QualifiedAttributeID> getNonMainColumn(Variable var, int relativeIndexWrtMainColumn) {

			Set<QualifiedAttributeID> columnRefs = columnReferences.get(var);
			if (columnRefs == null || columnRefs.size() == 0) {
				throw new RuntimeException("Unbound variable found in WHERE clause: " + var);
			}

			/*
			 * For each column reference corresponding to the variable.
			 *
			 * For instance, columnRef is `Qans4View`.`v1` .
			 */
			for (QualifiedAttributeID mainColumn : columnRefs) {
				RelationID relationId = mainColumn.getRelation();

				/*
				 * If the var is defined in a ViewDefinition, then there is a
				 * column for the type and we just need to refer to that column.
				 *
				 * For instance, tableColumnType becomes `Qans4View`.`v1QuestType` .
				 */
				RelationDefinition def = dataDefinitionsById.get(relationId);

				if (def != null && (def instanceof ParserViewDefinition)) {
					ParserViewDefinition viewDefinition = (ParserViewDefinition)def;

					List<QualifiedAttributeID> columnIds = viewDefinition.getAttributes().stream()
							.map(Attribute::getQualifiedID)
							.collect(Collectors.toList());
					int mainColumnIndex = columnIds.indexOf(mainColumn) + 1;

					Attribute typeColumn = viewDefinition.getAttribute(mainColumnIndex + relativeIndexWrtMainColumn);
					return Optional.of(typeColumn.getQualifiedID());
				}
			}

			return Optional.empty();
		}

	}
}
