package org.semanticweb.ontop.owlrefplatform.core;

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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.google.common.collect.ArrayListMultimap;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.CQCUtilities;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.DatalogNormalizer;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.PullOutEqualityNormalizer;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.PullOutEqualityNormalizerImpl;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import org.semanticweb.ontop.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.EmptyQueryResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestGraphResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestResultset;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import org.semanticweb.ontop.owlrefplatform.core.translator.DatalogToSparqlTranslator;
import org.semanticweb.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import org.semanticweb.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLift;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.datalog.DatalogProgram2QueryConverter;
import org.semanticweb.ontop.renderer.DatalogProgramRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * The obda statement provides the implementations necessary to query the
 * reformulation platform reasoner from outside, i.e. Protege
 */
public class QuestStatement implements OBDAStatement {

	private static final boolean ALLOW_QUERY_CACHING = true;

	private SQLQueryGenerator querygenerator = null;

	private boolean canceled = false;

	private boolean queryIsParsed = false;

	private ParsedQuery parsedQ = null;

	private Statement sqlstatement;

	private QuestConnection conn;

	private final Quest questInstance;

	private static Logger log = LoggerFactory.getLogger(QuestStatement.class);

	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	private QueryExecutionThread executionthread;

	private DatalogProgram programAfterRewriting;

	private DatalogProgram programAfterUnfolding;

	final Map<String, String> querycache;

	final Map<String, List<String>> signaturecache;

	private Map<String, ParsedQuery> sesameQueryCache;

	final Map<String, Boolean> isbooleancache;

	final Map<String, Boolean> isconstructcache;

	final Map<String, Boolean> isdescribecache;

	final SparqlAlgebraToDatalogTranslator translator;

	/**
	 * Index function symbols (predicate) that have multiple types.
	 * Such predicates should be managed carefully. See DatalogUnfolder.liftTypes() for more details.
	 *
	 * Not that this index may be modified by the DatalogUnfolder.
	 */
	private Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex = ArrayListMultimap.create();

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

		this.conn = conn;
		// this.unfoldingmechanism = questinstance.unfolder;
		this.querygenerator = questinstance.cloneDataSourceQueryGenerator();

		this.sqlstatement = st;
	}

	public Quest getQuestInstance() {
		return questInstance;
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
			canceled = true;
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

						// FOR debugging H2 in-memory database
						// org.h2.tools.Server.startWebServer(conn.conn);

						set = sqlstatement.executeQuery(sql);



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
	public org.semanticweb.ontop.model.ResultSet execute(String strquery) throws OBDAException {
		if (strquery.isEmpty()) {
			throw new OBDAException("Cannot execute an empty query");
		}
		ParsedQuery pq = null;
		QueryParser qp = QueryParserUtil.createParser(QueryLanguage.SPARQL);
		try {
			pq = qp.parseQuery(strquery, null);
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		// encoding ofquery type into numbers
		if (SPARQLQueryUtility.isSelectQuery(pq)) {
			parsedQ = pq;
			queryIsParsed = true;
			TupleResultSet executedQuery = executeTupleQuery(strquery, 1);
			return executedQuery;

		} else if (SPARQLQueryUtility.isAskQuery(pq)) {
			parsedQ = pq;
			queryIsParsed = true;
			TupleResultSet executedQuery = executeTupleQuery(strquery, 2);
			return executedQuery;
		} else if (SPARQLQueryUtility.isConstructQuery(pq)) {

			// Here we need to get the template for the CONSTRUCT query results
			try {
				templ = new SesameConstructTemplate(strquery);
			} catch (MalformedQueryException e) {
				e.printStackTrace();
			}

			// Here we replace CONSTRUCT query with SELECT query
			strquery = SPARQLQueryUtility.getSelectFromConstruct(strquery);
			GraphResultSet executedGraphQuery = executeGraphQuery(strquery, 3);
			return executedGraphQuery;

		} else if (SPARQLQueryUtility.isDescribeQuery(pq)) {
			// create list of uriconstants we want to describe
			List<String> constants = new ArrayList<String>();
			if (SPARQLQueryUtility.isVarDescribe(strquery)) {
				// if describe ?var, we have to do select distinct ?var first
				String sel = SPARQLQueryUtility.getSelectVarDescribe(strquery);
				org.semanticweb.ontop.model.ResultSet resultSet = (org.semanticweb.ontop.model.ResultSet) this.executeTupleQuery(sel, 1);
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
				try {
					constants.add(SPARQLQueryUtility.getDescribeURI(strquery));
				} catch (MalformedQueryException e) {
					e.printStackTrace();
				}
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
	 * @param pq
	 * @param signature
	 * @return
	 */

	private DatalogProgram translateAndPreProcess(ParsedQuery pq, List<String> signature) {
		DatalogProgram program = null;

		try {
			if (questInstance.isSemIdx()) {
				translator.setSemanticIndexUriRef(questInstance.getSemanticIndexRepository().getUriMap());
			}
			program = translator.translate(pq, signature);

			log.debug("Translated query: \n{}", program);

			DatalogUnfolder unfolder = new DatalogUnfolder(program.clone(), new HashMap<Predicate, List<Integer>>());

			if (questInstance.isSemIdx()==true){
				multiTypedFunctionSymbolIndex = questInstance.copyMultiTypedFunctionSymbolIndex();
			}

			//Flattening !!
			program = unfolder.unfold(program, "ans1",QuestConstants.BUP,false, multiTypedFunctionSymbolIndex);


		} catch (Exception e) {
			e.printStackTrace();
			OBDAException ex = new OBDAException(e.getMessage());
			ex.setStackTrace(e.getStackTrace());

			throw e;

		}
		log.debug("Replacing equivalences...");
		program = questInstance.getVocabularyValidator().replaceEquivalences(program);
		return program;

	}



	private DatalogProgram getUnfolding(DatalogProgram query) throws OBDAException {

		log.debug("Start the partial evaluation process...");

		DatalogUnfolder unfolder = (DatalogUnfolder) questInstance.getQuestUnfolder().getDatalogUnfolder();


		if (query.getRules().size() > 0) {
			try {
				IntermediateQuery intermediateQuery = DatalogProgram2QueryConverter.convertDatalogProgram(query,
						unfolder.getExtensionalPredicates());
				log.debug("Initial intermediate query: \n" + intermediateQuery.toString());
			} catch (DatalogProgram2QueryConverter.InvalidDatalogProgramException e) {
				throw new OBDAException(e.getLocalizedMessage());
			}
		}


		//This instnce of the unfolder is carried from Quest, and contains the mappings.
		DatalogProgram unfolding = unfolder.unfold((DatalogProgram) query, "ans1",QuestConstants.BUP, true,
				multiTypedFunctionSymbolIndex);

		log.debug("Partial evaluation: \n{}", unfolding);

		//removeNonAnswerQueries(unfolding);

		//log.debug("After target rules removed: \n{}", unfolding);

		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.setUriTemplateMatcher(questInstance.getUriTemplateMatcher());
		evaluator.evaluateExpressions(unfolding);

		log.debug("Boolean expression evaluated: \n{}", unfolding);

		// PUSH TYPE HERE
		log.debug("Pushing types...");
		unfolding = liftTypes(unfolding, multiTypedFunctionSymbolIndex);

		if (unfolding.getRules().size() > 0) {
			try {
				// TODO: go further with this new data structure
				IntermediateQuery intermediateQuery = DatalogProgram2QueryConverter.convertDatalogProgram(unfolding,
						unfolder.getExtensionalPredicates());
				log.debug("New intermediate query: \n" + intermediateQuery.toString());
			} catch (DatalogProgram2QueryConverter.InvalidDatalogProgramException e) {
				throw new OBDAException(e.getLocalizedMessage());
			}
		}

		log.debug("Pulling out equalities...");

		//TODO: use Guice instead
		PullOutEqualityNormalizer normalizer = new PullOutEqualityNormalizerImpl();

		List<CQIE> normalizedRules = new ArrayList<>();
		for (CQIE rule: unfolding.getRules()) {
			normalizedRules.add(normalizer.normalizeByPullingOutEqualities(rule));
		}

		OBDAQueryModifiers queryModifiers = unfolding.getQueryModifiers();
		unfolding = ofac.getDatalogProgram(queryModifiers, normalizedRules);

		log.debug("\n Partial evaluation ended.\n{}", unfolding);

		return unfolding;
	}

	/**
	 * Lift types of the Datalog program.
	 * Returns a new one.
	 */
	private DatalogProgram liftTypes(DatalogProgram datalogProgram,
									 Multimap<Predicate, Integer> multiTypedFunctionSymbolMap) {

		List<CQIE> newTypedRules = TypeLift.liftTypes(datalogProgram.getRules(), multiTypedFunctionSymbolMap);

		OBDAQueryModifiers queryModifiers = datalogProgram.getQueryModifiers();
		//TODO: can we avoid using this intermediate variable???
		//datalogProgram.removeAllRules();
		datalogProgram = ofac.getDatalogProgram();
		datalogProgram.appendRule(newTypedRules);
		datalogProgram.setQueryModifiers(queryModifiers);
		log.debug("Types Pushed: \n{}",datalogProgram);

		return datalogProgram;
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
		translator.getSignature(query, signatureContainer);


		// Translate the SPARQL algebra to datalog program
		DatalogProgram initialProgram = translateAndPreProcess(query, signatureContainer);

		// Perform the query rewriting
		DatalogProgram programAfterRewriting = questInstance.getRewriter().rewrite(initialProgram);

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

		DatalogProgram rewriting = questInstance.getRewriter().rewrite(program);
		return DatalogProgramRenderer.encode(rewriting);
	}

	/***
	 * Returns the SQL query for a given SPARQL query. In the process, the
	 * signature of the query will be set into the query container and the jena
	 * Query object created (or cached) will be set as jenaQueryContainer[0] so
	 * that it can be used in other process after getUnfolding.
	 *
	 * If the query is not already cached, it will be cached in this process.
	 *
	 * @param strquery
	 * @return
	 * @throws Exception
	 */
	public String getUnfolding(String strquery) throws Exception {
		String sql = "";


		// Check the cache first if the system has processed the query string
		// before
		if (ALLOW_QUERY_CACHING && querycache.containsKey(strquery)) {
			// Obtain immediately the SQL string from cache
			sql = querycache.get(strquery);

			//signatureContainer = signaturecache.get(strquery);
			//query = sesameQueryCache.get(strquery);

		}
		else {


			ParsedQuery query = null;

			if (!queryIsParsed){
				QueryParser qp = QueryParserUtil.createParser(QueryLanguage.SPARQL);
				query = qp.parseQuery(strquery, null); // base URI is null
				//queryIsParsed = true;
			} else {
				query = parsedQ;
				queryIsParsed = false;
			}

			List<String> signatureContainer = new LinkedList<String>();
			translator.getSignature(query, signatureContainer);

			sesameQueryCache.put(strquery, query);
			signaturecache.put(strquery, signatureContainer);

			DatalogProgram program = translateAndPreProcess(query, signatureContainer);
			try {
				// log.debug("Input query:\n{}", strquery);

				for (CQIE q : program.getRules()) {
					// ROMAN: unfoldJoinTrees clones the query, so the statement below does not change anything
					DatalogNormalizer.unfoldJoinTrees(q, false);
				}

				log.debug("Normalized program: \n{}", program);

				/*
				 * Empty unfolding, constructing an empty result set
				 */
				if (program.getRules().size() < 1)
					throw new OBDAException("Error, the translation of the query generated 0 rules. This is not possible for any SELECT query (other queries are not supported by the translator).");

				log.debug("Start the rewriting process...");

				final long startTime0 = System.currentTimeMillis();

				// Query optimization w.r.t Sigma rules
				for (CQIE cq : program.getRules())
					CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), questInstance.getDataDependencies());
				programAfterRewriting = questInstance.getRewriter().rewrite(program);
				for (CQIE cq : program.getRules())
					CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), questInstance.getDataDependencies());

				rewritingTime = System.currentTimeMillis() - startTime0;


				final long startTime = System.currentTimeMillis();
				programAfterUnfolding = getUnfolding(programAfterRewriting);
				unfoldingTime = System.currentTimeMillis() - startTime;


				sql = getSQL(programAfterUnfolding, signatureContainer);
				// cacheQueryAndProperties(strquery, sql);
				querycache.put(strquery, sql);
			}
			catch (Exception e1) {
				log.debug(e1.getMessage(), e1);

				OBDAException obdaException = new OBDAException("Error rewriting and unfolding into SQL\n" + e1.getMessage());
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}
		}
		return sql;
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


	@Override
	public void cancel() throws OBDAException {
		canceled = true;
		try {
			QuestStatement.this.executionthread.cancel();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	/**
	 * Called to check whether the statement was cancelled on purpose
	 * @return
	 */
	public boolean isCanceled(){
		return canceled;
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

	 *            Indicates if indexes (if any) should be dropped before
	 *            inserting the tuples and recreated afterwards. Note, if no
	 *            index existed before the insert no drop will be done and no
	 *            new index will be created.

	 * @throws SQLException
	 */
	public int insertData(Iterator<Assertion> data, boolean useFile, int commit, int batch) throws SQLException {
		int result = -1;

		EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(data, questInstance.getReasoner());

		if (!useFile) {

			result = questInstance.getSemanticIndexRepository().insertData(conn.conn, newData, commit, batch);
		} else {
			try {
				// File temporalFile = new File("quest-copy.tmp");
				// FileOutputStream os = new FileOutputStream(temporalFile);
				result = (int) questInstance.getSemanticIndexRepository().loadWithFile(conn.conn, newData);
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
		questInstance.getSemanticIndexRepository().createIndexes(conn.conn);
	}

	public void dropIndexes() throws Exception {
		questInstance.getSemanticIndexRepository().dropIndexes(conn.conn);
	}

	public boolean isIndexed() {
		if (questInstance.getSemanticIndexRepository() == null)
			return false;
		return questInstance.getSemanticIndexRepository().isIndexed(conn.conn);
	}

	public void dropRepository() throws SQLException {
		if (questInstance.getSemanticIndexRepository() == null)
			return;
		questInstance.getSemanticIndexRepository().dropDBSchema(conn.conn);
	}

	/***
	 * In an ABox store (classic) this methods triggers the generation of the
	 * schema and the insertion of the metadata.
	 *
	 * @throws SQLException
	 */
	public void createDB() throws SQLException {
		questInstance.getSemanticIndexRepository().createDBSchema(conn.conn, false);
		questInstance.getSemanticIndexRepository().insertMetadata(conn.conn);
	}

	public void analyze() throws Exception {
		questInstance.getSemanticIndexRepository().collectStatistics(conn.conn);
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
		if(programAfterRewriting.getRules() != null)
			return programAfterRewriting.getRules().size();
		else return 0;
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
		if( programAfterUnfolding.getRules() != null )
			return programAfterUnfolding.getRules().size();
		else return 0;
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

	private String getSqlString(String sparqlString) {
		return querycache.get(sparqlString);
	}

	private static int getBodySize(List<? extends Function> atoms) {
		int counter = 0;
		for (Function atom : atoms) {
			Predicate predicate = atom.getFunctionSymbol();
			if (!(predicate instanceof BuiltinPredicate)) {
				counter++;
			}
		}
		return counter;
	}
}
