package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.queryanswering.QueryResultSet;
import inf.unibz.it.obda.queryanswering.Statement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import org.obda.owlrefplatform.core.reformulation.QueryRewriter;
import org.obda.owlrefplatform.core.resultset.OWLOBDARefResultSet;
import org.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import org.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.tools.parser.SPARQLDatalogTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OBDAEpistemicStatement implements Statement {

	private QueryRewriter			rewriter			= null;
	private UnfoldingMechanism		unfoldingmechanism	= null;
	private SourceQueryGenerator	querygenerator		= null;
	private EvaluationEngine		engine				= null;
	private String					query				= null;
//	private APIController			apic				= null;
	private boolean					canceled			= false;

//	private DatalogProgram			rewriting			= null;
//	private DatalogProgram			unfolding			= null;
	private QueryResultSet			result				= null;
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	public OBDAEpistemicStatement(UnfoldingMechanism unf, QueryRewriter rew, SourceQueryGenerator gen, EvaluationEngine eng, String query) {

		this.rewriter = rew;
		this.unfoldingmechanism = unf;
		this.querygenerator = gen;
		this.engine = eng;
		this.query = query;
//		this.apic = apic;

	}

	/**
	 * Returns the result set for the given query
	 */
	public QueryResultSet getResultSet() throws Exception {

		if (result == null) {
			
			
			// FIRST WE try to analyze the query to find the CQs and the SQL part
			
			LinkedList<String> sql = new LinkedList<String>();
			LinkedList<String> cqs = new LinkedList<String>();

			StringBuffer query = new StringBuffer(this.query);
			Pattern pattern = Pattern.compile("[eE][tT][aA][bB][lL][eE]\\s*\\((\\r?\\n|\\n|.)+?\\)",Pattern.MULTILINE);
			
			while (true) {
				
				String[] splitquery = pattern.split(query.toString(), 2);
				if (splitquery.length > 1) {
					sql.add(splitquery[0]);
					query.delete(0, splitquery[0].length());
					int position = query.toString().indexOf(splitquery[1]);
					
					String regex = query.toString().substring(0,position); 
					
					cqs.add(regex.substring(regex.indexOf("(")+1, regex.length()-1));
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
			for (int i =0; i < cqs.size(); i++) {
				log.debug("Processing embedded query #{}",i);
				String cq = cqs.get(i);
				try {
				DatalogProgram p = t.parse(cq);
				DatalogProgram rew = (DatalogProgram)rewriter.rewrite(p);
				DatalogProgram unf = unfoldingmechanism.unfold(rew);
				querygenerator.getViewManager().storeOrgQueryHead(p.getRules().get(0).getHead());
				String finasql = querygenerator.generateSourceQuery(unf);
				log.debug("SQL: {}", finasql);
				sqlforcqs.add(finasql);
				} catch (Exception e) {
					log.error("Error processing nested query #{}", i);
					log.error(e.getMessage(), e);
					throw new Exception("Error processing nested query #" + i, e);
				}
			}
			
			// Now we concatenate the simple SQL with the rewritten SQL to generate the query over DB.
			
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

			ResultSet set = engine.execute(finalquery.toString());
			result = new OWLOBDARefResultSet(set);	
		}
		return result;
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public DatalogProgram getRewriting(DatalogProgram query) throws Exception {

		DatalogProgram rewriting = (DatalogProgram) rewriter.rewrite(query);
		return rewriting;
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getUnfolding(DatalogProgram rewriting) throws Exception {

		String sql = null;
		DatalogProgram unfolding = unfoldingmechanism.unfold(rewriting);
		sql = querygenerator.generateSourceQuery(unfolding);
		return sql;
	}

	/**
	 * Returns the number of tuples returned by the query
	 */
	@Override
	public int getTupleCount() throws Exception {

		String unf = getUnfolding();
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

	@Override
	public String getRewriting() throws Exception {
		throw new Exception("Epistemic Statements dont support this operation.");
	}

	@Override
	public String getUnfolding() throws Exception {
		throw new Exception("Epistemic Statements dont support this operation.");
	}

}
