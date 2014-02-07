/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Starting from a graph build a DAG. 
 * Considering  equivalences and redundancies, it eliminates cycles executes
 * transitive reduction.
	// contains the representative node with the set of equivalent mappings.
 */

/***
 * A map to keep the relationship between 'eliminated' and 'remaining'
 * nodes, created while computing equivalences by eliminating cycles in the
 * graph.
 */

public class DAGBuilder {

	private static OntologyFactory fac = OntologyFactoryImpl.getInstance();

	/**
	 * *Construct a DAG starting from a given graph with already known
	 * equivalent nodes and representative nodes
	 * 
	 * @param graph needs a graph with or without cycles
	 * @param equivalents a map between the node and its equivalent nodes
	 * @param representatives a map between the node and its representative node
	 */
	public static SimpleDirectedGraph <Equivalences<Description>,DefaultEdge> getDAG(DefaultDirectedGraph<Description,DefaultEdge> graph, 
			Map<Description, Equivalences<Description>> equivalencesMap) {

		SimpleDirectedGraph<Equivalences<Description>,DefaultEdge> dag0 = factorize(graph, equivalencesMap);

		choosePropertyRepresentatives(dag0, equivalencesMap);
		
		chooseClassRepresentatives(dag0, equivalencesMap);
		
		return dag0;
	}

	/**
	 */
	private static <T> SimpleDirectedGraph<Equivalences<T>,DefaultEdge> factorize(DefaultDirectedGraph<T,DefaultEdge> graph, 
																			Map<T, Equivalences<T>> equivalencesMap) {

		// each set contains vertices which together form a strongly connected
		// component within the given graph
		GabowSCC<T, DefaultEdge> inspector = new GabowSCC<T, DefaultEdge>(graph);
		List<Equivalences<T>> equivalenceSets = inspector.stronglyConnectedSets();

		SimpleDirectedGraph<Equivalences<T>,DefaultEdge> dag = 
				new SimpleDirectedGraph<Equivalences<T>,DefaultEdge>(DefaultEdge.class);
		
		for (Equivalences<T> equivalenceSet : equivalenceSets)  {
			for (T node : equivalenceSet) 
				equivalencesMap.put(node, equivalenceSet);
			
			dag.addVertex(equivalenceSet);
		}

		for (Equivalences<T> equivalenceSet : equivalenceSets)  {
			for (T e : equivalenceSet) {			
				for (DefaultEdge edge : graph.outgoingEdgesOf(e)) {
					T t = graph.getEdgeTarget(edge);
					if (!equivalenceSet.contains(t))
						dag.addEdge(equivalenceSet, equivalencesMap.get(t));
				}
				for (DefaultEdge edge : graph.incomingEdgesOf(e)) {
					T s = graph.getEdgeSource(edge);
					if (!equivalenceSet.contains(s))
						dag.addEdge(equivalencesMap.get(s), equivalenceSet);
				}
			}
		}
		return removeRedundantEdges(dag);
	}

	private static <T> SimpleDirectedGraph<T,DefaultEdge> removeRedundantEdges(SimpleDirectedGraph<T,DefaultEdge> graph) {
	
		SimpleDirectedGraph <T,DefaultEdge> dag = new SimpleDirectedGraph <T,DefaultEdge> (DefaultEdge.class);

		for (T v : graph.vertexSet())
			dag.addVertex(v);
		
		for (DefaultEdge edge : graph.edgeSet()) {
			T v1 = graph.getEdgeSource(edge);
			T v2 = graph.getEdgeTarget(edge);
			boolean redundant = false;

			if (graph.outDegreeOf(v1) > 1) {
				// an edge is redundant if 
				//  its source has an edge going to a vertex 
				//         from which the target is reachable (in one step) 
				for (DefaultEdge e2 : graph.outgoingEdgesOf(v1)) 
					if (graph.containsEdge(graph.getEdgeTarget(e2), v2)) {
						redundant = true;
						break;
					}
			}
			if (!redundant)
				dag.addEdge(v1, v2);
		}
		return dag;
	}

	/***
	 * Eliminates all cycles in the graph by computing all strongly connected
	 * components and eliminating all but one node in each of the components
	 * from the graph. The result of this transformation is that the graph
	 * becomes a DAG.
	 * 
	 * 
	 * <p>
	 * In the process two objects are generated, an 'Equivalence map' and a
	 * 'replacementMap'. The first can be used to get the implied equivalences
	 * of the TBox. The second can be used to locate the node that is
	 * representative of an eliminated node.
	 * 
	 * <p>
	 * Computation of the strongly connected components is done using Gabow SCC
	 * algorithm.
	 * 
	 */

	private static void choosePropertyRepresentatives(SimpleDirectedGraph<Equivalences<Description>,DefaultEdge> dag, 
															Map<Description, Equivalences<Description>> equivalencesMap) {
		
		Deque<Equivalences<Description>> asymmetric1 = new LinkedList<Equivalences<Description>>();
		Deque<Equivalences<Description>> asymmetric2 = new LinkedList<Equivalences<Description>>();
		Set<Equivalences<Description>> symmetric = new HashSet<Equivalences<Description>>();
		
		for (Equivalences<Description> equivalenceSet : dag.vertexSet()) {
			if (!(equivalenceSet.iterator().next() instanceof Property))
				continue;
			
			if (equivalenceSet.size() == 1) {
				Property p = (Property)equivalenceSet.iterator().next();
				equivalenceSet.setRepresentative(p);
				if (!p.isInverse())
					equivalenceSet.setIndexed();
			}

			if (equivalenceSet.getRepresentative() != null)
				continue;
			
			Property representative = getNamedRepresentative(equivalenceSet);
			if (representative == null)
				representative = (Property)equivalenceSet.iterator().next();
			
			equivalenceSet.setRepresentative(representative);
			
			Property inverse = fac.createProperty(representative.getPredicate(), !representative.isInverse());
			Equivalences<Description> inverseEquivalenceSet = equivalencesMap.get(inverse);
			if (!inverseEquivalenceSet.contains(representative)) {
				inverseEquivalenceSet.setRepresentative(inverse);
				assert (equivalenceSet != inverseEquivalenceSet);
				asymmetric1.add(equivalenceSet);
				asymmetric2.add(inverseEquivalenceSet);
			}
			else {
				assert (equivalenceSet == inverseEquivalenceSet);
				symmetric.add(equivalenceSet);				
			}			
		}

		while (!asymmetric1.isEmpty()) {
			Equivalences<Description> c1 = asymmetric1.pollFirst();
			Equivalences<Description> c2 = asymmetric2.pollFirst();
			assert (!c1.isIndexed() || !c2.isIndexed());
			if (c1.isIndexed() || c2.isIndexed()) 
				continue;
						
			Set<Equivalences<Description>> set = getRoleComponent(dag, c1, symmetric);
			boolean swap = false;
			for (Equivalences<Description> eq : set) 
				if (dag.outDegreeOf(eq) == 0) {
					Property p = (Property)c1.getRepresentative();
					if (p.isInverse()) {
						swap = true;
						break;
					}
				}
			if (swap)
				set = getRoleComponent(dag, c2, symmetric);
			
			for (Equivalences<Description> eq : set) {
				Property rep = (Property)eq.getRepresentative();
				if (rep.isInverse()) {
					Property rep2 = getNamedRepresentative(eq); 
					if (rep2 != null) {
						eq.setRepresentative(rep2);
						Property inverse = fac.createProperty(rep2.getPredicate(), !rep2.isInverse());
						Equivalences<Description> inverseEquivalenceSet = equivalencesMap.get(inverse);
						inverseEquivalenceSet.setRepresentative(inverse);
					}
				}
				eq.setIndexed();
			}
		}
		
		for (Equivalences<Description> sym : symmetric)
			sym.setIndexed();
		
		/*
		for (Equivalences<Description> equivalenceClass : dag.vertexSet()) {
			if (equivalenceClass.iterator().next() instanceof Property) {
				System.out.println(" " + equivalenceClass);
				if (equivalenceClass.getRepresentative() == null)
					System.out.println("NULL REP FOR: " + equivalenceClass);
				if (!equivalenceClass.isIndexed()) {
					Property representative = (Property) equivalenceClass.getRepresentative();
					Property inverse = fac.createProperty(representative.getPredicate(), !representative.isInverse());
					Equivalences<Description> inverseEquivalenceSet = equivalencesMap.get(inverse);
					if (!inverseEquivalenceSet.isIndexed())
						System.out.println("NOT INDEXED: " + equivalenceClass + " AND " + inverseEquivalenceSet);
				}
			}
		}
		*/
		//System.out.println("RESULT: " + dag);
		//System.out.println("MAP: " + equivalencesMap);

	}	
	
	private static void chooseClassRepresentatives(SimpleDirectedGraph<Equivalences<Description>,DefaultEdge> dag, 
				Map<Description, Equivalences<Description>> equivalencesMap) {

		for (Equivalences<Description> equivalenceSet : dag.vertexSet()) {

			if (!(equivalenceSet.iterator().next() instanceof BasicClassDescription))
				continue;

			BasicClassDescription representative = null;
			
			if (equivalenceSet.size() <= 1) {
				representative = (BasicClassDescription)equivalenceSet.iterator().next();
			}
			else {
				// find a named class as a representative 
				for (Description e : equivalenceSet) 
					if (e instanceof OClass) {
						representative = (BasicClassDescription)e;
						break;
					}
				
				if (representative == null) {
					PropertySomeRestriction first = (PropertySomeRestriction)equivalenceSet.iterator().next();
					Property prop = fac.createProperty(first.getPredicate(), first.isInverse());
					Property propRep = (Property) equivalencesMap.get(prop).getRepresentative();
					representative = fac.createPropertySomeRestriction(propRep.getPredicate(), propRep.isInverse());
				}
			}

			equivalenceSet.setRepresentative(representative);
			if (representative instanceof OClass)
				equivalenceSet.setIndexed();
		}
	}
	
	private static Set<Equivalences<Description>> getRoleComponent(SimpleDirectedGraph<Equivalences<Description>,DefaultEdge> dag, 
																		Equivalences<Description> node, Set<Equivalences<Description>> symmetric)		{
		
		Set<Equivalences<Description>> set = new HashSet<Equivalences<Description>>();
		
		Deque<Equivalences<Description>> queue = new LinkedList<Equivalences<Description>>();
		queue.add(node);
		set.add(node);

		while (!queue.isEmpty()) {
			Equivalences<Description> eq = queue.pollFirst();
			for (DefaultEdge e : dag.outgoingEdgesOf(eq)) {
				Equivalences<Description> t = dag.getEdgeTarget(e);
				if (!set.contains(t) && !symmetric.contains(t)) {
					set.add(t);
					queue.add(t);
				}
			}
			for (DefaultEdge e : dag.incomingEdgesOf(eq)) {
				Equivalences<Description> s = dag.getEdgeSource(e);
				if (!set.contains(s) && !symmetric.contains(s)) {
					set.add(s);
					queue.add(s);
				}
			}
		}	
		return set;
	}
	

	

	private static Property getNamedRepresentative(Equivalences<Description> properties) {
		Property representative = null;
		for (Description rep : properties) 
			if (!((Property) rep).isInverse()) {
				representative = (Property)rep;
				break;
			}
		return representative;
	}

	

	
}
