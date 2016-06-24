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

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.execution.NativeQueryExecutionException;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.inf.ontop.owlrefplatform.core.resultset.*;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.ParsedQuery;
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
public abstract class QuestStatement implements OBDAStatement {

	public final IQuest questInstance;
	private final QuestQueryProcessor engine;
	private final OBDAConnection conn;


	private QueryExecutionThread executionThread;
	private boolean canceled = false;


	private static final Logger log = LoggerFactory.getLogger(QuestStatement.class);


	public QuestStatement(IQuest questInstance, OBDAConnection conn) {
		this.questInstance = questInstance;
		this.engine = questInstance.getEngine();
		this.conn = conn;
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

		private ResultSet resultSet;	  // only for SELECT and ASK queries
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

		public ResultSet getResultSet() {
			return resultSet;
		}

		public void cancel() throws NativeQueryExecutionException {
			canceled = true;
			if (!executingTargetQuery) {
				this.stop();
			} else {
				cancelTargetQueryStatement();
			}
		}

		@Override
		public void run() {
			try {
				/**
				 * Executes the target query.
				 */
				log.debug("Executing the query and get the result...");
					try {
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
				} catch (NativeQueryExecutionException e) {
						exception = e;
						log.error(e.getMessage(), e);

						throw new OBDAException("Error executing the target query: \n" + e.getMessage() + "\nTarget query:\n " + executableQuery, e);
				}
				log.debug("Execution finished.\n");
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
			throws NativeQueryExecutionException, OBDAException;

	/**
	 * TODO: describe
	 */
	protected abstract TupleResultSet executeBooleanQuery(ExecutableQuery executableQuery) throws NativeQueryExecutionException;

	/**
	 * TODO: describe
	 */
	protected GraphResultSet executeDescribeQuery(ExecutableQuery executableQuery) throws NativeQueryExecutionException, OBDAException {
		return executeGraphQuery(executableQuery, true);
	}

	/**
	 * TODO: describe
	 */
	protected GraphResultSet executeConstructQuery(ExecutableQuery executableQuery) throws NativeQueryExecutionException, OBDAException {
		return executeGraphQuery(executableQuery, false);
	}

	/**
	 * TODO: describe
	 */
	protected abstract GraphResultSet executeGraphQuery(ExecutableQuery executableQuery, boolean collectResults) throws NativeQueryExecutionException, OBDAException;

	/**
	 * Cancel the processing of the target query.
	 */
	protected abstract void cancelTargetQueryStatement() throws NativeQueryExecutionException;

	/**
	 * Calls the necessary tuple or graph query execution Implements describe
	 * uri or var logic Returns the result set for the given query
	 */
	@Override
	public ResultSet execute(String strquery) throws OBDAException {
		if (strquery.isEmpty()) {
			throw new OBDAException("Cannot execute an empty query");
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
					ResultSet resultSet = executeTupleQuery(sel, engine.getParsedQuery(sel), QueryType.SELECT);
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
		}
		catch (MalformedQueryException e) {
			throw new OBDAException(e);
			
		}
		throw new OBDAException("Error, the result set was null");
	}
	



	/**
	 * The method executes select or ask queries by starting a new quest
	 * execution thread
	 * 
	 * @param strquery
	 *            the select or ask query string
	 * @param type  (SELECT or ASK)
	 * @return the obtained TupleResultSet result
	 * @throws OBDAException
	 */
	private ResultSet executeTupleQuery(String strquery, ParsedQuery pq, QueryType type) throws OBDAException {

		log.debug("Executing SPARQL query: \n{}", strquery);

		return executeInThread(pq, type, Optional.empty());
	}

	private ResultSet executeGraphQuery(String strquery, QueryType type) throws OBDAException {
		
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
			throw new OBDAException(e);
		}
	}
	
	

	/**
	 * Internal method to start a new query execution thread type defines the
	 * query type SELECT, ASK, CONSTRUCT, or DESCRIBE
	 */
	private ResultSet executeInThread(ParsedQuery pq, QueryType type, Optional<SesameConstructTemplate> templ) throws OBDAException {
		CountDownLatch monitor = new CountDownLatch(1);
		String sql = engine.translateIntoNativeQuery(pq);
		List<String> signature = engine.getQuerySignature(pq);
		QueryExecutionThread executionthread = new QueryExecutionThread(sql, signature, type, templ, monitor);
		this.executionThread = executionthread;
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

	
	@Override
	public void cancel() throws OBDAException {
		canceled = true;
		try {
			QuestStatement.this.executionThread.cancel();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public TupleResultSet getResultSet() throws OBDAException {
		return null;
	}


	@Override
	public String getSPARQLRewriting(String query) throws OBDAException {
		return engine.getSPARQLRewriting(query);
	}
	
}
