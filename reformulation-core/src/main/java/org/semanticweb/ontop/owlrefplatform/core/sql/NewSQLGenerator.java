package org.semanticweb.ontop.owlrefplatform.core.sql;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.ontop.mapping.QueryUtils;
import org.semanticweb.ontop.model.AlgebraOperatorPredicate;
import org.semanticweb.ontop.model.BNode;
import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.DataTypePredicate;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.NumericalOperationPredicate;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.URITemplatePredicate;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.OBDAQueryModifiers.OrderCondition;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.DatalogNormalizer;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.DB2SQLDialectAdapter;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.HSQLSQLDialectAdapter;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLAdapterFactory;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.DataDefinition;
import org.semanticweb.ontop.sql.TableDefinition;
import org.semanticweb.ontop.sql.ViewDefinition;
import org.semanticweb.ontop.sql.api.Attribute;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class NewSQLGenerator extends AbstractQueryGenerator implements NativeQueryGenerator {

	private static final String INDENT = "    ";
	
	static final ImmutableMap<Predicate, String> booleanPredicateToQueryString;
	static {
		Map<Predicate, String> temp = new HashMap<>();
		
		temp.put(OBDAVocabulary.EQ, "%s = %s");
		temp.put(OBDAVocabulary.NEQ, "%s <> %s");
		temp.put(OBDAVocabulary.GT, "%s > %s");
		temp.put(OBDAVocabulary.GTE, "%s >= %s");
		temp.put(OBDAVocabulary.LT, "%s < %s");
		temp.put(OBDAVocabulary.LTE, "%s <= %s");
		temp.put(OBDAVocabulary.AND, "%s AND %s");
		temp.put(OBDAVocabulary.OR, "%s OR %s");
		temp.put(OBDAVocabulary.NOT, "NOT %s");
		temp.put(OBDAVocabulary.IS_NULL, "%s IS NULL");
		temp.put(OBDAVocabulary.IS_NOT_NULL, "%s IS NOT NULL");
		temp.put(OBDAVocabulary.IS_TRUE, "%s IS TRUE");
		temp.put(OBDAVocabulary.SPARQL_LIKE, "%s LIKE %s");
		//we do not need the operator for regex, it should not be used, because the sql adapter will take care of this
		temp.put(OBDAVocabulary.SPARQL_REGEX, ""); 
				
		booleanPredicateToQueryString = ImmutableMap.copyOf(temp);
	}

	static final ImmutableMap<Predicate, String> arithmeticPredicateToQueryString;
	static {
		Map<Predicate, String> temp = new HashMap<>();
		
		temp.put(OBDAVocabulary.ADD, "%s + %s");
		temp.put(OBDAVocabulary.SUBTRACT, "%s - %s");
		temp.put(OBDAVocabulary.MULTIPLY, "%s * %s");
						
		arithmeticPredicateToQueryString = ImmutableMap.copyOf(temp);
	}

	static final ImmutableMap<String, Integer> predicateSQLTypes;
	static final int defaultSQLType = Types.VARCHAR;
	static {
		Map<String, Integer> temp = new HashMap<>();
		temp.put(OBDAVocabulary.XSD_INTEGER_URI, new Integer(Types.INTEGER));
		temp.put(OBDAVocabulary.XSD_INT_URI, new Integer(Types.INTEGER));
		temp.put(OBDAVocabulary.XSD_DOUBLE_URI, new Integer(Types.DOUBLE));
		temp.put(OBDAVocabulary.XSD_BOOLEAN_URI, new Integer(Types.BOOLEAN));
		temp.put(OBDAVocabulary.XSD_DECIMAL_URI, new Integer(Types.DECIMAL));
		temp.put(OBDAVocabulary.XSD_DATETIME_URI, new Integer(Types.DATE));		
		temp.put(OBDAVocabulary.XSD_STRING_URI, new Integer(Types.VARCHAR));
		temp.put(OBDAVocabulary.RDFS_LITERAL_URI, new Integer(Types.VARCHAR));		
		temp.put(OBDAVocabulary.SPARQL_AVG_URI, new Integer(Types.DECIMAL));
		temp.put(OBDAVocabulary.SPARQL_SUM_URI, new Integer(Types.DECIMAL));
		temp.put(OBDAVocabulary.SPARQL_COUNT_URI, new Integer(Types.DECIMAL));
		temp.put(OBDAVocabulary.SPARQL_MAX_URI, new Integer(Types.DECIMAL));
		temp.put(OBDAVocabulary.SPARQL_MIN_URI, new Integer(Types.DECIMAL));
		
		predicateSQLTypes = ImmutableMap.copyOf(temp);
	}
		

	private static final Table<Predicate, Predicate, Predicate> dataTypePredicateUnifyTable;
	static{
		dataTypePredicateUnifyTable = new ImmutableTable.Builder<Predicate, Predicate, Predicate>()
			.put(OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.XSD_DOUBLE)
			.put(OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.XSD_DECIMAL)
			.put(OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.XSD_INTEGER)
			.put(OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.OWL_REAL, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.XSD_DECIMAL)
			.put(OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.XSD_DOUBLE)
			.put(OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.OWL_REAL, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.XSD_DECIMAL)
			.put(OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.XSD_DOUBLE)
			.put(OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.XSD_DOUBLE)
			.put(OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.OWL_REAL, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.XSD_DOUBLE)
			.put(OBDAVocabulary.OWL_REAL, OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.OWL_REAL, OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.OWL_REAL, OBDAVocabulary.OWL_REAL, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.OWL_REAL, OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.SPARQL_COUNT, OBDAVocabulary.XSD_DECIMAL, OBDAVocabulary.XSD_DECIMAL)
			.put(OBDAVocabulary.SPARQL_COUNT, OBDAVocabulary.XSD_DOUBLE, OBDAVocabulary.XSD_DOUBLE)
			.put(OBDAVocabulary.SPARQL_COUNT, OBDAVocabulary.OWL_REAL, OBDAVocabulary.OWL_REAL)
			.put(OBDAVocabulary.SPARQL_COUNT, OBDAVocabulary.XSD_INTEGER, OBDAVocabulary.XSD_INTEGER)
			.build();
	}

	static final Map<String, Integer> predicateCodeTypes;
    private static final int UNDEFINED_TYPE_CODE = -1;
	static {
		Map<String, Integer> temp = new HashMap<>();
		temp.put(OBDAVocabulary.XSD_BOOLEAN_URI, new Integer(9));
		temp.put(OBDAVocabulary.XSD_DATETIME_URI, new Integer(8));		
		temp.put(OBDAVocabulary.XSD_STRING_URI, new Integer(7));
		temp.put(OBDAVocabulary.XSD_DOUBLE_URI, new Integer(6));
		temp.put(OBDAVocabulary.XSD_DECIMAL_URI, new Integer(5));
		temp.put(OBDAVocabulary.XSD_INTEGER_URI, new Integer(4));
		temp.put(OBDAVocabulary.RDFS_LITERAL_URI, new Integer(3));
		temp.put(OBDAVocabulary.QUEST_BNODE, new Integer(2));
     	temp.put(OBDAVocabulary.QUEST_URI, new Integer(1));
     	temp.put(OBDAVocabulary.NULL.toString(), new Integer(0));
		
		predicateCodeTypes = ImmutableMap.copyOf(temp);
	}
        

	/**
	 * Replace templates
	 */
	static final String REPLACE_START = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(";

	static final String REPLACE_END = ",' ', '%20')," + "'!', '%21')," + "'@', '%40'),"
			+ "'#', '%23')," + "'$', '%24')," + "'&', '%26'),"
			+ "'*', '%42'), " + "'(', '%28'), " + "')', '%29'), "
			+ "'[', '%5B'), " + "']', '%5D'), " + "',', '%2C'), "
			+ "';', '%3B'), " + "':', '%3A'), " + "'?', '%3F'), "
			+ "'=', '%3D'), " + "'+', '%2B'), " + "'''', '%22'), "
			+ "'/', '%2F')";

	/**
	 * Formatting template
	 */
    private static final String TYPE_STR = "%s AS \"%sQuestType\"" ;
	static final String VIEW_ANS_NAME = "Q%sView";

	private static final String QUEST_TYPE = "QuestType";

	
	//*********************\\
	// Instance attributes \\
	//*********************\\

	
	/**
	 * Query independent
	 */
	private final SQLDialectAdapter sqlAdapter;

	/**
	 * Query independent
	 * A flag whether to replace non-alpha characters in URIs with their unicodes
	 */
	private final boolean generateReplace;
    
	/**
	 * Query independent
	 * Flag for semantic indexing mode
	 */
	private final boolean isSemanticIndex;
	
	/**
	 * Query independent
	 *   used in the semantic indexing mode
	 *   maps URIs to their IDs
	 * TODO: make it immutable (to be done first in Quest.java)
	 */
	private final Map<String, Integer> uriRefIds;

    
    
	/*public NewSQLQueryGenerator(DBMetadata metadata, JDBCUtility jdbcutil, SQLDialectAdapter sqladapter, 
								boolean sqlGenerateReplace) {
		this(metadata, jdbcutil, sqladapter, sqlGenerateReplace, false, null);
	}

	public NewSQLQueryGenerator(DBMetadata metadata, JDBCUtility jdbcutil, SQLDialectAdapter sqladapter, 
								boolean sqlGenerateReplace, boolean isSemanticIndex, Map<String, Integer> uriRefIds) {
		super(metadata);
		
		this.jdbcUtil = jdbcutil;
		this.sqlAdapter = sqladapter;
		
		this.generateReplace = sqlGenerateReplace;
		
		this.isSemanticIndex = isSemanticIndex;
		if (uriRefIds != null) { 
			//TODO : this does not work!! seems that uriRedIds is modified later
			//this.uriRefIds = new HashMap<>(uriRefIds);
			this.uriRefIds = uriRefIds;
		}
		else
			this.uriRefIds = null;
	}*/
	
    @AssistedInject
	private NewSQLGenerator(@Assisted DBMetadata metadata, @Assisted OBDADataSource dataSource, QuestPreferences preferences) {
        // TODO: make these attributes immutable.
        // TODO: avoid the null value
        this(metadata, dataSource, null, preferences);
    }

    @AssistedInject
    private NewSQLGenerator(@Assisted DBMetadata metadata, @Assisted OBDADataSource dataSource,
                         @Assisted Map<String, Integer> uriRefIds, QuestPreferences preferences) {
    	super(metadata);
    	
        String driverURI = dataSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
        this.sqlAdapter = SQLAdapterFactory.getSQLDialectAdapter(driverURI, preferences);

        this.generateReplace = Boolean.valueOf((String) preferences.get(QuestPreferences.SQL_GENERATE_REPLACE));

        String aBoxMode = (String) preferences.get(QuestPreferences.ABOX_MODE);
        this.isSemanticIndex = aBoxMode.equals(QuestConstants.CLASSIC);
        this.uriRefIds = uriRefIds;
    }


	/**
	 * This implementation is query-independent, so no need to clone it
	 * Returns itself.
	 */
	@Override
	public NativeQueryGenerator cloneIfNecessary() {
		return this;
	}

	@Override
	public String getBooleanOperatorTemplate(Predicate booleanPredicate) {
		if (booleanPredicateToQueryString.containsKey(booleanPredicate)) {
			return booleanPredicateToQueryString.get(booleanPredicate);
		}

		throw new RuntimeException("Unknown boolean operator: " + booleanPredicate);
	}
	
	@Override
	public String getArithmeticOperatorTemplate(Predicate arithmeticPredicate) {
		if (arithmeticPredicateToQueryString.containsKey(arithmeticPredicate)) {
			return arithmeticPredicateToQueryString.get(arithmeticPredicate);
		}

		throw new RuntimeException("Unknown arithmetic operator: " + arithmeticPredicate);
	}

	/**
	 * TODO: should we rename it generateNativeQuery?
	 */
	@Override
	public String generateSourceQuery(DatalogProgram query, List<String> signature) throws OBDAException {
	
		DatalogProgram normalizedQuery = normalizeProgram(query);

		if(query.getQueryModifiers().hasModifiers()) {
			return generateQueryWithModifiers(normalizedQuery, signature);
		} else {		
			return generateQuery(normalizedQuery, signature);
		}
	}

	//TODO: should it be a method of DatalogProgram?
	private DatalogProgram normalizeProgram(DatalogProgram program) {
		Set<CQIE> normalizedRules = new HashSet<>();
		for (CQIE rule : program.getRules()) {
			normalizedRules.add(normalizeRule(rule));
		}
		//TODO: need a method that would return a new DatalogProgram from rules and modifiers
		//OBDADataFactoryImpl.getInstance().getDatalogProgram(normalizedRules);
		DatalogProgram normalizedProgram = program.clone();
		normalizedProgram.removeAllRules();
		normalizedProgram.appendRule(normalizedRules);
		return normalizedProgram;
	}

	private CQIE normalizeRule(CQIE rule) {
		CQIE normalizedRule = DatalogNormalizer.foldJoinTrees(rule, true);
		DatalogNormalizer.pullUpNestedReferences(normalizedRule, false);
		DatalogNormalizer.addMinimalEqualityToLeftJoin(normalizedRule);
		return normalizedRule;
	}


	/**
	 * Generates a query with modifiers by creating the modifiers string and 
	 * wrapping them around a query without modifiers.
	 * 
	 * Calls method {@link #generateQuery(DatalogProgram, List)}.
	 */
	private String generateQueryWithModifiers(DatalogProgram queryProgram, List<String> signature) {

		final String outerViewName = "SUB_QVIEW";

		List<OrderCondition> conditions = queryProgram.getQueryModifiers().getSortConditions();

		List<Variable> groupby = queryProgram.getQueryModifiers().getGroupConditions();
		// if (!groupby.isEmpty()) {
		// subquery += "\n" + sqladapter.sqlGroupBy(groupby, "") + " " +
		// havingStr + "\n";
		// }
		// List<OrderCondition> conditions =
		// query.getQueryModifiers().getSortConditions();


		String modifier = "";
		if (!conditions.isEmpty()) {
			modifier += sqlAdapter.sqlOrderBy(conditions, outerViewName)
					+ "\n";
		}
		long limit = queryProgram.getQueryModifiers().getLimit();
		long offset = queryProgram.getQueryModifiers().getOffset();
		if (limit != -1 || offset != -1) {
			modifier += sqlAdapter.sqlSlice(limit, offset) + "\n";
		}

		String subquery = generateQuery(queryProgram, signature);

		String sql = "SELECT *\n";
		sql += "FROM (\n";
		sql += subquery + "\n";
		sql += ") " + outerViewName + "\n";
		sql += modifier;
		return sql;
	}

	/**
	 * Generates a UNION of SELECT PROJECT JOIN queries (with no modifier). 
	 * 
	 * This method is used by createQueryInfo, when creating Ans views 
	 * 
	 */
	private String generateQuery(DatalogProgram query, List<String> signature) {

		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(query);
		Multimap<Predicate, CQIE> ruleIndex = depGraph.getRuleIndex();
		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();

		// creates and stores information about the query: isDistinct, isOrderBy, 
		// creates view definitions for ans predicates other than ans1
		QueryInfo queryInfo = createQueryInfo(query, depGraph); 

		// This should be ans1, and the rules defining it.
		int size = predicatesInBottomUp.size();
		Predicate predAns1 = predicatesInBottomUp.get(size-1);
		
		Collection<CQIE> ansRules = ruleIndex.get(predAns1);
		List<Predicate> headDataTypes = detectHeadDataTypes(ansRules);
		boolean isAns1 = true;
		
		List<String> queryStrings = Lists.newArrayListWithCapacity(ansRules.size());
		for (CQIE cq : ansRules) {
			/* Main loop, constructing the SPJ query for each CQ */

			String querystr = generateQueryFromSingleRule(cq, signature, isAns1, headDataTypes, queryInfo);
			queryStrings.add(querystr);
		}

		return createUnionFromSQLList(queryStrings, queryInfo.isDistinct());
	}

	/**
	 * Creates query meta information, such as
	 *  - isDistinct
	 *  - isOrderBy
	 *  - view definitions of ans predicates other than ans1
	 * @param query
	 * @param depGraph
	 * @return
	 */
	private QueryInfo createQueryInfo(DatalogProgram query, DatalogDependencyGraphGenerator depGraph) {
		
		QueryInfo queryInfo = new QueryInfo(query);
		return createAnsViews(depGraph, query, queryInfo);
	}

	private String createUnionFromSQLList(List<String> queryStrings, boolean isDistinct) {
		String UNION = null;
		if (isDistinct) {
			UNION = "UNION";
		} else {
			UNION = "UNION ALL";
		}
		
		StringBuilder result = new StringBuilder();
		result.append("(");
		Joiner.on(")\n " + UNION + " \n (").appendTo(result, queryStrings);
		result.append(")");

		return result.toString();
	}

	/**
	 * Creates a view for every ans predicate in the Datalog input program,
	 * except for ans1. 
	 * 
	 * It does so in bottom-up fashion since the views for higher level 
	 * predicates need to use the view definitions of the lower level predicates.
	 */
	private QueryInfo createAnsViews(DatalogDependencyGraphGenerator depGraph, DatalogProgram query,
									QueryInfo queryInfo) {

		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();

		List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();

		Iterator<Predicate> iterator = predicatesInBottomUp.iterator();
		
		QueryInfo currentQueryInfo = queryInfo;
		
		int numPreds = predicatesInBottomUp.size();
		int i = 0;
		while (i < numPreds - 1) {
			Predicate pred = iterator.next();
			if (extensionalPredicates.contains(pred)) {
				/*
				 * extensional predicates are defined by DBs
				 */
			} else {
				boolean isAns1 = false;
				currentQueryInfo = createView(pred, depGraph.getRuleIndex().get(pred), isAns1, currentQueryInfo);
			}
			i++;
		}
		
		return currentQueryInfo;
	}

	/**
	 * Creates a view for the predicate pred from the rule list, and 
	 * returns a new queryInfo augmented with the new view definition
	 * 
	 * @param pred
	 * @param ruleList
	 * @param isAns1
	 * @param queryInfo
	 * 		the previously created queryInfo, that is, it contains the view definitions for the 
	 * 		lower level predicates
	 * @return
	 */
	private QueryInfo createView(Predicate pred, Collection<CQIE> ruleList, boolean isAns1, QueryInfo queryInfo) {
		/* Creates BODY of the view query */

		List<Predicate> headDataTypes = detectHeadDataTypes(ruleList);
		int headArity = 0;
		
		List<String> sqls = Lists.newArrayListWithExpectedSize(ruleList.size());
		for (CQIE rule : ruleList) {
			Function cqHead = rule.getHead();

			// FIXME: the arity of the predicate might be wrong, should be fixed
			// in the unfolder
			// headArity = cqHead.getArity();
			headArity = cqHead.getTerms().size();

			List<String> varContainer = QueryUtils.getVariableNamesInAtom(cqHead);

			/* Creates the SQL for the View */
			String sqlQuery = generateQueryFromSingleRule(rule, varContainer, isAns1, headDataTypes, queryInfo);
			sqls.add(sqlQuery);
		}


		String viewName = createAnsViewName(pred.getName());
		String unionView = createUnionFromSQLList(sqls, false);

		// Hard coded variable names
		List<String> columns = Lists.newArrayListWithExpectedSize(3 * headArity);
		for (int i = 0; i < headArity; i++) {
			columns.add("v" + i + QUEST_TYPE);
			columns.add("v" + i + "lang");
			columns.add("v" + i);
		}

		/* Creates the View itself */
		ViewDefinition viewDefinition = metadata.createViewDefinition(viewName, unionView, columns);
		QueryInfo newQueryInfo = QueryInfo.addAnsViewDefinition(queryInfo, viewDefinition);
		return newQueryInfo;
	}

	public static String createAnsViewName(String tableName) {
		return String.format(VIEW_ANS_NAME, tableName);
	}

	/**
	 * Generates a SELECT FROM WHERE query from a conjunctive query.
	 * 
	 * Creates a query variable index in order to know the table alias of each atom and
	 * the column references of each variable. 
	 * 
	 * @param cq
	 * @param signature
	 * @param isAns1
	 * @param headDataTypes
	 * @param queryInfo
	 * @return
	 */
	private String generateQueryFromSingleRule(CQIE cq, List<String> signature,
			boolean isAns1, List<Predicate> headDataTypes, QueryInfo queryInfo) {
		SQLAliasVariableIndex index = new SQLAliasVariableIndex(cq, metadata, queryInfo, sqlAdapter);

		String SELECT = generateSelectClause(cq, index, isAns1, signature, headDataTypes);
		String FROM = generateFROM(cq.getBody(), index);
		String WHERE = generateWHERE(cq.getBody(), index);
		String GROUP = generateGroupBy(cq.getBody(), index);
		String HAVING = generateHaving(cq.getBody(), index);
		
		String querystr = SELECT + FROM + WHERE + GROUP + HAVING;
		return querystr;
	}

	private String generateGroupBy(List<Function> body, SQLAliasVariableIndex index) {

		List<Variable> varsInGroupBy = Lists.newArrayList();
		for (Function atom : body) {
			if (atom.getFunctionSymbol().equals(OBDAVocabulary.SPARQL_GROUP)) {
				varsInGroupBy.addAll(QueryUtils.getVariablesInAtom(atom));
			}
		}

		List<String> groupReferences = Lists.newArrayList();
		for (Variable var : varsInGroupBy) {
			Collection<String> references = index.getColumnReferences(var);
			groupReferences.addAll(references);
		}

		StringBuilder result = new StringBuilder();
		if (!groupReferences.isEmpty()) {
			result.append(" GROUP BY ");
			Joiner.on(" , ").appendTo(result, groupReferences);
		}

		return result.toString();
	}

	private String generateHaving(List<Function> body, QueryVariableIndex index) {
		List <Term> conditions = new ArrayList<> ();
		List <Function> condFunctions = new ArrayList<> ();

		for (Function atom : body) {
			if (atom.getFunctionSymbol().equals(OBDAVocabulary.SPARQL_HAVING)) {
				conditions = atom.getTerms();
				break;
			}
		}
		if (conditions.isEmpty()) {
			return "";
		}

		for(Term cond : conditions){
			condFunctions.add((Function) cond);
		}
		
		Set<String> condSet = getConditionsString(condFunctions, index);
		StringBuilder result = new StringBuilder();
		result.append(" HAVING ( ").append(Joiner.on("").join(condSet)).append(" ) ");
		return result.toString();
	}
	
	private String generateSelectClause(CQIE query, SQLAliasVariableIndex index,
			boolean isAns1, List<String> signature,
			List<Predicate> headDataTypes) {
		StringBuilder selectString = new StringBuilder("SELECT ");
		if (index.isDistinct()) {
			selectString.append("DISTINCT ");
		}
		
		/*
		 * If the head has size 0 this is a boolean query.
		 */
		List<Term> headterms = query.getHead().getTerms();
		if (headterms.size() == 0) {
			selectString.append("'true' as x");
			return selectString.toString();
		}

		/*
		 * We have a non-boolean query.
		 */
		Iterator<Term> headTermIter = headterms.iterator();
		Iterator<Predicate> headDataTypeIter = headDataTypes.iterator();

		int headPos = 0;
		while (headTermIter.hasNext()) {
			Term headTerm = headTermIter.next();
			Predicate headDataType = headDataTypeIter.next();
			
			String varName = createVariableName(headPos, isAns1, signature);

			String typeColumn = generateTypeColumnForSELECT(headTerm, varName, index);
			String mainColumn = generateMainColumnForSELECT(headTerm, varName, index, headDataType);
			String langColumn = generateLangColumnForSELECT(headTerm, varName, index);

			selectString.append("\n   ");
			selectString.append(typeColumn).append(", ");
			selectString.append(langColumn).append(", ");
			selectString.append(mainColumn);
			if (headTermIter.hasNext()) {
				selectString.append(", ");
			}

			headPos++;
		}
		return selectString.toString();
	}

	/**
	 * Returns the variable name. 
	 * When isAns1 is true, we need to name it according to the signature.
	 */
    private String createVariableName(int headPos, boolean isAns1, List<String> signature) {
    	if (isAns1) {
    		return signature.get(headPos);
		} else {
			return "v" + headPos;
		}	
    }

	/**
     * Infers the type of a projected term.
     *
     * @param projectedTerm
     * @param varName Name of the variable
     * @param index Used when the term correspond to a column name
     * @return A string like "5 AS ageQuestType"
     */
	private String generateTypeColumnForSELECT(Term projectedTerm, String varName,
			SQLAliasVariableIndex index) {

		if (projectedTerm instanceof Function) {
			return getCompositeTermType((Function) projectedTerm, varName);
		}
        else if (projectedTerm instanceof URIConstant) {
			return String.format(TYPE_STR, predicateCodeTypes.get(OBDAVocabulary.QUEST_URI), varName);
		}
        else if (projectedTerm == OBDAVocabulary.NULL) {
			return String.format(TYPE_STR, predicateCodeTypes.get(OBDAVocabulary.NULL.toString()), varName);
		}
        else if (projectedTerm instanceof Variable) {
			return getTypeFromVariable((Variable) projectedTerm, index, varName);
		}

        // Unusual term
		throw new RuntimeException("Cannot generate SELECT for term: "
				+ projectedTerm.toString());
	}
	
    /**
     * Gets the type expression for a composite term.
     *
     * There is two common form of composite terms considered here:
     *   1. Typed variable. For instance, "http://...#decimal(cost)"
     *         should return something like "5 AS costQuestType"
     *   2. Aggregation. For instance, "SUM(http://...#decimal(cost))"
     *         should return something like "5 AS totalCostQuestType"
     *
     *   Basically, it tries to infer the type by looking at function symbols.
     *
     */
    private static String getCompositeTermType(Function compositeTerm, String varName) {
        
        int typeCode = UNDEFINED_TYPE_CODE;

        Predicate mainFunctionSymbol = compositeTerm.getFunctionSymbol();
        switch(mainFunctionSymbol.getName()) {
            /**
             * Aggregation cases
             */
            case OBDAVocabulary.SPARQL_COUNT_URI:
                typeCode = getCodeTypeFromFunctionSymbol(OBDAVocabulary.XSD_INTEGER);
                break;

            case OBDAVocabulary.SPARQL_SUM_URI:
            case OBDAVocabulary.SPARQL_AVG_URI:
            case OBDAVocabulary.SPARQL_MIN_URI:
            case OBDAVocabulary.SPARQL_MAX_URI:

                // We look at the sub-term
                Term subTerm = compositeTerm.getTerm(0);
                if (subTerm instanceof Function) {
                    Function compositeSubTerm = (Function) subTerm;
                    typeCode = getCodeTypeFromFunctionSymbol(compositeSubTerm.getFunctionSymbol());
                }

                /**
                 * Sometimes we cannot infer the type by looking at the term.
                 *
                 * In such a case, we cast the aggregate to a xsd:double
                 * (any number can be promoted to a double http://www.w3.org/TR/xpath20/#promotion) .
                 */
                if (typeCode == UNDEFINED_TYPE_CODE) {
                    typeCode = getCodeTypeFromFunctionSymbol(OBDAVocabulary.XSD_DOUBLE);
                }
                break;

            /**
             * Not a (known) aggregation function symbol
             */
            default:
                typeCode = getCodeTypeFromFunctionSymbol(mainFunctionSymbol);
                if (typeCode == UNDEFINED_TYPE_CODE) {
                    throw new RuntimeException("Cannot generate the SQL query " +
                            "because of an untyped term: " + compositeTerm.toString());
                }
        }

        return String.format(TYPE_STR, typeCode, varName);
    }

    /**
     * Converts a function symbol into a code type (integer).
     *
     * May return an UNDEFINED_TYPE_CODE value.
     */
    private static int getCodeTypeFromFunctionSymbol(Predicate predicate) {
    	String predName = predicate.getName();
        return predicateCodeTypes.containsKey(predName) ? predicateCodeTypes.get(predName) : UNDEFINED_TYPE_CODE;
    }

    /**
     * Gets the type of a variable.
     *
     * Such variable does not hold this information, so we have to look
     * at the database metadata.
     *
     */
    private String getTypeFromVariable(Variable var, SQLAliasVariableIndex index, String varName) {
        Collection<String> columnRefs = index.getColumnReferences(var);

        /**
         * By default, we assume that the variable is a URI.
         *
         * TODO: why?
         */
        String typeCode = predicateCodeTypes.get(OBDAVocabulary.QUEST_URI).toString();

        /**
         * For each column reference corresponding to the variable.
         *
         * For instance, columnRef is `Qans4View`.`v1` .
         */
        for (String columnRef : columnRefs) {
        	String[] tableAndColumn = getTableAndColumn(columnRef);
            String quotedTable = getQuotedTable(columnRef);
            
            DataDefinition definition = findDataDefinition(tableAndColumn[0], index);
            /**
             * If the var is defined in a ViewDefinition, then there is a
             * column for the type and we just need to refer to that column.
             *
             * For instance, tableColumnType becomes `Qans4View`.`v1QuestType` .
             */
            if (definition instanceof ViewDefinition) {
            	String columnType = tableAndColumn[1] + QUEST_TYPE;
            	String tableColumnType = sqlAdapter.sqlQualifiedColumn(quotedTable, columnType);
                typeCode = tableColumnType;
                break;
            }
        }

        return String.format(TYPE_STR, typeCode, varName);
    }

	private static String getQuotedTable(String columnRef) {
		String[] splits = columnRef.split("\\.");
		return splits[0];
	}

	private static String[] getTableAndColumn(String columnRef) {
		String[] splits = columnRef.split("\\.");
		splits[0] = removeQuotes(splits[0]);
		splits[1] = removeQuotes(splits[1]);
		return splits;
	}

	private DataDefinition findDataDefinition(String table, SQLAliasVariableIndex index) {
		DataDefinition definition = metadata.getDefinition(table);
		if (definition == null) {
			definition = index.getViewDefinition(table);
		}
		return definition;
	}

	private String findTableName(String table, SQLAliasVariableIndex index) {		
		if (table.startsWith("QVIEW")) {
			return index.findTableName(table);
		}		
		return table;
	}

	private String generateMainColumnForSELECT(Term headTerm, String varName, 
			SQLAliasVariableIndex index, Predicate typePredicate) {

		String mainColumn = null;
		String mainTemplate = "%s AS %s";

		if (headTerm == OBDAVocabulary.NULL) {
			mainColumn = "NULL";
		} 
		else if (headTerm instanceof URIConstant || headTerm instanceof Variable) {
			mainColumn = getNativeString(headTerm, index);
		}
		else if (headTerm instanceof Function) {
			/*
			 * if it's a function, we need to get the nested value if its a
			 * datatype function (this is the case for all literal columns), or 
			 * we need to do the CONCAT if its URI(....).
			 */
			Function atom = (Function) headTerm;
	
			// Similar to what is done in #getConditionString()
			if (atom.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI) ||
				atom.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_BNODE)) {
				mainColumn = getTemplateAsString(atom, index);			
			} 			
			else if (atom.getFunctionSymbol() instanceof DataTypePredicate) {
				mainColumn = getDataTypeConditionString(atom, index);
			}			
			else if (atom.getFunctionSymbol().isAggregationPredicate()) {
				mainColumn = getAggregateString(atom, index);
			}			
			else {
				throw new IllegalArgumentException(
						"Error generating SQL query. Found an invalid function during translation: "
								+ atom.toString());
			}
		} else {
			throw new RuntimeException("Cannot generate SELECT for term: "
					+ headTerm.toString());
		}

		/*
		 * If we have a column we need to still CAST to VARCHAR
		 */
		if (mainColumn.charAt(0) != '\'' && mainColumn.charAt(0) != '(') {
			if (typePredicate != null){
				
				int sqlType = getPredicateNativeType(typePredicate);				
				mainColumn = sqlAdapter.sqlCast(mainColumn, sqlType);
			}			
		}
				
		String format = String.format(mainTemplate, mainColumn, sqlAdapter.sqlQuote(varName));
		return format;
	}

    /**
    * Adding the ColType column to the projection (used in the result
    * set to know the type of constant)
    */
	private String generateLangColumnForSELECT(Term headTerm, String varName,
			QueryVariableIndex index) {

		String langStr = "%s AS \"%sLang\"";

		if (headTerm instanceof Function) {
			Function atom = (Function) headTerm;
			Predicate function = atom.getFunctionSymbol();

			if (function.equals(OBDAVocabulary.RDFS_LITERAL) || 
				function.equals(OBDAVocabulary.RDFS_LITERAL_LANG)) {
				if (atom.getTerms().size() > 1) {
					/*
					 * Case for rdf:literal s with a language, we need to select
					 * 2 terms from ".., rdf:literal(?x,"en"),
					 * 
					 * and signature "name" * we will generate a select with the
					 * projection of 2 columns
					 * 
					 * , 'en' as nameqlang, view.colforx as name,
					 */
					String lang = null;
					int last = atom.getTerms().size() - 1;
					Term langTerm = atom.getTerms().get(last);
					if (langTerm == OBDAVocabulary.NULL) {

						if (sqlAdapter instanceof HSQLSQLDialectAdapter) {
							lang = "CAST(NULL AS VARCHAR(3))";
						} else {
							lang = "NULL";
						}

					} else {
						lang = getNativeString(langTerm, index);
					}
					return (String.format(langStr, lang, varName));
				}
			}
		}


		//TODO get rid of this
		if (sqlAdapter instanceof HSQLSQLDialectAdapter) {
			return (String.format(langStr, "CAST(NULL AS VARCHAR(3))", varName));
		} 
		return (String.format(langStr,  "NULL", varName));
	}

	
	private String generateFROM(List<Function> atoms, SQLAliasVariableIndex index) {
		String tableDefinitions = generateTableDefinitionsForFROM(atoms, index, true, false, "");
		return "\n FROM \n" + tableDefinitions;
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
	 * @param inneratoms
	 * @param index
	 * @param isTopLevel
	 *            indicates if the list of atoms is actually the main body of
	 *            the conjunctive query. If it is, no JOIN is generated, but a
	 *            cross product with WHERE clause. Moreover, the isLeftJoin
	 *            argument will be ignored.
	 * 
	 * @return
	 */
	private String generateTableDefinitionsForFROM(List<Function> inneratoms,
			SQLAliasVariableIndex index, boolean isTopLevel, boolean isLeftJoin,
			String indent) {
		String indent2 = indent + INDENT;

		/*
		 * We now collect the view definitions for each data atom each
		 * condition, and each each nested Join/LeftJoin
		 */
		List<String> tableDefinitions = new ArrayList<>();
		for (Term innerAtom : inneratoms) {
			String definition = generateTableDefinition((Function) innerAtom, index, indent2);
			if (!definition.isEmpty()) {
				tableDefinitions.add(definition);
			}
		}

		/*
		 * Now we generate the table definition, this will be either a comma
		 * separated list for TOP level (FROM clause) or a Join/LeftJoin
		 * (possibly nested if there are more than 2 table definitions in the
		 * current list) in case this method was called recursively.
		 */
		StringBuilder tableDefinitionsString = new StringBuilder();

		if (isTopLevel) {
			if (tableDefinitions.isEmpty()) {
				tableDefinitionsString.append("(" + jdbcUtil.getDummyTable() + ") tdummy ");
			} else {
				tableDefinitionsString.append(indent);
				Joiner.on(",\n" + indent).appendTo(tableDefinitionsString, tableDefinitions);
			}
		} else {
			/*
			 * This is actually a Join or LeftJoin, so we form the JOINs/LEFT
			 * JOINs and the ON clauses
			 */
			String joinClause = formJoinOnClauses(inneratoms, index, isLeftJoin, indent, tableDefinitions);
			tableDefinitionsString.append(joinClause);
		}
		return tableDefinitionsString.toString();
	}
	
	String formJoinOnClauses(List<Function> inneratoms,
			SQLAliasVariableIndex index, boolean isLeftJoin, String indent,
			List<String> tableDefinitions) {
		
		int size = tableDefinitions.size();
		if (size == 0) {
			throw new RuntimeException("Cannot generate definition for empty data");
		}
		if (size == 1) {
			return tableDefinitions.get(0);
		}

		String JOIN_KEYWORD = null;
		if (isLeftJoin) {
			JOIN_KEYWORD = "LEFT OUTER JOIN";
		} else {
			JOIN_KEYWORD = "JOIN";
		}
		
//		String JOIN = "\n" + indent + "(\n" + indent + "%s\n" + indent
//				+ JOIN_KEYWORD + "\n" + indent + "%s\n" + indent + ")";

		String JOIN = indent + indent + "%s\n" + indent
				+ JOIN_KEYWORD + "\n" + indent + "%s" + indent + "";
		
		
		/*
		 * To form the JOIN we will cycle through each data definition,
		 * nesting the JOINs as we go. The conditions in the ON clause will
		 * go on the TOP level only.
		 */
		String currentJoin = String.format(JOIN, tableDefinitions.get(size - 2), tableDefinitions.get(size - 1));
		tableDefinitions.remove(size - 1);
		tableDefinitions.remove(size - 2);

		int currentSize = tableDefinitions.size();
		while (currentSize > 0) {
			currentJoin = String.format(JOIN, tableDefinitions.get(currentSize - 1), currentJoin);
			tableDefinitions.remove(currentSize - 1);
			currentSize = tableDefinitions.size();
		}
		tableDefinitions.add(currentJoin);

		/*
		 * If there are ON conditions we add them now. We need to remove the
		 * last parenthesis ')' and replace it with ' ON %s)' where %s are
		 * all the conditions
		 */
		Set<String> conditions = getConditionsString(inneratoms, index);
		String ON_CLAUSE = String.format(" ON\n%s\n " + indent, conjoinConditions(conditions));

		StringBuilder joinString = new StringBuilder();
		joinString.append(currentJoin);
		joinString.append(ON_CLAUSE);
		return joinString.toString();
	}

	private String conjoinConditions(Collection<String> conditions) {
		return Joiner.on(" AND\n").join(conditions);
	}

	/**
	 * Returns the table definition for the given atom. If the atom is a simple
	 * table or view, then it returns the value as defined by the
	 * SQLAliasVariableIndex. If the atom is a Join or Left Join, it will call
	 * getTableDefinitions on the nested term list.
	 */
	private String generateTableDefinition(Function atom,
			SQLAliasVariableIndex index, String indent) {
		Predicate predicate = atom.getFunctionSymbol();
		if (predicate instanceof BooleanOperationPredicate
				|| predicate instanceof NumericalOperationPredicate
				|| predicate instanceof DataTypePredicate) {
			// These don't participate in the FROM clause
			return "";
		} 
		
		else if (predicate instanceof AlgebraOperatorPredicate) {
			if (predicate.equals(OBDAVocabulary.SPARQL_GROUP)) {
				return "";
			}

			List<Function> innerTerms = new ArrayList<Function>();
			for (Term innerTerm : atom.getTerms()) {
				innerTerms.add((Function) innerTerm);
			}
			
			if (predicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
				return generateTableDefinitionsForFROM(innerTerms, index, false, false, indent + INDENT);
			} 
			else if (predicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
				return generateTableDefinitionsForFROM(innerTerms, index, false, true, indent + INDENT);
			}
		}

		/*
		 * This is a data atom
		 */
		String definition = index.getViewDefinition(atom);
		return definition;
	}

	private String generateWHERE(List<Function> atoms, QueryVariableIndex index) {
		Set<String> conditions = getConditionsString(atoms, index);
		if (conditions.isEmpty()) {
			return "";
		}
		
		return "\nWHERE \n" + conjoinConditions(conditions);
	}

	private List<Predicate> detectHeadDataTypes(Collection<CQIE> rules) {
		int ansArity = rules.iterator().next().getHead().getTerms().size();

		List<Predicate> ansTypes = Lists.newArrayListWithCapacity(ansArity);
		for (int k = 0; k < ansArity; k++) {
			ansTypes.add(null);
		}

		for (CQIE rule : rules) {
			Function head = rule.getHead();
			List<Term> terms = head.getTerms();
			for (int j = 0; j < terms.size(); j++) {
				Term term = terms.get(j);
				detectHeadTermDataType(term, ansTypes, j);
			}
		}
		return ansTypes;
	}

	/**
	 * Detects the type a head term by looking at the term: whether it is a
	 * function, or a variable, or a constant.
	 * 
	 * Integrates this information as the j-th element of the list ansTypes
	 * (that is, if the new type is more precise/general, it stores the new type,
	 * otherwise it keeps the old type).
	 * 
	 * @param term
	 * @param ansTypes
	 * @param j
	 */
    private void detectHeadTermDataType(Term term, List<Predicate> ansTypes, int j) {
    	if (term instanceof Function) {
			Function atom = (Function) term;
			Predicate typePred = atom.getFunctionSymbol();

			if (typePred.getName().equals(OBDAVocabulary.QUEST_BNODE)) {
				ansTypes.set(j, OBDAVocabulary.XSD_STRING);
			} 
			else if (typePred.getName().equals(OBDAVocabulary.QUEST_URI)) {
				Predicate unifiedType = unifyTypes(ansTypes.get(j), typePred);
				ansTypes.set(j, unifiedType);
			}
			else if (typePred.isDataTypePredicate()) {
				Predicate unifiedType = unifyTypes(ansTypes.get(j), typePred);
				ansTypes.set(j, unifiedType);
			}
			// Aggregate predicates different from COUNT
			else if (typePred.isAggregationPredicate() && !typePred.equals(OBDAVocabulary.SPARQL_COUNT)) {

				Term agTerm = atom.getTerm(0);
				if (agTerm instanceof Function) {
					Predicate termTypePred = ((Function) agTerm).getFunctionSymbol();
					Predicate unifiedType = unifyTypes(ansTypes.get(j), termTypePred);
					ansTypes.set(j, unifiedType);
				} else {
					Predicate unifiedType = unifyTypes(ansTypes.get(j), OBDAVocabulary.XSD_DECIMAL);
					ansTypes.set(j, unifiedType);
				}
			} 
			else 
				throw new IllegalArgumentException();
	
		} else if (term instanceof Variable) {
			// FIXME: properly handle the types by checking the metadata
			ansTypes.set(j, OBDAVocabulary.XSD_STRING);
		} else if (term instanceof BNode) {
			ansTypes.set(j, OBDAVocabulary.XSD_STRING);
		} else if (term instanceof URIConstant) {
			ansTypes.set(j, OBDAVocabulary.XSD_STRING);
		} else if (term instanceof ValueConstant) {
			COL_TYPE type = ((ValueConstant) term).getType();
			Predicate typePredicate = OBDADataFactoryImpl.getInstance().getTypePredicate(type);
			Predicate unifiedType = unifyTypes(ansTypes.get(j), typePredicate);
			ansTypes.set(j, unifiedType);
		} 
	}

	/**
	 * Unifies the input types
	 * 
	 * For instance,
	 * 
	 * [int, double] -> double
	 * [int, varchar] -> varchar
	 * [int, int] -> int
	 * 
	 * @param predicate
	 * @param typePred
	 * @return
	 */
	private Predicate unifyTypes(Predicate type1, Predicate type2) {

		if (type1 == null) {
			return type2;
		} else if (type1.equals(type2)) {
			return type1;
		} else if (dataTypePredicateUnifyTable.contains(type1, type2)) {
			return dataTypePredicateUnifyTable.get(type1, type2);
		} else if (type2 == null) {
			throw new NullPointerException("type2 cannot be null");
		} else {
			return OBDAVocabulary.XSD_STRING;
		}
	}
	
    
    
	@Override
	protected String getAlgebraConditionString(Function atom, QueryVariableIndex index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getDataTypeConditionString(Function atom, QueryVariableIndex index) {
		if (! atom.isDataTypeFunction() ) {
			throw new RuntimeException("Invoked for non-datatype function " + atom.getFunctionSymbol() + "!");
		}
		
		Term term1 = atom.getTerm(0);
		//TODO: is the following correct?
		//What about the type, actually?
		return getNativeString(term1, index);
	}

	
	@Override
	protected String getBooleanConditionString(Function atom, QueryVariableIndex index) {
		if (!atom.isBooleanFunction()) {
			throw new RuntimeException("Invoked for non-Boolean function " + atom.getFunctionSymbol() + "!");
		}

		if (atom.getFunctionSymbol().equals(OBDAVocabulary.SPARQL_REGEX)) {
			return getRegularExpressionString(atom, index);
		}
		else if (atom.getArity() == 1) {
			return getUnaryBooleanConditionString(atom, index);
		} 
		else if (atom.getArity() == 2) {
			return getBinaryBooleanConditionString(atom, index);
		}
		else
			throw new RuntimeException("Cannot translate boolean function: " + atom);

	}

	/**
	 * Returns string representation for (boolean) SPARQL_REGEX atoms
	 * 
	 * @param atom
	 * @param index
	 * @return
	 */
	private String getRegularExpressionString(Function atom, QueryVariableIndex index) {
		boolean caseinSensitive = false;
		boolean multiLine = false;
		boolean dotAllMode = false;
		if (atom.getArity() == 3) {
			if (atom.getTerm(2).toString().contains("i")) {
				caseinSensitive = true;
			}
			if (atom.getTerm(2).toString().contains("m")) {
				multiLine = true;
			}
			if (atom.getTerm(2).toString().contains("s")) {
				dotAllMode = true;
			}
		}
		
		String column = getNativeString(atom.getTerm(0), index);
		String pattern = getNativeString(atom.getTerm(1), index);
		return sqlAdapter.sqlRegex(column, pattern, caseinSensitive, multiLine, dotAllMode);
	}

	/**
	 * Creates a native string for binary boolean functions, such as AND, OR,
	 * EQ, NEQ, GT, etc.
	 * 
	 * @param atom
	 * @param index
	 * @return
	 */
	private String getBinaryBooleanConditionString(Function atom, QueryVariableIndex index) {
		Predicate booleanPredicate = atom.getFunctionSymbol();
		String expressionFormat = getBooleanOperatorTemplate(booleanPredicate);

		String leftOp = getNativeString(atom.getTerm(0), index);
		String rightOp = getNativeString(atom.getTerm(1), index);

		String result = String.format(expressionFormat, leftOp, rightOp);
		return String.format("(%s)", result);
	}

	/**
	 * Creates a native string for unary boolean functions, such as 
	 * IS NULL, IS NOT NULL, NOT, IS TRUE, etc.
	 * 
	 * @param atom
	 * @param index
	 * @return
	 */
	private String getUnaryBooleanConditionString(Function atom, QueryVariableIndex index) {
		Predicate booleanPredicate = atom.getFunctionSymbol();
		Term term1 = atom.getTerm(0);

		String expressionFormat = getBooleanOperatorTemplate(booleanPredicate);
		if (expressionFormat.contains("IS TRUE")) {
			return getIsTrueString(term1, index);
		}
		else if (expressionFormat.contains("NOT %s")) {
			// If term1 is a Function that is not a data type function, 
			// we proceed as with other cases. Otherwise, we call function getNotString()
			if(! (term1 instanceof Function && !((Function)term1).isDataTypeFunction()) )
				return getNotString(term1, index);
		}
		
		String op = getNativeString(term1, index);
		return String.format(expressionFormat, op);
	}

	private String getNotString(Term term1, QueryVariableIndex index) {
		String column = getNativeString(term1, index);

		// find data type of term and evaluate accordingly
		int type = detectVariableDataType(term1);
		if (type == Types.INTEGER)
			return String.format("NOT %s > 0", column);
		if (type == Types.DOUBLE)
			return String.format("NOT %s > 0", column);
		if (type == Types.BOOLEAN)
			return String.format("NOT %s", column);
		if (type == Types.VARCHAR)
			return String.format("NOT LENGTH(%s) > 0", column);
		return "0";
	}

	private String getIsTrueString(Term term1, QueryVariableIndex index) {
		String column = getNativeString(term1, index);
		int type = detectVariableDataType(term1);

		// find data type of term and evaluate accordingly
		if (type == Types.INTEGER)
			return String.format("%s > 0", column);
		if (type == Types.DOUBLE)
			return String.format("%s > 0", column);
		if (type == Types.BOOLEAN)
			return String.format("%s", column);
		if (type == Types.VARCHAR)
			return String.format("LENGTH(%s) > 0", column);
		return "1";
	}

	
	private int detectVariableDataType(Term term) {

		if (term instanceof Variable){
			throw new RuntimeException("Cannot return the SQL type for: " + term);
		}
		else if (term instanceof Function){
			Function atom = (Function) term;
			if (atom.isDataTypeFunction()) {
				return getPredicateNativeType(atom.getFunctionSymbol());
			}
		}  
		
		// Return varchar for unknown
		return defaultSQLType;
	}

	//TODO: find a proper place for this method 
	private int getPredicateNativeType(Predicate p) {
		return predicateSQLTypes.containsKey(p.getName()) ? predicateSQLTypes.get(p.getName()).intValue() : defaultSQLType;
	}

	
	@Override
	protected String getNativeLexicalForm(URIConstant uc) {
		if (isSemanticIndex) {
			String uri = uc.toString();
			int id = findUriID(uri);
			return jdbcUtil.getSQLLexicalForm(String.valueOf(id));
		}
		
		return jdbcUtil.getSQLLexicalForm(uc.getURI());
	}

	@Override
	protected String getNativeLexicalForm(ValueConstant ct) {
		if (isSemanticIndex) {
			if (ct.getType() == COL_TYPE.OBJECT || ct.getType() == COL_TYPE.LITERAL) {
				int id = findUriID(ct.getValue());
				if (id >= 0) {
					//return jdbcutil.getSQLLexicalForm(String.valueOf(id));
					return String.valueOf(id);
				}
				else {
					return jdbcUtil.getSQLLexicalForm(ct);
				}
			}
		}
		
		return jdbcUtil.getSQLLexicalForm(ct);
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
	private int findUriID(String uri) {

		Integer id = uriRefIds.get(uri);
		if (id != null)
			return id;
		return -2;
	}

	
	@Override
	/**
	 * Functions	 
	 */
	protected String getArithmeticConditionString(Function atom, QueryVariableIndex index) {
		String expressionFormat = getArithmeticOperatorTemplate(atom.getFunctionSymbol());

		String bracketsTemplate = "(%s)";
		String leftOp = String.format(bracketsTemplate, getNativeString(atom.getTerm(0), index));
		String rightOp = String.format(bracketsTemplate, getNativeString(atom.getTerm(1), index));
		String result = String.format(expressionFormat, leftOp, rightOp);
	
		return String.format(bracketsTemplate, result);
	}

	@Override
	protected String getAggregateConditionString(Function atom, QueryVariableIndex index) {
		Predicate predicate = atom.getFunctionSymbol();
		
		if (predicate.equals(OBDAVocabulary.SPARQL_COUNT) || 
				predicate.equals(OBDAVocabulary.SPARQL_AVG) || 
				predicate.equals(OBDAVocabulary.SPARQL_SUM)) {
			
			return getAggregateString(atom, index);
		} 

		throw new RuntimeException("Unexpected function in the query: " + atom);
	}

	/**
	 * Converts all aggregates COUNT, SUM, AVG, MIN, MAX to string.
	 * 
	 * Is used both for the SELECT, and for the WHERE clauses. 
	 * 
	 * @param atom
	 * @param index
	 * @return
	 */
	private String getAggregateString(Function atom, QueryVariableIndex index) {
		String columnName; 
		if (atom.getTerm(0).toString().equals("*")) {
			columnName = "*";
		}
		else {
			columnName = getNativeString(atom.getTerm(0), index);
		}
		
		return atom.getFunctionSymbol().getName().toUpperCase() + "(" + columnName + ")";	
	}
	

	static String removeAllQuotes(String string) {
		while (string.startsWith("\"") && string.endsWith("\"")) {
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	private static String removeQuotes(String string) {
		if ( (string.startsWith("\"") && string.endsWith("\"")) ||
			(string.startsWith("\'") && string.endsWith("\'")) ||
			(string.startsWith("`") && string.endsWith("`")) ) {
			return string.substring(1, string.length() - 1);
		}
		return string;
	}

	@Override
	protected String getLanguageConditionString(Function atom, QueryVariableIndex index) {
		Variable var = (Variable) atom.getTerm(0);
		String langC = getColumnName(var, index);
		
		String langColumn = langC.replaceAll("`$", "Lang`");
		return langColumn;
	}

	@Override
	protected String getCastConditionString(Function atom, QueryVariableIndex index) {
		String columnName = getNativeString(atom.getTerm(0), index);
		
		if (isStringColType(atom, (SQLAliasVariableIndex) index)) {
			return columnName;
		} 
		
		else {
			String datatype = ((Constant) atom.getTerm(1)).getValue();
			
			int sqlDatatype = -1;
			if (datatype.equals(OBDAVocabulary.XSD_STRING_URI)) {
				sqlDatatype = Types.VARCHAR;
			}
			return sqlAdapter.sqlCast(columnName, sqlDatatype);
		}
	}

	@Override
	protected String getSTRConditionString(Function atom, QueryVariableIndex index) {
		String columnName = getNativeString(atom.getTerm(0), index);
		if (isStringColType(atom, (SQLAliasVariableIndex) index)) {
			return columnName;
		} else {
			return sqlAdapter.sqlCast(columnName, Types.VARCHAR);
		}
	}

	@Override
	/**
	 * This method is used to obtain string representation of QUEST_URI and QUEST_BNODE predicates.
	 *  
	 * @param atom
	 * @param index
	 * @return
	 */
	protected String getTemplateAsString(Function atom, QueryVariableIndex index) {
		/*
		 * The first inner term determines the form of the result
		 */
		Term term1 = atom.getTerm(0);

		if (term1 instanceof ValueConstant || term1 instanceof BNode) {
			/*
			 * The atom is actually a template. The first parameter is a
			 * string of the form http://.../.../ or empty "{}" with place
			 * holders of the form {}. The rest are variables or constants that
			 * should be put in place of the place holders. We need to tokenize
			 * and form the CONCAT
			 */
			return concatenateFromTemplate(atom, (SQLAliasVariableIndex) index);
			
		} else if (term1 instanceof Variable) {
			/*
			 * The atom is of the form uri(x), we need to simply return the value of X
			 */
			return getNativeString(term1, index);

		} else if (term1 instanceof URIConstant) {
			/*
			 * The atom is of the form uri("http://some.uri/"), i.e., a
			 * concrete URI, we return the string representing that URI.
			 */
			return getNativeString(term1, index);
		}

		/*
		 * Unsupported case
		 */
		throw new IllegalArgumentException(
				"Error, cannot generate URI constructor clause for a term: "
						+ atom.toString());

	}

	/**
	 * Concatenates the atom which is a template. The first term of the atom is
	 * a string of the form http://.../.../ or empty "{}" with place holders of
	 * the form {}. The rest are variables or constants that should be put in
	 * place of the place holders.
	 * 
	 * The predicate of the atom is QUEST_URI or QUEST_BNODE, and 
	 * the first term is a ValueConstant or a BNode.
	 * 
	 */
	private String concatenateFromTemplate(Function atom, SQLAliasVariableIndex index) {
		// We already know that term1 is either a BNode or a ValueConstant
		Term term1 = atom.getTerm(0);
		
		String literalValue = "";
		if (term1 instanceof BNode) {
            literalValue = ((BNode) term1).getValue();
		} else {
            literalValue = ((ValueConstant) term1).getValue();
		}
		
		String replace1 = "", replace2 = "";
        if(generateReplace) {
            replace1 = REPLACE_START;
			replace2 = REPLACE_END;
        } 

		/*
		 * New we actually concatenate the rest of the atom, that is, 
		 * terms starting from the second one. 
		 */
        List<String> vex = concatenateTailOfTemplate(atom, index, literalValue, replace1, replace2);
		if (vex.size() == 1) {
			return vex.get(0);
		}
		

		String[] params = new String[vex.size()];
		params = vex.toArray(params);
		
		boolean isDistinctOrOrderBy = index.isDistinct() || index.isOrderBy();
		return NewSQLGenerator.getStringConcatenation(sqlAdapter, params, isDistinctOrOrderBy);
	}
	
	/**
	 * Concatenates the terms of atom starting from the second term.
	 * 
	 * Note that if atom has only 1 element, there is nothing to concatenate.
	 */
	private List<String> concatenateTailOfTemplate(Function atom,
			SQLAliasVariableIndex index, String literalValue, String replace1,
			String replace2) {
        String template = removeAllQuotes(literalValue);
		String[] split = template.split("[{][}]");

		List<String> vex = new ArrayList<String>();
		if (split.length > 0 && !split[0].isEmpty()) {
			vex.add(jdbcUtil.getSQLLexicalForm(split[0]));
		}

		int size = atom.getTerms().size();
		if (size > 1) {
			if (atom.getFunctionSymbol().equals(OBDAVocabulary.RDFS_LITERAL) ||
				atom.getFunctionSymbol().equals(OBDAVocabulary.RDFS_LITERAL_LANG)) {
				size--;
			}
			for (int termIndex = 1; termIndex < size; termIndex++) {
				Term currentTerm = atom.getTerm(termIndex);
				String repl = "";
				if (isStringColType(currentTerm, index)) {
					repl = replace1
							+ getNativeString(currentTerm, index)
							+ replace2;
				} else {
					repl = replace1
							+ sqlAdapter.sqlCast(getNativeString(currentTerm, index), Types.VARCHAR) 
							+ replace2;
				}
				vex.add(repl);
				if (termIndex < split.length) {
					vex.add(jdbcUtil.getSQLLexicalForm(split[termIndex]));
				}
			}
		}

		return vex;
	}

	private boolean isStringColType(Term term, SQLAliasVariableIndex index) {
		/*
		 * term is a Function
		 */
		if (term instanceof Function) {
			Function function = (Function) term;
			Predicate predicate = function.getFunctionSymbol();
			
			if (predicate instanceof URITemplatePredicate) {
				/*
				 * A URI function always returns a string, thus it is a string
				 * column type.
				 */
				if (isSemanticIndex)
					return false;
				return true;
			}
			
			else {
				if (function.getTerms().size() == 1) {
					if (predicate.equals(OBDAVocabulary.SPARQL_COUNT)) {
						return false;
					}
					/*
					 * Update the term with the parent term's first parameter.
					 * Note: this method is confusing :(
					 */
					Term term1 = function.getTerm(0);
					return isStringColType(term1, index);
				}
			}
		}
		/*
		 * term is a Variable
		 */
		else if (term instanceof Variable) {
			Collection<String> columnRefs = index.getColumnReferences((Variable) term);
			
			//TODO check if there is next
			String columnRef = columnRefs.iterator().next();
			
			String[] tableAndColumn = getTableAndColumn(columnRef);
			String table = findTableName(tableAndColumn[0], index);

			return isStringColType(table, tableAndColumn[1]);
		}
		return false;
	}

	private boolean isStringColType(String table, String col) {
		List<TableDefinition> tables = metadata.getTableList();
		for (TableDefinition tabledef : tables) {
			
			if (tabledef.getName().equals(table)) {
				List<Attribute> attr = tabledef.getAttributes();
				for (Attribute a : attr) {
			
					if (a.getName().equals(col)) {
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
		return false;
	}

	/**
	 * 
	 * @param adapter
	 * @param params
	 * @param isDistinctOrOrderBy
	 * 		needed to handle DB2 issue SQL0134N
	 * @return
	 */
	private static String getStringConcatenation(SQLDialectAdapter adapter, String[] params, 
												boolean isDistinctOrOrderBy) {
		String toReturn = adapter.strconcat(params);
		if (adapter instanceof DB2SQLDialectAdapter) {
			/*
			 * A work around to handle DB2 (>9.1) issue SQL0134N: Improper use
			 * of a string column, host variable, constant, or function name.
			 * http
			 * ://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic
			 * =%2Fcom.ibm.db2.luw.messages.sql.doc%2Fdoc%2Fmsql00134n.html
			 */
			if (isDistinctOrOrderBy) {
				return adapter.sqlCast(toReturn, Types.VARCHAR);
			}
		}
		return toReturn;
	}


}
