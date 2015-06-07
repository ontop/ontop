package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


class InequalitiesSatisfiability {	
	private static final Logger LOGGER = LoggerFactory.getLogger(InequalitiesSatisfiability.class);
	private static final OBDADataFactory FACTORY = OBDADataFactoryImpl.getInstance();
	private static final DatatypeFactory DTFACTORY = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	
	/**
	 * @return the Double value of a numeric Constant object
	 */
	public static Double constantValue(Constant c) {
		String value = c.getValue();
		COL_TYPE type = c.getType();
		
		if (type == null)
			return null;
		
		Predicate p = DTFACTORY.getTypePredicate(type);
		
		if(DTFACTORY.isInteger(p)) {
			return new Long(Long.parseLong(value)).doubleValue();
		} else if(DTFACTORY.isFloat(p)) { 
			return Double.parseDouble(value);
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if a Constant is either an Integer or a Float number
	 *
	private static boolean isNumeric(Constant c) {
		Predicate.COL_TYPE type = c.getType();
		if (type != null) {
			Predicate p = DTFACTORY.getTypePredicate(type);
			return DTFACTORY.isInteger(p) || DTFACTORY.isFloat(p);
		}
		return false;	
	}
	*/
	
	/**
	 * @return Constant object that corresponds to some Double
	 */
	private static Constant doubleToConstant(Double d) {
		return FACTORY.getConstantLiteral(d.toString(), COL_TYPE.DOUBLE);
	}
	
	/**
	 * Checks unsatisfiability of a query with respect to the numerical comparisons occurring in it;
	 * this version checks (un)satisfiability on the domain of real numbers.
	 * The algorithm is implemented after "Sha Guo et al: Solving Satisfiability and Implication Problems in Database Systems".
	 * 
	 * @param q the OBA query to check for satisfiability
	 * @return true if the query is found unsatisfiable, false otherwise.
	 */
	public static boolean unsatisfiable(CQIE q) {
		LOGGER.debug("Checking satisfiability of OBDA query");
		return check(q.getBody());
	}

	/**
	 * @param body the body of an OBDA query
	 * @return true if the query is found unsatisfiable, false otherwise.
	 */
	public static boolean check(List<Function> body) {
		List<Function> atoms = new ArrayList<>(body);
		
		/*
		 * The minimum ranges of the variables among the comparisons
		 */
		Map<Variable, DoubleInterval> mranges = new HashMap<Variable, DoubleInterval>() {
			private static final long serialVersionUID = 1L;
			/*
			 * Override the get method in order to use default intervals  
			 */
			@Override
		    public DoubleInterval get(Object key) {
		    	if(!containsKey(key)) 
		    		return new DoubleInterval();
		    	return super.get(key);
		    }
		};
		
		/*
		 * The unequality (neq) constraints
		 */
		Map<Variable, Set<Term>> neq = new HashMap<Variable, Set<Term>>() {
			private static final long serialVersionUID = 1L;
			/*
			 * Override the get method in order to use default values
			 */
			@Override
			public Set<Term> get(Object key) {
		    	if(!containsKey(key)) {
		    		Set<Term> val = new HashSet<>();
		    		put((Variable) key, val);
		    		return val;
		    	}
		    	return super.get(key);
			}
		};
		
		/*
		 * The directed graph that contains the greater-than-or-equal (gte) relations
		 */
		DirectedGraph<Term, DefaultEdge> gteGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		/*
		 * The constants occurring in the inequalities atoms in the query
		 */
		List<Double> constants = new ArrayList<>();
		
		/*
		 * First step: eliminate trivial inequalities
		 * This is already performed in ExpressionEvaluator.java:
		 * - elimination of "const1 (op) const2": evalEqNeq, evalGtLt, evalGteLte;
		 * - elimination of "# = #", "# != #" : evalEqNeq.
		 */

		/*
		 * Scan the atoms in the body:
		 * - build the minimum range of each variable, that is the real interval
		 *   in which that variable is constrained to be, obtained by the
		 *   inequalities between variables and constants
		 * - start constructing the graph with the greater-than-or-equal relations
		 * - store the not-equal constraints  
		 */
		if (scanAtoms(atoms, mranges, neq, gteGraph))
			return true;
		
		/*
		 * Transport the information gathered with the minimum range of variables
		 * inside the greater-than-or-equal graph
		 */
		constrainVariablesRanges(mranges, gteGraph, constants);
		
		/*
		 * Encode in the graph the information about the linear ordering of
		 * real valued constants
		 */
		if (!constants.isEmpty()) {
			constrainConstantsOrder(gteGraph, constants);
		}
		
		/*
		 * Compute and inspect the strongly connected components of the graph
		 */
		return inspectStronglyConnectedComponents(neq, gteGraph);
	}

	/**
	 * Scan the list of atoms building the minimum range of variables, 
	 * constructing the greater-than-or-equal graph, storing the not-equals constraints 
	 */
	private static boolean scanAtoms(List<Function> atoms,
			Map<Variable, DoubleInterval> mranges,
			Map<Variable, Set<Term>> neq,
			DirectedGraph<Term, DefaultEdge> gteGraph) {
		
		for (int atomidx = 0; atomidx < atoms.size(); atomidx++) {
			Function atom = atoms.get(atomidx);
			Predicate pred = atom.getFunctionSymbol();
			
			if (!(pred instanceof BooleanOperationPredicate) || pred.getArity() != 2) {
				continue;
			}
			LOGGER.debug("binary BooleanOperationPredicate: " + atom.toString());
			Term t0 = atom.getTerm(0),
				 t1 = atom.getTerm(1);
			/*
			 * Ignore unsupported constants
			 *
			if (t0 instanceof Constant && !isNumeric((Constant) t0))
					continue;
			if (t1 instanceof Constant && !isNumeric((Constant) t1))
					continue;
			*/
			
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
				atoms.add(FACTORY.getFunctionNEQ(t0, t1));
			} /*
			   * Replace EQ by two GTE  
			   */
			  else if (pred == OBDAVocabulary.EQ) {
				pred = OBDAVocabulary.GTE;
				atoms.add(FACTORY.getFunctionGTE(t1, t0));
			}
			
			if (pred == OBDAVocabulary.NEQ) {
				if (t0 instanceof Variable) {
					neq.get((Variable) t0).add(t1);
				}
				if (t1 instanceof Variable) {
					neq.get((Variable) t1).add(t0);
				}
			} else if (pred == OBDAVocabulary.GTE) {
				if (t0 instanceof Variable && t1 instanceof Constant) {
					Variable var = (Variable) t0;
					Double value = constantValue((Constant) t1);
					if (value != null) {
						DoubleInterval interval; 
						try {
							interval = mranges.get(var).withLowerBound(value);
						} catch (IllegalArgumentException e) {
							return true;
						}
						mranges.put(var, interval);
					}
				} else if (t0 instanceof Constant && t1 instanceof Variable) {
					Variable var = (Variable) t1;
					Double value = constantValue((Constant) t0);
					if (value != null) {
						DoubleInterval interval; 
						try {
							interval = mranges.get(var).withUpperBound(value);
						} catch (IllegalArgumentException e) {
							return true;
						}
						mranges.put(var, interval);
					}
				} else if (t0 instanceof Variable && t1 instanceof Variable) {
					gteGraph.addVertex(t0);
					gteGraph.addVertex(t1);
					gteGraph.addEdge(t0, t1);
				}
			}
			
		}
		
		return false;
	}

	/**
	 * Add to the graph for each variable the fact that 
	 * lowerBound <= variable <= upperBound holds.
	 */
	private static void constrainVariablesRanges(
			Map<Variable, DoubleInterval> mranges,
			DirectedGraph<Term, DefaultEdge> gteGraph, List<Double> constants) {
		
		for (Entry<Variable, DoubleInterval> cursor: mranges.entrySet()) {
			LOGGER.debug("mrange " + cursor.getKey());
			DoubleInterval interval = cursor.getValue();
			Double lowerBound = interval.getLowerBound();
			Double upperBound = interval.getUpperBound();
			if (lowerBound > Double.NEGATIVE_INFINITY) {
				Constant c = doubleToConstant(lowerBound);
				gteGraph.addVertex(c);
				gteGraph.addVertex(cursor.getKey());
				gteGraph.addEdge(cursor.getKey(), c);
				LOGGER.debug("lower bound edge: " + cursor.getKey() + "->" + lowerBound);
				constants.add(lowerBound);
			}
			if (upperBound < Double.POSITIVE_INFINITY) {
				Constant c = doubleToConstant(upperBound);
				gteGraph.addVertex(c);
				gteGraph.addVertex(cursor.getKey());
				gteGraph.addEdge(c, cursor.getKey());
				LOGGER.debug("upper bound edge: " + upperBound + "->" + cursor.getKey());
				constants.add(upperBound);
			}
		}
	}

	/**
	 * Adds to the graph the constraints about the constants encountered:
	 * these concern the comparison chain c_0 <= c_1 <= ... <= c_n
	 */
	private static void constrainConstantsOrder(
			DirectedGraph<Term, DefaultEdge> gteGraph, List<Double> constants) {
		
		/*
		 * Linearly sort the constants encountered
		 */
		Collections.sort(constants);
		
		Constant last = doubleToConstant(constants.get(0));
		Constant tmp;
		for (int i = 0, size = constants.size(); i < size - 1; i += 1) {
			/*
			 * Ignore the duplicates:
			 */
			if (!constants.get(i).equals(constants.get(i + 1))) {
				tmp = doubleToConstant(constants.get(i + 1));
				gteGraph.addEdge(tmp, last);
				LOGGER.debug("number constraint edge: " + tmp.getValue() + "->" + last.getValue());
				last = tmp;
			}
		}
	}

	/**
	 * Compute the strongly connected components of the gteGraph
	 * and check them against the neq constraints
	 * @return does the gteGraph violates the neq constraints?
	 */
	private static boolean inspectStronglyConnectedComponents(
			Map<Variable, Set<Term>> neq,
			DirectedGraph<Term, DefaultEdge> gteGraph) {
		
		StrongConnectivityInspector<Term, DefaultEdge> insp = new StrongConnectivityInspector<>(gteGraph);
		List<Set<Term>> scc = insp.stronglyConnectedSets();
		
		for (Set<Term> component: scc) {
			Constant cur = null;
			LOGGER.debug("s-c-component: " + Arrays.toString(component.toArray()));
			Set<Term> forbidden = new HashSet<>();
			for (Term t: component) {
				/*
				 * Check if the component contains two constants that are different
				 */
				if (t instanceof Constant) {
					if (cur == null) {
						cur = (Constant) t;
					} else if (!t.equals(cur)) {
						return true;
					}
				}
				if (t instanceof Variable && neq.containsKey((Variable) t))
					forbidden.addAll(neq.get((Variable) t));
			}
			/*
			 * Check for violation of a neq-constraint
			 */
			if (!Collections.disjoint(component, forbidden)) {
				return true;
			}
		}
		
		return false;
	}
	
}
