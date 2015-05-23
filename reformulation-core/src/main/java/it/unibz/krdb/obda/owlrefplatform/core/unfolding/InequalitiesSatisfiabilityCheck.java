package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


/*
 * ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.setUriTemplateMatcher(questInstance.getUriTemplateMatcher());
		evaluator.evaluateExpressions(unfolding);
 */

class InequalitiesSatisfiabilityCheck {
	private OBDADataFactory fac;
	
	InequalitiesSatisfiabilityCheck(OBDADataFactory fac){
		this.fac = fac;
	}
	
	/*private Double value(Constant c) {
		// FIXME see also: DatatypeFactoryImpl for XSD_*, RDFS_*
		String value = c.getValue();
		COL_TYPE type = c.getType();
		if (type == COL_TYPE.INT) {
			return new Long(Long.parseLong(value)).doubleValue();
		} else if (type == COL_TYPE.DOUBLE || type == COL_TYPE.FLOAT) {
			return Double.parseDouble(value);
		}
		return null;
	}*/
	
	public boolean check(CQIE q) {
		/*
		 * Check unsatisfiability of a query, following ? the algorithm for solving the
		 * satisfiability problem on the real domain that can be found in
		 * Sha Guo and Wei Sun and Mark A. Weiss, "Solving Satisfiability and Implication Problems in Database Systems".
		 * 
		 * This function returns true iff the query is found unsatisfiable by this method.
		 */
		
		List<Function> atoms = (List<Function>) ((ArrayList<Function>) q.getBody()).clone();		
		List<Constant> constants = new ArrayList<Constant>();
		DirectedGraph<Term, DefaultEdge> gteGraph = new DefaultDirectedGraph<Term, DefaultEdge>(DefaultEdge.class);
		
		/*
		 * The first step is to eliminate trivial inequalities,
		 * this was already performed in ExpressionEvaluator.java:
		 * - elimination of "C1 op C2": evalEqNeq, evalGtLt, evalGteLte;
		 * - elimination of "# = #", "# != #": evalEqNeq.
		 */
		
		for (int atomidx = 0; atomidx < atoms.size(); atomidx++) {
			Function atom = atoms.get(atomidx);
			Predicate pred = atom.getFunctionSymbol();
			
			if (!(pred instanceof BooleanOperationPredicate)) {
				continue;
			}
			Term t0 = atom.getTerm(0),
				 t1 = atom.getTerm(1);
			
			if (t0 instanceof Constant)
				constants.add((Constant) t0);
			if (t1 instanceof Constant)
				constants.add((Constant) t1);
			
			/*
			 * Remove LT's and LTE's by swapping the terms
			 */
			if (pred == OBDAVocabulary.LTE) {
				atom = fac.getFunctionGTE(t1, t0);
				pred = OBDAVocabulary.GTE;
			} else if (pred == OBDAVocabulary.LT) {
				atom = fac.getFunctionGT(t1, t0);
				pred = OBDAVocabulary.GT;
			}
			
			/*
			 * Flatten ANDs
			 */
			if (pred == OBDAVocabulary.AND) {
				if (t0 instanceof Function)
					atoms.add((Function) t0);
				if (t1 instanceof Function)
					atoms.add((Function) t1);
				continue;
			} /*
			   * Replace GT with GTE and NEQ
			   */
			  else if (pred == OBDAVocabulary.GT){
				pred = OBDAVocabulary.GTE;
				atoms.add(fac.getFunctionNEQ(t0, t1));
			} /*
			   * Replace EQ by two GTE  
			   */
			  else if (pred == OBDAVocabulary.EQ) {
				pred = OBDAVocabulary.GTE;
				atoms.add(fac.getFunctionNEQ(t1, t0));
			}
			
			if (pred == OBDAVocabulary.NEQ) {
				if ((t0 instanceof Variable || t0 instanceof Constant) &&
					(t1 instanceof Variable || t1 instanceof Constant) ) {
						//neqGraph.addEdge(t0, t1);
					}				
			} else if (pred == OBDAVocabulary.GTE) {
				if ((t0 instanceof Variable || t0 instanceof Constant) &&
					(t1 instanceof Variable || t1 instanceof Constant) ) {
					gteGraph.addEdge(t0, t1);
				}
			}
			
		}
		
		/*
		 * Add the number constraints
		 */
		Collections.sort(constants, new Comparator<Constant>() {
			public int compare(Constant one, Constant two) {
				return 1;
			}
		});
		for (int i = 0; i < constants.size() - 1; i += 1) {
			if (!constants.get(i).equals(constants.get(i + 1))) {
				gteGraph.addEdge(constants.get(i), constants.get(i + 1));
				//neqGraph.addEdge(constants.get(i), constants.get(i + 1));
			}
		}

		/*
		 * Construct the labelled directed graph
		 */
		//List<List<Integer>> connections = new ArrayList<List<Integer>>();
		/*
		 * Detect all SCCs
		 */
		//List<List<Integer>> sccs = this.tarjan(connections);
		
		/*
		 *  Otherwise, collapse SCCs and obtain an acyclic graph GScollapsed
		 */
		
		return false;
	}

	
}
