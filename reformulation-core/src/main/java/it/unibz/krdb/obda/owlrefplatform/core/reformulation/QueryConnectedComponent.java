package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.TermUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
 * 
 * types of predicates (as of 1 October 2014)
 * 
 * Constant: NULL (string), TRUE, FALSE (boolean)
 * 
 * NumericalOperationPredicate: MINUS, ADD, SUBTRACT, MULTIPLY
 * BooleanOperationPredicate: AND, NOT, OR, EQ, NEQ, GTE, GT, LTE, LT, IS_NULL, IS_NOT_NULL, IS_TRUE, 
 *                            SPARQL_IS_LITERAL_URI, SPARQL_IS_URI, SPARQL_IS_IRI, SPARQL_IS_BLANK, SPARQL_LANGMATCHES, 
 *                            SPARQL_REGEX, SPARQL_LIKE
 * NonBooleanOperationPredicate: SPARQL_STR, SPARQL_DATATYPE, SPARQL_LANG                        
 * DataTypePredicate: RDFS_LITERAL, RDFS_LITERAL_LANG, XSD_STRING, XSD_INTEGER, XSD_DECIMAL, XSD_DOUBLE, XSD_DATETIME,
 *                    XSD_BOOLEAN, XSD_DATE, XSD_TIME, XSD_YEAR
 * Predicate: QUEST_TRIPLE_PRED, QUEST_CAST                    
 * AlgebraOperatorPredicate: SPARQL_JOIN, SPARQL_LEFTJOIN 
 *
 */

public class QueryConnectedComponent {

	private List<Term> variables; 	
	private List<Loop> quantifiedVariables;   
	private List<Term> freeVariables;
	
	private final List<Edge> edges;  // a connect component contains a list of edges 
	private final Loop loop;  //                                   or a loop if it is degenerate 
	
	private final List<Function> nonDLAtoms;
	
	private boolean noFreeTerms; // no free variables and no constants 
	                             // if true the component can be mapped onto the anonymous part of the canonical model

	/**
	 * constructor is private as instances created only by the static method getConnectedComponents
	 * 
	 * @param edges: a list of edges in the connected component
	 * @param loop: a loop if the component is degenerate
	 * @param nonDLAtoms: a list of non-DL atoms in the connected component
	 * @param terms: terms that are covered by the edges
	 */
	
	private QueryConnectedComponent(List<Edge> edges, List<Function> nonDLAtoms, List<Loop> terms) {
		this.edges = edges;
		this.nonDLAtoms = nonDLAtoms;

		this.loop = isDegenerate() && !terms.isEmpty() ? terms.get(0) : null; 
				
		quantifiedVariables = new ArrayList<Loop>(terms.size());
		variables = new ArrayList<Term>(terms.size());
		freeVariables = new ArrayList<Term>(terms.size());
		noFreeTerms = true;
		
		for (Loop l: terms) {
			Term t = l.getTerm(); 
			if (t instanceof Variable) {
				variables.add(t);
				//if (headterms.contains(t))
				if (l.isExistentialVariable())
					quantifiedVariables.add(l);
				else {
					freeVariables.add(t);
					noFreeTerms = false;
				}
			}
			else
				noFreeTerms = false; // not a variable -- better definition?
		}
	}
	
	public static Loop getLoop(Term t, Map<Term, Loop> allLoops, Set<Term> headTerms) {
		Loop l = allLoops.get(t);
		if (l == null) {
			boolean isExistentialVariable =  ((t instanceof Variable) && !headTerms.contains(t));
			l = new Loop(t, isExistentialVariable);
			allLoops.put(t, l);
		}
		return l;
	}
	
	private static QueryConnectedComponent getConnectedComponent(Map<TermPair, Edge> pairs, Map<Term, Loop> allLoops, List<Function> nonDLAtoms,
																Term seed) {
		Set<Term> ccTerms = new HashSet<Term>((allLoops.size() * 2) / 3);
		List<Edge> ccEdges = new ArrayList<Edge>(pairs.size());
		List<Function> ccNonDLAtoms = new LinkedList<Function>();
		List<Loop> ccLoops = new ArrayList<Loop>(allLoops.size());
		
		ccTerms.add(seed);
		Loop seedLoop = allLoops.get(seed);
		if (seedLoop != null) {
			ccLoops.add(seedLoop);
			allLoops.remove(seed);
		}
		
		// expand the current CC by adding all edges that are have at least one of the terms in them
		boolean expanded = true;
		while (expanded) {
			expanded = false;
			Iterator<Entry<TermPair, Edge>> i = pairs.entrySet().iterator();
			//i = pairs.entrySet().iterator();
			while (i.hasNext()) {
				Edge edge = i.next().getValue();
				Term t0 = edge.getTerm0();
				Term t1 = edge.getTerm1();
				if (ccTerms.contains(t0)) {
					if (ccTerms.add(t1))  { // the other term is already there
						ccLoops.add(edge.getLoop1());
						allLoops.remove(t1); // remove the loops that are covered by the edges in CC
					}
				}
				else if (ccTerms.contains(t1)) {
					if (ccTerms.add(t0))  { // the other term is already there
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
			
			// non-DL atoms
			Iterator<Function> ni = nonDLAtoms.iterator();
			while (ni.hasNext()) {
				Function atom = ni.next();
				boolean intersects = false;
				Set<Variable> atomVars = new HashSet<>();
				TermUtils.addReferencedVariablesTo(atomVars, atom);
				for (Variable t : atomVars) 
					if (ccTerms.contains(t)) {
						intersects = true;
						break;
					}
				
				if (intersects) {
					ccNonDLAtoms.add(atom);
					ccTerms.addAll(atomVars);
					for (Variable v : atomVars) {
						allLoops.remove(v);
					}
					expanded = true;
					ni.remove();
				}
			}
		}
		return new QueryConnectedComponent(ccEdges, ccNonDLAtoms, ccLoops); 
	}
	
	/**
	 * getConnectedComponents creates a list of connected components of a given CQ
	 * 
	 * @param cqie: CQ to be split into connected components 
	 * @return list of connected components
	 */
	
	public static List<QueryConnectedComponent> getConnectedComponents(CQIE cqie) {

		Set<Term> headTerms = new HashSet<Term>(cqie.getHead().getTerms());

		// collect all edges and loops 
		//      an edge is a binary predicate P(t, t') with t \ne t'
		// 		a loop is either a unary predicate A(t) or a binary predicate P(t,t)
		//      a nonDL atom is an atom with a non-data predicate 
		Map<TermPair, Edge> pairs = new HashMap<TermPair, Edge>();
		Map<Term, Loop> allLoops = new HashMap<Term, Loop>();
		List<Function> nonDLAtoms = new LinkedList<Function>();
		
		for (Function a: cqie.getBody()) {
			Predicate p = a.getFunctionSymbol();
			if (p.isDataPredicate() && !p.isTriplePredicate()) { // if DL predicates 
			//if (p.isClass() || p.isObjectProperty() || p.isDataProperty()) { // if DL predicate (throws NullPointerException)
				Term t0 = a.getTerm(0);				
				if (a.getArity() == 2 && !t0.equals(a.getTerm(1))) {
					// proper DL edge between two distinct terms
					Term t1 = a.getTerm(1);
					TermPair pair = new TermPair(t0, t1);
					Edge edge =  pairs.get(pair); 
					if (edge == null) {
						Loop l0 = getLoop(t0, allLoops, headTerms);
						Loop l1 = getLoop(t1, allLoops, headTerms);
						edge = new Edge(l0, l1);
						pairs.put(pair, edge);
					}
					edge.bAtoms.add(a);			
				}
				else {
					Loop l0 = getLoop(t0, allLoops, headTerms);
					l0.atoms.add(a);
				}
			}
			else { // non-DL precicate
				//log.debug("NON-DL ATOM {}",  a);
				nonDLAtoms.add(a);
			}
		}	

		
		List<QueryConnectedComponent> ccs = new LinkedList<>();
		
		// form the list of connected components from the list of edges
		while (!pairs.isEmpty()) {
			Edge edge = pairs.entrySet().iterator().next().getValue();			
			ccs.add(getConnectedComponent(pairs, allLoops, nonDLAtoms, edge.getTerm0()));			
		}
		
		while (!nonDLAtoms.isEmpty()) {
			//log.debug("NON-DL ATOMS ARE NOT EMPTY: {}", nonDLAtoms);
			Function f = nonDLAtoms.iterator().next(); 
			Set<Variable> vars = new HashSet<>();
			TermUtils.addReferencedVariablesTo(vars, f);
			Variable v = vars.iterator().next();
			ccs.add(getConnectedComponent(pairs, allLoops, nonDLAtoms, v));			
		}

		// create degenerate connected components for all remaining loops (which are disconnected from anything else)
		//for (Entry<term, Loop> loop : allLoops.entrySet()) {
		while (!allLoops.isEmpty()) {
			Term seed = allLoops.keySet().iterator().next();
			ccs.add(getConnectedComponent(pairs, allLoops, nonDLAtoms, seed));			
			//ccs.add(new QueryConnectedComponent(Collections.EMPTY_LIST, loop.getValue(), Collections.EMPTY_LIST, Collections.singletonList(loop.getValue())));
		}
				
		return ccs;
	}
	
	public Loop getLoop() {
		return loop;
	}
	
	/**
	 * boolean isDenenerate() 
	 * 
	 * @return true if the component is degenerate (has no proper edges with two distinct terms)
	 */
	
	public boolean isDegenerate() {
		return edges.isEmpty(); // && nonDLAtoms.isEmpty();
	}
	
	/**
	 * boolean hasNoFreeTerms()
	 * 
	 * @return true if all terms of the connected component are existentially quantified variables
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
	 * List<Term> getVariables()
	 * 
	 * @return the list of variables in the connected components
	 */
	
	public List<Term> getVariables() {
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
	 * List<Term> getFreeVariables()
	 * 
	 * @return the list of free variables in the connected component
	 */
	
	public List<Term> getFreeVariables() {
		return freeVariables;
	}

	public List<Function> getNonDLAtoms() {
		return nonDLAtoms;
	}
	
	/**
	 * Loop: class representing loops of connected components
	 * 
	 * a loop is characterized by a term and a set of atoms involving only that term
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	static class Loop {
		private final Term term;
		private final List<Function> atoms;
		private final boolean isExistentialVariable;
		
		public Loop(Term term, boolean isExistentialVariable) {
			this.term = term;
			this.isExistentialVariable = isExistentialVariable;
			this.atoms = new ArrayList<Function>(10);
		}
		
		public Term getTerm() {
			return term;
		}
		
		public Collection<Function> getAtoms() {
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
	 * an edge is characterized by a pair of terms and a set of atoms involving only those terms
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	static class Edge {
		private final Loop l0, l1;
		private final List<Function> bAtoms;
		
		public Edge(Loop l0, Loop l1) {
			this.bAtoms = new ArrayList<Function>(10);
			this.l0 = l0;
			this.l1 = l1;
		}

		public Loop getLoop0() {
			return l0;
		}
		
		public Loop getLoop1() {
			return l1;
		}
		
		public Term getTerm0() {
			return l0.term;
		}
		
		public Term getTerm1() {
			return l1.term;
		}
		
		public Collection<Function> getBAtoms() {
			return bAtoms;
		}
		
		public List<Function> getAtoms() {
			List<Function> allAtoms = new ArrayList<Function>(bAtoms.size() + l0.atoms.size() + l1.atoms.size());
			allAtoms.addAll(bAtoms);
			allAtoms.addAll(l0.atoms);
			allAtoms.addAll(l1.atoms);
			return allAtoms;
		}
		
		@Override
		public String toString() {
			return "edge: {" + l0.term + ", " + l1.term + "}" + bAtoms + l0.atoms + l1.atoms;
		}
	}
	
	/**
	 * TermPair: a simple abstraction of *unordered* pair of Terms (i.e., {t1, t2} and {t2, t1} are equal)
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	private static class TermPair {
		private final Term t0, t1;

		public TermPair(Term t0, Term t1) {
			this.t0 = t0;
			this.t1 = t1;
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
			return "term pair: {" + t0 + ", " + t1 + "}";
		}
		
		@Override
		public int hashCode() {
			return t0.hashCode() ^ t1.hashCode();
		}
	}	
}
