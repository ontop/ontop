package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

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

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.impl.TermUtils;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.Map.Entry;

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

	private final ImmutableList<Variable> variables;
	private final ImmutableList<Loop> quantifiedVariables;
	private final ImmutableList<Variable> freeVariables;
	
	private final ImmutableList<Edge> edges;  // a connected component contains a list of edges
	private final Loop loop;  //                                   or a loop if it is degenerate 
	
	private final ImmutableList<Function> nonDLAtoms;
	
	private final boolean noFreeTerms; // no free variables and no constants
	                             // if true the component can be mapped onto the anonymous part of the canonical model

	/**
	 * constructor is private as instances created only by the static method getConnectedComponents
	 * 
	 * @param edges: a list of edges in the connected component
	 * @param nonDLAtoms: a list of non-DL atoms in the connected component
	 * @param terms: terms that are covered by the edges
	 */
	
	private QueryConnectedComponent(ImmutableList<Edge> edges, ImmutableList<Function> nonDLAtoms, ImmutableList<Loop> terms) {
		this.edges = edges;
		this.nonDLAtoms = nonDLAtoms;

		this.loop = isDegenerate() && !terms.isEmpty() ? terms.get(0) : null;

		this.variables = terms.stream()
				.map(Loop::getTerm)
				.filter(t -> (t instanceof Variable))
				.map(t -> (Variable)t)
				.collect(ImmutableCollectors.toList());
		this.freeVariables = terms.stream()
				.filter(l -> !l.isExistentialVariable() && (l.getTerm() instanceof Variable))
				.map(l -> (Variable)l.getTerm())
				.collect(ImmutableCollectors.toList());
		this.quantifiedVariables = terms.stream()
				.filter(Loop::isExistentialVariable)
				.collect(ImmutableCollectors.toList());

		this.noFreeTerms = (terms.size() == variables.size()) && freeVariables.isEmpty();
	}
	
	public static Loop getLoop(Term t, Map<Term, Loop> allLoops, ImmutableList<Function> atoms, ImmutableSet<Variable> headTerms) {
		return allLoops.computeIfAbsent(t,
				n -> new Loop(n, ((n instanceof Variable) && !headTerms.contains(n)), atoms));
	}
	
	private static QueryConnectedComponent getConnectedComponent(Map<TermPair, Edge> pairs, Map<Term, Loop> allLoops, List<Function> nonDLAtoms, Term seed, ImmutableSet<Variable> headVariables) {

		Set<Term> ccTerms = new HashSet<>((allLoops.size() * 2) / 3);

		ImmutableList.Builder<Edge> ccEdges = ImmutableList.builder();
		ImmutableList.Builder<Function> ccNonDLAtoms = ImmutableList.builder();
		ImmutableList.Builder<Loop> ccLoops = ImmutableList.builder();
		
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
		return new QueryConnectedComponent(ccEdges.build(), ccNonDLAtoms.build(), ccLoops.build());
	}
	
	/**
	 * getConnectedComponents creates a list of connected components of a given CQ
	 * 
	 * @param cqie : CQ to be split into connected components
	 * @param atomFactory
	 * @return list of connected components
	 */
	
	public static List<QueryConnectedComponent> getConnectedComponents(ClassifiedTBox reasoner, CQIE cqie,
																	   AtomFactory atomFactory,
																	   ImmutabilityTools immutabilityTools) {

		ImmutableSet<Variable> headVariables = ImmutableSet.copyOf(cqie.getHead().getVariables());

		// collect all edges and loops 
		//      an edge is a binary predicate P(t, t') with t \ne t'
		// 		a loop is either a unary predicate A(t) or a binary predicate P(t,t)
		//      a nonDL atom is an atom with a non-data predicate 
		ImmutableMultimap.Builder<TermPair, Function> pz = ImmutableMultimap.builder();
		ImmutableMultimap.Builder<Term, Function> lz = ImmutableMultimap.builder();
		List<Function> nonDLAtoms = new LinkedList<>();

		for (Function atom : cqie.getBody()) {
			// TODO: support quads
			if (atom.isDataFunction() && (atom.getFunctionSymbol() instanceof TriplePredicate)) { // if DL predicates
				Function a = getCanonicalForm(reasoner, atom, atomFactory, immutabilityTools);

				ImmutableList<ImmutableTerm> arguments = a.getTerms().stream()
						.map(immutabilityTools::convertIntoImmutableTerm)
						.collect(ImmutableCollectors.toList());
				boolean isClass = ((TriplePredicate) a.getFunctionSymbol()).getClassIRI(arguments).isPresent();

				Term t0 = a.getTerm(0);
				if (!isClass && !t0.equals(a.getTerm(2))) {
					// proper DL edge between two distinct terms
					Term t1 = a.getTerm(2);
					TermPair pair = new TermPair(t0, t1);
					pz.put(pair, a);
				}
				else {
					lz.put(t0, a);
				}
			}
			else {
				nonDLAtoms.add(atom);
			}
		}

		Map<Term, Loop> allLoops = new HashMap<>();
		for (Entry<Term, Collection<Function>> e : lz.build().asMap().entrySet()) {
			Loop l0 = getLoop(e.getKey(), allLoops, ImmutableList.copyOf(e.getValue()),  headVariables);
			allLoops.put(e.getKey(), l0);
		}

		Map<TermPair, Edge> pairs = new HashMap<>();
		for (Entry<TermPair, Collection<Function>> e : pz.build().asMap().entrySet()) {
			TermPair pair = e.getKey();
			Loop l0 = getLoop(pair.t0, allLoops, ImmutableList.of(), headVariables);
			Loop l1 = getLoop(pair.t1, allLoops, ImmutableList.of(), headVariables);
			pairs.put(pair, new Edge(l0, l1, ImmutableList.copyOf(e.getValue())));
		}

		List<QueryConnectedComponent> ccs = new LinkedList<>();
		
		// form the list of connected components from the list of edges
		while (!pairs.isEmpty()) {
			Term seed = pairs.entrySet().iterator().next().getKey().t0;
			ccs.add(getConnectedComponent(pairs, allLoops, nonDLAtoms, seed, headVariables));
		}
		
		while (!nonDLAtoms.isEmpty()) {
			//log.debug("NON-DL ATOMS ARE NOT EMPTY: {}", nonDLAtoms);
			Function f = nonDLAtoms.iterator().next(); 
			Set<Variable> vars = new HashSet<>();
			TermUtils.addReferencedVariablesTo(vars, f);
			Variable v = vars.iterator().next();
			ccs.add(getConnectedComponent(pairs, allLoops, nonDLAtoms, v, headVariables));
		}

		// create degenerate connected components for all remaining loops (which are disconnected from anything else)
		while (!allLoops.isEmpty()) {
			Term seed = allLoops.keySet().iterator().next();
			ccs.add(getConnectedComponent(pairs, allLoops, nonDLAtoms, seed, headVariables));
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
		return edges.isEmpty();
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
	
	public ImmutableList<Edge> getEdges() {
		return edges;
	}
	
	/**
	 * List<Term> getVariables()
	 * 
	 * @return the list of variables in the connected components
	 */
	
	public ImmutableList<Variable> getVariables() {
		return variables;		
	}

	/**
	 * Set<Variable> getQuantifiedVariables()
	 * 
	 * @return the collection of existentially quantified variables
	 */
	
	public ImmutableList<Loop> getQuantifiedVariables() {
		return quantifiedVariables;		
	}
	
	/**
	 * List<Term> getFreeVariables()
	 * 
	 * @return the list of free variables in the connected component
	 */
	
	public ImmutableList<Variable> getFreeVariables() {
		return freeVariables;
	}

	public ImmutableList<Function> getNonDLAtoms() {
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
		private final ImmutableList<Function> atoms;
		private final boolean isExistentialVariable;
		
		public Loop(Term term, boolean isExistentialVariable, ImmutableList<Function> atoms) {
			this.term = term;
			this.isExistentialVariable = isExistentialVariable;
			this.atoms = atoms;
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
		private final ImmutableList<Function> bAtoms;
		
		public Edge(Loop l0, Loop l1, ImmutableList<Function> bAtoms) {
			this.bAtoms = bAtoms;
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
			List<Function> allAtoms = new ArrayList<>(bAtoms.size() + l0.atoms.size() + l1.atoms.size());
			allAtoms.addAll(bAtoms);
			allAtoms.addAll(l0.getAtoms());
			allAtoms.addAll(l1.getAtoms());
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

	private static Function getCanonicalForm(ClassifiedTBox reasoner, Function bodyAtom,
											 AtomFactory atomFactory, ImmutabilityTools immutabilityTools) {
		TriplePredicate triplePredicate = (TriplePredicate) bodyAtom.getFunctionSymbol();

		ImmutableList<ImmutableTerm> arguments = bodyAtom.getTerms().stream()
				.map(immutabilityTools::convertIntoImmutableTerm)
				.collect(ImmutableCollectors.toList());

		Optional<IRI> classIRI = triplePredicate.getClassIRI(arguments);
		Optional<IRI> propertyIRI = triplePredicate.getPropertyIRI(arguments);

		// the contains tests are inefficient, but tests fails without them
		// p.isClass etc. do not work correctly -- throw exceptions because COL_TYPE is null

		if (classIRI.isPresent() && reasoner.classes().contains(classIRI.get())) {
			OClass c = reasoner.classes().get(classIRI.get());
			OClass equivalent = (OClass)reasoner.classesDAG().getCanonicalForm(c);
			if (equivalent != null && !equivalent.equals(c)) {
				return atomFactory.getMutableTripleBodyAtom(bodyAtom.getTerm(0), equivalent.getIRI());
			}
		}
		else if (propertyIRI.isPresent() && reasoner.objectProperties().contains(propertyIRI.get())) {
			ObjectPropertyExpression ope = reasoner.objectProperties().get(propertyIRI.get());
			ObjectPropertyExpression equivalent = reasoner.objectPropertiesDAG().getCanonicalForm(ope);
			if (equivalent != null && !equivalent.equals(ope)) {
				if (!equivalent.isInverse())
					return atomFactory.getMutableTripleBodyAtom(bodyAtom.getTerm(0), equivalent.getIRI(), bodyAtom.getTerm(2));
				else
					return atomFactory.getMutableTripleBodyAtom(bodyAtom.getTerm(2), equivalent.getIRI(), bodyAtom.getTerm(0));
			}
		}
		else if (propertyIRI.isPresent()  && reasoner.dataProperties().contains(propertyIRI.get())) {
			DataPropertyExpression dpe = reasoner.dataProperties().get(propertyIRI.get());
			DataPropertyExpression equivalent = reasoner.dataPropertiesDAG().getCanonicalForm(dpe);
			if (equivalent != null && !equivalent.equals(dpe)) {
				return atomFactory.getMutableTripleBodyAtom(bodyAtom.getTerm(0), equivalent.getIRI(), bodyAtom.getTerm(2));
			}
		}
		return bodyAtom;
	}

}
