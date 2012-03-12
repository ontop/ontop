package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.codec.DatalogProgramToTextCodec;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAConnection;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.EmptyQueryResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.OWLOBDARefResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.SPARQLDatalogTranslator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryException;

/**
 * The obda statement provides the implementations necessary to query the
 * reformulation platform reasoner from outside, i.e. protege
 * 
 * 
 */

public class QuestStatement implements OBDAStatement {

	private QueryRewriter rewriter = null;

	private UnfoldingMechanism unfoldingmechanism = null;

	private SourceQueryGenerator querygenerator = null;

	private QueryVocabularyValidator validator = null;

	private OBDAModel unfoldingOBDAModel = null;

	private boolean canceled = false;

	Logger log = LoggerFactory.getLogger(QuestStatement.class);
	private Statement sqlstatement;

	private RDBMSDataRepositoryManager repository;

	private DatalogProgram unfoldingProgram;

	private QuestConnection conn;

	protected Quest questInstance;

	public QuestStatement(Quest questinstance, QuestConnection conn, Statement st) {

		this.questInstance = questinstance;
		this.repository = questinstance.dataRepository;
		this.conn = conn;
		this.rewriter = questinstance.rewriter;
		this.unfoldingmechanism = questinstance.unfolder;
		this.querygenerator = questinstance.datasourceQueryGenerator;
		// this.engine = eng;

		this.sqlstatement = st;
		this.validator = questinstance.vocabularyValidator;
		// this.query = query;
		this.unfoldingOBDAModel = questinstance.unfoldingOBDAModel;

	}

	/**
	 * Returns the result set for the given query
	 */
	@Override
	public OBDAResultSet execute(String strquery) throws OBDAException {

		if (strquery.split("[eE][tT][aA][bB][lL][eE]").length > 1) {
			return executeEpistemicQuery(strquery);
		}
		if (strquery.contains("/*direct*/")) {
			return executeDirectQuery(strquery);
		} else {
			return executeConjunctiveQuery(strquery);
		}
	}

	/***
	 * This method will 'chop' the original query into the subqueries, computing
	 * the SQL for each of the nested query and composing everything into a
	 * single SQL.
	 * 
	 * @param strquery
	 * @return
	 * @throws Exception
	 */
	private OBDAResultSet executeEpistemicQuery(String strquery) throws OBDAException {
		try {
			OBDAResultSet result;

			String epistemicUnfolding = getSQLEpistemic(strquery);
			ResultSet set = sqlstatement.executeQuery(epistemicUnfolding);
			result = new OWLOBDARefResultSet(set, this);

			return result;
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	private OBDAResultSet executeDirectQuery(String query) throws OBDAException {
		try {
			OBDAResultSet result;
			ResultSet set = sqlstatement.executeQuery(query);
			result = new OWLOBDARefResultSet(set, this);
			return result;
		} catch (Exception e) {
			throw new OBDAException(e);
		}
	}

	private String getSQL(DatalogProgram program, List<String> signature) throws OBDAException {
		// Check the datalog object
		if (validator != null) {
			log.debug("Validating the user query...");
			boolean isValid = validator.validate(program);

			if (!isValid) {
				Vector<String> invalidList = validator.getInvalidPredicates();

				String msg = "";
				for (String predicate : invalidList) {
					msg += "- " + predicate + "\n";
				}
				throw new OBDAException("These predicates are missing from the ontology's vocabulary: \n" + msg);
			}
		}
		log.debug("Replacing equivalences...");
		program = validator.replaceEquivalences(program);

		// If the datalog is valid, proceed to the next process.
		signature.addAll(getSignature(program));

		log.debug("Start the rewriting process...");
		OBDAQuery rewriting = rewriter.rewrite(program);

		log.debug("Start the unfolding process...");
		OBDAQuery unfolding = unfoldingmechanism.unfold((DatalogProgram) rewriting);

		if (((DatalogProgram) unfolding).getRules().size() == 0)
			return "";
		log.debug("Producing the SQL string...");
		String sql = querygenerator.generateSourceQuery((DatalogProgram) unfolding, signature);

		return sql;
	}

	private OBDAResultSet executeConjunctiveQuery(String strquery) throws OBDAException {
		// Contruct the datalog program object from the query string
		log.debug("Input user query:\n" + strquery);
		DatalogProgram program = getDatalogQuery(strquery);

		List<String> signature = new LinkedList<String>();
		String sql = getSQL(program, signature);

		OBDAResultSet result;

		log.debug("Executing the query and get the result...");
		if (sql.equals("")) {
			/***
			 * Empty unfolding, constructing an empty result set
			 */
			if (program.getRules().size() < 1) {
				throw new OBDAException("Error, invalid query");
			}
			result = new EmptyQueryResultSet(signature, this);
		} else {
			ResultSet set;
			try {
				set = sqlstatement.executeQuery(sql);
			} catch (SQLException e) {
				throw new OBDAException(e);
			}
			if (isDPBoolean(program)) {
				result = new BooleanOWLOBDARefResultSet(set, this);
			} else {
				result = new OWLOBDARefResultSet(set, this);
			}
		}

		log.debug("Finish.\n");
		return result;
	}

	/**
	 * Extracts the signature of a CQ query given as a DatalogProgram. Only
	 * variables are accepted in the signature of queries.
	 * 
	 * @param program
	 * @return
	 * @throws Exception
	 */
	private List<String> getSignature(DatalogProgram program) throws OBDAException {
		if (program.getRules().size() < 1)
			throw new OBDAException("Invalid query");

		List<String> signature = new LinkedList<String>();
		for (Term term : program.getRules().get(0).getHead().getTerms()) {
			if (term instanceof Variable) {
				signature.add(((Variable) term).getName());
			} else {
				throw new OBDAException("Only variables are allowed in the head of queries");
			}
		}
		return signature;
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getRewriting(String strquery) throws Exception {
		// TODO FIX to limit to SPARQL input and output
		DatalogProgram program = getDatalogQuery(strquery);

		log.debug("Replacing equivalences...");
		program = validator.replaceEquivalences(program);

		OBDAQuery rewriting = rewriter.rewrite(program);
		DatalogProgramToTextCodec codec = new DatalogProgramToTextCodec(unfoldingOBDAModel);
		return codec.encode((DatalogProgram) rewriting);
	}

	private String getSQLEpistemic(String strquery) throws OBDAException {
		// FIRST WE try to analyze the query to find the CQs and the SQL part

		LinkedList<String> sql = new LinkedList<String>();
		LinkedList<String> cqs = new LinkedList<String>();

		StringBuffer query = new StringBuffer(strquery);
		Pattern pattern = Pattern.compile("[eE][tT][aA][bB][lL][eE]\\s*\\((\\r?\\n|\\n|.)+?\\)", Pattern.MULTILINE);

		while (true) {

			String[] splitquery = pattern.split(query.toString(), 2);
			if (splitquery.length > 1) {
				sql.add(splitquery[0]);
				query.delete(0, splitquery[0].length());
				int position = query.toString().indexOf(splitquery[1]);

				String regex = query.toString().substring(0, position);

				cqs.add(regex.substring(regex.indexOf("(") + 1, regex.length() - 1));
				query = new StringBuffer(splitquery[1]);

			} else {
				sql.add(splitquery[0]);
				break;
			}
		}

		// Now we generate the SQL for each CQ

		SPARQLDatalogTranslator t = new SPARQLDatalogTranslator();
		LinkedList<String> sqlforcqs = new LinkedList<String>();
		log.debug("Found {} embedded queries.", cqs.size());
		for (int i = 0; i < cqs.size(); i++) {
			log.debug("Processing embedded query #{}", i);
			String cq = cqs.get(i);
			try {
				DatalogProgram p = t.parse(cq);
				List<String> signature = getSignature(p);
				String finasql = getSQL(p, signature);
				log.debug("SQL: {}", finasql);
				sqlforcqs.add(finasql);
			} catch (Exception e) {
				log.error("Error processing nested query #{}", i);
				log.error(e.getMessage(), e);
				throw new OBDAException("Error processing nested query #" + i, e);
			}
		}

		// Now we concatenate the simple SQL with the rewritten SQL to generate
		// the query over DB.

		log.debug("Forming the final SQL.");
		StringBuffer finalquery = new StringBuffer();
		for (int i = 0; i < sql.size(); i++) {
			finalquery.append(sql.get(i));
			if (sqlforcqs.size() > i) {
				finalquery.append("(");
				finalquery.append(sqlforcqs.get(i));
				finalquery.append(")");
			}
		}
		log.debug("Final SQL query: {}", finalquery.toString());

		if (finalquery.toString().equals(""))
			throw new OBDAException("Invalid SQL. The SQL query cannot be empty");

		return finalquery.toString();

	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getUnfolding(String strquery) throws Exception {
		String sql = null;
		if (strquery.split("[eE][tT][aA][bB][lL][eE]").length > 1) {
			sql = getSQLEpistemic(strquery);
		}
		if (strquery.contains("/*direct*/")) {
			sql = strquery;
		} else {
			DatalogProgram p = getDatalogQuery(strquery);
			sql = getSQL(p, getSignature(p));
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

	/**
	 * Checks whether the given query is boolean or not
	 * 
	 * @param dp
	 *            the given datalog program
	 * @return true if the query is boolean, false otherwise
	 */
	private boolean isDPBoolean(DatalogProgram dp) {

		List<CQIE> rules = dp.getRules();
		Iterator<CQIE> it = rules.iterator();
		boolean bool = true;
		while (it.hasNext() && bool) {
			CQIE query = it.next();
			Atom a = query.getHead();
			if (a.getTerms().size() != 0) {
				bool = false;
			}
		}
		return bool;
	}

	@Override
	public void close() throws OBDAException {
		try {
			sqlstatement.close();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	private DatalogProgram getDatalogQuery(String query) throws OBDAException {
		SPARQLDatalogTranslator sparqlTranslator = new SPARQLDatalogTranslator();

		DatalogProgram queryProgram = null;
		try {
			queryProgram = sparqlTranslator.parse(query);
		} catch (QueryException e) {
			log.warn(e.getMessage());
		}

		if (queryProgram == null) { // if the SPARQL translator doesn't work,
			// use the Datalog parser.
			DatalogProgramParser datalogParser = new DatalogProgramParser();
			try {
				queryProgram = datalogParser.parse(query);
			} catch (RecognitionException e) {
				log.warn(e.getMessage());
				queryProgram = null;
			} catch (IllegalArgumentException e2) {
				log.warn(e2.getMessage());
			}
		}

		if (queryProgram == null) // if it is still null
			throw new OBDAException("Unsupported syntax");

		return queryProgram;
	}

	@Override
	public void cancel() throws OBDAException {
		// TODO Auto-generated method stub

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
			throw new OBDAException(e);
		}

	}

	@Override
	public int getMaxRows() throws OBDAException {
		try {
			return sqlstatement.getMaxRows();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void getMoreResults() throws OBDAException {
		try {
			sqlstatement.getMoreResults();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setFetchSize(int rows) throws OBDAException {
		try {
			sqlstatement.setFetchSize(rows);
		} catch (SQLException e) {
			throw new OBDAException(e);
		}

	}

	@Override
	public void setMaxRows(int max) throws OBDAException {
		try {
			sqlstatement.setMaxRows(max);
		} catch (SQLException e) {
			throw new OBDAException(e);

		}

	}

	@Override
	public void setQueryTimeout(int seconds) throws OBDAException {
		try {
			sqlstatement.setQueryTimeout(seconds);
		} catch (SQLException e) {
			throw new OBDAException(e);

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getQueryTimeout() throws OBDAException {
		try {
			return sqlstatement.getQueryTimeout();
		} catch (SQLException e) {
			throw new OBDAException(e);

		}
	}

	@Override
	public boolean isClosed() throws OBDAException {
		try {
			return sqlstatement.isClosed();
		} catch (SQLException e) {
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
		if (!useFile)
			result = repository.insertData(conn.conn, data, commit, batch);
		else {
			try {
				// File temporalFile = new File("quest-copy.tmp");
				// FileOutputStream os = new FileOutputStream(temporalFile);
				result = (int) repository.loadWithFile(conn.conn, data);
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

}
