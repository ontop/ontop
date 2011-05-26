package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.codec.DatalogProgramToTextCodec;
import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.DatalogProgram;
import inf.unibz.it.obda.model.Query;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.Variable;
import inf.unibz.it.obda.parser.DatalogProgramParser;
import inf.unibz.it.obda.parser.SPARQLDatalogTranslator;
import inf.unibz.it.obda.queryanswering.QueryResultSet;
import inf.unibz.it.obda.queryanswering.Statement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.runtime.RecognitionException;
import org.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import org.obda.owlrefplatform.core.reformulation.QueryRewriter;
import org.obda.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import org.obda.owlrefplatform.core.resultset.EmptyQueryResultSet;
import org.obda.owlrefplatform.core.resultset.OWLOBDARefResultSet;
import org.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import org.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryException;

/**
 * The obda statement provides the implementations necessary to query the
 * reformulation platform reasoner from outside, i.e. protege
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class OBDAStatement implements Statement {

	private QueryRewriter			rewriter			= null;
	private UnfoldingMechanism		unfoldingmechanism	= null;
	private SourceQueryGenerator	querygenerator		= null;
	private EvaluationEngine		engine				= null;
	// private DatalogProgram query = null;
	private APIController			apic				= null;
	private boolean					canceled			= false;

	// private DatalogProgram rewriting = null;
	// private DatalogProgram unfolding = null;
	// private QueryResultSet result = null;

	Logger							log					= LoggerFactory.getLogger(OBDAStatement.class);

	public OBDAStatement(UnfoldingMechanism unf, QueryRewriter rew, SourceQueryGenerator gen, EvaluationEngine eng, APIController apic) {

		this.rewriter = rew;
		this.unfoldingmechanism = unf;
		this.querygenerator = gen;
		this.engine = eng;
		// this.query = query;
		this.apic = apic;
		// if (query.getRules().size() > 0) {
		// Atom head = query.getRules().get(0).getHead();
		// querygenerator.getViewManager().storeOrgQueryHead(head);
		// }

	}

	/**
	 * Returns the result set for the given query
	 */
	@Override
	public QueryResultSet executeQuery(String strquery) throws Exception {

		if (strquery.split("[eE][tT][aA][bB][lL][eE]").length > 1) {
			return executeEpistemicQuery(strquery);
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
	private QueryResultSet executeEpistemicQuery(String strquery) throws Exception {
		QueryResultSet result;

		String epistemicUnfolding = getUnfoldingEpistemic(strquery);
		ResultSet set = engine.execute(epistemicUnfolding);
		result = new OWLOBDARefResultSet(set);

		return result;
	}

	private QueryResultSet executeConjunctiveQuery(String strquery) throws Exception {
		DatalogProgram program = getDatalogQuery(strquery);

		List<String> signature = getSignature(program);

		Query rewriting = rewriter.rewrite(program);
		Query unfolding = unfoldingmechanism.unfold((DatalogProgram) rewriting);
		String sql = querygenerator.generateSourceQuery((DatalogProgram) unfolding, signature);

		QueryResultSet result;

		if (sql.equals("")) {
			/***
			 * Empty unfolding, constructing an empty resultset
			 */
			if (program.getRules().size() < 1)
				throw new Exception("Error, empty query");
			
			result = new EmptyQueryResultSet(signature);
		} else {
			ResultSet set = engine.execute(sql);
			if (isDPBoolean(program)) {
				result = new BooleanOWLOBDARefResultSet(set);
			} else {
				result = new OWLOBDARefResultSet(set);
			}
		}

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
				signature.add(term.getName());
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
		Query rewriting = rewriter.rewrite(program);
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
		DatalogProgram program = getDatalogQuery(strquery);
		Query rewriting = rewriter.rewrite(program);
		Query unfolding = unfoldingmechanism.unfold((DatalogProgram) rewriting);
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
			ResultSet set = engine.execute(newsql);
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
		engine.closeStatement();
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

}
