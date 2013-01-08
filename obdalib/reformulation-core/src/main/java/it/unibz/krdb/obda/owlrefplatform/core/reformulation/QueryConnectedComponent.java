package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * QueryConnectedComponent represents a connected component of a CQ
 * 
 * keeps track of variables (both quantified and free) and edges
 * 
 * a connected component can either be degenerate (if it has no proper edges, i.e., just a loop)
 * 
 * @author Roman Kontchakov
 *
 */

public class QueryConnectedComponent {

	private List<NewLiteral> variables; 	
	private List<Loop> quantifiedVariables;   
	private List<NewLiteral> freeVariables;
	
	private final List<Edge> edges;  // a connect component contains a list of edges 
	private final Loop loop;  //                                   or a loop if it is degenerate 
	
	private boolean noFreeTerms; // no free variables and no constants 
	                             // if true the component can be mapped onto the anonymous part of the canonical model

	
	/**
	 * constructor: it is private as instances created only by the static method getConnectedComponents
	 * 
	 * @param edges: a list of edges in the connected component
	 * @param terms: terms that are coveted by the edges
	 */
	
	private QueryConnectedComponent(List<Edge> edges, Loop loop, List<Loop> terms) {
		this.edges = edges;
		this.loop = loop;

		quantifiedVariables = new ArrayList<Loop>(terms.size());
		variables = new ArrayList<NewLiteral>(terms.size());
		freeVariables = new ArrayList<NewLiteral>(terms.size());
		noFreeTerms = true;
		
		for (Loop l: terms) {
			NewLiteral t = l.getTerm(); 
			if (t instanceof Variable) {
				variables.add(t);
				//if (headNewLiterals.contains(t))
				if (l.isExistentialVariable())
					quantifiedVariables.add(l);
				else 
					{
					freeVariables.add(t);
					noFreeTerms = false;
				}
			}
			else
				noFreeTerms = false; // not a variable -- better definition?
		}
	}
	
	private static boolean isExistentialVariable(NewLiteral t, Set<NewLiteral> headTerms) {
		return ((t instanceof Variable) && !headTerms.contains(t));
	}

	/**
	 * getConnectedComponents creates a list of connected components of a given CQ
	 * 
	 * @param cqie: CQ to be split into connected components 
	 * @return list of connected components
	 */
	
	public static List<QueryConnectedComponent> getConnectedComponents(CQIE cqie) {
		List<QueryConnectedComponent> ccs = new ArrayList<QueryConnectedComponent>();

		Set<NewLiteral> headTerms = new HashSet<NewLiteral>(cqie.getHead().getTerms());


		// collect all edges and loops 
		//      an edge is a binary predicate P(t, t') with t \ne t'
		// 		a loop is either a unary predicate A(t) or a binary predicate P(t,t)
		Map<TermPair, Edge> pairs = new HashMap<TermPair, Edge>();
		Map<NewLiteral, Loop> allLoops = new HashMap<NewLiteral, Loop>();
		
		for (Atom a: cqie.getBody()) {
			NewLiteral t0 = a.getTerm(0);				
			if (a.getArity() == 2 && !t0.equals(a.getTerm(1))) {
				NewLiteral t1 = a.getTerm(1);
				TermPair pair = new TermPair(t0, t1);
				Edge edge =  pairs.get(pair); 
				if (edge == null) {
					Loop l0 = allLoops.get(t0);
					if (l0 == null) {
						l0 = new Loop(t0, isExistentialVariable(t0, headTerms));
						allLoops.put(t0, l0);
					}
					Loop l1 = allLoops.get(t1);
					if (l1 == null) {
						l1 = new Loop(t1, isExistentialVariable(t1, headTerms));
						allLoops.put(t1, l1);
					}				
					edge = new Edge(l0, l1);
					pairs.put(pair, edge);
				}
				edge.bAtoms.add(a);			
			}
			else {
				Loop l0 = allLoops.get(t0);
				if (l0 == null) {
					l0 = new Loop(t0, isExistentialVariable(t0, headTerms));
					allLoops.put(t0, l0);
				}
				l0.atoms.add(a);
			}
		}	
		
		// form the list of connected components from the list of edges
		while (!pairs.isEmpty()) {
			List<Edge> ccEdges = new ArrayList<Edge>(pairs.size());
			Set<NewLiteral> ccTerms = new HashSet<NewLiteral>((allLoops.size() * 2) / 3);
			Iterator<Entry<TermPair, Edge>> i = pairs.entrySet().iterator();
			List<Loop> ccLoops = new ArrayList<Loop>(allLoops.size());
			
			// add the first available edge to the current CC
			Edge edge0 = i.next().getValue();
			ccEdges.add(edge0);
			ccTerms.add(edge0.getTerm0());
			ccLoops.add(edge0.getLoop0());
			allLoops.remove(edge0.getTerm0());
			ccTerms.add(edge0.getTerm1());
			ccLoops.add(edge0.getLoop1());
			allLoops.remove(edge0.getTerm1());
			i.remove();
			
			// expand the current CC by adding all edges that are have at least one of the NewLiterals in them
			boolean expanded = true;
			while (expanded) {
				expanded = false;
				i = pairs.entrySet().iterator();
				while (i.hasNext()) {
					Edge edge = i.next().getValue();
					NewLiteral t0 = edge.getTerm0();
					NewLiteral t1 = edge.getTerm1();
					if (ccTerms.contains(t0)) {
						if (ccTerms.add(t1))  { // the other NewLiteral is already there
							ccLoops.add(edge.getLoop1());
							allLoops.remove(t1); // remove the loops that are covered by the edges in CC
						}
					}
					else if (ccTerms.contains(t1)) {
						if (ccTerms.add(t0))  {// the other NewLiteral is already there
							ccLoops.add(edge.getLoop0()); 
							allLoops.remove(t0); // remove the loops that are covered by the edges in CC
						}
					}
					else
						continue;
					
					ccEdges.add(edge);
					expanded = true;
					i.remove();
				}
			} 
			
			ccs.add(new QueryConnectedComponent(ccEdges, null, ccLoops));			
		}
		
		// create degenerate connected components for all remaining loops (which are disconnected from anything else)
		for (Entry<NewLiteral, Loop> loop : allLoops.entrySet()) {
			ccs.add(new QueryConnectedComponent(Collections.EMPTY_LIST, loop.getValue(), Collections.singletonList(loop.getValue())));
		}
		
		return ccs;
	}
	
	public Loop getLoop() {
		return loop;
	}
	
	/**
	 * boolean isDenenerate() 
	 * 
	 * @return true if the component is degenerate (has no proper edges with two distinct NewLiterals)
	 */
	
	public boolean isDegenerate() {
		return edges.isEmpty();
	}
	
	/**
	 * boolean hasNoFreeNewLiterals()
	 * 
	 * @return true if all NewLiterals of the connected component are existentially quantified variables
	 */
	
	public boolean hasNoFreeTerms() {
		return noFreeTerms;
	}
	
	/**
	 * List<Edge> getEdges()
	 * 
	 * @return the list of edges in the connected component
	 */
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	/**
	 * List<NewLiteral> getVariables()
	 * 
	 * @return the list of variables in the connected components
	 */
	
	public List<NewLiteral> getVariables() {
		return variables;		
	}

	/**
	 * Set<Variable> getQuantifiedVariables()
	 * 
	 * @return the collection of existentially quantified variables
	 */
	
	public Collection<Loop> getQuantifiedVariables() {
		return quantifiedVariables;		
	}
	
	/**
	 * List<NewLiteral> getFreeVariables()
	 * 
	 * @return the list of free variables in the connected component
	 */
	
	public List<NewLiteral> getFreeVariables() {
		return freeVariables;
	}

	/**
	 * Loop: class representing loops of connected components
	 * 
	 * a loop is characterized by a NewLiteral and a set of atoms involving only that NewLiteral
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	static class Loop {
		private final NewLiteral term;
		private Collection<Atom> atoms;
		private final boolean isExistentialVariable;
		
		public Loop(NewLiteral term, boolean isExistentialVariable) {
			this.term = term;
			this.isExistentialVariable = isExistentialVariable;
			this.atoms = new ArrayList<Atom>(10);
		}
		
		public NewLiteral getTerm() {
			return term;
		}
		
		public Collection<Atom> getAtoms() {
			return atoms;
		}
		
		public boolean isExistentialVariable() {
			return isExistentialVariable;
		}
		
		@Override
		public String toString() {
			return "loop: {" + term + "}" + atoms;
		}
		
		@Override 
		public boolean equals(Object o) {
			if (o instanceof Loop) 
				return term.equals(((Loop)o).term);
			return false;
		}
		
		@Override
		public int hashCode() {
			return term.hashCode();
		}
		
	}
	
	/**
	 * Edge: class representing edges of connected components
	 * 
	 * an edge is characterized by a pair of NewLiterals and a set of atoms involving only those NewLiterals
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	static class Edge {
		private final Loop l0, l1;
		private Collection<Atom> bAtoms;
		
		public Edge(Loop l0, Loop l1) {
			this.bAtoms = new ArrayList<Atom>(10);
			this.l0 = l0;
			this.l1 = l1;
		}

		public Loop getLoop0() {
			return l0;
		}
		
		public Loop getLoop1() {
			return l1;
		}
		
		public NewLiteral getTerm0() {
			return l0.term;
		}
		
		public NewLiteral getTerm1() {
			return l1.term;
		}
		
		public Collection<Atom> getAtoms0() {
			return l0.atoms;
		}
		
		public Collection<Atom> getAtoms1() {
			return l1.atoms;
		}

		public Collection<Atom> getBAtoms() {
			return bAtoms;
		}
		
		public List<Atom> getAtoms() {
			List<Atom> extAtoms = new ArrayList<Atom>(bAtoms.size() + l0.atoms.size() + l1.atoms.size());
			extAtoms.addAll(bAtoms);
			extAtoms.addAll(l0.atoms);
			extAtoms.addAll(l1.atoms);
			return extAtoms;
		}
		
		@Override
		public String toString() {
			return "edge: {" + l0.term + ", " + l1.term + "}" + bAtoms + l0.atoms + l1.atoms;
		}
	}
	
	/**
	 * TermPair: a simple abstraction of *unordered* pair of NewLiterals (i.e., {t1, t2} and {t2, t1} are equal)
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	private static class TermPair {
		private final NewLiteral t0, t1;
		private final int hashCode;

		public TermPair(NewLiteral t0, NewLiteral t1) {
			this.t0 = t0;
			this.t1 = t1;
			this.hashCode = t0.hashCode() ^ t1.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof TermPair) {
				TermPair other = (TermPair) o;
				if (this.t0.equals(other.t0) && this.t1.equals(other.t1))
					return true;
				if (this.t0.equals(other.t1) && this.t1.equals(other.t0))
					return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "NewLiteral pair: {" + t0 + ", " + t1 + "}";
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
	}	
}
