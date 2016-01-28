package it.unibz.krdb.obda.owlrefplatform.core;

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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.*;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SesameConstructTemplate;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.ParsedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * The obda statement provides the implementations necessary to query the
 * reformulation platform reasoner from outside, i.e. Protege
 */
public class QuestStatement implements OBDAStatement {

	public final Quest questInstance;
	private final QuestQueryProcessor engine;
	private final QuestConnection conn;
	private final Statement sqlStatement;


	private QueryExecutionThread executionThread;
	private boolean canceled = false;
	
	
	private static final Logger log = LoggerFactory.getLogger(QuestStatement.class);


	public QuestStatement(Quest questInstance, QuestConnection conn, Statement st) {
		this.questInstance = questInstance;
		this.engine = this.questInstance.getEngine();
		this.conn = conn;
		this.sqlStatement = st;
	}

	private enum QueryType {
		SELECT,
		ASK,  
		CONSTRUCT,
		DESCRIBE
	}

	private class QueryExecutionThread extends Thread {

		private final CountDownLatch monitor;
		private final String sql;
		private final List<String> signature;
		private final QueryType type;
		private final SesameConstructTemplate templ; // only for CONSTRUCT and DESCRIBE queries
		
		private TupleResultSet tupleResult;	  // only for SELECT and ASK queries
		private GraphResultSet graphResult;   // only for CONSTRUCT and DESCRIBE queries
		private Exception exception = null;
		private boolean executingSQL = false;

		public QueryExecutionThread(String sql, List<String> signature, QueryType type, SesameConstructTemplate templ, CountDownLatch monitor) {
			this.monitor = monitor;
			this.sql = sql;
			this.signature = signature;
			this.templ = templ;
			this.type = type;
		}

		public boolean errorStatus() {
			return exception != null;
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
				sqlStatement.cancel();
			}
		}

		@Override
		public void run() {
			try {
				// Obtaining the query from the cache
				 
				log.debug("Executing the SQL query and get the result...");
				if (sql.equals("")) {
					if (type != QueryType.ASK) 
						tupleResult = new EmptyTupleResultSet(signature, QuestStatement.this);
					else
						tupleResult = new BooleanResultSet(false, QuestStatement.this);
				} 
				else {
					try {
//                        FOR debugging H2 in-memory database
//                        try {
//                            org.h2.tools.Server.startWebServer(conn.getConnection());
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                        }
						// Execute the SQL query string
						executingSQL = true;
						java.sql.ResultSet set = sqlStatement.executeQuery(sql);

						// Store the SQL result to application result set.
						switch (type) {
						case SELECT:
							if (questInstance.hasDistinctResultSet()) 
								tupleResult = new QuestDistinctTupleResultSet(set, signature, QuestStatement.this);
							else 
								tupleResult = new QuestTupleResultSet(set, signature, QuestStatement.this);
							break;
						
						case ASK:
							tupleResult = new BooleanResultSet(set, QuestStatement.this);
							break;
						
						case CONSTRUCT:
							TupleResultSet tuples = new QuestTupleResultSet(set, signature, QuestStatement.this);
							graphResult = new QuestGraphResultSet(tuples, templ, false);
							break;
							
						case DESCRIBE:
							tuples = new QuestTupleResultSet(set, signature, QuestStatement.this);
							graphResult = new QuestGraphResultSet(tuples, templ, true);
							break;
						}
					} 
					catch (SQLException e) {
						exception = e;
						log.error(e.getMessage(), e);
						throw new OBDAException("Error executing SQL query: \n" + e.getMessage() + "\nSQL query:\n " + sql, e);
					}
				}
				log.debug("Execution finished.\n");
			} 
			catch (Exception e) {
				e.printStackTrace();
				exception = e;
				log.error(e.getMessage(), e);
			} 
			finally {
				monitor.countDown();
			}
		}
	}

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
				TupleResultSet executedQuery = executeTupleQuery(strquery, pq, QueryType.SELECT);
				return executedQuery;
			} 
			else if (SPARQLQueryUtility.isAskQuery(pq)) {
				TupleResultSet executedQuery = executeTupleQuery(strquery, pq, QueryType.ASK);
				return executedQuery;
			} 
			else if (SPARQLQueryUtility.isConstructQuery(pq)) {
				GraphResultSet executedGraphQuery = executeGraphQuery(strquery, QueryType.CONSTRUCT);
				return executedGraphQuery;	
			} 
			else if (SPARQLQueryUtility.isDescribeQuery(pq)) {
				// create list of URI constants we want to describe
				List<String> constants = new LinkedList<>();
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
					GraphResultSet set = executeGraphQuery(str, QueryType.DESCRIBE);
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
					GraphResultSet set = executeGraphQuery(str, QueryType.DESCRIBE);
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
	private TupleResultSet executeTupleQuery(String strquery, ParsedQuery pq, QueryType type) throws OBDAException {

		log.debug("Executing SPARQL query: \n{}", strquery);

		QueryExecutionThread executionthread = startExecute(pq, type, null);
		TupleResultSet result = executionthread.getTupleResult();
		if (result == null)
			throw new RuntimeException("Error, the result set was null");

		return result;
	}

	private GraphResultSet executeGraphQuery(String strquery, QueryType type) throws OBDAException {
		
		log.debug("Executing SPARQL query: \n{}", strquery);
		
		try {
			// Here we need to get the template for the CONSTRUCT query results
			SesameConstructTemplate templ = new SesameConstructTemplate(strquery);
			String query = SPARQLQueryUtility.getSelectFromConstruct(strquery);
			ParsedQuery pq = engine.getParsedQuery(query);
			
			QueryExecutionThread executionthread = startExecute(pq, type, templ);
			GraphResultSet executedGraphQuery = executionthread.getGraphResult();
			return executedGraphQuery;
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
	private QueryExecutionThread startExecute(ParsedQuery pq, QueryType type, SesameConstructTemplate templ) throws OBDAException {
		CountDownLatch monitor = new CountDownLatch(1);
		String sql = engine.getSQL(pq);
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
		return executionthread;
	}

	


	/**
	 * Returns the number of tuples returned by the query
	 */
	public long getTupleCount(String query) throws Exception {

		ParsedQuery pq = engine.getParsedQuery(query); 
		String unf = engine.getSQL(pq);
		String newsql = "SELECT count(*) FROM (" + unf + ") t1";
		if (!canceled) {
			java.sql.ResultSet set = sqlStatement.executeQuery(newsql);
			if (set.next()) {
				return set.getLong(1);
			} else {
				throw new Exception("Tuple count failed due to empty result set.");
			}
		} else {
			throw new Exception("Action canceled.");
		}
	}

	@Override
	public void close() throws OBDAException {
		try {
			if (sqlStatement != null)
				sqlStatement.close();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
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
	public int getFetchSize() throws OBDAException {
		try {
			return sqlStatement.getFetchSize();
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public int getMaxRows() throws OBDAException {
		try {
			return sqlStatement.getMaxRows();
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void getMoreResults() throws OBDAException {
		try {
			sqlStatement.getMoreResults();
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setFetchSize(int rows) throws OBDAException {
		try {
			sqlStatement.setFetchSize(rows);
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setMaxRows(int max) throws OBDAException {
		try {
			sqlStatement.setMaxRows(max);
		} catch (Exception e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setQueryTimeout(int seconds) throws OBDAException {
		try {
			sqlStatement.setQueryTimeout(seconds);
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public TupleResultSet getResultSet() throws OBDAException {
		return null;
	}

	@Override
	public int getQueryTimeout() throws OBDAException {
		try {
			return sqlStatement.getQueryTimeout();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public boolean isClosed() throws OBDAException {
		try {
			return sqlStatement.isClosed();
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}


	/***
	 * Inserts a stream of ABox assertions into the repository.
	 * 
	 * @param data
	 * 
	 * @throws SQLException
	 */
	public int insertData(Iterator<Assertion> data,  int commit, int batch) throws SQLException {
		int result = questInstance.getSemanticIndexRepository().insertData(conn.getConnection(), data, commit, batch);
		return result;
	}




	@Override
	public String getSPARQLRewriting(String query) throws OBDAException {
		return engine.getSPARQLRewriting(query);
	}
	
}
