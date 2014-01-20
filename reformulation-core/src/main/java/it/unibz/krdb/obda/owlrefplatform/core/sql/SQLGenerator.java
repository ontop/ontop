package it.unibz.krdb.obda.owlrefplatform.core.sql;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;
import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NumericalOperationPredicate;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.DB2SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.obda.utils.DatalogDependencyGraphGenerator;
import it.unibz.krdb.obda.utils.QueryUtils;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.ViewDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openrdf.model.Literal;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * This class generates a SQL string from the datalog program coming from the unfolder.
 * 
 * @author mrezk, mariano, guohui
 *
 */
public class SQLGenerator implements SQLQueryGenerator {

	private static final long serialVersionUID = 7477161929752147045L;

	/**
	 * Operator symbols
	 */
	private static final String EQ_OPERATOR = "%s = %s";
	private static final String NEQ_OPERATOR = "%s <> %s";
	private static final String GT_OPERATOR = "%s > %s";
	private static final String GTE_OPERATOR = "%s >= %s";
	private static final String LT_OPERATOR = "%s < %s";
	private static final String LTE_OPERATOR = "%s <= %s";
	private static final String AND_OPERATOR = "%s AND %s";
	private static final String OR_OPERATOR = "%s OR %s";
	private static final String NOT_OPERATOR = "NOT %s";
	private static final String IS_NULL_OPERATOR = "%s IS NULL";
	private static final String IS_NOT_NULL_OPERATOR = "%s IS NOT NULL";

	private static final String ADD_OPERATOR = "%s + %s";
	private static final String SUBSTRACT_OPERATOR = "%s - %s";
	private static final String MULTIPLY_OPERATOR = "%s * %s";

	private static final String INDENT = "    ";

	private static final String IS_TRUE_OPERATOR = "%s IS TRUE";
	
	/**
	 * Formatting template
	 */
	private static final String VIEW_NAME = "Q%sVIEW%s";
	private static final String VIEW_ANS_NAME = "Q%sView";

	private final DBMetadata metadata;
	private final JDBCUtility jdbcutil;
	private final SQLDialectAdapter sqladapter;

	private boolean isDistinct = false;
	private boolean isOrderBy = false;
	private boolean isSI = false;
	
	
	private Map<String, Integer> uriRefIds;
	private Multimap<Predicate, CQIE> ruleIndex ;
//	private List<Function> ansViews = new LinkedList<Function>(); 
	/*
	 * The map of predicates to the canonical atoms.
	 * 
	 * In the canoical atoms, all the variables are in a function of URI template or datatype
	 * 
	 * For instance : ans5 -> ans5(rdf:Literal(name), URI(":{}", id), xsd:integer(salary))
	 */
	private Map<Predicate, Term> predicateCanonicalAtoms;
	
	private Map<Predicate,String> sqlAnsViewMap;
	
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(SQLGenerator.class);

	

	/**
	 * This method is in charge of generating the SQL query from a Datalog program
	 * 
	 * @param metadata
	 * 		This is an instance of {@link #DBMetadata}
	 * @param jdbcutil
	 * 		This is the set of useful tools to created JDBC compliant queries. It depends on the DB driver, for instance:
	 * com.mysql.jdbc.Driver. {@see #JDBCUtility}
	 * @param sqladapter
	 * This contains the syntax that each DB uses.  {@see #SQLDialectAdapter}
	 */
	public SQLGenerator(DBMetadata metadata, JDBCUtility jdbcutil, SQLDialectAdapter sqladapter) {
		this.metadata = metadata;
		this.jdbcutil = jdbcutil;
		this.sqladapter = sqladapter;
	}
	
	@Override
	public void setUriIds (Map<String,Integer> uriid){
		this.isSI = true;
		this.uriRefIds = uriid;
	}

	/**
	 * Generates and SQL query ready to be executed by Quest. Each query is a
	 * SELECT FROM WHERE query. To know more about each of these see the inner
	 * method descriptions.
	 * Observe that the SQL itself will be done by {@link #generateQuery(DatalogProgram, List, String, Map, List, Set)}
	 * 
	 * @param queryProgram
	 * 		This is a arbitrary Datalog Program. In this program ans predicates will be translated to Views.
	 * @param signature
	 * 		The Select variables in the SPARQL query
	 */
	@Override
	public String generateSourceQuery(DatalogProgram queryProgram, List<String> signature) throws OBDAException {
		
		/*
		 * normalize the program before generating the SQL
		 */
		//queryProgram = DatalogNormalizer.normalizeDatalogProgram(queryProgram);

		generatePredicateCanonicalAtomsMap(queryProgram);
		
		normalizeProgram(queryProgram);
		
		
		
		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(queryProgram);

		sqlAnsViewMap = new HashMap<Predicate, String>();
		
		ruleIndex = depGraph.getRuleIndex();
		
	

		
		Multimap<Predicate, CQIE> ruleIndexByBodyPredicate = depGraph.getRuleIndexByBodyPredicate();
		
		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();		
		
		List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();
		
		
		
		isDistinct = hasSelectDistinctStatement(queryProgram);
		isOrderBy = hasOrderByClause(queryProgram);
		if (queryProgram.getQueryModifiers().hasModifiers()) {
			final String indent = "   ";
			final String outerViewName = "SUB_QVIEW";
			String subquery = generateQuery(queryProgram, signature, indent, ruleIndex, 
					ruleIndexByBodyPredicate, predicatesInBottomUp, extensionalPredicates);

			String modifier = "";
			List<OrderCondition> conditions = queryProgram.getQueryModifiers().getSortConditions();
			if (!conditions.isEmpty()) {
				modifier += sqladapter.sqlOrderBy(conditions, outerViewName) + "\n";
			}
			long limit = queryProgram.getQueryModifiers().getLimit();
			long offset = queryProgram.getQueryModifiers().getOffset();
			if (limit != -1 || offset != -1) {
				modifier += sqladapter.sqlSlice(limit, offset) + "\n";
			}
			String sql = "SELECT *\n";
			sql += "FROM (\n";
			sql += subquery + "\n";
			sql += ") " + outerViewName + "\n";
			sql += modifier;
			return sql;
		} else {
			return generateQuery(queryProgram, signature, "", ruleIndex,  ruleIndexByBodyPredicate, predicatesInBottomUp, extensionalPredicates);
		}
	}
	
	/**
	 * 
	 * 
	 * Generates the map of predicates to their canonical atoms.
	 * 
	 * In the canonical atoms, all the variables are in a function of URI template or datatype
	 * 
	 * For instance : ans5 -> ans5(rdf:Literal(name), URI(":{}", id), xsd:integer(salary))
	 *
	 * 
	 * @param queryProgram
	 */
	private void generatePredicateCanonicalAtomsMap(DatalogProgram queryProgram) {
		
		predicateCanonicalAtoms = Maps.newHashMap();
		
		OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
		
		Set<Predicate> finishedPredicates = Sets.newHashSet();
		
		for(CQIE rule : queryProgram.getRules() ){
			Queue<Term> queue = new LinkedList<Term>(rule.getBody());
			
			while (!queue.isEmpty()) {
				Term qHead = queue.poll();
				if (qHead instanceof Function) {
					Function func = (Function) qHead;

					if (func.isBooleanFunction() || func.isArithmeticFunction()
							|| func.isDataTypeFunction()
							|| func.isAlgebraFunction()) {
						queue.addAll(func.getTerms());
					} else if (func.isDataFunction()) {
						
						Predicate predicate = func.getFunctionSymbol();
						
						if(finishedPredicates.contains(predicate))
							continue;
						
						List<CQIE> dependents = queryProgram.getRules(predicate);
						
						if(dependents.isEmpty()){
							/*
							 * don't care extentional predicates 
							 */
							continue;
						}
						
						List<Term> canonicalTerms = Lists.newArrayList();
						int arity = predicate.getArity();
						for(int i = 0; i < arity; i++){
							Term term = func.getTerm(i);
							
							if(isTyped(term)){
								canonicalTerms.add(term);
								continue;
							}
							
							for(CQIE r : dependents){
								term = r.getHead().getTerm(i);
								if(isTyped(term)){
									canonicalTerms.add(term);
									break;
								}
							}
							
						}
						
						/*
						 * TODO: put meaningful variable names
						 */
						
						predicateCanonicalAtoms.put(predicate, factory.getFunction(predicate, canonicalTerms));
						
						finishedPredicates.add(predicate);
						
					}
				} else /* !(queueHead instanceof Function) */{
					// NO-OP
				}
			}

			
		}
		
	}

	private boolean isTyped(Term term) {
		return !(term instanceof Variable) && !(term instanceof ValueConstant);
	}

	private boolean hasSelectDistinctStatement(DatalogProgram query) {
		boolean toReturn = false;
		if (query.getQueryModifiers().hasModifiers()) {
			toReturn = query.getQueryModifiers().isDistinct();
		}
		return toReturn;
	}
	
	private boolean hasOrderByClause(DatalogProgram query) {
		boolean toReturn = false;
		if (query.getQueryModifiers().hasModifiers()) {
			final List<OrderCondition> conditions = query.getQueryModifiers().getSortConditions();
			toReturn = (conditions.isEmpty()) ? false : true;
		}
		return toReturn;
	}
	
	/**
	 * Main method. Generates the full SQL query, taking into account
	 * limit/offset/order by.
	 * An important part of this program is {@link #createViewFrom(Predicate, DBMetadata, Map, DatalogProgram, List)}
	 * that will create a view for every ans prodicate in the Datalog input program. 
	 * @param query
	 * 		This is a arbitrary Datalog Program. In this program ans predicates will be translated to Views.
	 * 
	 * 		NOTE (xiao): actually current implementation only handles Datalog programs that have a tree-like dependency graph 
	 * 
	 * @param signature
	 * 		The Select variables in the SPARQL query
	 * @param indent
	 * @param ruleIndex
	 * 		The index that maps intentional predicates to its rules
	 * @param ruleIndexByBodyPredicate 
	 * @param predicatesInBottomUp
	 * 		The topologically ordered predicates in <code> query </query>. {@link #DatalogDependencyGraphGenerator}
	 * @param extensionalPredicates
	 * 		The predicates that are not defined by any rule in <code> query </query>
	 * @return
	 * @throws OBDAException
	 */
	private String generateQuery(DatalogProgram query, List<String> signature,
			String indent,  Multimap<Predicate, CQIE> ruleIndex, Multimap<Predicate, CQIE> ruleIndexByBodyPredicate, 
			List<Predicate> predicatesInBottomUp, List<Predicate> extensionalPredicates) throws OBDAException {


		int numPreds = predicatesInBottomUp.size();
		int i = 0;

		/**
		 * 				ANS i > 1
		 */

		
		// create a view for every ans prodicate in the Datalog input program.
		while (i < numPreds - 1) {
			Predicate pred = predicatesInBottomUp.get(i);
			if (extensionalPredicates.contains(pred)) {
				/*
				 * extensional predicates are defined by DBs
				 */
			} else {
				boolean isAns1 = false;
				createViewFrom(pred, metadata, ruleIndex,
						ruleIndexByBodyPredicate, query, signature, isAns1);
			}
			i++;
		}	

		
		/**
		 * 				ANS 1
		 */
		
		//This should be ans1, and the rules defining it.
		Predicate predAns1 = predicatesInBottomUp.get(i);
		Collection<CQIE> ansrules = ruleIndex.get(predAns1);

		
		List<String> queryStrings = Lists.newArrayListWithCapacity(ansrules.size());
		/* Main loop, constructing the SPJ query for each CQ */
		
		for (CQIE cq : ansrules) {

			/*
			 * Here we normalize so that the form of the CQ is as close to the
			 * form of a normal SQL algebra as possible, 
			 */
			boolean isAns1 = true;
			String querystr = generateQueryFromSingleRule(cq,signature, isAns1);
			
			queryStrings.add(querystr);
		}

		
		StringBuilder result = createUnionFromSQLList(queryStrings);

		return result.toString();
	}

	/**
	 * Takes a list of SQL strings, and returns SQL1 UNION SQL 2 UNION....
	 * This method complements {@link #generateQueryFromSingleRule}
	 * 
	 * @param list of SQL strings
	 * @return Union of sql queries
	 */
	private StringBuilder createUnionFromSQLList(List<String> queriesStrings) {
		Iterator<String> queryStringIterator = queriesStrings.iterator();
		StringBuilder result = new StringBuilder();
		if (queryStringIterator.hasNext()) {
			result.append(queryStringIterator.next());
		}

		String UNION = null;
		if (isDistinct) {
			UNION = "UNION";
		} else {
			UNION = "UNION ALL";
		}
		while (queryStringIterator.hasNext()) {
			result.append("\n");
			result.append(UNION);
			result.append("\n\n");
			result.append(queryStringIterator.next());
		}
		return result;
	}

	/**
	 * Takes 1 single Datalog rule <code> cq </code> and return the SQL translation of that rule. 
	 * It is a helper method for {@link #generateQuery(DatalogProgram, List, String, Map, List, Set)}
	 * @param cq
	 * @param signature
	 * @return
	 * @throws OBDAException
	 */
	public String generateQueryFromSingleRule(CQIE cq,	List<String> signature, boolean isAns1) throws OBDAException 
	{
		//for (CQIE cq : query.getRules()) {

		//normalizeRule(cq);

		QueryAliasIndex index = new QueryAliasIndex(cq);

		boolean innerdistincts = false;
		
		//&& numberOfQueries == 1
		if (isDistinct) {
			innerdistincts = true;
		}

		String FROM = getFROM(cq.getBody(), index);
		String WHERE = getWHERE(cq.getBody(), index);
		String SELECT = getSelectClause(signature, cq, index, innerdistincts, isAns1);

		String querystr = SELECT + FROM + WHERE;
		return querystr ;
	}

	/**
	 * Here we normalize so that the form of the CQ is as close to the
	 * form of a normal SQL algebra as possible, particularly, no shared
	 * variables, only joins by means of equality. Also, equalities in
	 * nested expressions (JOINS) are kept at their respective levels to
	 * generate correct ON and wHERE clauses.
	 * @param cq
	 */
	private void normalizeRule(CQIE cq) {
		
//			log.debug("Before pushing equalities: \n{}", cq);

		DatalogNormalizer.enforceEqualities(cq, false);

//			log.debug("Before folding Joins: \n{}", cq);

		DatalogNormalizer.foldJoinTrees(cq, false);

//			log.debug("Before pulling out equalities: \n{}", cq);
		
		DatalogNormalizer.pullOutEqualities(cq);
		
//			log.debug("Before pulling out Left Join Conditions: \n{}", cq);
		
		DatalogNormalizer.pullOutLeftJoinConditions(cq);
		
//			log.debug("Before pulling up nested references: \n{}", cq);

		DatalogNormalizer.pullUpNestedReferences(cq, false);

//			log.debug("Before adding trivial equalities: \n{}, cq);", cq);

		DatalogNormalizer.addMinimalEqualityToLeftJoin(cq);

//			log.debug("Normalized CQ: \n{}", cq);
	}
	
	/**
	 * Normalizes a program, i.e., list of rules, in place
	 * 
	 * @param program
	 */
	private void normalizeProgram(DatalogProgram program){
		for(CQIE rule : program.getRules()){
			normalizeRule(rule);
		}
	}
	

	/**
	* This Method was created to handle the semantics of OPTIONAL when there
	* are multiple mappings or Unions. It will take mappings of the form
	*<ul>
	*<li> Concept <- definition1</li>
	*<li> Concept <- definition2 </li>
	*</ul>
	* And will generate a view of the form
	*<ul>
	* <li> QConceptView = definition1 UNION definition2
	*</ul>
	* This view is stored in the <code>metadata </code>. See {@link #DBMetadata}
	*
	* The idea is to use the view definition in the case of Union in the Optionals/LeftJoins
	 * @param ruleIndex 
	 * @param ruleIndexByBodyPredicate 
	 * @param query 
	 * @param signature 
	 * @throws OBDAException 
	*
	* @throws Exception
	*/

	private void createViewFrom(Predicate pred, DBMetadata metadata,
			Multimap<Predicate, CQIE> ruleIndex, Multimap<Predicate, CQIE> ruleIndexByBodyPredicate, DatalogProgram query,
			List<String> signature, boolean isAns1) throws OBDAException
	{

		/* Creates BODY of the view query */

		Collection<CQIE> ruleList = ruleIndex.get(pred);
		
		String unionView = "";

		for (CQIE rule : ruleList) {
			Function cqHead = rule.getHead();
			
			List<String> varContainer = QueryUtils.getVariableNamesInAtom(cqHead);
			/* Creates the SQL for the View */
			String sqlQuery = generateQueryFromSingleRule(rule, varContainer, isAns1);
			
			if (unionView.equals("")){
				unionView = "(" + sqlQuery + ")";
			} else {
				unionView = unionView + "\n" + "\n UNION \n (" + sqlQuery + ")";
			}

		}
		
		
		
		String viewname = String.format(VIEW_ANS_NAME, pred);
		//String viewname = "Q" + pred + "View";
		/* Creates the View itself */
		ViewDefinition viewU = metadata.createViewDefinition(viewname,	unionView, true);
		metadata.add(viewU);
		sqlAnsViewMap.put(pred,unionView);
	}


	
	
	/***
	 * Returns a string with boolean conditions formed with the boolean atoms
	 * found in the atoms list.
	 */
	private LinkedHashSet<String> getBooleanConditionsString(List<Function> atoms, QueryAliasIndex index) {
		LinkedHashSet<String> conditions = new LinkedHashSet<String>();
		for (int atomidx = 0; atomidx < atoms.size(); atomidx++) {
			Term innerAtom = atoms.get(atomidx);
			Function innerAtomAsFunction = (Function) innerAtom;
			if (innerAtomAsFunction.isBooleanFunction()) {
				String condition = getSQLCondition(innerAtomAsFunction, index);
				conditions.add(condition);
			}else if (innerAtomAsFunction.isDataTypeFunction()) {
				String condition = getSQLString(innerAtom, index, false);
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
		if (isUnary(atom)) {
			// For unary boolean operators, e.g., NOT, IS NULL, IS NOT NULL.
			// added also for IS TRUE
			String expressionFormat = getBooleanOperatorString(functionSymbol);
			Term term = atom.getTerm(0);
			String column = getSQLString(term, index, false);
			if (expressionFormat.contains("NOT %s") ) {
				// find data type of term and evaluate accordingly
				//int type = 8;
				if (term instanceof Function) {
					Function f = (Function) term;
					if (!f.isDataTypeFunction()) return String.format(expressionFormat, column);
				}
				int type = getVariableDataType(term, index);
				if (type == Types.INTEGER) return String.format("NOT %s > 0", column);
				if (type == Types.DOUBLE) return String.format("NOT %s > 0", column);
				if (type == Types.BOOLEAN) return String.format("NOT %s", column);
				if (type == Types.VARCHAR) return String.format("NOT LENGTH(%s) > 0", column);
				return "0;";
			}
			if (expressionFormat.contains("IS TRUE")) {
				// find data type of term and evaluate accordingly
				//int type = 8;
				int type = getVariableDataType(term, index);
				if (type == Types.INTEGER) return String.format("%s > 0", column);
				if (type == Types.DOUBLE) return String.format("%s > 0", column);
				if (type == Types.BOOLEAN) return String.format("%s", column);
				if (type == Types.VARCHAR) return String.format("LENGTH(%s) > 0", column);
				return "1;";
			}
			return String.format(expressionFormat, column);
		} else if (isBinary(atom)) {
			if (atom.isBooleanFunction()) {
				// For binary boolean operators, e.g., AND, OR, EQ, GT, LT, etc. _
				String expressionFormat = getBooleanOperatorString(functionSymbol);
				Term left = atom.getTerm(0);
				Term right = atom.getTerm(1);
				String leftOp = getSQLString(left, index, true);
				String rightOp = getSQLString(right, index, true);
				return String.format("(" + expressionFormat + ")", leftOp, rightOp);
			} else if (atom.isArithmeticFunction()) {
				// For numerical operators, e.g., MUTLIPLY, SUBSTRACT, ADDITION
				String expressionFormat = getNumericalOperatorString(functionSymbol);
				Term left = atom.getTerm(0);
				Term right = atom.getTerm(1);
				String leftOp = getSQLString(left, index, true);
				String rightOp = getSQLString(right, index, true);
				return String.format("(" + expressionFormat + ")", leftOp, rightOp);
			} else {
				throw new RuntimeException("The binary function " 
						+ functionSymbol.toString() + " is not supported yet!");
			}
		} else {
			if (functionSymbol == OBDAVocabulary.SPARQL_REGEX) {
				boolean caseinSensitive = false;
				if (atom.getArity() == 3) {
					if (atom.getTerm(2).toString().contains("i")) {
						caseinSensitive = true;
					} 
				}
				Term p1 = atom.getTerm(0);
				Term p2 = atom.getTerm(1);
				
				String column = getSQLString(p1, index, false);
				String pattern = getSQLString(p2, index, false);
				return sqladapter.sqlRegex(column, pattern, caseinSensitive);
			} else {
				throw new RuntimeException("The builtin function "
						+ functionSymbol.toString() + " is not supported yet!");
			}
		}
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
	 * @param isTopLevel
	 *            indicates if the list of atoms is actually the main body of
	 *            the conjunctive query. If it is, no JOIN is generated, but a
	 *            cross product with WHERE clause. Moreover, the isLeftJoin
	 *            argument will be ignored.
	 * 
	 * @return
	 */
	private String getTableDefinitions(List<Function> inneratoms,
			QueryAliasIndex index, boolean isTopLevel, boolean isLeftJoin,
			String indent) {
		/*
		 * We now collect the view definitions for each data atom each
		 * condition, and each each nested Join/LeftJoin
		 */
		List<String> tableDefinitions = new LinkedList<String>();
		for (int atomidx = 0; atomidx < inneratoms.size(); atomidx++) {
			Term innerAtom = inneratoms.get(atomidx);
			Function innerAtomAsFunction = (Function) innerAtom;
			String indent2 = indent + INDENT;
			String definition = getTableDefinition(innerAtomAsFunction, index, indent2);
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

		int size = tableDefinitions.size();
		if (isTopLevel) {
			if (size == 0) {
				tableDefinitionsString.append("(" + jdbcutil.getDummyTable() + ") tdummy ");
				
			} else {
			Iterator<String> tableDefinitionsIterator = tableDefinitions.iterator();
			tableDefinitionsString.append(indent);
			tableDefinitionsString.append(tableDefinitionsIterator.next());
			while (tableDefinitionsIterator.hasNext()) {
				tableDefinitionsString.append(",\n");
				tableDefinitionsString.append(indent);
				tableDefinitionsString.append(tableDefinitionsIterator.next());
			}
			}
		} else {
			/*
			 * This is actually a Join or LeftJoin, so we form the JOINs/LEFT
			 * JOINs and the ON clauses
			 */
			String JOIN_KEYWORD = null;
			if (isLeftJoin) {
				JOIN_KEYWORD = "LEFT OUTER JOIN";
			} else {
				JOIN_KEYWORD = "JOIN";
			}
			String JOIN = "\n" + indent + "(\n" + indent + "%s\n" + indent
					+ JOIN_KEYWORD + "\n" + indent + "%s\n" + indent + ")";

			if (size == 0) {
				throw new RuntimeException("Cannot generate definition for empty data");
			}
			if (size == 1) {
				return tableDefinitions.get(0);
			}

			/*
			 * To form the JOIN we will cycle through each data definition,
			 * nesting the JOINs as we go. The conditions in the ON clause will
			 * go on the TOP level only.
			 */
			String currentJoin = String.format(JOIN,
					tableDefinitions.get(size - 2),
					tableDefinitions.get(size - 1));
			tableDefinitions.remove(size - 1);
			tableDefinitions.remove(size - 2);

			int currentSize = tableDefinitions.size();
			while (currentSize > 0) {
				currentJoin = String.format(JOIN, tableDefinitions.get(currentSize - 1), currentJoin);
				tableDefinitions.remove(currentSize - 1);
				currentSize = tableDefinitions.size();
			}
			tableDefinitions.add(currentJoin);

			tableDefinitionsString.append(currentJoin);
			/*
			 * If there are ON conditions we add them now. We need to remove the
			 * last parenthesis ')' and replace it with ' ON %s)' where %s are
			 * all the conditions
			 */
			String conditions = getConditionsString(inneratoms, index, true, indent);

			if (conditions.length() > 0 && tableDefinitionsString.lastIndexOf(")") != -1) {
				int lastidx = tableDefinitionsString.lastIndexOf(")");
				tableDefinitionsString.delete(lastidx, tableDefinitionsString.length());
				String ON_CLAUSE = String.format("ON\n%s\n " + indent + ")", conditions);
				tableDefinitionsString.append(ON_CLAUSE);
			}
		}
		return tableDefinitionsString.toString();
	}

	/**
	 * Returns the table definition for the given atom. If the atom is a simple
	 * table or view, then it returns the value as defined by the
	 * QueryAliasIndex. If the atom is a Join or Left Join, it will call
	 * getTableDefinitions on the nested term list.
	 */
	private String getTableDefinition(Function atom, QueryAliasIndex index, String indent) {
		Predicate predicate = atom.getFunctionSymbol();
		if (predicate instanceof BooleanOperationPredicate
				|| predicate instanceof NumericalOperationPredicate
				|| predicate instanceof DataTypePredicate) {
			// These don't participate in the FROM clause
			return "";
		} else if (predicate instanceof AlgebraOperatorPredicate) {
			List<Function> innerTerms = new LinkedList<Function>();
			for (Term innerTerm : atom.getTerms()) {
				innerTerms.add((Function) innerTerm);
			}
			if (predicate == OBDAVocabulary.SPARQL_JOIN) {
				String indent2 = indent + INDENT;
				String tableDefinitions = getTableDefinitions(innerTerms, index, false, false, indent2);
				return tableDefinitions;
			} else if (predicate == OBDAVocabulary.SPARQL_LEFTJOIN) {
				return getTableDefinitions(innerTerms, index, false, true, indent + INDENT);
			}
		}

		/*
		 * This is a data atom
		 */
		String def = index.getViewDefinition(atom);
		return def;
	}

	private String getFROM(List<Function> atoms, QueryAliasIndex index) {
		String tableDefinitions = getTableDefinitions(atoms, index, true, false, "");
		return "\n FROM \n" + tableDefinitions;
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

		LinkedHashSet<String> equalityConditions = new LinkedHashSet<String>();

		// if (processShared)
		LinkedHashSet<String> conditionsSharedVariablesAndConstants = getConditionsSharedVariablesAndConstants(atoms, index, processShared);
		equalityConditions.addAll(conditionsSharedVariablesAndConstants);
		LinkedHashSet<String> booleanConditions = getBooleanConditionsString(atoms, index);

		LinkedHashSet<String> conditions = new LinkedHashSet<String>();
		conditions.addAll(equalityConditions);
		conditions.addAll(booleanConditions);

		/*
		 * Collecting all the conditions in a single string for the ON or WHERE
		 * clause
		 */
		StringBuilder conditionsString = new StringBuilder();
		Iterator<String> conditionsIterator = conditions.iterator();
		if (conditionsIterator.hasNext()) {
			conditionsString.append(indent);
			conditionsString.append(conditionsIterator.next());
		}
		while (conditionsIterator.hasNext()) {
			conditionsString.append(" AND\n");
			conditionsString.append(indent);
			conditionsString.append(conditionsIterator.next());
		}
		return conditionsString.toString();
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
			return atom.getVariables();
		}
		if (atom.isBooleanFunction()) {
			return new HashSet<Variable>();
		}
		if (atom.isDataTypeFunction()) {
			return new HashSet<Variable>();
		}
		/*
		 * we have an alebra opertaor (join or left join) if its a join, we need
		 * to collect all the varaibles of each nested atom., if its a left
		 * join, only of the first data/algebra atom (the left atom).
		 */
		boolean isLeftJoin = false;
		boolean foundFirstDataAtom = false;

		if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN) {
			isLeftJoin = true;
		}
		LinkedHashSet<Variable> innerVariables = new LinkedHashSet<Variable>();
		for (Term t : atom.getTerms()) {
			if (isLeftJoin && foundFirstDataAtom) {
				break;
			}
			Function asFunction = (Function) t;
			if (asFunction.isBooleanFunction()) {
				continue;
			}
			innerVariables.addAll(getVariableReferencesWithLeftJoin(asFunction));
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
	private LinkedHashSet<String> getConditionsSharedVariablesAndConstants(
			List<Function> atoms, QueryAliasIndex index, boolean processShared) {
		LinkedHashSet<String> equalities = new LinkedHashSet<String>();

		Set<Variable> currentLevelVariables = new LinkedHashSet<Variable>();
		if (processShared) {
			for (Function atom : atoms) {
				currentLevelVariables.addAll(getVariableReferencesWithLeftJoin(atom));
			}
		}
		
		/*
		 * For each variable we collect all the columns that shold be equated
		 * (due to repeated positions of the variable). then we form atoms of
		 * the form "COL1 = COL2"
		 */
		for (Variable var : currentLevelVariables) {
			Collection<String> references = index.getColumnReferences(var);
			if (references.size() < 2) {
				// No need for equality
				continue;
			}
			Iterator<String> referenceIterator = references.iterator();
			String leftColumnReference = referenceIterator.next();
			while (referenceIterator.hasNext()) {
				String rightColumnReference = referenceIterator.next();
				String equality = String.format("(%s = %s)", leftColumnReference, rightColumnReference);
				equalities.add(equality);
				leftColumnReference = rightColumnReference;
			}
		}

		for (Function atom : atoms) {
			if (!atom.isDataFunction()) {
				continue;
			}
			for (int idx = 0; idx < atom.getArity(); idx++) {
				Term l = atom.getTerm(idx);
				if (l instanceof Constant) {
					String value = getSQLString(l, index, false);
					String columnReference = index.getColumnReference(atom, idx);
					equalities.add(String.format("(%s = %s)", columnReference, value));
				}
			}

		}
		return equalities;
	}

	// return variable SQL data type
	private int getVariableDataType (Term term, QueryAliasIndex idx) {
		Function f = (Function) term;
		if (f.isDataTypeFunction()) {
			Predicate p = f.getFunctionSymbol();
			if (p.toString() == OBDAVocabulary.XSD_BOOLEAN_URI) return Types.BOOLEAN;
			if (p.toString() == OBDAVocabulary.XSD_INT_URI)  return Types.INTEGER;
			if (p.toString() == OBDAVocabulary.XSD_INTEGER_URI)  return Types.INTEGER;
			if (p.toString() == OBDAVocabulary.XSD_DOUBLE_URI) return Types.DOUBLE;
			if (p.toString() == OBDAVocabulary.XSD_STRING_URI) return Types.VARCHAR;
			if (p.toString() == OBDAVocabulary.RDFS_LITERAL_URI) return Types.VARCHAR;
		}
		// Return varchar for unknown
		return Types.VARCHAR;
	}

	private String getWHERE(List<Function> atoms, QueryAliasIndex index) {
		String conditions = getConditionsString(atoms, index, false, "");
		if (conditions.isEmpty()) {
			return "";
		}
		return "\nWHERE \n" + conditions;
	}

	/**
	 * produces the select clause of the sql query for the given CQIE
	 * @param q
	 *            the query
	 * @return the sql select clause
	 */
	private String getSelectClause(List<String> signature, CQIE query,
			QueryAliasIndex index, boolean distinct, boolean isAns1) throws OBDAException {
		/*
		 * If the head has size 0 this is a boolean query.
		 */
		List<Term> headterms = query.getHead().getTerms();
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ");
		if (distinct) {
			sb.append("DISTINCT ");
		}
		if (headterms.size() == 0) {
			sb.append("true as x");
			return sb.toString();
		}

		Iterator<Term> hit = headterms.iterator();
		int hpos = 0;
		
		
		
		while (hit.hasNext()) {
			Term ht = hit.next();
			
			/*
			 * When isAns1==1, we may need the use the <code>signature</code> for the varName 
			 */
			String varName = "v" + hpos;
			
			/*
			 * For simplicity, we assume there is only one variable in the term ht.
			 * 
			 * TODO: handle the general case
			 */
			
			String typeColumn;
			String mainColumn;
			if(!isAns1){
				typeColumn = getTypeColumnForSELECT(ht, varName);
				 mainColumn = getMainColumnForSELECT(ht, signature, hpos, varName, index, isAns1);
			} else {
				
				if(ht instanceof Variable){
					/* 
					 * In case of Variable, we wrap the variable with a proper URI or datatype
					 */
					Function atom = QueryUtils.findOneAtomInRuleBody(query, (Variable) ht);
					int j = atom.getTerms().indexOf(ht);
					Term canonicalAtom = predicateCanonicalAtoms.get(atom.getFunctionSymbol());
					Term tt = ((Function)canonicalAtom).getTerm(j );
					Predicate predicate = ((Function)tt).getFunctionSymbol();
				
					OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
					Function f = factory.getFunction(predicate, ht);
					typeColumn = getTypeColumnForSELECT(f,  varName);
					mainColumn = getMainColumnForSELECT(f, signature, hpos, varName, index, isAns1);
					
					
					
				} else {
					typeColumn = getTypeColumnForSELECT(ht, varName);
					mainColumn = getMainColumnForSELECT(ht, signature, hpos, varName, index, isAns1);
				}
			}
			String langColumn = getLangColumnForSELECT(ht, varName,	index);
			//String mainColumn = getMainColumnForSELECT(ht, varName, index, isAns1);

			sb.append("\n   ");
			sb.append(typeColumn);
			sb.append(", ");
			sb.append(langColumn);
			sb.append(", ");
			sb.append(mainColumn);
			if (hit.hasNext()) {
				sb.append(", ");
			}
			hpos++;
		}
		return sb.toString();
	}


	private String getMainColumnForSELECT(Term ht,
			List<String> signature, int hpos, String varName, QueryAliasIndex index, boolean isAns1) {

		String mainColumn = null;

		String mainTemplate = "%s AS %s";

		if (ht instanceof URIConstant) {
			URIConstant uc = (URIConstant) ht;
			mainColumn = jdbcutil.getSQLLexicalForm(uc.getURI().toString());
		} else if (ht == OBDAVocabulary.NULL) {
			mainColumn = "NULL";
		} else if (ht instanceof Function) {
			/*
			 * if it's a function we need to get the nested value if its a
			 * datatype function or we need to do the CONCAT if its URI(....).
			 */
			Function ov = (Function) ht;
			Predicate function = ov.getFunctionSymbol();
			String functionString = function.toString();

			/*
			 * Adding the column(s) with the actual value(s)
			 */
			if (function instanceof DataTypePredicate) {
				/*
				 * Case where we have a typing function in the head (this is the
				 * case for all literal columns
				 */
				String termStr = null;
				int size = ov.getTerms().size();
				if ((function instanceof Literal) || size > 2 )
				{
					termStr = getSQLStringForTemplateFunction(ov, index);
				}
				else {
					Term term = ov.getTerms().get(0);
					if (term instanceof ValueConstant) {
						termStr = jdbcutil.getSQLLexicalForm((ValueConstant) term);
					} else {
						termStr = getSQLString(term, index, false);
					}
				}
				mainColumn = termStr;

			} else if (functionString.equals(OBDAVocabulary.QUEST_URI)) {

				if(isAns1){
					/*
					 * New template based URI building functions
					 */
					mainColumn = getSQLStringForTemplateFunction(ov, index);
				} else {
					/*
					 * TODO: Handle the case of multiple variables
					 */
					mainColumn = getSQLString(((Variable) ov.getTerm(1)), index, false);
					
				}

			} else if (functionString.equals(OBDAVocabulary.QUEST_BNODE)) {
				/*
				 * New template based BNODE building functions
				 */
				mainColumn = getSQLStringForTemplateFunction(ov, index);

			} else {
				throw new IllegalArgumentException(
						"Error generating SQL query. Found an invalid function during translation: "
								+ ov.toString());
			}
		} else {
			throw new RuntimeException("Cannot generate SELECT for term: " + ht.toString());
		}

		/*
		 * If the we have a column we need to still CAST to VARCHAR
		 */
		if (mainColumn.charAt(0) != '\'' && mainColumn.charAt(0) != '(') {
			if (!isStringColType(ht, index)) {
				mainColumn = sqladapter.sqlCast(mainColumn, Types.VARCHAR);
			}
		}
		String format ;
		String varNameAns1;
		if (isAns1){
			varNameAns1 = signature.get(hpos);
			 format = String.format(mainTemplate, mainColumn, sqladapter.sqlQuote(varNameAns1));
				
		} else{
			format = String.format(mainTemplate, mainColumn, sqladapter.sqlQuote(varName));
					
		}
		
		return format;
	}

	private String getLangColumnForSELECT(Term ht, String varName, QueryAliasIndex index) {

		String langStr = "%s AS \"%sLang\"";

		//String varName = signature.get(hpos);
		if (ht instanceof Function) {
			Function ov = (Function) ht;
			Predicate function = ov.getFunctionSymbol();

			if (function == OBDAVocabulary.RDFS_LITERAL || function == OBDAVocabulary.RDFS_LITERAL_LANG)
				if (ov.getTerms().size() > 1) {
				/*
				 * Case for rdf:literal s with a language, we need to select 2
				 * terms from ".., rdf:literal(?x,"en"),
				 * 
				 * and signature "name" * we will generate a select with the
				 * projection of 2 columns
				 * 
				 * , 'en' as nameqlang, view.colforx as name,
				 */
				String lang = null;
				int last = ov.getTerms().size()-1;
				Term langTerm = ov.getTerms().get(last);
				if (langTerm == OBDAVocabulary.NULL) {
					lang = "NULL";
				} else if (langTerm instanceof ValueConstant) {
					lang = jdbcutil.getSQLLexicalForm((ValueConstant) langTerm);
				} else {
					lang = getSQLString(langTerm, index, false);
				}
				return (String.format(langStr, lang, varName));
			}
		}
		return (String.format(langStr, "NULL", varName));

	}

	private String getTypeColumnForSELECT(Term ht, String varName) {

		String typeStr = "%s AS \"%sQuestType\"";
		
		if (ht instanceof Function) {
			Function ov = (Function) ht;
			Predicate function = ov.getFunctionSymbol();
			String functionString = function.getName();
			
			/*
			 * Adding the ColType column to the projection (used in the result
			 * set to know the type of constant)
			 */
			if (functionString.equals(OBDAVocabulary.XSD_BOOLEAN.getName().toString())) {
				return (String.format(typeStr, 9, varName));
			} else if (functionString.equals(OBDAVocabulary.XSD_DATETIME_URI)) {
				return (String.format(typeStr, 8, varName));
			} else if (functionString.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
				return (String.format(typeStr, 5, varName));
			} else if (functionString.equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
				return (String.format(typeStr, 6, varName));
			} else if (functionString.equals(OBDAVocabulary.XSD_INTEGER_URI)) {
				return (String.format(typeStr, 4, varName));
			} else if (functionString.equals(OBDAVocabulary.XSD_STRING_URI)) {
				return (String.format(typeStr, 7, varName));
			} else if (functionString.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
				return (String.format(typeStr, 3, varName));
			} else if (functionString.equals(OBDAVocabulary.QUEST_URI)) {
				return (String.format(typeStr, 1, varName));
			} else if (functionString.equals(OBDAVocabulary.QUEST_BNODE)) {
				return (String.format(typeStr, 2, varName));
			}
		} else if (ht instanceof URIConstant) {
			return (String.format(typeStr, 1, varName));
		} else if (ht == OBDAVocabulary.NULL) {
			return (String.format(typeStr, 0, varName));
		}
		throw new RuntimeException("Cannot generate SELECT for term: " + ht.toString());

	}

	public String getSQLStringForTemplateFunction(Function ov, QueryAliasIndex index) {
		/*
		 * The first inner term determines the form of the result
		 */
		Term t = ov.getTerms().get(0);
		Term c;
	
		String literalValue = "";
		
		if (t instanceof ValueConstant || t instanceof BNode) {
			/*
			 * The function is actually a template. The first parameter is a
			 * string of the form http://.../.../ or empty "{}" with place holders of the form
			 * {}. The rest are variables or constants that should be put in
			 * place of the palce holders. We need to tokenize and form the
			 * CONCAT
			 */
			if (t instanceof BNode) {
				c = (BNode) t;
				literalValue = ((BNode) t).getValue();
			} else {
				c = (ValueConstant) t;	
				literalValue = ((ValueConstant) t).getValue();
			}		
			Predicate pred = ov.getFunctionSymbol();
			String replace1 = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
					"REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" ;
			String replace2 = ",' ', '%20')," +
					  "'!', '%21')," +
					  "'@', '%40'),"+
					  "'#', '%23')," +
					  "'$', '%24'),"+
					  "'&', '%26'),"+
					  "'*', '%42'), "+
					  "'(', '%28'), "+
					  "')', '%29'), "+
					  "'[', '%5B'), "+
					  "']', '%5D'), "+
					  "',', '%2C'), "+
					  "';', '%3B'), "+
					  "':', '%3A'), "+
					  "'?', '%3F'), "+
					  "'=', '%3D'), "+
					  "'+', '%2B'), "+
					  "'''', '%22'), "+
					  "'/', '%2F')";
				
			String template = trim(literalValue);
			String[] split = template.split("[{][}]");
			
			List<String> vex = new LinkedList<String>();
			if (split.length > 0 && !split[0].isEmpty()) {
				vex.add(jdbcutil.getSQLLexicalForm(split[0]));
			}
			
			/*
			 * New we concat the rest of the function, note that if there is only 1 element
			 * there is nothing to concatenate
			 */
			if (ov.getTerms().size() > 1) {
				int size = ov.getTerms().size();
				if (pred == OBDAVocabulary.RDFS_LITERAL || pred == OBDAVocabulary.RDFS_LITERAL_LANG) {
					size--;
				}
				for (int termIndex = 1; termIndex < size; termIndex++) {
					Term currentTerm = ov.getTerms().get(termIndex);
					String repl = "";
					if (isStringColType(currentTerm, index)) {
						repl = replace1 + (getSQLString(currentTerm, index, false)) + replace2;
					} else {
						repl = replace1 + sqladapter.sqlCast(getSQLString(currentTerm, index, false), Types.VARCHAR) + replace2;
					}
					vex.add(repl);
					if (termIndex < split.length ) {
						vex.add(jdbcutil.getSQLLexicalForm(split[termIndex]));
					}
				}
			}
		
			if (vex.size() == 1) {
				
				return vex.get(0);
			}
			String[] params = new String[vex.size()];
			int i = 0;
			for (String param : vex) {
				params[i] = param;
				i += 1;
			}
			return getStringConcatenation(sqladapter, params);
			
		} else if (t instanceof Variable) {
			/*
			 * The function is of the form uri(x), we need to simply return the
			 * value of X
			 */
			return getSQLString(((Variable) t), index, false);
			
		} else if (t instanceof URIConstant) {
			/*
			 * The function is of the form uri("http://some.uri/"), i.e., a
			 * concrete URI, we return the string representing that URI.
			 */
			URIConstant uc = (URIConstant) t;
			return jdbcutil.getSQLLexicalForm(uc.getURI().toString());
		}

		/*
		 * Unsupported case
		 */
		throw new IllegalArgumentException("Error, cannot generate URI constructor clause for a term: " + ov.toString());

	}
	
	private String getStringConcatenation(SQLDialectAdapter adapter, String[] params) {
		String toReturn = sqladapter.strconcat(params);
		if (adapter instanceof DB2SQLDialectAdapter) {
			/*
			 * A work around to handle DB2 (>9.1) issue SQL0134N: Improper use of a string column, host variable, constant, or function name.
			 * http://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic=%2Fcom.ibm.db2.luw.messages.sql.doc%2Fdoc%2Fmsql00134n.html
			 */
			if (isDistinct || isOrderBy) {
				return adapter.sqlCast(toReturn, Types.VARCHAR);
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
				 * A URI function always returns a string, thus it is a string column type.
				 */
				if (isSI) return false;
				return true;
			} else {
				if (isUnary(function)) {
					/*
					 * Update the term with the parent term's first parameter.
					 * Note: this method is confusing :(
					 */
					 term = function.getTerm(0);
					 return isStringColType(term, index);
				}
			}
		} else if (term instanceof Variable) {
			Collection<String> viewdef = index.getColumnReferences((Variable) term);
			String def = viewdef.iterator().next();
			String col = trim(def.split("\\.")[1]);
			String table = def.split("\\.")[0];
			if (def.startsWith("QVIEW")) {
				Map<Function, String> views = index.viewNames;
				for (Function func : views.keySet()) {
					String value = views.get(func);
					if (value.equals(def.split("\\.")[0])) {
						table = func.getFunctionSymbol().toString();
						break;
					}
				}
			}
			List<TableDefinition> tables = metadata.getTableList();
			for (TableDefinition tabledef: tables) {
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
		}
		return false;
	}

	private String trim(String string) {
		while (string.startsWith("\"") && string.endsWith("\"")) {
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}
	
	/**
	 * Determines if it is a unary function.
	 */
	private boolean isUnary(Function fun) {
		return (fun.getArity() == 1) ? true : false;
	}

	/**
	 * Determines if it is a binary function.
	 */
	private boolean isBinary(Function fun) {
		return (fun.getArity() == 2) ? true : false;
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
	public String getSQLString(Term term, QueryAliasIndex index, boolean useBrackets) {
		if (term == null) {
			return "";
		}
		if (term instanceof ValueConstant) {
			ValueConstant ct = (ValueConstant) term;
			if (isSI) {
				if (ct.getType() == COL_TYPE.OBJECT || ct.getType() == COL_TYPE.LITERAL) {
					int id = getUriid(ct.getValue());
					if (id >= 0)
						return jdbcutil.getSQLLexicalForm(String.valueOf(id));
				}
			}
			return jdbcutil.getSQLLexicalForm(ct);
		} else if (term instanceof URIConstant) {
			if (isSI) {
				String uri = term.toString();
				int id = getUriid(uri);
				return jdbcutil.getSQLLexicalForm(String.valueOf(id));
			}
			URIConstant uc = (URIConstant) term;
			return jdbcutil.getSQLLexicalForm(uc.toString());
		} else if (term instanceof Variable) {
			Variable var = (Variable) term;
			Collection<String> posList = index.getColumnReferences(var);
			if (posList == null || posList.size() == 0) {
				throw new RuntimeException("Unbound variable found in WHERE clause: " + term);
			}
			return posList.iterator().next();
		}

		/* If its not constant, or variable its a function */

		Function function = (Function) term;
		Predicate functionSymbol = function.getFunctionSymbol();
		Term term1 = function.getTerms().get(0);
		int size = function.getTerms().size();

		if (functionSymbol instanceof DataTypePredicate) {
			if (functionSymbol.getType(0) == COL_TYPE.UNSUPPORTED) {
				throw new RuntimeException("Unsupported type in the query: " + function);
			}
			if (size == 1) {
				// atoms of the form integer(x)
				return getSQLString(term1, index, false);
			} else 	{
				return getSQLStringForTemplateFunction(function, index);
			}
		} else if (functionSymbol instanceof BooleanOperationPredicate) {
			// atoms of the form EQ(x,y)
			String expressionFormat = getBooleanOperatorString(functionSymbol);
			if (isUnary(function)) {
				// for unary functions, e.g., NOT, IS NULL, IS NOT NULL
				// also added for IS TRUE
				if (expressionFormat.contains("IS TRUE")) {
					// find data type of term and evaluate accordingly
					String column = getSQLString(term1, index, false);
					int type = getVariableDataType(term1, index);
					if (type == Types.INTEGER) return String.format("%s > 0", column);
					if (type == Types.DOUBLE) return String.format("%s > 0", column);
					if (type == Types.BOOLEAN) return String.format("%s", column);
					if (type == Types.VARCHAR) return String.format("LENGTH(%s) > 0", column);
					return "1";
				}
				String op = getSQLString(term1, index, true);
				return String.format(expressionFormat, op);
				
			} else if (isBinary(function)) {
				// for binary functions, e.g., AND, OR, EQ, NEQ, GT, etc.
				String leftOp = getSQLString(term1, index, true);
				Term term2 = function.getTerms().get(1);
				String rightOp = getSQLString(term2, index, true);
				String result = String.format(expressionFormat, leftOp, rightOp);
				if (useBrackets) {
					return String.format("(%s)", result);
				} else {
					return result;
				}
			} else {
				throw new RuntimeException("Cannot translate boolean function: " + functionSymbol);
			}
			
		} else if (functionSymbol instanceof NumericalOperationPredicate) {
			String expressionFormat = getNumericalOperatorString(functionSymbol);
			String leftOp = getSQLString(term1, index, true);
			Term term2 = function.getTerms().get(1);
			String rightOp = getSQLString(term2, index, true);
			String result = String.format(expressionFormat, leftOp, rightOp);
			if (useBrackets) {
				return String.format("(%s)", result);
			} else {
				return result;
			}
			
		} else {
			String functionName = functionSymbol.toString();
			if (functionName.equals(OBDAVocabulary.QUEST_CAST_STR)) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				String datatype = ((Constant) function.getTerm(1)).getValue();
				int sqlDatatype = -1;
				if (datatype.equals(OBDAVocabulary.XSD_STRING_URI)) {
					sqlDatatype = Types.VARCHAR;
				}
				if (isStringColType(function, index)) {
					return columnName;
				} else {
					return sqladapter.sqlCast(columnName, sqlDatatype);
				}
			} else if (functionName.equals(OBDAVocabulary.SPARQL_STR_URI)) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				if (isStringColType(function, index)) {
					return columnName;
				} else {
					return sqladapter.sqlCast(columnName, Types.VARCHAR);
				}
			}
		}

		/*
		 * The atom must be of the form uri("...", x, y)
		 */
		String functionName = function.getFunctionSymbol().toString();
		if (functionName.equals(OBDAVocabulary.QUEST_URI) || functionName.equals(OBDAVocabulary.QUEST_BNODE)) {
			return getSQLStringForTemplateFunction(function, index);
		} else {
			throw new RuntimeException("Unexpected function in the query: " + functionSymbol);
		}
	}


	/***
	 * We look for the ID in the list of IDs, if its not there, we return -2, which we know will never appear
	 * on the DB. This is correct because if a constant appears in a query, and that constant was never inserted
	 * in the DB, the query must be empty (that atom), by putting -2 as id, we will enforce that.
	 * @param uri
	 * @return
	 */
	private int getUriid(String uri) {
		
		Integer id = uriRefIds.get(uri);
		if (id != null)
			return id;
		return -2;
					
	}

	/**
	 * Returns the SQL string for the boolean operator, including placeholders
	 * for the terms to be used, e.g., %s = %s, %s IS NULL, etc.
	 * 
	 * @param functionSymbol
	 * @return
	 */
	private String getBooleanOperatorString(Predicate functionSymbol) {
		String operator = null;
		if (functionSymbol.equals(OBDAVocabulary.EQ)) {
			operator = EQ_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.NEQ)) {
			operator = NEQ_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.GT)) {
			operator = GT_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.GTE)) {
			operator = GTE_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.LT)) {
			operator = LT_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.LTE)) {
			operator = LTE_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.AND)) {
			operator = AND_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.OR)) {
			operator = OR_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.NOT)) {
			operator = NOT_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.IS_NULL)) {
			operator = IS_NULL_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.IS_NOT_NULL)) {
			operator = IS_NOT_NULL_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.IS_TRUE)) {
			operator = IS_TRUE_OPERATOR;
		} else {
			throw new RuntimeException("Unknown boolean operator: " + functionSymbol);
		}
		return operator;
	}

	private String getNumericalOperatorString(Predicate functionSymbol) {
		String operator = null;
		if (functionSymbol.equals(OBDAVocabulary.ADD)) {
			operator = ADD_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.SUBSTRACT)) {
			operator = SUBSTRACT_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.MULTIPLY)) {
			operator = MULTIPLY_OPERATOR;
		} else {
			throw new RuntimeException("Unknown numerical operator: " + functionSymbol);
		}
		return operator;
	}

	/**
	 * Utility class to resolve "database" atoms to view definitions ready to be
	 * used in a FROM clause, and variables, to column references defined over
	 * the existing view definitons of a query.
	 */
	public class QueryAliasIndex {

		Map<Function, String> viewNames = new HashMap<Function, String>();
		Map<Function, String> tableNames = new HashMap<Function, String>();
		Map<Function, DataDefinition> dataDefinitions = new HashMap<Function, DataDefinition>();
		//Map<Variable, LinkedHashSet<String>> columnReferences = new HashMap<Variable, LinkedHashSet<String>>();
		Multimap<Variable, String> columnReferences = ArrayListMultimap.create();


		int dataTableCount = 0;
		boolean isEmpty = false;
		
		
		public QueryAliasIndex(CQIE query) {
			List<Function> body = query.getBody();
			generateViews(body);
		}

		private void generateViews(List<Function> atoms) {
			for (Function atom : atoms) {
				/*
				 * This will be called recursively if necessary
				 */
				generateViewsIndexVariables(atom);
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
		 */
		private void generateViewsIndexVariables(Function atom) {
			if (atom.getFunctionSymbol() instanceof BooleanOperationPredicate) {
				return;
			} else if (atom.getFunctionSymbol() instanceof AlgebraOperatorPredicate) {
				List<Term> lit = atom.getTerms();
				for (Term subatom : lit) {
					if (subatom instanceof Function) {
						generateViewsIndexVariables((Function) subatom);
					}
				}
			} 

			Predicate tablePredicate = atom.getFunctionSymbol();
			String tableName = tablePredicate.getName();
			DataDefinition def = metadata.getDefinition(tableName);
	
			if (def == null) {
				/*
				 * There is no definition for this atom, its not a database
				 * predicate. We check if it is an ans predicate and it has a view: 
				 */
				//tableName = "Q"+tableName+"View";
				tableName = String.format(VIEW_ANS_NAME, tableName);
				def = metadata.getDefinition(tableName);
				if (def == null){
					isEmpty = true;
					return;
				} else{
					viewNames.put(atom, tableName);
				}
			} else{
				
				String simpleTableViewName = String.format(VIEW_NAME, tableName,dataTableCount);
				viewNames.put(atom, simpleTableViewName);
			}
			dataTableCount += 1;
			tableNames.put(atom, tableName);
			dataDefinitions.put(atom, def);
			
			indexVariables(atom);
		}

		private void indexVariables(Function atom) {
			DataDefinition def = dataDefinitions.get(atom);
			Predicate atomName= atom.getFunctionSymbol();
			String viewName = viewNames.get(atom);
			viewName = sqladapter.sqlQuote(viewName);
			for (int index = 0; index < atom.getTerms().size(); index++) {
				Term term = atom.getTerms().get(index);
				
				if(term instanceof Variable) {
					/*
					 * the index of attributes of the definition starts from 1
					 */
					String columnName ;
					
					
					if (ruleIndex.containsKey(atomName)){
						//If I am here it means that it is not a database table but a view from an Ans predicate
						int attPos = 3*(index+1);
						columnName = def.getAttributeName(attPos);
					}else{
						columnName = def.getAttributeName(index + 1);	
					}
					
					
					columnName = trim(columnName);
					
					String reference = sqladapter.sqlQualifiedColumn(viewName, columnName);
					columnReferences.put((Variable)term, reference);
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
		public Collection<String> getColumnReferences(Variable var) {
			return columnReferences.get(var);
		}

		/***
		 * Generates the view definition, i.e., "tablename viewname".
		 */
		public String getViewDefinition(Function atom) {
			DataDefinition def = dataDefinitions.get(atom);
			String viewname = viewNames.get(atom);
			viewname = sqladapter.sqlQuote(viewname);
					
			if (def instanceof TableDefinition) {
				return sqladapter.sqlTableName(tableNames.get(atom), viewname);
			} else if (def instanceof ViewDefinition) {
				String viewdef = ((ViewDefinition) def).getStatement();
				String formatView = String.format("(%s) %s", viewdef, viewname);
				return formatView;
			}
			
			//Should be an ans atom.
			Predicate pred = atom.getFunctionSymbol();
			String view = sqlAnsViewMap.get(pred); 
			viewname = "Q" + pred + "View";
			viewname = sqladapter.sqlQuote(viewname);
			
			if (view!=null) {
				String formatView = String.format("(%s) %s", view, viewname);
				return formatView;
				
			}
			
			
			
			throw new RuntimeException("Impossible to get data definition for: " + atom + ", type: " + def);
		}

		public String getView(Function atom) {
			return viewNames.get(atom);
		}

		public String getColumnReference(Function atom, int column) {
			String viewName = getView(atom);
			DataDefinition def = dataDefinitions.get(atom);
			String columnname = def.getAttributeName(column + 1);
			return sqladapter.sqlQualifiedColumn(viewName, columnname);
		}
	}
}
