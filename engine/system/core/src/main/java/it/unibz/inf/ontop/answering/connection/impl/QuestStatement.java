package it.unibz.inf.ontop.answering.connection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryContext;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.query.*;
import it.unibz.inf.ontop.query.resultset.impl.DefaultDescribeGraphResultSet;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.query.resultset.BooleanResultSet;
import it.unibz.inf.ontop.query.resultset.GraphResultSet;
import it.unibz.inf.ontop.query.resultset.OBDAResultSet;
import it.unibz.inf.ontop.query.resultset.TupleResultSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


/**
 * Abstract implementation of OntopStatement.
 *
 */
public abstract class QuestStatement implements OntopStatement {

	private final QueryReformulator engine;
	private final QueryLogger.Factory queryLoggerFactory;
	private final QueryContext.Factory queryContextFactory;

	private QueryExecutionThread<?,?> executionThread;
	private boolean canceled = false;


	private static final Logger log = LoggerFactory.getLogger(QuestStatement.class);


	public QuestStatement(QueryReformulator queryProcessor) {
		this.engine = queryProcessor;
		this.queryLoggerFactory = queryProcessor.getQueryLoggerFactory();
		this.queryContextFactory = queryProcessor.getQueryContextFactory();
	}

	/**
	 * Execution thread
	 */
	private class QueryExecutionThread<R extends OBDAResultSet, Q extends KGQuery<R>> extends Thread {

		private final Q inputQuery;
		private final QueryLogger queryLogger;
		private final QueryContext queryContext;
		private final Evaluator<R, Q> evaluator;
		private final CountDownLatch monitor;

		private R resultSet;	  // only for SELECT and ASK queries
		private Exception exception;
		private boolean executingTargetQuery;

		QueryExecutionThread(Q inputQuery, QueryLogger queryLogger, QueryContext queryContext, Evaluator<R,Q> evaluator,
						CountDownLatch monitor) {
			this.inputQuery = inputQuery;
			this.queryLogger = queryLogger;
			this.queryContext = queryContext;
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
				/*
				 * Executes the target query.
				 */
				executingTargetQuery = true;
				resultSet = evaluator.evaluate(inputQuery, queryContext, queryLogger);
				// NB: finished if the result set is blocking!
				log.debug("Result set unblocked.\n");
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

	private TupleResultSet executeSelectQuery(SelectQuery inputQuery, QueryContext queryContext, QueryLogger queryLogger)
			throws OntopQueryEvaluationException, OntopReformulationException {
		return executeSelectQuery(inputQuery, queryContext, queryLogger, true);
	}

	private TupleResultSet executeSelectQuery(SelectQuery inputQuery, QueryContext queryContext, QueryLogger queryLogger,
											  boolean shouldAlsoCloseStatement)
			throws OntopQueryEvaluationException, OntopReformulationException {
		IQ executableQuery = engine.reformulateIntoNativeQuery(inputQuery, queryContext, queryLogger);
		logExecutionStartingMessage();
		return executeSelectQuery(executableQuery, queryLogger, shouldAlsoCloseStatement);
	}

	@Override
	public TupleResultSet executeSelectQuery(IQ executableQuery, QueryLogger queryLogger)
			throws OntopQueryEvaluationException {
		return executeSelectQuery(executableQuery, queryLogger, true);
	}

	protected abstract TupleResultSet executeSelectQuery(IQ executableQuery, QueryLogger queryLogger,
														 boolean shouldAlsoCloseStatement)
			throws OntopQueryEvaluationException;

	private BooleanResultSet executeBooleanQuery(AskQuery inputQuery, QueryContext queryContext, QueryLogger queryLogger)
			throws OntopQueryEvaluationException, OntopReformulationException {
		IQ executableQuery = engine.reformulateIntoNativeQuery(inputQuery, queryContext, queryLogger);
		logExecutionStartingMessage();
		return executeBooleanQuery(executableQuery, queryLogger);
	}

	private GraphResultSet executeConstructQuery(ConstructQuery constructQuery, QueryContext queryContext, QueryLogger queryLogger)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException, OntopReformulationException {
		return executeConstructQuery(constructQuery, queryContext, queryLogger, true);
	}

	private GraphResultSet executeConstructQuery(ConstructQuery constructQuery, QueryContext queryContext, QueryLogger queryLogger,
												 boolean shouldAlsoCloseStatement)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException, OntopReformulationException {
		IQ executableQuery = engine.reformulateIntoNativeQuery(constructQuery, queryContext, queryLogger);
		logExecutionStartingMessage();
		return executeConstructQuery(constructQuery.getConstructTemplate(), executableQuery, queryLogger, shouldAlsoCloseStatement);
	}

	@Override
	public GraphResultSet executeConstructQuery(ConstructTemplate constructTemplate, IQ executableQuery, QueryLogger queryLogger)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException {
		return executeConstructQuery(constructTemplate, executableQuery, queryLogger, true);
	}

	protected abstract GraphResultSet executeConstructQuery(ConstructTemplate constructTemplate, IQ executableQuery,
															QueryLogger queryLogger, boolean shouldAlsoCloseStatement)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException;

	protected GraphResultSet executeDescribeQuery(DescribeQuery describeQuery, QueryContext queryContext, QueryLogger queryLogger)
			throws OntopQueryEvaluationException, OntopConnectionException, OntopReformulationException, OntopResultConversionException {
		return new DefaultDescribeGraphResultSet(describeQuery, queryLogger, queryLoggerFactory, queryContext,
				(selectQuery, qContext, logger) -> executeSelectQuery(selectQuery, qContext, logger, false),
				(constructQuery, qContext, logger) -> executeConstructQuery(constructQuery, qContext, logger, false),
				this::close);
	}

	private void logExecutionStartingMessage() {
		log.debug("Executing the query and get the result...");
	}

	/**
	 * Cancel the processing of the target query.
	 */
	protected abstract void cancelExecution() throws OntopQueryEvaluationException;

	/**
	 * Calls the necessary tuple or graph query execution Implements describe
	 * uri or var logic Returns the result set for the given query
	 */
	@Override
	public <R extends OBDAResultSet> R execute(KGQuery<R> inputQuery) throws OntopConnectionException,
			OntopReformulationException, OntopQueryEvaluationException, OntopResultConversionException {
		return execute(inputQuery, ImmutableMultimap.of());
	}

	@Override
	public <R extends OBDAResultSet> R execute(KGQuery<R> inputQuery, ImmutableMultimap<String, String> httpHeaders)
			throws OntopConnectionException, OntopReformulationException, OntopQueryEvaluationException, OntopResultConversionException {

		if (inputQuery instanceof SelectQuery) {
			return (R) executeInThread((SelectQuery) inputQuery, httpHeaders, (inputQuery1, queryContext, queryLogger) -> executeSelectQuery(inputQuery1, queryContext, queryLogger));
		}
		else if (inputQuery instanceof AskQuery) {
			return (R) executeInThread((AskQuery) inputQuery, httpHeaders, (inputQuery1, queryContext, queryLogger) -> executeBooleanQuery(inputQuery1, queryContext, queryLogger));
		}
		else if (inputQuery instanceof DescribeQuery) {
			return (R) executeInThread((DescribeQuery) inputQuery, httpHeaders, (describeQuery, queryContext, queryLogger) -> executeDescribeQuery(describeQuery, queryContext, queryLogger));
		}
		else if (inputQuery instanceof ConstructQuery) {
			return (R) executeInThread((ConstructQuery) inputQuery, httpHeaders, (constructQuery, queryContext, queryLogger) -> executeConstructQuery(constructQuery, queryContext, queryLogger));
		}
		else {
			throw new OntopUnsupportedInputQueryException("Unsupported query type: " + inputQuery);
		}
	}

	/**
	 * Internal method to start a new query execution thread type defines the
	 * query type SELECT, ASK, CONSTRUCT, or DESCRIBE
	 */
	private <R extends OBDAResultSet, Q extends KGQuery<R>> R executeInThread(Q inputQuery, ImmutableMultimap<String, String> httpHeaders,
																			  Evaluator<R, Q> evaluator)
			throws OntopReformulationException, OntopQueryEvaluationException {

		ImmutableMap<String, String> normalizedHttpHeaders = normalizeHttpHeaders(httpHeaders);

		// TODO: use normalizedHttpHeaders
		QueryLogger queryLogger = queryLoggerFactory.create(httpHeaders);
		queryLogger.setSparqlQuery(inputQuery.getOriginalString());

		QueryContext queryContext = queryContextFactory.create(normalizedHttpHeaders);

		CountDownLatch monitor = new CountDownLatch(1);

		QueryExecutionThread<R, Q> executionthread = new QueryExecutionThread<>(inputQuery, queryLogger, queryContext, evaluator,
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
				queryLogger.declareEvaluationException(ex);
				throw (OntopQueryEvaluationException) ex;
			}
			else {
				queryLogger.declareEvaluationException(ex);
				throw new OntopQueryEvaluationException(ex);
			}
		}

		if (canceled) {
			canceled = false;
			throw new OntopQueryEvaluationException("Query execution was cancelled");
		}
		return executionthread.getResultSet();
	}

	/**
	 * TODO: move somewhere else?
	 * FROM RFC2616: It MUST be possible to combine the multiple header fields into one "field-name: field-value" pair,
	 * without changing the semantics of the message, by appending each subsequent field-value to the first,
	 * each separated by a comma.
	 */
	private ImmutableMap<String, String> normalizeHttpHeaders(ImmutableMultimap<String, String> httpHeaders) {
		return httpHeaders.asMap().entrySet().stream()
				.collect(ImmutableCollectors.toMap(
						e -> e.getKey().toLowerCase(),
						e -> String.join(",", e.getValue())));
	}


	@Override
	public void cancel() throws OntopConnectionException {
		canceled = true;
		try {
			executionThread.cancel();
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
	public <R extends OBDAResultSet> String getRewritingRendering(KGQuery<R> query) throws OntopReformulationException {
		return engine.getRewritingRendering(query);
	}

	@Override
	public  <R extends OBDAResultSet>  IQ getExecutableQuery(KGQuery<R> inputQuery) throws OntopReformulationException {
		return engine.reformulateIntoNativeQuery(inputQuery, queryContextFactory.create(ImmutableMap.of()),
				queryLoggerFactory.create(ImmutableMultimap.of()));
	}

}
