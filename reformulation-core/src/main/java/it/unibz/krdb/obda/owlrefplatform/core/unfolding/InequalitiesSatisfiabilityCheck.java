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
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;


class InequalitiesSatisfiabilityCheck {
	private final OBDADataFactory fac;
	
	InequalitiesSatisfiabilityCheck(OBDADataFactory fac){
		this.fac = fac;
	}
	
	/*NTripleAssertionIteratorTest.testIteratorTest
	 * Return the Double value of a Constant object that has "numerical value"
	 * FIXME TODO!
	 */
	private Double value(Constant c) {
		String value = c.getValue();
		COL_TYPE type = c.getType();
		if (type == COL_TYPE.INT || type == COL_TYPE.INTEGER || type == COL_TYPE.LONG ||
			type == COL_TYPE.NON_POSITIVE_INTEGER || type== COL_TYPE.POSITIVE_INTEGER ||
			type == COL_TYPE.NON_NEGATIVE_INTEGER || type== COL_TYPE.NEGATIVE_INTEGER ||
			type == COL_TYPE.UNSIGNED_INT) {
			return new Long(Long.parseLong(value)).doubleValue();
		} else if (type == COL_TYPE.DOUBLE || type == COL_TYPE.FLOAT) {
			return Double.parseDouble(value);
		}
		return null;
	}
	
	private boolean supported_constant(Constant c) {
		COL_TYPE type = c.getType();
		return type == COL_TYPE.INTEGER || type == COL_TYPE.LONG || type == COL_TYPE.DOUBLE || type == COL_TYPE.FLOAT;
	}

	/*
	 * Compare two Constant objects as in the Java convention. See Comparable.
	 * TODO
	 */
	private int constants_compare(Constant o1, Constant o2) { 
		Double val1 = value(o1);
		Double val2 = value(o2);
		if (val1 == null || val2 == null) {
			return -1; // FIXME
		}
		return Double.compare(val1, val2);
	}
	
	/*
	 * Check unsatisfiability of a query with respect to the numerical comparisons occurring in it;
	 * this version checks (un)satisfiability on the real domain.
	 * The algorithm is implemented after "Sha Guo et al: Solving Satisfiability and Implication Problems in Database Systems".
	 * 
	 * Return true if the query is found unsatisfiable, false otherwise.
	 */
	public boolean check(CQIE q) {
		List<Function> atoms = new ArrayList<>(q.getBody());
		
		/*
		 * The minimum ranges of the variables among the comparisons
		 */
		HashMap<Variable, Constant[]> mranges = new HashMap<>();
		
		/*
		 * The un-equality (neq) constraints
		 */
		HashMap<Variable, Set<Term>> neq = new HashMap<>();
		
		/*
		 * The directed graph that contains the greater-than-or-equal (gte) relations
		 */
		DirectedGraph<Term, DefaultEdge> gteGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		/*
		 * The first step is to eliminate trivial inequalities; anyway
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
			
			/*
			 * Ignore unsupported constants
			 */
			if (t0 instanceof Constant && !supported_constant((Constant) t0))
					continue;
			if (t1 instanceof Constant && !supported_constant((Constant) t1))
					continue;
			
			/*
			 * Remove LT's and LTE's by swapping the terms
			 */
			if (pred == OBDAVocabulary.LTE) {
				//atom = fac.getFunctionGTE(t1, t0);
				Term tmp = t0; t0 = t1; t1 = tmp;
				pred = OBDAVocabulary.GTE;
			} else if (pred == OBDAVocabulary.LT) {
				//atom = fac.getFunctionGT(t1, t0);
				Term tmp = t0; t0 = t1; t1 = tmp;
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
				atoms.add(fac.getFunctionGTE(t1, t0));
			}
			
			if (pred == OBDAVocabulary.NEQ) {
				if (t0 instanceof Variable) {
					if (!neq.containsKey(t0)) {
						neq.put((Variable) t0, new HashSet<Term>());
					}
					neq.get((Variable) t0).add(t1);
				}
				if (t1 instanceof Variable) {
					if (!neq.containsKey(t1)) {
						neq.put((Variable) t1, new HashSet<Term>());
					}
					neq.get((Variable) t1).add(t0);
				}
			} else if (pred == OBDAVocabulary.GTE) {
				if (t0 instanceof Variable && t1 instanceof Constant) {
					Constant[] bounds;
					if (!mranges.containsKey((Variable) t0)) {
						bounds = new Constant[]{null, null};
						mranges.put((Variable) t0, bounds);
					} else {
						bounds = mranges.get((Variable) t0);
					}
					if (bounds[0] == null || this.constants_compare((Constant) t1, bounds[0]) == 1) {
						bounds[0] = (Constant) t1;
					}
				} else if (t0 instanceof Constant && t1 instanceof Variable) {
					Constant[] bounds;
					if (!mranges.containsKey((Variable) t1)) {
						bounds = new Constant[]{null, null};
						mranges.put((Variable)  t1, bounds);
					} else {
						bounds = mranges.get((Variable) t1);
					}
					if (bounds[1] == null || this.constants_compare((Constant) t0, bounds[1]) == -1) {
						bounds[1] = (Constant) t0;
					}
				} else if (t0 instanceof Variable && t1 instanceof Variable) {
					gteGraph.addVertex(t0);
					gteGraph.addVertex(t1);
					gteGraph.addEdge(t0, t1);
				}
			}
			
		}
		
		List<Constant> constants = new ArrayList<>();
		
		/*
		 * Add to the graph the constraints about variables
		 */
		for (Entry<Variable, Constant[]> cursor: mranges.entrySet()) {
			Constant bounds[] = cursor.getValue();
			if (bounds[0] != null && bounds[1] != null && this.constants_compare(bounds[0], bounds[1]) == 1) {
				return true;
			}
			if (bounds[0] != null) {
				gteGraph.addVertex(bounds[0]);
				gteGraph.addVertex(cursor.getKey());
				gteGraph.addEdge(cursor.getKey(), bounds[0]);
				constants.add(bounds[0]);
			}
			if (bounds[1] != null) {
				gteGraph.addVertex(bounds[1]);
				gteGraph.addVertex(cursor.getKey());
				gteGraph.addEdge(bounds[1], cursor.getKey());
				constants.add(bounds[1]);
			}
		}
		
		/*
		 * Add to the graph the number constraints
		 */
		Collections.sort(constants, new Comparator<Constant>() {
			public int compare(Constant o1, Constant o2) {
				return constants_compare(o1, o2);
			}
		});
		for (int i = 0; i < constants.size() - 1; i += 1) {
			if (!constants.get(i).equals(constants.get(i + 1))) {
				gteGraph.addEdge(constants.get(i), constants.get(i + 1));
				//neq.get(constants.get(i)).add(constants.get(i + 1));
			}
		}
		
		/*
		 * Compute the strongly connected components of the gteGraph
		 */
		StrongConnectivityInspector<Term, DefaultEdge> insp = new StrongConnectivityInspector<Term, DefaultEdge>(gteGraph);
		List<Set<Term>> scc = insp.stronglyConnectedSets();
		
		for (Set<Term> component: scc) {
			Constant cur = null;
			Set<Term> forbidden = new HashSet<>();
			for (Term t: component) {
				/*
				 * Check if the component contains two constants that are different
				 */
				if (t instanceof Constant) {
					if (cur == null) {
						cur = (Constant)t;
					} else if (!t.equals(cur)) {
						return true;
					}
				}
				if (t instanceof Variable && neq.containsKey((Variable) t))
					forbidden.addAll(neq.get((Variable) t));
			}
			if (!Collections.disjoint(component, forbidden)) {
				return true;
			}
		}
		
		return false;
	}

	
}
