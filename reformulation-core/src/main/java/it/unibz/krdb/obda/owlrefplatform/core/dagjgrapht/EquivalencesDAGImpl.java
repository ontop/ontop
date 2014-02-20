/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * DAG from an OntologyGraph
 * 
 * The vertices of the DAG are equivalence sets (Equivalences)
 * The edges form the minimal set whose transitive and reflexive closure
 * coincides with the transitive and reflexive closure of the ontology graph
 *  
 * The key component is the Gabow SCC algorithm for computing 
 * strongly connected components
 * 
*/

public class EquivalencesDAGImpl<T> implements EquivalencesDAG<T> {
	
	private SimpleDirectedGraph <Equivalences<T>,DefaultEdge> dag;
	private Map<T, Equivalences<T>> equivalencesMap;

	public EquivalencesDAGImpl(DefaultDirectedGraph<T,DefaultEdge> graph) {
		
		this.equivalencesMap = new HashMap<T, Equivalences<T>>();
		SimpleDirectedGraph<Equivalences<T>,DefaultEdge> dag0 = factorize(graph, this.equivalencesMap);
		this.dag = removeRedundantEdges(dag0);
	}
	
	public Set<Equivalences<T>> vertexSet() {
		return dag.vertexSet();
	}
	
	/** 
	 * 
	 */
	@Override
	public Equivalences<T> getVertex(T v) {
		return equivalencesMap.get(v);
	}
	
	/**
	 * 
	 */
	@Override
	public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
		LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

		for (DefaultEdge edge : dag.incomingEdgesOf(v)) {	
			Equivalences<T> source = dag.getEdgeSource(edge);
			result.add(source);
		}
		return Collections.unmodifiableSet(result);
	}

	/** 
	 * 
	 */
	@Override
	public Set<Equivalences<T>> getSub(Equivalences<T> v) {

		LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

		BreadthFirstIterator<Equivalences<T>, DefaultEdge>  iterator = 
					new BreadthFirstIterator<Equivalences<T>, DefaultEdge>(
							new EdgeReversedGraph<Equivalences<T>, DefaultEdge>(dag), v);

		while (iterator.hasNext()) {
			Equivalences<T> child = iterator.next();
			result.add(child);
		}
		return Collections.unmodifiableSet(result);
	}
	
	/** 
	 * 
	 */
	@Override
	public Set<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
		LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

		for (DefaultEdge edge : dag.outgoingEdgesOf(v)) {	
			Equivalences<T> source = dag.getEdgeTarget(edge);
			result.add(source);
		}
		return Collections.unmodifiableSet(result);
	}
	
	/** 
	 * 
	 */
	@Override
	public Set<Equivalences<T>> getSuper(Equivalences<T> v) {

		LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

		BreadthFirstIterator<Equivalences<T>, DefaultEdge>  iterator = 
				new BreadthFirstIterator<Equivalences<T>, DefaultEdge>(dag, v);

		while (iterator.hasNext()) {
			Equivalences<T> parent = iterator.next();
			result.add(parent);
		}
		return Collections.unmodifiableSet(result);
	}

	@Override
	public String toString() {
		return dag.toString() + 
				"\n\nEquivalencesMap\n" + equivalencesMap;
	}

	/** 
	 * 
	 */
	@Override
	public Iterator<Equivalences<T>> iterator() {
		return dag.vertexSet().iterator();
	}
	
	
	/*
	 *  test only methods
	 */
	
	@Deprecated
	public int edgeSetSize() {
		return dag.edgeSet().size();
	}
	
	
	
	/*
	 *  construction: main algorithms (static generic methods)
	 */
	
	private static <TT> SimpleDirectedGraph<Equivalences<TT>,DefaultEdge> factorize(DefaultDirectedGraph<TT,DefaultEdge> graph, 
																				Map<TT, Equivalences<TT>> equivalencesMap) {
		// each set contains vertices which together form a strongly connected
		// component within the given graph
		GabowSCC<TT, DefaultEdge> inspector = new GabowSCC<TT, DefaultEdge>(graph);
		List<Equivalences<TT>> equivalenceSets = inspector.stronglyConnectedSets();

		SimpleDirectedGraph<Equivalences<TT>,DefaultEdge> dag0 = 
					new SimpleDirectedGraph<Equivalences<TT>,DefaultEdge>(DefaultEdge.class);

		for (Equivalences<TT> equivalenceSet : equivalenceSets)  {
			for (TT node : equivalenceSet) 
				equivalencesMap.put(node, equivalenceSet);

			dag0.addVertex(equivalenceSet);
		}

		for (Equivalences<TT> equivalenceSet : equivalenceSets)  {
			for (TT e : equivalenceSet) {			
				for (DefaultEdge edge : graph.outgoingEdgesOf(e)) {
					TT t = graph.getEdgeTarget(edge);
					if (!equivalenceSet.contains(t))
						dag0.addEdge(equivalenceSet, equivalencesMap.get(t));
				}
				for (DefaultEdge edge : graph.incomingEdgesOf(e)) {
					TT s = graph.getEdgeSource(edge);
					if (!equivalenceSet.contains(s))
						dag0.addEdge(equivalencesMap.get(s), equivalenceSet);
				}
			}
		}
		return removeRedundantEdges(dag0);
	}

	private static <TT> SimpleDirectedGraph<TT,DefaultEdge> removeRedundantEdges(SimpleDirectedGraph<TT,DefaultEdge> graph) {

		SimpleDirectedGraph <TT,DefaultEdge> dag = new SimpleDirectedGraph <TT,DefaultEdge> (DefaultEdge.class);

		for (TT v : graph.vertexSet())
			dag.addVertex(v);

		for (DefaultEdge edge : graph.edgeSet()) {
			TT v1 = graph.getEdgeSource(edge);
			TT v2 = graph.getEdgeTarget(edge);
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
}
