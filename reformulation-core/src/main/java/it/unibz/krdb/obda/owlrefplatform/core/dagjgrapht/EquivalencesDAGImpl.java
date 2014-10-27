package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

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
	
	private final SimpleDirectedGraph <Equivalences<T>,DefaultEdge> dag;
	private final Map<T, Equivalences<T>> equivalencesMap;
	
	private final Map<Equivalences<T>, Set<Equivalences<T>>> cacheSub;
	private final Map<T, Set<T>> cacheSubRep;

	
	private DefaultDirectedGraph<T,DefaultEdge> graph; // used in tests and SIGMA reduction
	
	public EquivalencesDAGImpl(DefaultDirectedGraph<T,DefaultEdge> graph, SimpleDirectedGraph <Equivalences<T>,DefaultEdge> dag, Map<T, Equivalences<T>> equivalencesMap) {	
		this.graph = graph;
		this.dag = dag;
		this.equivalencesMap = equivalencesMap;
		
		this.cacheSub = new HashMap<Equivalences<T>, Set<Equivalences<T>>>();
		this.cacheSubRep = new HashMap<T, Set<T>>();
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

		Set<Equivalences<T>> result = cacheSub.get(v);
		if (result == null) {
			result = new LinkedHashSet<Equivalences<T>>();

			BreadthFirstIterator<Equivalences<T>, DefaultEdge>  iterator = 
						new BreadthFirstIterator<Equivalences<T>, DefaultEdge>(
								new EdgeReversedGraph<Equivalences<T>, DefaultEdge>(dag), v);

			while (iterator.hasNext()) {
				Equivalences<T> child = iterator.next();
				result.add(child);
			}
			result = Collections.unmodifiableSet(result);
			cacheSub.put(v, result);
		}
		return result; 
	}

	/** 
	 * 
	 */
	@Override
	public Set<T> getSubRepresentatives(T v) {
		Equivalences<T> eq = equivalencesMap.get(v);
		
		if (eq == null)
			return Collections.singleton(v);
		
		Set<T> result = cacheSubRep.get(eq.getRepresentative());
		if (result == null) {
			result = new LinkedHashSet<T>();

			BreadthFirstIterator<Equivalences<T>, DefaultEdge>  iterator = 
						new BreadthFirstIterator<Equivalences<T>, DefaultEdge>(
								new EdgeReversedGraph<Equivalences<T>, DefaultEdge>(dag), eq);

			while (iterator.hasNext()) {
				Equivalences<T> child = iterator.next();
				result.add(child.getRepresentative());
			}
			result = Collections.unmodifiableSet(result);
			cacheSubRep.put(eq.getRepresentative(), result);
		}
		return result; 
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
	int edgeSetSize() {
		return dag.edgeSet().size();
	}

	@Deprecated 
	public int vertexSetSize() { 
		return dag.vertexSet().size();
	}
	
	
	public DefaultDirectedGraph<T,DefaultEdge> getGraph() {
		if (graph == null) {
			graph = new DefaultDirectedGraph<T,DefaultEdge>(DefaultEdge.class);

			for (Equivalences<T> node : dag.vertexSet()) {
				for (T v : node) 
					graph.addVertex(v);
				for (T v : node)  {
					graph.addEdge(v, node.getRepresentative());
					graph.addEdge(node.getRepresentative(), v);
				}
			}
			
			for (DefaultEdge edge : dag.edgeSet()) 
				graph.addEdge(dag.getEdgeSource(edge).getRepresentative(), dag.getEdgeTarget(edge).getRepresentative());
		}
		return graph;
		
	}
	
	
	
	/*
	 *  construction: main algorithms (static generic methods)
	 */
	
	public static <TT> EquivalencesDAGImpl<TT> getEquivalencesDAG(DefaultDirectedGraph<TT,DefaultEdge> graph) {
		
		
		// each set contains vertices which together form a strongly connected
		// component within the given graph
		GabowSCC<TT, DefaultEdge> inspector = new GabowSCC<TT, DefaultEdge>(graph);
		List<Equivalences<TT>> equivalenceSets = inspector.stronglyConnectedSets();

		SimpleDirectedGraph<Equivalences<TT>,DefaultEdge> dag0 = 
					new SimpleDirectedGraph<Equivalences<TT>,DefaultEdge>(DefaultEdge.class);
		Map<TT, Equivalences<TT>> equivalencesMap = new HashMap<TT, Equivalences<TT>>();

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
		

		// removed redundant edges
		
		SimpleDirectedGraph <Equivalences<TT>,DefaultEdge> dag = 
						new SimpleDirectedGraph <Equivalences<TT>,DefaultEdge> (DefaultEdge.class);

		for (Equivalences<TT> v : dag0.vertexSet())
			dag.addVertex(v);

		for (DefaultEdge edge : dag0.edgeSet()) {
			Equivalences<TT> v1 = dag0.getEdgeSource(edge);
			Equivalences<TT> v2 = dag0.getEdgeTarget(edge);
			boolean redundant = false;

			if (dag0.outDegreeOf(v1) > 1) {
				// an edge is redundant if 
				//  its source has an edge going to a vertex 
				//         from which the target is reachable (in one step) 
				for (DefaultEdge e2 : dag0.outgoingEdgesOf(v1)) 
					if (dag0.containsEdge(dag0.getEdgeTarget(e2), v2)) {
						redundant = true;
						break;
					}
			}
			if (!redundant)
				dag.addEdge(v1, v2);
		}
		
		return new EquivalencesDAGImpl<TT>(graph, dag, equivalencesMap);
	}

}
