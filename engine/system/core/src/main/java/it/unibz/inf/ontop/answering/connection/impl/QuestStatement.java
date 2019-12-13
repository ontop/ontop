package it.unibz.inf.ontop.answering.connection.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.input.*;
import it.unibz.inf.ontop.answering.resultset.*;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.IRIConstant;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


/**
 * Abstract implementation of OntopStatement.
 *
 */
public abstract class QuestStatement implements OntopStatement {

	private final QueryReformulator engine;
	private final InputQueryFactory inputQueryFactory;

	private QueryExecutionThread executionThread;
	private boolean canceled = false;


	private static final Logger log = LoggerFactory.getLogger(QuestStatement.class);


	public QuestStatement(QueryReformulator queryProcessor, InputQueryFactory inputQueryFactory) {
		this.engine = queryProcessor;
		this.inputQueryFactory = inputQueryFactory;
	}

	/**
	 * TODO: explain
	 */
	@FunctionalInterface
	private interface Evaluator<R extends OBDAResultSet, Q extends InputQuery<R>> {

		R evaluate(Q inputQuery, IQ executableQuery)
				throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException;
	}

	/**
	 * Execution thread
	 */
	private class QueryExecutionThread<R extends OBDAResultSet, Q extends InputQuery<R>> extends Thread {

		private final Q inputQuery;
		private final QuestStatement.Evaluator<R, Q> evaluator;
		private final CountDownLatch monitor;
		private final IQ executableQuery;

		private R resultSet;	  // only for SELECT and ASK queries
		private Exception exception;
		private boolean executingTargetQuery;

		QueryExecutionThread(Q inputQuery, IQ executableQuery, Evaluator<R,Q> evaluator,
							 CountDownLatch monitor) {
			this.executableQuery = executableQuery;
			this.inputQuery = inputQuery;
			this.evaluator = evaluator;
			this.monitor = monitor;
			this.exception = null;
			this.executingTargetQuery = false;
		}

		public boolean errorStatus() {
			return exception != null;
		}

		public Exception getException() {
			return exception;
		}

		public R getResultSet() {
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
				resultSet = evaluator.evaluate(inputQuery, executableQuery);
				log.debug("Execution finished.\n");
				/*
				 * TODO: re-handle the timeout exception.
				 */
			} catch (Exception e) {
				exception = e;
				log.error(e.getMessage(), e);
			} finally {
				monitor.countDown();
			}
		}
	}

	protected abstract TupleResultSet executeSelectQuery(IQ executableQuery)
			throws OntopQueryEvaluationException;

	private TupleResultSet executeSelectQuery(SelectQuery inputQuery, IQ executableQuery)
			throws OntopQueryEvaluationException {
		return executeSelectQuery(executableQuery);
	}

	protected abstract BooleanResultSet executeBooleanQuery(IQ executableQuery)
			throws OntopQueryEvaluationException;

	private BooleanResultSet executeBooleanQuery(AskQuery inputQuery, IQ executableQuery)
			throws OntopQueryEvaluationException {
		return executeBooleanQuery(executableQuery);
	}

	/**
	 * TODO: describe
	 */
	private SimpleGraphResultSet executeDescribeConstructQuery(ConstructQuery constructQuery, IQ executableQuery)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException {
		return executeGraphQuery(constructQuery, executableQuery, true);
	}

	/**
	 * TODO: describe
	 */
	private SimpleGraphResultSet executeConstructQuery(ConstructQuery constructQuery, IQ executableQuery)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException {
		return executeGraphQuery(constructQuery, executableQuery, false);
	}

	/**
	 * TODO: refactor
	 */
	protected abstract SimpleGraphResultSet executeGraphQuery(ConstructQuery query, IQ executableQuery,
															  boolean collectResults)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException;

	/**
	 * Cancel the processing of the target query.
	 */
	protected abstract void cancelExecution() throws OntopQueryEvaluationException;

	/**
	 * Calls the necessary tuple or graph query execution Implements describe
	 * uri or var logic Returns the result set for the given query
	 */
	@Override
	public <R extends OBDAResultSet> R execute(InputQuery<R> inputQuery) throws OntopConnectionException,
            OntopReformulationException, OntopQueryEvaluationException, OntopResultConversionException {
		if (inputQuery instanceof SelectQuery) {
			return (R) executeInThread((SelectQuery) inputQuery, this::executeSelectQuery);
		}
		else if (inputQuery instanceof AskQuery) {
			return (R) executeInThread((AskQuery) inputQuery, this::executeBooleanQuery);
		}
		else if (inputQuery instanceof ConstructQuery) {
			return (R) executeInThread((ConstructQuery) inputQuery, this::executeConstructQuery);
		}
		else if (inputQuery instanceof DescribeQuery) {
			return (R) executeDescribeQuery((DescribeQuery) inputQuery);
		}
		else {
			throw new OntopUnsupportedInputQueryException("Unsupported query type: " + inputQuery);
		}
	}

	/**
	 * TODO: completely refactor this old-way of processing DESCRIBE.
	 *  ---> should be converted into 1 CONSTRUCT query
	 */
	private SimpleGraphResultSet executeDescribeQuery(DescribeQuery inputQuery)
			throws OntopReformulationException, OntopResultConversionException, OntopConnectionException,
			OntopQueryEvaluationException {

		ImmutableSet<String> constants = extractDescribeQueryConstants(inputQuery);

		SimpleGraphResultSet describeResultSet = null;

		try {
			// execute describe <uriconst> in subject position
			for (String constant : constants) {
				// for each constant we execute a construct with
				// the uri as subject, and collect the results
				String str = SPARQLQueryUtility.getConstructSubjQuery(constant);
				ConstructQuery constructQuery = inputQueryFactory.createConstructQuery(str);

				SimpleGraphResultSet set = executeInThread(constructQuery, this::executeDescribeConstructQuery);
				if (describeResultSet == null) { // just for the first time
					describeResultSet = set;
				} else if (set != null) {
					// 2nd and manyth times execute, but collect result into one object
					while (set.hasNext())
						describeResultSet.addNewResult(set.next());
				}
			}
			// execute describe <uriconst> in object position
			for (String constant : constants) {
				String str = SPARQLQueryUtility.getConstructObjQuery(constant);

				ConstructQuery constructQuery = inputQueryFactory.createConstructQuery(str);
				SimpleGraphResultSet set = executeInThread(constructQuery, this::executeDescribeConstructQuery);

				if (describeResultSet == null) { // just for the first time
					describeResultSet = set;
				} else if (set != null) {
					while (set.hasNext())
						describeResultSet.addNewResult(set.next());
				}
			}
			// Exception is re-cast because not due to the initial input query
		} catch (OntopInvalidInputQueryException e) {
			throw new OntopReformulationException(e);
		}
		return describeResultSet;
	}

	private ImmutableSet<String> extractDescribeQueryConstants(DescribeQuery inputQuery)
			throws OntopQueryEvaluationException, OntopConnectionException,
            OntopReformulationException, OntopResultConversionException {
		String inputQueryString = inputQuery.getInputString();

		// create list of URI constants we want to describe
		if (SPARQLQueryUtility.isVarDescribe(inputQueryString)) {
			// if describe ?var, we have to do select distinct ?var first
			String sel = SPARQLQueryUtility.getSelectVarDescribe(inputQueryString);
			try {
				SelectQuery selectQuery = inputQueryFactory.createSelectQuery(sel);
				TupleResultSet resultSet = execute(selectQuery);

				ImmutableSet.Builder<String> constantSetBuilder = ImmutableSet.builder();
				while (resultSet.hasNext()) {
                    final OntopBindingSet bindingSet = resultSet.next();
                    Constant constant = bindingSet.getValues().get(0);
					if (constant instanceof IRIConstant) {
						// collect constants in list
						constantSetBuilder.add(((IRIConstant) constant).getIRI().getIRIString());
					}
				}
				return constantSetBuilder.build();
				// Exception is re-cast because not due to the initial input query
			} catch (OntopInvalidInputQueryException e) {
				throw new OntopReformulationException(e);
			}
		}
		else if (SPARQLQueryUtility.isURIDescribe(inputQueryString)) {
			// DESCRIBE <uri> gives direct results, so we put the
			// <uri> constant directly in the list of constants
			try {
				return ImmutableSet.of(SPARQLQueryUtility.getDescribeURI(inputQueryString));
			} catch (MalformedQueryException e) {
				throw new OntopReformulationException(e);
			}
		}
		else
			return ImmutableSet.of();
	}


	/**
	 * Internal method to start a new query execution thread type defines the
	 * query type SELECT, ASK, CONSTRUCT, or DESCRIBE
	 */
	private <R extends OBDAResultSet, Q extends InputQuery<R>> R executeInThread(Q inputQuery, Evaluator<R, Q> evaluator)
			throws OntopReformulationException, OntopQueryEvaluationException {

		log.debug("Executing SPARQL query: \n{}", inputQuery.getInputString());

		CountDownLatch monitor = new CountDownLatch(1);
		IQ executableQuery = engine.reformulateIntoNativeQuery(inputQuery);

		QueryExecutionThread<R, Q> executionthread = new QueryExecutionThread<>(inputQuery, executableQuery, evaluator,
				monitor);

		this.executionThread = executionthread;
		executionthread.start();
		try {
			monitor.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (executionthread.errorStatus()) {
			Exception ex = executionthread.getException();
			if (ex instanceof OntopReformulationException) {
				throw (OntopReformulationException) ex;
			}
			else if (ex instanceof OntopQueryEvaluationException) {
				throw (OntopQueryEvaluationException) ex;
			}
			else {
				throw new OntopQueryEvaluationException(ex);
			}
		}

		if (canceled) {
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
	public String getRewritingRendering(InputQuery query) throws OntopReformulationException {
		return engine.getRewritingRendering(query);
	}


	@Override
	public IQ getExecutableQuery(InputQuery inputQuery) throws OntopReformulationException {
			return engine.reformulateIntoNativeQuery(inputQuery);
	}

}
