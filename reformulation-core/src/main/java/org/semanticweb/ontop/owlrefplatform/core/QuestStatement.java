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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.DatalogNormalizer;
import org.semanticweb.ontop.owlrefplatform.core.execution.TargetQueryExecutionException;
import org.semanticweb.ontop.owlrefplatform.core.optimization.BasicJoinOptimizer;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import org.semanticweb.ontop.owlrefplatform.core.resultset.EmptyQueryResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestGraphResultSet;
import org.semanticweb.ontop.owlrefplatform.core.resultset.QuestResultset;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.owlrefplatform.core.translator.DatalogToSparqlTranslator;
import org.semanticweb.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import org.semanticweb.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLift;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.UnionNode;
import org.semanticweb.ontop.pivotalrepr.datalog.DatalogProgram2QueryConverter;
import org.semanticweb.ontop.renderer.DatalogProgramRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import javax.annotation.Nullable;


/**
 * Abstract class for QuestStatement.
 *
 * TODO: rename it (not now) AbstractQuestStatement.
 */
public abstract class QuestStatement implements IQuestStatement {

	private enum QueryType {
		ASK,
		CONSTRUCT,
		DESCRIBE,
		SELECT
	}

	private NativeQueryGenerator queryGenerator = null;

	private boolean canceled = false;

	private boolean queryIsParsed = false;

	private ParsedQuery parsedQ = null;

	private OBDAConnection conn;

	private final IQuest questInstance;

	private static Logger log = LoggerFactory.getLogger(QuestStatement.class);

	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	private QueryExecutionThread executionthread;

	private DatalogProgram programAfterRewriting;

	private IntermediateQuery queryAfterUnfolding;

	/**
	 * Index function symbols (predicate) that have multiple types.
	 * Such predicates should be managed carefully. See DatalogUnfolder.pushTypes() for more details.
	 *
	 * Not that this index may be modified by the DatalogUnfolder.
	 */
	private Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex = ArrayListMultimap.create();
	private final QueryCache queryCache;

	/*
	 * For benchmark purpose
	 */
	private long queryProcessingTime = 0;
	private long rewritingTime = 0;
	private long unfoldingTime = 0;


	protected QuestStatement(IQuest questinstance, OBDAConnection conn) {
		this.questInstance = questinstance;
		this.conn = conn;
		this.queryGenerator = questinstance.cloneIfNecessaryNativeQueryGenerator();
		this.queryCache = questinstance.getQueryCache();
	}

	@Override
	public IQuest getQuestInstance() {
		return questInstance;
	}

	/**
	 * TODO: implement it
	 * @return
	 * @throws OBDAException
	 */
	@Override
	public TupleResultSet getResultSet() throws OBDAException {
		return null;
	}

	/**
	 * Execution thread: cancellable.
	 */
	protected class QueryExecutionThread extends Thread {

		/**
		 * Note that this SPARQL query may have been rewritten
		 * (e.g. SPARQL CONSTRUCTs are transformed internally
		 *  into SPARQL SELECT queries)
		 */
		private final String sparqlQuery;
		private final QueryType queryType;
		private final CountDownLatch monitor;
		private Exception exception;
		private boolean error;
		private boolean executingTargetQuery;
		private ResultSet resultSet;
		private SesameConstructTemplate constructTemplate;

		protected QueryExecutionThread(String sparqlQuery, QueryType queryType, CountDownLatch monitor,
									   @Nullable SesameConstructTemplate constructTemplate) {
			this.sparqlQuery = sparqlQuery;
			this.queryType = queryType;
			this.monitor = monitor;
			this.constructTemplate = constructTemplate;
			this.exception = null;
			this.error = false;
			this.resultSet = null;
			this.executingTargetQuery = false;
		}

		public ResultSet getResultSet() {
			return resultSet;
		}

		public boolean errorStatus() {
			return error;
		}

		public Exception getException() {
			return exception;
		}

		public void cancel() throws TargetQueryExecutionException {
			canceled = true;
			if (!executingTargetQuery) {
				this.stop();
			} else {
				cancelTargetQueryStatement();
			}
		}

		@Override
		public void run() {

			log.debug("Executing SPARQL query: \n{}", sparqlQuery);
			try {

				/**
				 * Extracts the target query from the cache or computes it.
				 */
				TargetQuery targetQuery = queryCache.getTargetQuery(sparqlQuery);
				if (targetQuery == null) {
					targetQuery = unfoldAndGenerateTargetQuery(sparqlQuery, constructTemplate);
				}

				/**
				 * Executes the target query.
				 */
				log.debug("Executing the query and get the result...");
				try {
					executingTargetQuery = true;
					switch (queryType) {
						case ASK:
							resultSet = executeBooleanQuery(targetQuery);
							break;
						case SELECT:
							resultSet = executeSelectQuery(targetQuery);
							break;
						case CONSTRUCT:
							resultSet = executeConstructQuery(targetQuery);
							break;
						case DESCRIBE:
							resultSet = executeDescribeQuery(targetQuery);
							break;
					}
				} catch (TargetQueryExecutionException e) {
						exception = e;
						error = true;
						log.error(e.getMessage(), e);

						throw new OBDAException("Error executing the target query: \n" + e.getMessage() + "\nTarget query:\n " + targetQuery, e);
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
	 * TODO: describe
	 */
	protected abstract TupleResultSet executeSelectQuery(TargetQuery targetQuery) throws TargetQueryExecutionException, OBDAException;

	/**
	 * TODO: describe
	 */
	protected abstract TupleResultSet executeBooleanQuery(TargetQuery targetQuery) throws TargetQueryExecutionException;

	/**
	 * TODO: describe
	 */
	protected GraphResultSet executeDescribeQuery(TargetQuery targetQuery) throws TargetQueryExecutionException, OBDAException {
		return executeGraphQuery(targetQuery, true);
	}

	/**
	 * TODO: describe
	 */
	protected GraphResultSet executeConstructQuery(TargetQuery targetQuery) throws TargetQueryExecutionException, OBDAException {
		return executeGraphQuery(targetQuery, false);
	}

	/**
	 * TODO: describe
	 */
	protected abstract GraphResultSet executeGraphQuery(TargetQuery targetQuery, boolean collectResults) throws TargetQueryExecutionException, OBDAException;

	/**
	 * Cancel the processing of the target query.
	 */
	protected abstract void cancelTargetQueryStatement() throws TargetQueryExecutionException;

	/**
	 * Calls the necessary tuple or graph query execution Implements describe
	 * uri or var logic Returns the result set for the given query
	 *
	 * TODO: simplify this method
	 */
	@Override
	public ResultSet execute(String strquery) throws OBDAException {
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
			ResultSet resultSet = executeInThread(strquery, QueryType.SELECT);
			return resultSet;

		} else if (SPARQLQueryUtility.isAskQuery(pq)) {
			parsedQ = pq;
			queryIsParsed = true;
			ResultSet resultSet = executeInThread(strquery, QueryType.ASK);
			return resultSet;
		} else if (SPARQLQueryUtility.isConstructQuery(pq)) {

			SesameConstructTemplate constructTemplate;
			// Here we need to get the template for the CONSTRUCT query results
			try {
				constructTemplate = new SesameConstructTemplate(strquery);
			} catch (MalformedQueryException e) {
				e.printStackTrace();
				constructTemplate = null;
			}
			
			// Here we replace CONSTRUCT query with SELECT query
			String selectSparqlQuery = SPARQLQueryUtility.getSelectFromConstruct(strquery);
			ResultSet resultSet = executeInThread(selectSparqlQuery, QueryType.CONSTRUCT, constructTemplate);
			return resultSet;

		} else if (SPARQLQueryUtility.isDescribeQuery(pq)) {
			// create list of uriconstants we want to describe
			List<String> constants = new ArrayList<String>();
			if (SPARQLQueryUtility.isVarDescribe(strquery)) {
				// if describe ?var, we have to do select distinct ?var first
				String sel = SPARQLQueryUtility.getSelectVarDescribe(strquery);
				ResultSet resultSet = executeInThread(sel, QueryType.SELECT);

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
				SesameConstructTemplate constructTemplate;
				try {
					constructTemplate = new SesameConstructTemplate(str);
				} catch (MalformedQueryException e) {
					e.printStackTrace();
					constructTemplate = null;
				}
				str = SPARQLQueryUtility.getSelectFromConstruct(str);
				ResultSet resultSet = executeInThread(str, QueryType.DESCRIBE, constructTemplate);
				if (describeResultSet == null) {
					// just for the first time
					describeResultSet = (QuestGraphResultSet) resultSet;
				} else {
					// 2nd and manyth times execute, but collect result into one
					// object
					QuestGraphResultSet set = (QuestGraphResultSet) resultSet;
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

				SesameConstructTemplate constructTemplate;
				try {
					constructTemplate = new SesameConstructTemplate(str);
				} catch (MalformedQueryException e) {
					e.printStackTrace();
					constructTemplate = null;
				}
				str = SPARQLQueryUtility.getSelectFromConstruct(str);
				ResultSet resultSet = executeInThread(str, QueryType.DESCRIBE, constructTemplate);
				if (describeResultSet == null) {
					// just for the first time
					describeResultSet = (QuestGraphResultSet) resultSet;
				} else {
					QuestGraphResultSet set = (QuestGraphResultSet) resultSet;
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
	 */
	
	private DatalogProgram translateAndPreProcess(ParsedQuery pq) {
		DatalogProgram program = null;
		try {
			SparqlAlgebraToDatalogTranslator translator = questInstance.getSparqlAlgebraToDatalogTranslator();
			program = translator.translate(pq);

			log.debug("Datalog program translated from the SPARQL query: \n{}", program);

			DatalogUnfolder unfolder = new DatalogUnfolder(program.clone().getRules(), HashMultimap.<Predicate, List<Integer>>create());
			
			multiTypedFunctionSymbolIndex = questInstance.copyMultiTypedFunctionSymbolIndex();

			//Flattening !!
			program = unfolder.unfold(program, OBDAVocabulary.QUEST_QUERY,QuestConstants.BUP,false, multiTypedFunctionSymbolIndex);


			log.debug("Flattened program: \n{}", program);
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



	private IntermediateQuery getUnfolding(DatalogProgram query) throws OBDAException, EmptyQueryException {

		log.debug("Start the partial evaluation process...");

		DatalogUnfolder unfolder = (DatalogUnfolder) questInstance.getQuestUnfolder().getDatalogUnfolder();


//		if (query.getRules().size() > 0) {
//			try {
//				IntermediateQuery intermediateQuery = DatalogProgram2QueryConverter.convertDatalogProgram(query,
//						unfolder.getExtensionalPredicates());
//				log.debug("Initial intermediate query: \n" + intermediateQuery.toString());
//			} catch (DatalogProgram2QueryConverter.InvalidDatalogProgramException e) {
//				throw new OBDAException(e.getLocalizedMessage());
//			}
//		}


		//This instnce of the unfolder is carried from Quest, and contains the mappings.
		DatalogProgram unfolding = unfolder.unfold((DatalogProgram) query, "ans1",QuestConstants.BUP, true,
				multiTypedFunctionSymbolIndex);

		log.debug("Data atoms evaluated: \n{}", unfolding);

		//removeNonAnswerQueries(unfolding);

		//log.debug("After target rules removed: \n{}", unfolding);

		ExpressionEvaluator evaluator = questInstance.getExpressionEvaluator();
		evaluator.evaluateExpressions(unfolding);
		
		/*
			UnionOfSqlQueries ucq = new UnionOfSqlQueries(questInstance.getUnfolder().getCQContainmentCheck());
			for (CQIE cq : unfolding.getRules())
				ucq.add(cq);
			
			List<CQIE> rules = new ArrayList<>(unfolding.getRules());
			unfolding.removeRules(rules); 
			
			for (CQIE cq : ucq.asCQIE()) {
				unfolding.appendRule(cq);
			}
			log.debug("CQC performed ({} rules): \n{}", unfolding.getRules().size(), unfolding);
		 
		 */

		log.debug("Boolean expression evaluated: \n{}", unfolding);

		// PUSH TYPE HERE
		//log.debug("Pushing types...");
		//unfolding = liftTypes(unfolding, multiTypedFunctionSymbolIndex);

		if (unfolding.getRules().size() > 0) {
			try {
				IntermediateQuery intermediateQuery = DatalogProgram2QueryConverter.convertDatalogProgram(
						questInstance.getMetadataForQueryOptimization(), unfolding,
						unfolder.getExtensionalPredicates());
				log.debug("New directly translated intermediate query: \n" + intermediateQuery.toString());

				// BasicTypeLiftOptimizer typeLiftOptimizer = new BasicTypeLiftOptimizer();
				// intermediateQuery = typeLiftOptimizer.optimize(intermediateQuery);

				log.debug("New lifted query: \n" + intermediateQuery.toString());


				BasicJoinOptimizer joinOptimizer = new BasicJoinOptimizer();
				intermediateQuery = joinOptimizer.optimize(intermediateQuery);
				log.debug("New query after join optimization: \n" + intermediateQuery.toString());
				
				return intermediateQuery;
				
			} catch (DatalogProgram2QueryConverter.InvalidDatalogProgramException e) {
				throw new OBDAException(e.getLocalizedMessage());
			}
		}
		else {
			throw new EmptyQueryException();
		}
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
		datalogProgram = ofac.getDatalogProgram(queryModifiers);
		datalogProgram.appendRule(newTypedRules);
		log.debug("Types Pushed: \n{}",datalogProgram);

		return datalogProgram;
	}


	private TargetQuery generateTargetQuery(IntermediateQuery intermediateQuery, ImmutableList<String> signature,
											@Nullable SesameConstructTemplate constructTemplate) throws OBDAException {
		log.debug("Producing the native query string...");

		String nativeQueryString = queryGenerator.generateSourceQuery(intermediateQuery, signature);

		log.debug("Resulting native query: \n{}", nativeQueryString);

		return new TargetQuery(nativeQueryString, signature, constructTemplate);
	}

	/**
	 * Internal method to start a new query execution thread.
	 */
	private ResultSet executeInThread(String sparqlQuery, QueryType queryType, SesameConstructTemplate constructTemplate) throws OBDAException {
		CountDownLatch monitor = new CountDownLatch(1);
		executionthread = new QueryExecutionThread(sparqlQuery, queryType, monitor, constructTemplate);
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

		return executionthread.getResultSet();
	}

	private ResultSet executeInThread(String sparqlQuery, QueryType queryType) throws OBDAException {
		return executeInThread(sparqlQuery, queryType, null);
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
	@Override
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

		SparqlAlgebraToDatalogTranslator translator = questInstance.getSparqlAlgebraToDatalogTranslator();

		// Obtain the query signature
		ImmutableList<String> signatureContainer = translator.getSignature(query);


		//SparqlAlgebraToDatalogTranslator translator = questInstance.getSparqlAlgebraToDatalogTranslator();		
		//List<String> signatureContainer = translator.getSignature(query);
		
		
		// Translate the SPARQL algebra to datalog program
		DatalogProgram initialProgram = translateAndPreProcess(query/*, signatureContainer*/);
		
		// Perform the query rewriting
		DatalogProgram programAfterRewriting = questInstance.getRewriting(initialProgram);
		
		// Translate the output datalog program back to SPARQL string
		// TODO Re-enable the prefix manager using Sesame prefix manager
//		PrefixManager prefixManager = new SparqlPrefixManager(query.getPrefixMapping());
		DatalogToSparqlTranslator datalogTranslator = new DatalogToSparqlTranslator();
		return datalogTranslator.translate(programAfterRewriting);
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getRewriting(ParsedQuery query) throws OBDAException {
		// TODO FIX to limit to SPARQL input and output

		DatalogProgram program = translateAndPreProcess(query);

		DatalogProgram rewriting = questInstance.getRewriting(program);
		return DatalogProgramRenderer.encode(rewriting);
	}

	/***
	 * Returns the target query for a given SPARQL query.
	 *
	 * If the query is not already cached, it will be cached in this process.
	 *
	 */
	@Override
	public TargetQuery unfoldAndGenerateTargetQuery(String sparqlQuery) throws OBDAException {
		return unfoldAndGenerateTargetQuery(sparqlQuery, null);
	}

	protected TargetQuery unfoldAndGenerateTargetQuery(String sparqlQuery, @Nullable SesameConstructTemplate constructTemplate) throws OBDAException {

		/**
		 * Looks for the target query in the cache.
		 * If found, returns it immediately.
		 */
		TargetQuery targetQuery = queryCache.getTargetQuery(sparqlQuery);
		if (targetQuery != null)
			return targetQuery;

		/**
		 * However, computes this target query.
		 */

		ParsedQuery sparqlTree;

		if (!queryIsParsed){
			QueryParser qp = QueryParserUtil.createParser(QueryLanguage.SPARQL);
			try {
				sparqlTree = qp.parseQuery(sparqlQuery, null); // base URI is null
			} catch (MalformedQueryException e) {
				throw new OBDAException(e.getMessage());
			}
			//queryIsParsed = true;
		} else {
			sparqlTree = parsedQ;
			/**
			 * TODO: Why???
			 */
			queryIsParsed = false;
		}

		SparqlAlgebraToDatalogTranslator translator = questInstance.getSparqlAlgebraToDatalogTranslator();
		ImmutableList<String> signatureContainer = translator.getSignature(sparqlTree);


		DatalogProgram program = translateAndPreProcess(sparqlTree);
		try {
			// log.debug("Input query:\n{}", sparqlQuery);

			for (CQIE q : program.getRules()) {
				// ROMAN: unfoldJoinTrees clones the query, so the statement below does not change anything
				DatalogNormalizer.unfoldJoinTrees(q);
			}

			log.debug("Normalized program: \n{}", program);

			/*
			 * Empty unfolding, constructing an empty result set
			 */
			if (program.getRules().size() < 1)
				throw new OBDAException("Error, the translation of the query generated 0 rules. This is not possible for any SELECT query (other queries are not supported by the translator).");

			log.debug("Start the rewriting process...");

			final long startTime0 = System.currentTimeMillis();
			programAfterRewriting = questInstance.getRewriting(program);
			rewritingTime = System.currentTimeMillis() - startTime0;


			final long startTime = System.currentTimeMillis();
			queryAfterUnfolding = getUnfolding(programAfterRewriting);
			unfoldingTime = System.currentTimeMillis() - startTime;


			targetQuery = generateTargetQuery(queryAfterUnfolding, signatureContainer, constructTemplate);
			queryCache.cacheTargetQuery(sparqlQuery, targetQuery);

		}
		/**
		 * TODO: throw an exception when is empty
		 */
		catch (EmptyQueryException e) {
			return new TargetQuery("", signatureContainer, constructTemplate);
		}
		catch (Exception e1) {
			log.debug(e1.getMessage(), e1);

			OBDAException obdaException = new OBDAException("Error rewriting and unfolding into SQL\n" + e1.getMessage());
			obdaException.setStackTrace(e1.getStackTrace());
			throw obdaException;
		}
		return targetQuery;
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
	@Override
	public boolean isCanceled(){
		return canceled;
	}
	
	@Override
	public int executeUpdate(String query) throws OBDAException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * Methods for getting the benchmark parameters
	 */
	@Override
	public long getQueryProcessingTime() {
		return queryProcessingTime;
	}

	@Override
	public long getRewritingTime() {
		return rewritingTime;
	}

	@Override
	public long getUnfoldingTime() {
		return unfoldingTime;
	}

	@Override
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

	@Override
	public int getUCQSizeAfterUnfolding() {
		Optional<QueryNode> optionalviceRoot =
				queryAfterUnfolding.getFirstChild(queryAfterUnfolding.getRootConstructionNode());
		if (optionalviceRoot.isPresent()) {
			QueryNode viceRoot = optionalviceRoot.get();
			if (viceRoot instanceof UnionNode) {
				return queryAfterUnfolding.getChildren(viceRoot).size();
			} else {
				return 1;
			}
		}
		else {
			throw new RuntimeException("Inconsistent intermediate query: must have more than one node");
		}
	}

//	public int getMinQuerySizeAfterUnfolding() {
//		int toReturn = Integer.MAX_VALUE;
//		List<CQIE> rules = queryAfterUnfolding.getRules();
//		for (CQIE rule : rules) {
//			int querySize = getBodySize(rule.getBody());
//			if (querySize < toReturn) {
//				toReturn = querySize;
//			}
//		}
//		return (toReturn == Integer.MAX_VALUE) ? 0 : toReturn;
//	}
//
//	public int getMaxQuerySizeAfterUnfolding() {
//		int toReturn = Integer.MIN_VALUE;
//		List<CQIE> rules = queryAfterUnfolding.getRules();
//		for (CQIE rule : rules) {
//			int querySize = getBodySize(rule.getBody());
//			if (querySize > toReturn) {
//				toReturn = querySize;
//			}
//		}
//		return (toReturn == Integer.MIN_VALUE) ? 0 : toReturn;
//	}

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
