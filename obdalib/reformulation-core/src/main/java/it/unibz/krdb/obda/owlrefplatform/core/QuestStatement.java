package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.codec.DatalogProgramToTextCodec;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDAConnection;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.ConstructGraphResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.DescribeGraphResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.EmptyQueryResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.QuestResultset;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * The obda statement provides the implementations necessary to query the
 * reformulation platform reasoner from outside, i.e. protege
 * 
 * 
 */

public class QuestStatement implements OBDAStatement {

	private QueryRewriter rewriter = null;

	private UnfoldingMechanism unfoldingmechanism = null;

	private SQLQueryGenerator querygenerator = null;

	private QueryVocabularyValidator validator = null;

	private OBDAModel unfoldingOBDAModel = null;

	private boolean canceled = false;

	private DatalogProgram unfoldingProgram;

	private Statement sqlstatement;

	private RDBMSDataRepositoryManager repository;

	private QuestConnection conn;

	protected Quest questInstance;

	private static Logger log = LoggerFactory.getLogger(QuestStatement.class);

	Thread runningThread = null;

	private QueryExecutionThread executionthread;

	final Map<String, String> querycache;

	final Map<String, List<String>> signaturecache;

	final Map<String, Boolean> isbooleancache;

	final Map<String, Boolean> isconstructcache;

	final Map<String, Boolean> isdescribecache;

	final SparqlAlgebraToDatalogTranslator translator;

	public QuestStatement(Quest questinstance, QuestConnection conn,
			Statement st) {

		this.questInstance = questinstance;

		this.translator = new SparqlAlgebraToDatalogTranslator(
				this.questInstance.getUriTemplateMatcher());
		this.querycache = questinstance.getSQLCache();
		this.signaturecache = questinstance.getSignatureCache();
		this.isbooleancache = questinstance.getIsBooleanCache();
		this.isconstructcache = questinstance.getIsConstructCache();
		this.isdescribecache = questinstance.getIsDescribeCache();

		this.repository = questinstance.dataRepository;
		this.conn = conn;
		this.rewriter = questinstance.rewriter;
		this.unfoldingmechanism = questinstance.unfolder;
		this.querygenerator = questinstance.datasourceQueryGenerator;

		this.sqlstatement = st;
		this.validator = questinstance.vocabularyValidator;
		this.unfoldingOBDAModel = questinstance.unfoldingOBDAModel;
	}

	private class QueryExecutionThread extends Thread {

		private final CountDownLatch monitor;
		private final String strquery;
		private OBDAResultSet tupleResult;
		private GraphResultSet graphResult;
		private Exception exception;
		private boolean error = false;
		private boolean executingSQL = false;

		public QueryExecutionThread(String strquery, CountDownLatch monitor) {
			this.monitor = monitor;
			this.strquery = strquery;
		}

		public boolean errorStatus() {
			return error;
		}

		public Exception getException() {
			return exception;
		}

		public OBDAResultSet getTupleResult() {
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
			try {
				if (!querycache.containsKey(strquery)) {
					String sqlQuery = getUnfolding(strquery);
					cacheQueryAndProperties(strquery, sqlQuery);
				}
				String sql = querycache.get(strquery);
				List<String> signature = signaturecache.get(strquery);
				boolean isBoolean = isbooleancache.get(strquery);
				boolean isConstruct = isconstructcache.get(strquery);
				boolean isDescribe = isdescribecache.get(strquery);

				log.debug("Executing the query and get the result...");
				if (sql.equals("") && !isBoolean) {
					tupleResult = new EmptyQueryResultSet(signature,
							QuestStatement.this);
				} else if (sql.equals("")) {
					tupleResult = new BooleanOWLOBDARefResultSet(false,
							QuestStatement.this);
				} else {
					try {
						// Check pre-condition for DESCRIBE
						if (isDescribe && signature.size() > 1) {
							throw new OBDAException(
									"Only support query with one variable in DESCRIBE");
						}
						// Execute the SQL query string
						executingSQL = true;
						ResultSet set = sqlstatement.executeQuery(sql);

						// Store the SQL result to application result set.
						if (isBoolean) {
							tupleResult = new BooleanOWLOBDARefResultSet(set,
									QuestStatement.this);
						} else if (isConstruct) {
							Query query = QueryFactory.create(strquery);
							Template template = query.getConstructTemplate();
							OBDAResultSet tuples = new QuestResultset(set,
									signature, QuestStatement.this);
							graphResult = new ConstructGraphResultSet(tuples,
									template);
						} else if (isDescribe) {
							Query query = QueryFactory.create(strquery);
							PrefixMapping pm = query.getPrefixMapping();
							OBDAResultSet tuples = new QuestResultset(set,
									signature, QuestStatement.this);
							graphResult = new DescribeGraphResultSet(tuples, pm);
						} else { // is tuple-based results
							tupleResult = new QuestResultset(set, signature,
									QuestStatement.this);
						}
					} catch (SQLException e) {
						throw new OBDAException("Error executing SQL query: \n"
								+ e.getMessage() + "\nSQL query:\n " + sql);
					}
				}
				log.debug("Finish.\n");
			} catch (Exception e) {
				exception = e;
				error = true;
			} finally {
				monitor.countDown();
			}
		}
	}

	/**
	 * Returns the result set for the given query
	 */
	@Override
	public OBDAResultSet execute(String strquery) throws OBDAException {
		if (strquery.isEmpty()) {
			throw new OBDAException("Cannot execute an empty query");
		}
		return executeSelectQuery(strquery);
	}

	@Override
	public GraphResultSet executeConstruct(String strquery)
			throws OBDAException {
		if (strquery.isEmpty()) {
			throw new OBDAException("Cannot execute an empty query");
		}
		return executeConstructQuery(strquery);
	}

	@Override
	public GraphResultSet executeDescribe(String strquery) throws OBDAException {
		if (strquery.isEmpty()) {
			throw new OBDAException("Cannot execute an empty query");
		}
		return executeDescribeQuery(strquery);
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
	private DatalogProgram translateAndPreProcess(Query query)
			throws OBDAException {

		// Contruct the datalog program object from the query string
		DatalogProgram program = null;
		try {
			program = translator.translate(query);

			log.debug("Translated query: \n{}", program.toString());

			DatalogUnfolder unfolder = new DatalogUnfolder(program.clone(),
					new HashMap<Predicate, List<Integer>>());
			removeNonAnswerQueries(program);

			program = unfolder.unfold(program, "ans1");

			log.debug("Flattened query: \n{}", program.toString());
		} catch (Exception e) {
			e.printStackTrace();
			OBDAException ex = new OBDAException(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
		log.debug("Replacing equivalences...");
		program = validator.replaceEquivalences(program);
		return program;
	}

	private DatalogProgram getUnfolding(DatalogProgram query)
			throws OBDAException {

		log.debug("Start the partial evaluation process...");

		DatalogProgram unfolding = unfoldingmechanism.unfold(
				(DatalogProgram) query, "ans1");
		log.debug("Partial evaluation: {}\n", unfolding);

		removeNonAnswerQueries(unfolding);

		log.debug("After target rules removed: \n{}", unfolding);
		
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

	private String getSQL(DatalogProgram query, List<String> signature)
			throws OBDAException {
		if (((DatalogProgram) query).getRules().size() == 0) {
			return "";
		}
		log.debug("Producing the SQL string...");

		// query = DatalogNormalizer.normalizeDatalogProgram(query);
		String sql = querygenerator.generateSourceQuery((DatalogProgram) query,
				signature);

		log.debug("Resulting sql: \n{}", sql);
		return sql;
	}

	private OBDAResultSet executeSelectQuery(String strquery)
			throws OBDAException {
		CountDownLatch monitor = new CountDownLatch(1);
		executionthread = new QueryExecutionThread(strquery, monitor);
		executionthread.start();
		try {
			monitor.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		if (executionthread.errorStatus()) {
			throw new RuntimeException(executionthread.getException());
			// OBDAException ex = new
			// OBDAException(executionthread.getException().getMessage());
			// ex.setStackTrace(executionthread.getStackTrace());
			// throw ex;
		}

		if (canceled == true) {
			canceled = false;
			throw new OBDAException("Query execution was cancelled");
		}

		OBDAResultSet result = executionthread.getTupleResult();

		if (result == null)
			throw new RuntimeException("Error, the result set was null");

		return executionthread.getTupleResult();
	}

	private GraphResultSet executeConstructQuery(String strquery)
			throws OBDAException {
		CountDownLatch monitor = new CountDownLatch(1);
		executionthread = new QueryExecutionThread(strquery, monitor);
		executionthread.start();
		try {
			monitor.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (executionthread.errorStatus()) {
			OBDAException ex = new OBDAException(executionthread.getException()
					.getMessage());
			ex.setStackTrace(executionthread.getStackTrace());
			throw ex;
		}

		if (canceled == true) {
			canceled = false;
			throw new OBDAException("Query execution was cancelled");
		}
		return executionthread.getGraphResult();
	}

	private GraphResultSet executeDescribeQuery(String strquery)
			throws OBDAException {
		CountDownLatch monitor = new CountDownLatch(1);
		executionthread = new QueryExecutionThread(strquery, monitor);
		executionthread.start();
		try {
			monitor.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (executionthread.errorStatus()) {
			OBDAException ex = new OBDAException(executionthread.getException()
					.getMessage());
			ex.setStackTrace(executionthread.getStackTrace());
			throw ex;
		}

		if (canceled == true) {
			canceled = false;
			throw new OBDAException("Query execution was cancelled");
		}
		return executionthread.getGraphResult();
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getRewriting(String strquery) throws Exception {
		// TODO FIX to limit to SPARQL input and output
		log.debug("Input user query:\n" + strquery);

		Query query = QueryFactory.create(strquery);
		DatalogProgram program = translateAndPreProcess(query);

		OBDAQuery rewriting = rewriter.rewrite(program);
		DatalogProgramToTextCodec codec = new DatalogProgramToTextCodec(
				unfoldingOBDAModel);
		return codec.encode((DatalogProgram) rewriting);
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public DatalogProgram getRewriting(DatalogProgram program)
			throws OBDAException {
		OBDAQuery rewriting = rewriter.rewrite(program);
		return (DatalogProgram) rewriting;
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getUnfolding(String strquery) throws Exception {
		String sql = "";
		List<String> signature;

		// Check the cache first if the system has processed the query string before
		if (querycache.containsKey(strquery)) {
			// Obtain immediately the SQL string from cache
			sql = querycache.get(strquery);

		} else {
			Query query = QueryFactory.create(strquery);
			DatalogProgram program = translateAndPreProcess(query);
			try {
				log.debug("Input user query:\n" + strquery);

				
				for (CQIE q : program.getRules()) {
					DatalogNormalizer.unfoldJoinTrees(q, false);
				}

				log.debug("Normalized program: \n{}", program);

				/**
				 * Empty unfolding, constructing an empty result set
				 */
				if (program.getRules().size() < 1) {
					throw new OBDAException("Error, invalid query");
				}

			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);
				OBDAException obdaException = new OBDAException(e1);
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}

			log.debug("Start the rewriting process...");
			// DatalogProgram rewriting = program;
			DatalogProgram rewriting;
			try {
				rewriting = getRewriting(program);
			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);

				OBDAException obdaException = new OBDAException(
						"Error rewriting query. \n" + e1.getMessage());
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}

			DatalogProgram unfolding;
			try {
				unfolding = getUnfolding(rewriting);

				// unfolding = getUnfolding(program);
			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);
				OBDAException obdaException = new OBDAException(
						"Error unfolding query. \n" + e1.getMessage());
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}

			try {
				signature = getSignature(query);
				sql = getSQL(unfolding, signature);
				cacheQueryAndProperties(strquery, sql);
			} catch (Exception e1) {
				log.debug(e1.getMessage(), e1);

				OBDAException obdaException = new OBDAException(
						"Error generating SQL. \n" + e1.getMessage());
				obdaException.setStackTrace(e1.getStackTrace());
				throw obdaException;
			}
		}
		return sql;
	}

	private void cacheQueryAndProperties(String sparqlQuery, String sqlQuery) {
		// Store the query
		querycache.put(sparqlQuery, sqlQuery);

		// Store the query properties
		Query query = QueryFactory.create(sparqlQuery);
		
		List<String> signature = getSignature(query);
		signaturecache.put(sparqlQuery, signature);

		boolean isBoolean = isBoolean(query);
		isbooleancache.put(sparqlQuery, isBoolean);
		
		boolean isConstruct = isConstruct(query);
		isconstructcache.put(sparqlQuery, isConstruct);

		boolean isDescribe = isDescribe(query);
		isdescribecache.put(sparqlQuery, isDescribe);
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
				throw new Exception(
						"Tuple count faild due to empty result set.");
			}
		} else {
			throw new Exception("Action canceled.");
		}
	}

	@Override
	public void close() throws OBDAException {
		try {
			sqlstatement.close();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	// private DatalogProgram getDatalogQuery(String query) throws OBDAException
	// {
	// SPARQLDatalogTranslator sparqlTranslator = new SPARQLDatalogTranslator();
	//
	// DatalogProgram queryProgram = null;
	// try {
	// queryProgram = sparqlTranslator.parse(query);
	// } catch (QueryException e) {
	// log.warn(e.getMessage());
	// }
	//
	// if (queryProgram == null) { // if the SPARQL translator doesn't work,
	// // use the Datalog parser.
	// DatalogProgramParser datalogParser = new DatalogProgramParser();
	// try {
	// queryProgram = datalogParser.parse(query);
	// } catch (RecognitionException e) {
	// log.warn(e.getMessage());
	// queryProgram = null;
	// } catch (IllegalArgumentException e2) {
	// log.warn(e2.getMessage());
	// }
	// }
	//
	// if (queryProgram == null) // if it is still null
	// throw new OBDAException("Unsupported syntax");
	//
	// return queryProgram;
	// }

	private List<String> getSignature(Query query) {
		List<String> signature = translator.getSignature(query);
		return signature;
	}

	private boolean isBoolean(Query query) {
		return query.isAskType();
	}

	private boolean isConstruct(Query query) {
		return query.isConstructType();
	}

	public boolean isDescribe(Query query) {
		return query.isDescribeType();
	}

	@Override
	public void cancel() throws OBDAException {
		try {
			QuestStatement.this.executionthread.cancel();
		} catch (SQLException e) {
			OBDAException o = new OBDAException(e);
			o.setStackTrace(e.getStackTrace());
			throw o;
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
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}

	}

	@Override
	public int getMaxRows() throws OBDAException {
		try {
			return sqlstatement.getMaxRows();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}

	}

	@Override
	public void getMoreResults() throws OBDAException {
		try {
			sqlstatement.getMoreResults();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}

	}

	@Override
	public void setFetchSize(int rows) throws OBDAException {
		try {
			sqlstatement.setFetchSize(rows);
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}

	}

	@Override
	public void setMaxRows(int max) throws OBDAException {
		try {
			sqlstatement.setMaxRows(max);
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}

	}

	@Override
	public void setQueryTimeout(int seconds) throws OBDAException {
		try {
			sqlstatement.setQueryTimeout(seconds);
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}

	}

	public void setUnfoldingProgram(DatalogProgram unfoldingProgram) {
		this.unfoldingProgram = unfoldingProgram;
	}

	@Override
	public OBDAConnection getConnection() throws OBDAException {
		return conn;
	}

	@Override
	public OBDAResultSet getResultSet() throws OBDAException {
		return null;
	}

	@Override
	public int getQueryTimeout() throws OBDAException {
		try {
			return sqlstatement.getQueryTimeout();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	@Override
	public boolean isClosed() throws OBDAException {
		try {
			return sqlstatement.isClosed();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
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
	public int insertData(Iterator<Assertion> data, boolean useFile,
			int commit, int batch) throws SQLException {
		int result = -1;

		EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(
				data, questInstance.getEquivalenceMap());

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

		return result;
	}

	/***
	 * As before, but using recreateIndexes = false.
	 * 
	 * @param data
	 * @throws SQLException
	 */
	public int insertData(Iterator<Assertion> data, int commit, int batch)
			throws SQLException {
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
}
