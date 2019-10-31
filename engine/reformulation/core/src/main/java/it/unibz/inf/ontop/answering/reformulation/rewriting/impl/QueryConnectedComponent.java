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
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

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

	private final ImmutableList<Variable> variables;
	private final ImmutableList<Loop> quantifiedVariables;
	private final ImmutableList<Variable> freeVariables;
	
	private final ImmutableList<Edge> edges;  // a connected component contains a list of edges
	private final Optional<Loop> loop;  //                   or a loop if it is degenerate
	
	private final boolean noFreeTerms; // no free variables and no constants
	                             // if true the component can be mapped onto the anonymous part of the canonical model

	/**
	 * constructor is private as instances created only by the static method getConnectedComponents
	 * 
	 * @param edges: a list of edges in the connected component
	 * @param terms: terms that are covered by the edges
	 */
	
	private QueryConnectedComponent(ImmutableList<Edge> edges, ImmutableList<Loop> terms) {
		this.edges = edges;

		this.loop = isDegenerate() && !terms.isEmpty() ? Optional.of(terms.get(0)) : Optional.empty();

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

	private static QueryConnectedComponent getConnectedComponent(List<Entry<ImmutableSet<VariableOrGroundTerm>, Collection<DataAtom<RDFAtomPredicate>>>> pairs, Map<VariableOrGroundTerm, Loop> allLoops, ImmutableSet<VariableOrGroundTerm> seed, ImmutableSet<Variable> headVariables) {

		ImmutableList.Builder<Edge> ccEdges = ImmutableList.builder();
		Set<VariableOrGroundTerm> ccTerms = new HashSet<>(seed);

		// expand the current CC by adding all edges that are have at least one of the terms in them
		boolean expanded;
		do {
			expanded = false;
			for (Iterator<Entry<ImmutableSet<VariableOrGroundTerm>, Collection<DataAtom<RDFAtomPredicate>>>> i = pairs.iterator(); i.hasNext(); ) {
				Entry<ImmutableSet<VariableOrGroundTerm>, Collection<DataAtom<RDFAtomPredicate>>> e = i.next();
				Iterator<VariableOrGroundTerm> iterator = e.getKey().iterator();
				VariableOrGroundTerm t0 = iterator.next();
				VariableOrGroundTerm t1 = iterator.next();
				if (ccTerms.contains(t0))
					ccTerms.add(t1);   // the other term is already there
				else if (ccTerms.contains(t1))
					ccTerms.add(t0);   // the other term is already there
				else
					continue;

				Loop l0 =  allLoops.computeIfAbsent(t0, n -> new Loop(n, headVariables, ImmutableList.of()));
				Loop l1 =  allLoops.computeIfAbsent(t1, n -> new Loop(n, headVariables, ImmutableList.of()));
				ccEdges.add(new Edge(l0, l1, ImmutableList.copyOf(e.getValue())));
				expanded = true;
				i.remove();
			}
		} while (expanded);

		ImmutableList.Builder<Loop> ccLoops = ImmutableList.builder();
		for (VariableOrGroundTerm t : ccTerms) {
			Loop l = allLoops.remove(t);
			if (l != null)
				ccLoops.add(l);
		}

		return new QueryConnectedComponent(ccEdges.build(), ccLoops.build());
	}


	/**
	 * getConnectedComponents creates a list of connected components of a given CQ
	 * 
	 * @return list of connected components
	 */
	
	public static ImmutableList<QueryConnectedComponent> getConnectedComponents(ImmutableCQ<RDFAtomPredicate> cq) {

		ImmutableSet<Variable> answerVariables = ImmutableSet.copyOf(cq.getAnswerVariables());

		// collect all edges and loops
		//      an edge is a binary predicate P(t, t') with t \ne t'
		// 		a loop is either a unary predicate A(t) or a binary predicate P(t,t)
		ImmutableMultimap.Builder<ImmutableSet<VariableOrGroundTerm>, DataAtom<RDFAtomPredicate>> pz = ImmutableMultimap.builder();
		ImmutableMultimap.Builder<VariableOrGroundTerm, DataAtom<RDFAtomPredicate>> lz = ImmutableMultimap.builder();

		for (DataAtom<RDFAtomPredicate> a : cq.getAtoms()) {
			boolean isClass = a.getPredicate().getClassIRI(a.getArguments()).isPresent();
			// proper DL edge between two distinct terms
			if (!isClass && !a.getTerm(0).equals(a.getTerm(2)))
				// the index is an *unordered* pair
				pz.put(ImmutableSet.of(a.getTerm(0), a.getTerm(2)), a);
			else
				lz.put(a.getTerm(0), a);
		}

		Map<VariableOrGroundTerm, Loop> allLoops = new HashMap<>();
		for (Entry<VariableOrGroundTerm, Collection<DataAtom<RDFAtomPredicate>>> e : lz.build().asMap().entrySet()) {
			allLoops.put(e.getKey(), new Loop(e.getKey(), answerVariables, ImmutableList.copyOf(e.getValue())));
		}

		List<Entry<ImmutableSet<VariableOrGroundTerm>, Collection<DataAtom<RDFAtomPredicate>>>> pairs = new ArrayList<>(pz.build().asMap().entrySet());

		ImmutableList.Builder<QueryConnectedComponent> ccs = ImmutableList.builder();
		
		// form the list of connected components from the list of edges
		while (!pairs.isEmpty())
			ccs.add(getConnectedComponent(pairs, allLoops, pairs.iterator().next().getKey(), answerVariables));

		// create degenerate connected components for all remaining loops (which are disconnected from anything else)
		while (!allLoops.isEmpty())
			ccs.add(getConnectedComponent(pairs, allLoops, ImmutableSet.of(allLoops.keySet().iterator().next()), answerVariables));

		return ccs.build();
	}
	
	public Optional<Loop> getLoop() {
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

	/**
	 * Loop: class representing loops of connected components
	 * 
	 * a loop is characterized by a term and a set of atoms involving only that term
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	static class Loop {
		private final VariableOrGroundTerm term;
		private final ImmutableList<DataAtom<RDFAtomPredicate>> atoms;
		private final boolean isExistentialVariable;
		
		public Loop(VariableOrGroundTerm term, ImmutableSet<Variable> headVariables, ImmutableList<DataAtom<RDFAtomPredicate>> atoms) {
			this.term = term;
			this.isExistentialVariable = (term instanceof Variable) && !headVariables.contains(term);
			this.atoms = atoms;
		}
		
		public VariableOrGroundTerm getTerm() {
			return term;
		}
		
		public ImmutableList<DataAtom<RDFAtomPredicate>> getAtoms() { return atoms; }
		
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
		private final ImmutableList<DataAtom<RDFAtomPredicate>> bAtoms;
		
		public Edge(Loop l0, Loop l1, ImmutableList<DataAtom<RDFAtomPredicate>> bAtoms) {
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
		
		public VariableOrGroundTerm getTerm0() {
			return l0.term;
		}
		
		public VariableOrGroundTerm getTerm1() {
			return l1.term;
		}
		
		public ImmutableList<DataAtom<RDFAtomPredicate>> getBAtoms() {
			return bAtoms;
		}
		
		public ImmutableList<DataAtom<RDFAtomPredicate>> getAtoms() {
			return Stream.concat(bAtoms.stream(),
					Stream.concat(l0.getAtoms().stream(), l1.getAtoms().stream()))
					.collect(ImmutableCollectors.toList());
		}

		public boolean isCoveredBy(TreeWitness tw) {
		    return tw.getDomain().contains(l0.term) && tw.getDomain().contains(l1.term);
        }

		@Override
		public String toString() {
			return "edge: {" + l0.term + ", " + l1.term + "}" + bAtoms + l0.atoms + l1.atoms;
		}
	}
}
