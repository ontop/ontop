/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDAConnection;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.EmptyQueryResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.QuestGraphResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.QuestResultset;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.DatalogToSparqlTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SesameConstructTemplate;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlPrefixManager;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.krdb.obda.renderer.DatalogProgramRenderer;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * The obda statement provides the implementations necessary to query the
 * reformulation platform reasoner from outside, i.e. Protege
 */
public class QuestStatement implements OBDAStatement {

	private QueryRewriter rewriter = null;

	private SQLQueryGenerator querygenerator = null;

	private QueryVocabularyValidator validator = null;

	private OBDAModel unfoldingOBDAModel = null;

	private boolean canceled = false;

	private Statement sqlstatement;

	private RDBMSDataRepositoryManager repository;

	private QuestConnection conn;

	public Quest questInstance;

	private static Logger log = LoggerFactory.getLogger(QuestStatement.class);

	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	Thread runningThread = null;

	private QueryExecutionThread executionthread;

	private DatalogProgram programAfterRewriting;

	private DatalogProgram programAfterUnfolding;

	final Map<String, String> querycache;

	final Map<String, List<String>> signaturecache;

	//private Map<String, Query> jenaQueryCache;
	
	private Map<String, ParsedQuery> sesameQueryCache;

	final Map<String, Boolean> isbooleancache;

	final Map<String, Boolean> isconstructcache;

	final Map<String, Boolean> isdescribecache;

	final SparqlAlgebraToDatalogTranslator translator;
	
	SesameConstructTemplate templ = null;

	/*
	 * For benchmark purpose
	 */
	private long queryProcessingTime = 0;

	private long rewritingTime = 0;

	private long unfoldingTime = 0;

	public QuestStatement(Quest questinstance, QuestConnection conn, Statement st) {

		this.questInstance = questinstance;

		this.translator = new SparqlAlgebraToDatalogTranslator(this.questInstance.getUriTemplateMatcher());
		this.querycache = questinstance.getSQLCache();
		this.signaturecache = questinstance.getSignatureCache();
		//this.jenaQueryCache = questinstance.getJenaQueryCache();
		this.sesameQueryCache = questinstance.getSesameQueryCache();
		this.isbooleancache = questinstance.getIsBooleanCache();
		this.isconstructcache = questinstance.getIsConstructCache();
		this.isdescribecache = questinstance.getIsDescribeCache();

		this.repository = questinstance.dataRepository;
		this.conn = conn;
		this.rewriter = questinstance.rewriter;
		// this.unfoldingmechanism = questinstance.unfolder;
		this.querygenerator = questinstance.datasourceQueryGenerator;

		this.sqlstatement = st;
		this.validator = questinstance.vocabularyValidator;
		this.unfoldingOBDAModel = questinstance.unfoldingOBDAModel;
	}

	private class QueryExecutionThread extends Thread {

		private final CountDownLatch monitor;
		private final String strquery;
		// private Query query;
		private TupleResultSet tupleResult;
		private GraphResultSet graphResult;
		private Exception exception;
		private boolean error = false;
		private boolean executingSQL = false;

		boolean isBoolean = false, isConstruct = false, isDescribe = false, isSelect = false;

		public QueryExecutionThread(String strquery, CountDownLatch monitor) {
			this.monitor = monitor;
			this.strquery = strquery;
			// this.query = QueryFactory.create(strquery);
		}

		public void setQueryType(int type) {
			switch (type) {// encoding of query type to from numbers
			case 1:
				this.isSelect = true;
				break;
			case 2:
				this.isBoolean = true;
				break;
			case 3:
				this.isConstruct = true;
				break;
			case 4:
				this.isDescribe = true;
				break;
			}
		}

		public boolean errorStatus() {
			return error;
		}

		public Exception getException() {
			return exception;
		}

		public TupleResultSet getTupleResult() {
			return tupleResult;
		}

		public GraphResultSet getGraphResult() {
			return graphResult;
		}

		public void cancel() throws SQLException {
			if (!executingSQL) {
				this.stop();
			} else {
				sqlstatement.cancel();
			}
		}

		@Override
		public void run() {

			log.debug("Executing query: \n{}", strquery);

			try {

				if (!querycache.containsKey(strquery)) {
					getUnfolding(strquery);
				}
				/*
				 * Obtaineing the query from the cache
				 */
				String sql = getSqlString(strquery);
				List<String> signature = signaturecache.get(strquery);
				ParsedQuery query = sesameQueryCache.get(strquery);

				log.debug("Executing the query and get the result...");
				if (sql.equals("") && !isBoolean) {
					tupleResult = new EmptyQueryResultSet(signature, QuestStatement.this);
				} else if (sql.equals("")) {
					tupleResult = new BooleanOWLOBDARefResultSet(false, QuestStatement.this);
				} else {
					try {

						// Execute the SQL query string
						executingSQL = true;
						ResultSet set = null;
						// try {

						set = sqlstatement.executeQuery(sql);

						// }
						// catch(SQLException e)
						// {
						//
						// Store the SQL result to application result set.
						if (isSelect) { // is tuple-based results

							tupleResult = new QuestResultset(set, signature, QuestStatement.this);

						} else if (isBoolean) {
							tupleResult = new BooleanOWLOBDARefResultSet(set, QuestStatement.this);

						} else if (isConstruct || isDescribe) {
							boolean collectResults = false;
							if (isDescribe)
								collectResults = true;
							//Template template = query.getConstructTemplate();
							TupleResultSet tuples = null;

							tuples = new QuestResultset(set, signature, QuestStatement.this);
							graphResult = new QuestGraphResultSet(tuples, templ, collectResults);
						}
					} catch (SQLException e) {
						exception = e;
						error = true;
						log.error(e.getMessage(), e);

						throw new OBDAException("Error executing SQL query: \n" + e.getMessage() + "\nSQL query:\n " + sql, e);
					}
				}
				log.debug("Execution finished.\n");
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
				error = true;
				log.error(e.getMessage(), e);
			} finally {
				monitor.countDown();
			}
		}
	}

	/**
	 * Calls the necessary tuple or graph query execution Implements describe
	 * uri or var logic Returns the result set for the given query
	 */
	@Override
	public it.unibz.krdb.obda.model.ResultSet execute(String strquery) throws OBDAException {
		if (strquery.isEmpty()) {
			throw new OBDAException("Cannot execute an empty query");
		}

		// encoding ofquery type into numbers
		if (SPARQLQueryUtility.isSelectQuery(strquery)) {
			return executeTupleQuery(strquery, 1);

		} else if (SPARQLQueryUtility.isAskQuery(strquery)) {
			return executeTupleQuery(strquery, 2);
		} else if (SPARQLQueryUtility.isConstructQuery(strquery)) {
			
			// Here we need to get the template for the CONSTRUCT query results
			try {
				templ = new SesameConstructTemplate(strquery);
			} catch (MalformedQueryException e) {
				e.printStackTrace();
			}
			
			// Here we replace CONSTRUCT query with SELECT query
			strquery = SPARQLQueryUtility.getSelectFromConstruct(strquery);
			return executeGraphQuery(strquery, 3);
			
		} else if (SPARQLQueryUtility.isDescribeQuery(strquery)) {
			// create list of uriconstants we want to describe
			List<String> constants = new ArrayList<String>();
			if (SPARQLQueryUtility.isVarDescribe(strquery)) {
				// if describe ?var, we have to do select distinct ?var first
				String sel = SPARQLQueryUtility.getSelectVarDescribe(strquery);
				it.unibz.krdb.obda.model.ResultSet resultSet = (it.unibz.krdb.obda.model.ResultSet) this.executeTupleQuery(sel, 1);
				if (resultSet instanceof EmptyQueryResultSet)
					return null;
				else if (resultSet instanceof QuestResultset) {
					QuestResultset res = (QuestResultset) resultSet;
					while (res.nextRow()) {
						Constant constant = res.getConstant(1);
						if (constant instanceof URIConstant) {
							// collect constants in list
							constants.add(constant.getValue());
						}
					}
				}
			} else if (SPARQLQueryUtility.isURIDescribe(strquery)) {
				// DESCRIBE <uri> gives direct results, so we put the
				// <uri> constant directly in the list of constants
				constants.add(SPARQLQueryUtility.getDescribeURI(strquery));
			}

			QuestGraphResultSet describeResultSet = null;
			// execute describe <uriconst> in subject position
			for (String constant : constants) {
				// for each constant we execute a construct with
				// the uri as subject, and collect the results
				// in one graphresultset
				String str = SPARQLQueryUtility.getConstructSubjQuery(constant);
				try {
					templ = new SesameConstructTemplate(str);
				} catch (MalformedQueryException e) {
					e.printStackTrace();
				}
				str = SPARQLQueryUtility.getSelectFromConstruct(str);
				if (describeResultSet == null) {
					// just for the first time
					describeResultSet = (QuestGraphResultSet) executeGraphQuery(str, 4);
				} else {
					// 2nd and manyth times execute, but collect result into one
					// object
					QuestGraphResultSet set = (QuestGraphResultSet) executeGraphQuery(str, 4);
					if (set != null) {
						while (set.hasNext()) {
							// already process the result, add list<Assertion>
							// to internal buffer
							describeResultSet.addNewResultSet(set.next());
						}
					}
				}
			}
			// execute describe <uriconst> in object position
			for (String constant : constants) {
				String str = SPARQLQueryUtility.getConstructObjQuery(constant);
				try {
					templ = new SesameConstructTemplate(str);
				} catch (MalformedQueryException e) {
					e.printStackTrace();
				}
				str = SPARQLQueryUtility.getSelectFromConstruct(str);
				if (describeResultSet == null) {
					// just for the first time
					describeResultSet = (QuestGraphResultSet) executeGraphQuery(str, 4);
				} else {
					QuestGraphResultSet set = (QuestGraphResultSet) executeGraphQuery(str, 4);
					if (set != null) {
						while (set.hasNext()) {
							describeResultSet.addNewResultSet(set.next());
						}
					}
				}
			}
			return describeResultSet;
		}
		throw new OBDAException("Error, the result set was null");
	}


	/**
	 * Translates a SPARQL query into Datalog dealing with equivalences and
	 * verifying that the vocabulary of the query matches the one in the
	 * ontology. If there are equivalences to handle, this is where its done
	 * (i.e., renaming atoms that use predicates that have been replaced by a
	 * canonical one.
	 * 
	 * @param query
	 * @return
	 */
	private DatalogProgram translateAndPreProcess(ParsedQuery pq, List<String> signature) {
		DatalogProgram program = null;
		try {
			if (questInstance.isSemIdx()) {
				translator.setSI();
				translator.setUriRef(questInstance.getUriRefIds());
			}
			program = translator.translate(pq, signature);

			log.debug("Translated query: \n{}", program);

			DatalogUnfolder unfolder = new DatalogUnfolder(program.clone(), new HashMap<Predicate, List<Integer>>());
			
			//removeNonAnswerQueries(program);

			program = unfolder.unfold(program, "ans1",QuestConstants.BUP);

			log.debug("Flattened query: \n{}", program);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			OBDAException ex = new OBDAException(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		log.debug("Replacing equivalences...");
		program = validator.replaceEquivalences(program);
		return program;
	}



	private DatalogProgram getUnfolding(DatalogProgram query) throws OBDAException {

		log.debug("Start the partial evaluation process...");

		
		//This instnce of the unfolder is carried from Quest, and contains the mappings.
		DatalogProgram unfolding = questInstance.unfolder.unfold((DatalogProgram) query, "ans1",QuestConstants.TDOWN);
		log.debug("Partial evaluation: \n{}", unfolding);

		//removeNonAnswerQueries(unfolding);

		//log.debug("After target rules removed: \n{}", unfolding);

		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.setUriTemplateMatcher(questInstance.getUriTemplateMatcher());
		evaluator.evaluateExpressions(unfolding);

		log.debug("Boolean expression evaluated: \n{}", unfolding);
		log.debug("Partial evaluation ended.");

		return unfolding;
	}

	private void removeNonAnswerQueries(DatalogProgram program) {
		List<CQIE> toRemove = new LinkedList<CQIE>();
		for (CQIE rule : program.getRules()) {
			Predicate headPredicate = rule.getHead().getPredicate();
			if (!headPredicate.getName().toString().equals("ans1")) {
				toRemove.add(rule);
			}
		}
		program.removeRules(toRemove);
	}

	private String getSQL(DatalogProgram query, List<String> signature) throws OBDAException {
		if (((DatalogProgram) query).getRules().size() == 0) {
			return "";
		}
		log.debug("Producing the SQL string...");

		// query = DatalogNormalizer.normalizeDatalogProgram(query);
		String sql = querygenerator.generateSourceQuery((DatalogProgram) query, signature);

		log.debug("Resulting sql: \n{}", sql);
		return sql;
	}

	/**
	 * The method executes select or ask queries by starting a new quest
	 * execution thread
	 * 
	 * @param strquery
	 *            the select or ask query string
	 * @param type
	 *            1 - SELECT, 2 - ASK
	 * @return the obtained TupleResultSet result
	 * @throws OBDAException
	 */
	private TupleResultSet executeTupleQuery(String strquery, int type) throws OBDAException {

		startExecute(strquery, type);
		TupleResultSet result = executionthread.getTupleResult();

		if (result == null)
			throw new RuntimeException("Error, the result set was null");

		return result;
	}

	/**
	 * The method executes construct or describe queries
	 * 
	 * @param strquery
	 *            the query string
	 * @param type
	 *            3- CONSTRUCT, 4 - DESCRIBE
	 * @return the obtained GraphResultSet result
	 * @throws OBDAException
	 */
	private GraphResultSet executeGraphQuery(String strquery, int type) throws OBDAException {
		startExecute(strquery, type);
		return executionthread.getGraphResult();
	}

	/**
	 * Internal method to start a new query execution thread type defines the
	 * query type 1-SELECT, 2-ASK, 3-CONSTRUCT, 4-DESCRIBE
	 */
	private void startExecute(String strquery, int type) throws OBDAException {
		CountDownLatch monitor = new CountDownLatch(1);
		executionthread = new QueryExecutionThread(strquery, monitor);
		executionthread.setQueryType(type);
		executionthread.start();
		try {
			monitor.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (executionthread.errorStatus()) {
			OBDAException ex = new OBDAException(executionthread.getException().getMessage());
			ex.setStackTrace(executionthread.getStackTrace());
			throw ex;
		}

		if (canceled == true) {
			canceled = false;
			throw new OBDAException("Query execution was cancelled");
		}
	}

	/**
	 * Rewrites the given input SPARQL query and returns back an expanded SPARQL
	 * query. The query expansion involves query transformation from SPARQL
	 * algebra to Datalog objects and then translating back to SPARQL algebra.
	 * The transformation to Datalog is required to apply the rewriting
	 * algorithm.
	 * 
	 * @param sparql
	 *            The input SPARQL query.
	 * @return An expanded SPARQL query.
	 * @throws OBDAException
	 *             if errors occur during the transformation and translation.
	 */
	public String getSPARQLRewriting(String sparql) throws OBDAException {
		if (!SPARQLQueryUtility.isSelectQuery(sparql)) {
			throw new OBDAException("Support only SELECT query");
		}
		// Parse the SPARQL string into SPARQL algebra object
		QueryParser qp = QueryParserUtil.createParser(QueryLanguage.SPARQL);
		ParsedQuery query = null;
		try {
			query = qp.parseQuery(sparql, null); // base URI is null
		} catch (MalformedQueryException e) {
			throw new OBDAException(e);
		}
		
		// Obtain the query signature
		List<String> signatureContainer = new LinkedList<String>();
		getSignature(query, signatureContainer);
		
		
		// Translate the SPARQL algebra to datalog program
		DatalogProgram initialProgram = translateAndPreProcess(query, signatureContainer);
		
		// Perform the query rewriting
		DatalogProgram programAfterRewriting = getRewriting(initialProgram);
		
		// Translate the output datalog program back to SPARQL string
		// TODO Re-enable the prefix manager using Sesame prefix manager
//		PrefixManager prefixManager = new SparqlPrefixManager(query.getPrefixMapping());
		DatalogToSparqlTranslator datalogTranslator = new DatalogToSparqlTranslator();
		return datalogTranslator.translate(programAfterRewriting);
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getRewriting(ParsedQuery query, List<String> signature) throws Exception {
		// TODO FIX to limit to SPARQL input and output

		DatalogProgram program = translateAndPreProcess(query, signature);

		OBDAQuery rewriting = rewriter.rewrite(program);
		return DatalogProgramRenderer.encode((DatalogProgram) rewriting);
	}
	
	

	/**
	 * Returns the final rewriting of the given query
	 */
	public DatalogProgram getRewriting(DatalogProgram program) throws OBDAException {
		OBDAQuery rewriting = rewriter.rewrite(program);
		return (DatalogProgram) rewriting;
	}

	/***
	 * Returns the SQL query for a given SPARQL query. In the process, the
	 * signature of the query will be set into the query container and the jena
	 * Query object created (or cached) will be set as jenaQueryContainer[0] so
	 * that it can be used in other process after getUnfolding.
	 * 
	 * If the queyr is not already cached, it will be cached in this process.
	 * 
	 * @param strquery
	 * @param signatureContainer
	 * @param jenaQueryContainer
	 * @return
	 * @throws Exception
	 */
	public String getUnfolding(String strquery) throws Exception {
		String sql = "";
		Map<Predicate, List<CQIE>> rulesIndex = questInstance.sigmaRulesIndex;

		List<String> signatureContainer = new LinkedList<String>();
		//Query query;
		ParsedQuery query;
		
		// Check the cache first if the system has processed the query string
		// before
		if (querycache.containsKey(strquery)) {
			// Obtain immediately the SQL string from cache
			sql = querycache.get(strquery);

			signatureContainer = signaturecache.get(strquery);
			query = sesameQueryCache.get(strquery);

		} else {
			QueryParser qp = QueryParserUtil.createParser(QueryLanguage.SPARQL);
			query = qp.parseQuery(strquery, null); // base URI is null
			//getSignature(query, signatureContainer);
			getSignature(query, signatureContainer);

			sesameQueryCache.put(strquery, query);
			signaturecache.put(strquery, signatureContainer);

			//DatalogProgram program_test = translateAndPreProcess(query, signatureContainer);
			DatalogProgram program = translateAndPreProcess(query, signatureContainer);
			try {
				// log.debug("Input query:\n{}", strquery);

				for (CQIE q : program.getRules()) {
					DatalogNormalizer.unfoldJoinTrees(q, false);
				}

				log.debug("Normalized program: \n{}", program);

				/*
				 * Empty unfolding, constructing an empty result set
				 */
				if (program.getRules().size() < 1) {
					throw new OBDAException("Error, the translation of the query generated 0 rules. This is not possible for any SELECT query (other queries are not supported by the translator).");
				}

				/*
				 * Query optimization w.r.t Sigma rules
				 */
				optimizeQueryWithSigmaRules(program, rulesIndex);

			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);
				OBDAException obdaException = new OBDAException(e1);
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}

			log.debug("Start the rewriting process...");
			try {
				final long startTime = System.currentTimeMillis();
				programAfterRewriting = getRewriting(program);
				final long endTime = System.currentTimeMillis();
				rewritingTime = endTime - startTime;

				optimizeQueryWithSigmaRules(programAfterRewriting, rulesIndex);

			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);
				OBDAException obdaException = new OBDAException("Error rewriting query. \n" + e1.getMessage());
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}

			try {
				final long startTime = System.currentTimeMillis();
				programAfterUnfolding = getUnfolding(programAfterRewriting);
				final long endTime = System.currentTimeMillis();
				unfoldingTime = endTime - startTime;
			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);
				OBDAException obdaException = new OBDAException("Error unfolding query. \n" + e1.getMessage());
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}

			try {
				sql = getSQL(programAfterUnfolding, signatureContainer);
				// cacheQueryAndPr operties(strquery, sql);
			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);

				OBDAException obdaException = new OBDAException("Error generating SQL. \n" + e1.getMessage());
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}
		}
		querycache.put(strquery, sql);
		return sql;
	}

	/**
	 * 
	 * @param program
	 * @param rules
	 */
	private void optimizeQueryWithSigmaRules(DatalogProgram program, Map<Predicate, List<CQIE>> rulesIndex) {
		List<CQIE> unionOfQueries = new LinkedList<CQIE>(program.getRules());
		// for each rule in the query
		for (int qi = 0; qi < unionOfQueries.size(); qi++) {
			CQIE query = unionOfQueries.get(qi);
			// get query head, body
			Function queryHead = query.getHead();
			List<Function> queryBody = query.getBody();
			// for each atom in query body
			for (int i = 0; i < queryBody.size(); i++) {
				Set<Function> removedAtom = new HashSet<Function>();
				Function atomQuery = queryBody.get(i);
				Predicate predicate = atomQuery.getPredicate();

				// for each tbox rule
				List<CQIE> rules = rulesIndex.get(predicate);
				if (rules == null || rules.isEmpty()) {
					continue;
				}
				for (CQIE rule : rules) {
					// try to unify current query body atom with tbox rule body
					// atom
					rule = DatalogUnfolder.getFreshRule(rule, 4022013); // Random
																		// suffix
																		// number
					Function ruleBody = rule.getBody().get(0);
					Map<Variable, Term> theta = Unifier.getMGU(ruleBody, atomQuery);
					if (theta == null || theta.isEmpty()) {
						continue;
					}
					// if unifiable, apply to head of tbox rule
					Function ruleHead = rule.getHead();
					Function copyRuleHead = (Function) ruleHead.clone();
					Unifier.applyUnifier(copyRuleHead, theta);

					removedAtom.add(copyRuleHead);
				}

				for (int j = 0; j < queryBody.size(); j++) {
					if (j == i) {
						continue;
					}
					Function toRemove = queryBody.get(j);
					if (removedAtom.contains(toRemove)) {
						queryBody.remove(j);
						j -= 1;
						if (j < i) {
							i -= 1;
						}
					}
				}
			}
			// update query datalog program
			unionOfQueries.remove(qi);
			unionOfQueries.add(qi, ofac.getCQIE(queryHead, queryBody));
		}
		program.removeAllRules();
		program.appendRule(unionOfQueries);
	}

	/**
	 * Returns the number of tuples returned by the query
	 */
	public int getTupleCount(String query) throws Exception {

		String unf = getUnfolding(query);
		String newsql = "SELECT count(*) FROM (" + unf + ") t1";
		if (!canceled) {
			ResultSet set = sqlstatement.executeQuery(newsql);
			if (set.next()) {
				return set.getInt(1);
			} else {
				throw new Exception("Tuple count faild due to empty result set.");
			}
		} else {
			throw new Exception("Action canceled.");
		}
	}

	@Override
	public void close() throws OBDAException {
		try {
			if (sqlstatement != null)
				sqlstatement.close();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

//	private void getSignature(Query query, List<String> signatureContainer) {
//		translator.getSignature(query, signatureContainer);
//	}

	private void getSignature(ParsedQuery query, List<String> signatureContainer) {
		translator.getSignature(query, signatureContainer);
	}
	
	@Override
	public void cancel() throws OBDAException {
		try {
			QuestStatement.this.executionthread.cancel();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public int executeUpdate(String query) throws OBDAException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFetchSize() throws OBDAException {
		try {
			return sqlstatement.getFetchSize();
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public int getMaxRows() throws OBDAException {
		try {
			return sqlstatement.getMaxRows();
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void getMoreResults() throws OBDAException {
		try {
			sqlstatement.getMoreResults();
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setFetchSize(int rows) throws OBDAException {
		try {
			sqlstatement.setFetchSize(rows);
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setMaxRows(int max) throws OBDAException {
		try {
			sqlstatement.setMaxRows(max);
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setQueryTimeout(int seconds) throws OBDAException {
		try {
			sqlstatement.setQueryTimeout(seconds);
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public OBDAConnection getConnection() throws OBDAException {
		return conn;
	}

	@Override
	public TupleResultSet getResultSet() throws OBDAException {
		return null;
	}

	@Override
	public int getQueryTimeout() throws OBDAException {
		try {
			return sqlstatement.getQueryTimeout();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public boolean isClosed() throws OBDAException {
		try {
			return sqlstatement.isClosed();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	/***
	 * Inserts a stream of ABox assertions into the repository.
	 * 
	 * @param data
	 * @param recreateIndexes
	 *            Indicates if indexes (if any) should be droped before
	 *            inserting the tuples and recreated afterwards. Note, if no
	 *            index existed before the insert no drop will be done and no
	 *            new index will be created.
	 * @throws SQLException
	 */
	public int insertData(Iterator<Assertion> data, boolean useFile, int commit, int batch) throws SQLException {
		int result = -1;

		EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(data, questInstance.getEquivalenceMap());

		if (!useFile) {

			result = repository.insertData(conn.conn, newData, commit, batch);
		} else {
			try {
				// File temporalFile = new File("quest-copy.tmp");
				// FileOutputStream os = new FileOutputStream(temporalFile);
				result = (int) repository.loadWithFile(conn.conn, newData);
				// os.close();

			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		try {
			questInstance.updateSemanticIndexMappings();
			translator.setTemplateMatcher(questInstance.getUriTemplateMatcher());

		} catch (Exception e) {
			log.error("Error updating semantic index mappings after insert.", e);
		}

		return result;
	}

	/***
	 * As before, but using recreateIndexes = false.
	 * 
	 * @param data
	 * @throws SQLException
	 */
	public int insertData(Iterator<Assertion> data, int commit, int batch) throws SQLException {
		return insertData(data, false, commit, batch);
	}

	public void createIndexes() throws Exception {
		repository.createIndexes(conn.conn);
	}

	public void dropIndexes() throws Exception {
		repository.dropIndexes(conn.conn);
	}

	public boolean isIndexed() {
		if (repository == null)
			return false;
		return repository.isIndexed(conn.conn);
	}

	public void dropRepository() throws SQLException {
		if (repository == null)
			return;
		repository.dropDBSchema(conn.conn);
	}

	/***
	 * In an ABox store (classic) this methods triggers the generation of the
	 * schema and the insertion of the metadata.
	 * 
	 * @throws SQLException
	 */
	public void createDB() throws SQLException {
		repository.createDBSchema(conn.conn, false);
		repository.insertMetadata(conn.conn);
	}

	public void analyze() throws Exception {
		repository.collectStatistics(conn.conn);
	}

	/*
	 * Methods for getting the benchmark parameters
	 */
	public long getQueryProcessingTime() {
		return queryProcessingTime;
	}

	public long getRewritingTime() {
		return rewritingTime;
	}

	public long getUnfoldingTime() {
		return unfoldingTime;
	}

	public int getUCQSizeAfterRewriting() {
		return programAfterRewriting.getRules().size();
	}

	public int getMinQuerySizeAfterRewriting() {
		int toReturn = Integer.MAX_VALUE;
		List<CQIE> rules = programAfterRewriting.getRules();
		for (CQIE rule : rules) {
			int querySize = getBodySize(rule.getBody());
			if (querySize < toReturn) {
				toReturn = querySize;
			}
		}
		return toReturn;
	}

	public int getMaxQuerySizeAfterRewriting() {
		int toReturn = Integer.MIN_VALUE;
		List<CQIE> rules = programAfterRewriting.getRules();
		for (CQIE rule : rules) {
			int querySize = getBodySize(rule.getBody());
			if (querySize > toReturn) {
				toReturn = querySize;
			}
		}
		return toReturn;
	}

	public int getUCQSizeAfterUnfolding() {
		return programAfterUnfolding.getRules().size();
	}

	public int getMinQuerySizeAfterUnfolding() {
		int toReturn = Integer.MAX_VALUE;
		List<CQIE> rules = programAfterUnfolding.getRules();
		for (CQIE rule : rules) {
			int querySize = getBodySize(rule.getBody());
			if (querySize < toReturn) {
				toReturn = querySize;
			}
		}
		return (toReturn == Integer.MAX_VALUE) ? 0 : toReturn;
	}

	public int getMaxQuerySizeAfterUnfolding() {
		int toReturn = Integer.MIN_VALUE;
		List<CQIE> rules = programAfterUnfolding.getRules();
		for (CQIE rule : rules) {
			int querySize = getBodySize(rule.getBody());
			if (querySize > toReturn) {
				toReturn = querySize;
			}
		}
		return (toReturn == Integer.MIN_VALUE) ? 0 : toReturn;
	}

	public String getSqlString(String sparqlString) {
		return querycache.get(sparqlString);
	}

	private int getBodySize(List<? extends Function> atoms) {
		int counter = 0;
		for (Function atom : atoms) {
			Predicate predicate = atom.getPredicate();
			if (!(predicate instanceof BuiltinPredicate)) {
				counter++;
			}
		}
		return counter;
	}
}
