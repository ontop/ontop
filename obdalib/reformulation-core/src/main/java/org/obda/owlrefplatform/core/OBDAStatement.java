package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.queryanswering.QueryResultSet;
import inf.unibz.it.obda.queryanswering.Statement;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import org.obda.owlrefplatform.codecs.DatalogProgramToTextCodec;
import org.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import org.obda.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import org.obda.owlrefplatform.core.resultset.OWLOBDARefResultSet;
import org.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import org.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.reformulation.dllite.QueryRewriter;

/**
 * The obda statement provides the implementations necessary to query 
 * the reformulation platform reasoner from outside, i.e. protege
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class OBDAStatement implements Statement {

	private QueryRewriter rewriter = null;
	private UnfoldingMechanism unfoldingmechanism = null;
	private SourceQueryGenerator querygenerator = null;
	private EvaluationEngine engine = null;
	private DatalogProgram query = null;
	private APIController apic = null;

	private DatalogProgram rewriting = null;
	private DatalogProgram unfolding = null;
	private QueryResultSet result = null;

	public OBDAStatement(UnfoldingMechanism unf,
			QueryRewriter rew , SourceQueryGenerator gen, EvaluationEngine eng,
			DatalogProgram query,APIController apic){

		this.rewriter = rew;
		this.unfoldingmechanism = unf;
		this.querygenerator = gen;
		this.engine = eng;
		this.query = query;
		this.apic = apic;
		if(query.getRules().size() > 0){
			Atom head = query.getRules().get(0).getHead();
			querygenerator.getViewManager().storeOrgQueryHead(head);
		}

	}

	/**
	 * Returns the result set for the given query
	 */
	public QueryResultSet getResultSet() throws Exception {

		if(result == null){
			getUnfolding();
			String sql = querygenerator.generateSourceQuery(unfolding);
			if(sql.equals("")){
				
			}else{
				ResultSet set= engine.execute(sql);
				if(isDPBoolean(query)){
					result = new BooleanOWLOBDARefResultSet(set);
				}else{
					result = new OWLOBDARefResultSet(set);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getRewriting() throws Exception {

		if(rewriting == null){
			rewriting = (DatalogProgram) rewriter.rewrite(query);
		}
		DatalogProgramToTextCodec codec = new DatalogProgramToTextCodec(apic);
		return codec.encode(rewriting);
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getUnfolding() throws Exception {

		String sql = null;
		if(unfolding == null){
			getRewriting();
			unfolding = unfoldingmechanism.unfold(rewriting);
			sql = querygenerator.generateSourceQuery(unfolding);
		}
		return sql;
	}

	/**
	 * Returns the number of tuples returned by the query
	 */
	@Override
	public int getTupleCount() throws Exception {

		String unf = getUnfolding();
		String newsql = "SELECT count(*) FROM (" +unf+") t1";
		ResultSet set= engine.execute(newsql);
		if(set.next()){
			return set.getInt(1);
		}else{
			throw new Exception("Tuple count faild due to empty result set.");
		}
	}

	/**
	 * Checks whether the given query is boolean or not
	 * @param dp the given datalog program
	 * @return true if the query is boolean, false otherwise
	 */
	private boolean isDPBoolean(DatalogProgram dp){

		List<CQIE> rules = dp.getRules();
		Iterator<CQIE> it = rules.iterator();
		boolean bool = true;
		while(it.hasNext() && bool){
			CQIE query = it.next();
			Atom a = query.getHead();
			if(a.getTerms().size() !=0){
				bool = false;
			}
		}
		return bool;
	}

}
