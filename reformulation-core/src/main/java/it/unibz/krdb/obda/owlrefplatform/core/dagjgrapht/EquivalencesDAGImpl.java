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
import java.util.HashSet;
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

import com.google.common.collect.ImmutableMap;


/**
 * DAG from an OntologyGraph
 * 
 * The vertices of the DAG are equivalence sets (Equivalences)
 * The edges form the minimal set whose transitive and reflexive closure
 * coincides with the transitive and reflexive closure of the ontology graph
 *  
 *  @author Roman Kontchakov
 * 
*/

public class EquivalencesDAGImpl<T> implements EquivalencesDAG<T> {
	
	private final SimpleDirectedGraph <Equivalences<T>,DefaultEdge> dag;
	private final ImmutableMap<T, Equivalences<T>> vertexIndex;
	
	// maps all Ts (even from the non-reduced DAG) to the vertices of the possibly reduced  DAG
	private final ImmutableMap<T, Equivalences<T>> fullVertexIndex;   
	
	private final Map<Equivalences<T>, Set<Equivalences<T>>> cacheSub;
	private final Map<T, Set<T>> cacheSubRep;

	private DefaultDirectedGraph<T,DefaultEdge> graph; // used in tests only
	
	private EquivalencesDAGImpl(DefaultDirectedGraph<T,DefaultEdge> graph, SimpleDirectedGraph <Equivalences<T>,DefaultEdge> dag, ImmutableMap<T, Equivalences<T>> vertexIndex, ImmutableMap<T, Equivalences<T>> fullVertexIndex) {	
		this.graph = graph;
		this.dag = dag;
		this.vertexIndex = vertexIndex;
		this.fullVertexIndex = fullVertexIndex;
		
		this.cacheSub = new HashMap<>();
		this.cacheSubRep = new HashMap<>();
	}

	
	/** 
	 * 
	 */
	@Override
	public Equivalences<T> getVertex(T v) {
		return vertexIndex.get(v);
	}
	
	@Override
	public T getCanonicalForm(T v) {
		Equivalences<T> vs = fullVertexIndex.get(v);
		if (vs == null)
			return null;
		
		return vs.getRepresentative();		
	}
	
	/**
	 * 
	 */
	@Override
	public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
		Set<Equivalences<T>> result = new LinkedHashSet<>();

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
			result = new LinkedHashSet<>();

			BreadthFirstIterator<Equivalences<T>, DefaultEdge>  iterator = 
						new BreadthFirstIterator<Equivalences<T>, DefaultEdge>(
								new EdgeReversedGraph<>(dag), v);

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
		Equivalences<T> eq = vertexIndex.get(v);
		
		if (eq == null)
			return Collections.singleton(v);
		
		Set<T> result = cacheSubRep.get(eq.getRepresentative());
		if (result == null) {
			result = new LinkedHashSet<T>();

			BreadthFirstIterator<Equivalences<T>, DefaultEdge>  iterator = 
						new BreadthFirstIterator<Equivalences<T>, DefaultEdge>(
								new EdgeReversedGraph<>(dag), eq);

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
		Set<Equivalences<T>> result = new LinkedHashSet<>();

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

		Set<Equivalences<T>> result = new LinkedHashSet<>();

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
				"\n\nEquivalencesMap\n" + vertexIndex;
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

	@Deprecated 
	public int vertexSetSize() { 
		return dag.vertexSet().size();
	}
	
	
	public DefaultDirectedGraph<T,DefaultEdge> getGraph() {
		if (graph == null) {
			graph = new DefaultDirectedGraph<>(DefaultEdge.class);

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
		GabowSCC<TT, DefaultEdge> inspector = new GabowSCC<>(graph);
		List<Equivalences<TT>> equivalenceSets = inspector.stronglyConnectedSets();

		// create the vertex index
		
		ImmutableMap.Builder<TT, Equivalences<TT>> vertexIndexBuilder = new ImmutableMap.Builder<>();
		for (Equivalences<TT> equivalenceSet : equivalenceSets) {
			for (TT node : equivalenceSet) 
				vertexIndexBuilder.put(node, equivalenceSet);
		}
		ImmutableMap<TT, Equivalences<TT>> vertexIndex = vertexIndexBuilder.build();
		
		// compute the edges between the SCCs
		
		Map<Equivalences<TT>, Set<Equivalences<TT>>> outgoingEdges = new HashMap<>();
		
		for (DefaultEdge edge : graph.edgeSet()) {
			Equivalences<TT> v1 = vertexIndex.get(graph.getEdgeSource(edge));
			Equivalences<TT> v2 = vertexIndex.get(graph.getEdgeTarget(edge));
			if (v1 == v2)
				continue; // do not add loops
			
			Set<Equivalences<TT>> out = outgoingEdges.get(v1);
			if (out == null) {
				out = new HashSet<>();
				outgoingEdges.put(v1, out);
			}
			out.add(v2);
		}

		// compute the transitively reduced DAG
		
		SimpleDirectedGraph<Equivalences<TT>,DefaultEdge> dag = new SimpleDirectedGraph<>(DefaultEdge.class);
		for (Equivalences<TT> equivalenceSet : equivalenceSets)  
			dag.addVertex(equivalenceSet);
		
		for (Map.Entry<Equivalences<TT>, Set<Equivalences<TT>>> edges : outgoingEdges.entrySet()) {
			Equivalences<TT> v1 = edges.getKey();
			for (Equivalences<TT> v2 : edges.getValue()) {
				// an edge from v1 to v2 is redundant if 
				//  v1 has an edge going to a vertex v2p 
				//         from which v2 is reachable (in one step) 
				boolean redundant = false;
				if (edges.getValue().size() > 1) {
	 				for (Equivalences<TT> v2p : edges.getValue()) {
						Set<Equivalences<TT>> t2p = outgoingEdges.get(v2p);
						if (t2p!= null && t2p.contains(v2)) {
							redundant = true;
							break;
						}	
					}
				}
				if (!redundant)
					dag.addEdge(v1, v2);
			}
		}
		
		return new EquivalencesDAGImpl<TT>(graph, dag, vertexIndex, vertexIndex);
	}

	
	public static <T> EquivalencesDAGImpl<T> reduce(EquivalencesDAGImpl<T> source, SimpleDirectedGraph <Equivalences<T>,DefaultEdge> target) {
		
		ImmutableMap.Builder<T, Equivalences<T>> vertexIndexBuilder = new ImmutableMap.Builder<>();
		for (Equivalences<T> tSet : target.vertexSet()) {
			for (T s : source.getVertex(tSet.getRepresentative())) 
				if (tSet.contains(s)) 		
					vertexIndexBuilder.put(s, tSet);
		}
		ImmutableMap<T, Equivalences<T>> vertexIndex = vertexIndexBuilder.build();	
		
		// create induced edges in the target graph		
		for (Equivalences<T> sSet : source) {
			Equivalences<T> tSet = vertexIndex.get(sSet.getRepresentative());
			
			for (Equivalences<T> sSetSub : source.getDirectSub(sSet)) {
				Equivalences<T> tSetSub = vertexIndex.get(sSetSub.getRepresentative());
				target.addEdge(tSetSub, tSet);
			}
		}		
		
		return new EquivalencesDAGImpl<>(null, target, vertexIndex, source.vertexIndex);
	}
	
}
