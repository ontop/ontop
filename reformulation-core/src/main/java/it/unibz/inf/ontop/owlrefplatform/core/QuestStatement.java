package it.unibz.inf.ontop.owlrefplatform.core;

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

import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.inf.ontop.owlrefplatform.core.resultset.EmptyTupleResultSet;
import it.unibz.inf.ontop.owlrefplatform.core.resultset.QuestTupleResultSet;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import it.unibz.inf.ontop.reformulation.OBDAQueryProcessor;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;


/**
 * Abstract class for QuestStatement.
 *
 * TODO: rename it (not now) AbstractQuestStatement.
 */
public abstract class QuestStatement implements OntopStatement {

	private final OBDAQueryProcessor engine;

	private QueryExecutionThread executionThread;
	private boolean canceled = false;


	private static final Logger log = LoggerFactory.getLogger(QuestStatement.class);


	public QuestStatement(OBDAQueryProcessor queryProcessor) {
		this.engine = queryProcessor;
	}

	private enum QueryType {
		SELECT,
		ASK,  
		CONSTRUCT,
		DESCRIBE
	}

	private class QueryExecutionThread extends Thread {

		private final CountDownLatch monitor;
		private final QueryType queryType;
		private final Optional<SesameConstructTemplate> templ; // only for CONSTRUCT and DESCRIBE queries
		private final ExecutableQuery executableQuery;
		private final boolean doDistinctPostProcessing;

		private OBDAResultSet resultSet;	  // only for SELECT and ASK queries
		private Exception exception;
		private boolean executingTargetQuery;

		public QueryExecutionThread(ExecutableQuery executableQuery, QueryType queryType,
									Optional<SesameConstructTemplate> templ, CountDownLatch monitor,
									boolean doDistinctPostProcessing) {
			this.executableQuery = executableQuery;
			this.monitor = monitor;
			this.templ = templ;
			this.queryType = queryType;
			this.doDistinctPostProcessing = doDistinctPostProcessing;
			this.exception = null;
			this.executingTargetQuery = false;
		}

		public boolean errorStatus() {
			return exception != null;
		}

		public Exception getException() {
			return exception;
		}

		public OBDAResultSet getResultSet() {
			return resultSet;
		}

		public void cancel() throws OntopQueryEvaluationException {
			canceled = true;
			if (!executingTargetQuery) {
				this.stop();
			} else {
				cancelExecution();
			}
		}

		@Override
		public void run() {
			//                        FOR debugging H2 in-memory database
//			try {
//				org.h2.tools.Server.startWebServer(((QuestConnection)conn).getSQLConnection());
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
			try {
				/**
				 * Executes the target query.
				 */
				log.debug("Executing the query and get the result...");
				executingTargetQuery = true;
				switch (queryType) {
					case ASK:
						resultSet = executeBooleanQuery(executableQuery);
						break;
					case SELECT:
						resultSet = executeSelectQuery(executableQuery, doDistinctPostProcessing);
						break;
					case CONSTRUCT:
						resultSet = executeConstructQuery(executableQuery);
						break;
					case DESCRIBE:
						resultSet = executeDescribeQuery(executableQuery);
						break;
					}
				log.debug("Execution finished.\n");
				/*
				 * TODO: re-handle the timeout exception.
				 */
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
				log.error(e.getMessage(), e);
			} finally {
				monitor.countDown();
			}
		}
	}


	/**
	 * TODO: describe
	 */
	protected abstract TupleResultSet executeSelectQuery(ExecutableQuery executableQuery, boolean doDistinctPostProcessing)
			throws OntopQueryEvaluationException;

	/**
	 * TODO: describe
	 */
	protected abstract TupleResultSet executeBooleanQuery(ExecutableQuery executableQuery) throws OntopQueryEvaluationException;

	/**
	 * TODO: describe
	 */
	protected GraphResultSet executeDescribeQuery(ExecutableQuery executableQuery) throws OntopQueryEvaluationException {
		return executeGraphQuery(executableQuery, true);
	}

	/**
	 * TODO: describe
	 */
	protected GraphResultSet executeConstructQuery(ExecutableQuery executableQuery) throws OntopQueryEvaluationException {
		return executeGraphQuery(executableQuery, false);
	}

	/**
	 * TODO: describe
	 */
	protected abstract GraphResultSet executeGraphQuery(ExecutableQuery executableQuery, boolean collectResults)
			throws OntopQueryEvaluationException;

	/**
	 * Cancel the processing of the target query.
	 */
	protected abstract void cancelExecution() throws OntopQueryEvaluationException;

	/**
	 * Calls the necessary tuple or graph query execution Implements describe
	 * uri or var logic Returns the result set for the given query
	 */
	@Override
	public OBDAResultSet execute(String strquery) throws OntopQueryEvaluationException, OntopReformulationException {
		if (strquery.isEmpty()) {
			throw new OntopInvalidInputQueryException("Cannot process an empty query");
		}
		try {
			ParsedQuery pq = engine.getParsedQuery(strquery);
			if (SPARQLQueryUtility.isSelectQuery(pq)) {
				return executeTupleQuery(strquery, pq, QueryType.SELECT);
			} 
			else if (SPARQLQueryUtility.isAskQuery(pq)) {
				return executeTupleQuery(strquery, pq, QueryType.ASK);
			} 
			else if (SPARQLQueryUtility.isConstructQuery(pq)) {
				return executeGraphQuery(strquery, QueryType.CONSTRUCT);
			} 
			else if (SPARQLQueryUtility.isDescribeQuery(pq)) {
				// create list of URI constants we want to describe
				List<String> constants = new ArrayList<>();
				if (SPARQLQueryUtility.isVarDescribe(strquery)) {
					// if describe ?var, we have to do select distinct ?var first
					String sel = SPARQLQueryUtility.getSelectVarDescribe(strquery);
					OBDAResultSet resultSet = executeTupleQuery(sel, engine.getParsedQuery(sel), QueryType.SELECT);
					if (resultSet instanceof EmptyTupleResultSet)
						return null;
					else if (resultSet instanceof QuestTupleResultSet) {
						QuestTupleResultSet res = (QuestTupleResultSet) resultSet;
						while (res.nextRow()) {
							Constant constant = res.getConstant(1);
							if (constant instanceof URIConstant) {
								// collect constants in list
								constants.add(((URIConstant)constant).getURI());
							}
						}
					}
				} 
				else if (SPARQLQueryUtility.isURIDescribe(strquery)) {
					// DESCRIBE <uri> gives direct results, so we put the
					// <uri> constant directly in the list of constants
					try {
						constants.add(SPARQLQueryUtility.getDescribeURI(strquery));
					} catch (MalformedQueryException e) {
						e.printStackTrace();
					}
				}

				GraphResultSet describeResultSet = null;
				// execute describe <uriconst> in subject position
				for (String constant : constants) {
					// for each constant we execute a construct with
					// the uri as subject, and collect the results
					String str = SPARQLQueryUtility.getConstructSubjQuery(constant);
					GraphResultSet set = (GraphResultSet) executeGraphQuery(str, QueryType.DESCRIBE);
					if (describeResultSet == null) { // just for the first time
						describeResultSet = set;	
					} 
					else if (set != null) {
						// 2nd and manyth times execute, but collect result into one object
						while (set.hasNext()) 
							describeResultSet.addNewResultSet(set.next());
					}
				}
				// execute describe <uriconst> in object position
				for (String constant : constants) {
					String str = SPARQLQueryUtility.getConstructObjQuery(constant);
					GraphResultSet set = (GraphResultSet) executeGraphQuery(str, QueryType.DESCRIBE);
					if (describeResultSet == null) { // just for the first time
						describeResultSet = set;
					} 
					else if (set != null) {
						while (set.hasNext()) 
							describeResultSet.addNewResultSet(set.next());
					}
				}
				return describeResultSet;
			}
			else {
				throw new OntopInvalidInputQueryException("Unsupported query type: " + strquery);
			}
		}
		catch (MalformedQueryException e) {
			throw new OntopInvalidInputQueryException(e.getMessage());
			
		}
	}
	



	/**
	 * The method executes select or ask queries by starting a new quest
	 * execution thread
	 * 
	 * @param strquery
	 *            the select or ask query string
	 * @param type  (SELECT or ASK)
	 * @return the obtained TupleResultSet result
	 */
	private OBDAResultSet executeTupleQuery(String strquery, ParsedQuery pq, QueryType type)
			throws OntopQueryEvaluationException, OntopReformulationException {

		log.debug("Executing SPARQL query: \n{}", strquery);

		return executeInThread(pq, type, Optional.empty());
	}

	private OBDAResultSet executeGraphQuery(String strquery, QueryType type)
			throws OntopQueryEvaluationException, OntopReformulationException {
		
		log.debug("Executing SPARQL query: \n{}", strquery);
		
		try {
			// Here we need to get the template for the CONSTRUCT query results
			SesameConstructTemplate templ = new SesameConstructTemplate(strquery);
			String query = SPARQLQueryUtility.getSelectFromConstruct(strquery);
			ParsedQuery pq = engine.getParsedQuery(query);
			
			return executeInThread(pq, type, Optional.of(templ));
		} 
		catch (MalformedQueryException e) {
			e.printStackTrace();
			throw new OntopInvalidInputQueryException(e.getMessage());
		}
	}
	
	

	/**
	 * Internal method to start a new query execution thread type defines the
	 * query type SELECT, ASK, CONSTRUCT, or DESCRIBE
	 */
	private OBDAResultSet executeInThread(ParsedQuery pq, QueryType type, Optional<SesameConstructTemplate> templ)
			throws OntopQueryAnsweringException {
		CountDownLatch monitor = new CountDownLatch(1);
		ExecutableQuery executableQuery = engine.translateIntoNativeQuery(pq, templ);
		QueryExecutionThread executionthread = new QueryExecutionThread(executableQuery, type, templ, monitor,
				engine.hasDistinctResultSet());
		this.executionThread = executionthread;
		executionthread.start();
		try {
			monitor.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (executionthread.errorStatus()) {
			OntopQueryAnsweringException ex = new OntopQueryAnsweringException(executionthread.getException());
			ex.setStackTrace(executionthread.getStackTrace());
			throw ex;
		}

		if (canceled == true) {
			canceled = false;
			throw new OntopQueryEvaluationException("Query execution was cancelled");
		}
		return executionthread.getResultSet();
	}

	
	@Override
	public void cancel() throws OntopConnectionException {
		canceled = true;
		try {
			QuestStatement.this.executionThread.cancel();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

	/**
	 * Called to check whether the statement was cancelled on purpose
	 */
	public boolean isCanceled(){
		return canceled;
	}

	@Override
	public String getRewriting(String query) {
		ParsedQuery pq = engine.getParsedQuery(query);
		return engine.getRewriting(pq);
	}


	@Override
	public ExecutableQuery getExecutableQuery(String sparqlQuery)
			throws OntopReformulationException {
		try {
			ParsedQuery sparqlTree = engine.getParsedQuery(sparqlQuery);
			// TODO: handle the construction template correctly
			return engine.translateIntoNativeQuery(sparqlTree, Optional.empty());
		} catch (MalformedQueryException e) {
			throw new OntopInvalidInputQueryException(e);
		}
	}

}
