package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.codec.DatalogProgramToTextCodec;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.EmptyQueryResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.OWLOBDARefResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.SPARQLDatalogTranslator;

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

public class QuestOBDAStatement implements OBDAStatement {

	private QueryRewriter rewriter = null;

	private UnfoldingMechanism unfoldingmechanism = null;

	private SourceQueryGenerator querygenerator = null;

	private QueryVocabularyValidator validator = null;

	private OBDAModel apic = null;

	private boolean canceled = false;

	Logger log = LoggerFactory.getLogger(QuestOBDAStatement.class);
	private Statement statement;

	private RDBMSDataRepositoryManager repository;

	public QuestOBDAStatement(UnfoldingMechanism unf, QueryRewriter rew, SourceQueryGenerator gen, QueryVocabularyValidator val,
			Statement st, OBDAModel apic, RDBMSDataRepositoryManager repository) {

		this.repository = repository;

		this.rewriter = rew;
		this.unfoldingmechanism = unf;
		this.querygenerator = gen;
		// this.engine = eng;

		this.statement = st;
		this.validator = val;
		// this.query = query;
		this.apic = apic;

	}

	/**
	 * Returns the result set for the given query
	 */
	@Override
	public OBDAResultSet executeQuery(String strquery) throws Exception {

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
	private OBDAResultSet executeEpistemicQuery(String strquery) throws Exception {
		OBDAResultSet result;

		String epistemicUnfolding = getUnfoldingEpistemic(strquery);
		ResultSet set = statement.executeQuery(epistemicUnfolding);
		result = new OWLOBDARefResultSet(set, this);

		return result;
	}

	private OBDAResultSet executeDirectQuery(String query) throws Exception {
		OBDAResultSet result;
		ResultSet set = statement.executeQuery(query);
		result = new OWLOBDARefResultSet(set, this);
		return result;
	}

	private OBDAResultSet executeConjunctiveQuery(String strquery) throws Exception {
		// Contruct the datalog program object from the query string
		log.debug("Input user query:\n" + strquery);
		DatalogProgram program = getDatalogQuery(strquery);

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
				throw new Exception("These predicates are missing from the ontology's vocabulary: \n" + msg);
			}
		}
		log.debug("Replacing equivalences...");
		program = validator.replaceEquivalences(program);

		// If the datalog is valid, proceed to the next process.
		List<String> signature = getSignature(program);

		log.debug("Start the rewriting process...");
		OBDAQuery rewriting = rewriter.rewrite(program);

		log.debug("Start the unfolding process...");
		OBDAQuery unfolding = unfoldingmechanism.unfold((DatalogProgram) rewriting);

		log.debug("Producing the SQL string...");
		String sql = querygenerator.generateSourceQuery((DatalogProgram) unfolding, signature);

		OBDAResultSet result;

		log.debug("Executing the query and get the result...");
		if (sql.equals("")) {
			/***
			 * Empty unfolding, constructing an empty resultset
			 */
			if (program.getRules().size() < 1) {
				throw new Exception("Error, empty query");
			}
			result = new EmptyQueryResultSet(signature, this);
		} else {
			ResultSet set = statement.executeQuery(sql);
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
	private List<String> getSignature(DatalogProgram program) throws Exception {
		if (program.getRules().size() < 1)
			throw new Exception("Invalid query");

		List<String> signature = new LinkedList<String>();
		for (Term term : program.getRules().get(0).getHead().getTerms()) {
			if (term instanceof Variable) {
				signature.add(((Variable) term).getName());
			} else {
				throw new Exception("Only variables are allowed in the head of queries");
			}
		}
		return signature;
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getRewriting(String strquery) throws Exception {
		DatalogProgram program = getDatalogQuery(strquery);
		
		log.debug("Replacing equivalences...");
		program = validator.replaceEquivalences(program);

		
		OBDAQuery rewriting = rewriter.rewrite(program);
		DatalogProgramToTextCodec codec = new DatalogProgramToTextCodec(apic);
		return codec.encode((DatalogProgram) rewriting);
	}

	private String getUnfoldingEpistemic(String strquery) throws Exception {
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

				p = validator.replaceEquivalences(p);

				boolean isValid = validator.validate(p);

				if (!isValid) {
					Vector<String> invalidList = validator.getInvalidPredicates();

					String msg = "";
					for (String predicate : invalidList) {
						msg += "- " + predicate + "\n";
					}
					throw new Exception("These predicates are missing from the ontology's vocabulary: \n" + msg);
				}

				DatalogProgram rew = (DatalogProgram) rewriter.rewrite(p);
				DatalogProgram unf = unfoldingmechanism.unfold(rew);
				// querygenerator.getViewManager().storeOrgQueryHead(p.getRules().get(0).getHead());
				String finasql = querygenerator.generateSourceQuery(unf, getSignature(p));
				log.debug("SQL: {}", finasql);
				sqlforcqs.add(finasql);
			} catch (Exception e) {
				log.error("Error processing nested query #{}", i);
				log.error(e.getMessage(), e);
				throw new Exception("Error processing nested query #" + i, e);
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
			throw new SQLException("Invalid SQL. The SQL query cannot be empty");

		return finalquery.toString();

	}

	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getUnfolding(String strquery) throws Exception {
		return getUnfolding(strquery, true);
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getUnfolding(String strquery, boolean reformulate) throws Exception {
		DatalogProgram program = getDatalogQuery(strquery);
		
		log.debug("Replacing equivalences...");
		program = validator.replaceEquivalences(program);

		OBDAQuery unfolding = null;
		if (!reformulate) {
			unfolding = unfoldingmechanism.unfold(program);
		} else {
			OBDAQuery rewriting = rewriter.rewrite(program);
			unfolding = unfoldingmechanism.unfold((DatalogProgram) rewriting);
		}
		String sql = querygenerator.generateSourceQuery((DatalogProgram) unfolding, getSignature(program));
		return sql;
	}

	/**
	 * Returns the number of tuples returned by the query
	 */
	@Override
	public int getTupleCount(String query) throws Exception {

		String unf = getUnfolding(query);
		String newsql = "SELECT count(*) FROM (" + unf + ") t1";
		if (!canceled) {
			ResultSet set = statement.executeQuery(newsql);
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
	public void close() throws Exception {
		try {
			statement.close();
		} catch (SQLException e) {

		}
	}

	private DatalogProgram getDatalogQuery(String query) throws Exception {
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
			throw new Exception("Unsupported syntax");

		return queryProgram;
	}

	@Override
	public void addBatch(String query) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearBatch() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int executeUpdate(String query) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFetchSize() throws Exception {
		return statement.getFetchSize();

	}

	@Override
	public int getMaxRows() throws Exception {
		return statement.getMaxRows();

	}

	@Override
	public void getMoreResults() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFetchSize(int rows) throws Exception {
		statement.setFetchSize(rows);

	}

	@Override
	public void setMaxRows(int max) throws Exception {
		statement.setMaxRows(max);

	}

	@Override
	public void setQueryTimeout(int seconds) throws Exception {
		statement.setQueryTimeout(seconds);

	}

}
