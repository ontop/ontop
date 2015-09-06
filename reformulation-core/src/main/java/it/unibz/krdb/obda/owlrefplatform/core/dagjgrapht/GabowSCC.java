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


import java.util.*;

import org.jgrapht.*;

import com.google.common.collect.ImmutableSet;


/**
 * Allows obtaining the strongly connected components of a directed graph. 
 *
 *The implemented algorithm follows Cheriyan-Mehlhorn/Gabow's algorithm
 *Presented in Path-based depth-first search for strong and biconnected components by Gabow (2000).
 *The running time is order of O(|V|+|E|)

 *
 *
 */

public class GabowSCC<V, E>
{
    //~ Instance fields --------------------------------------------------------

    // the graph to compute the strongly connected sets 
    private final DirectedGraph<V, E> graph;

    // stores the vertices
    private Deque<VertexNumber<V>> stack = new ArrayDeque<VertexNumber<V>>();
    
    // the result of the computation, cached for future calls
    private List<Equivalences<V>> stronglyConnectedSets;

    // maps vertices to their VertexNumber object
    private Map<V, VertexNumber<V>> vertexToVertexNumber;
    
    //store the numbers
    private Deque<Integer> B = new ArrayDeque<Integer>();
    
    //number of vertexes
    private int c; 

    //~ Constructors -----------------------------------------------------------

    /**
     * The constructor of  GabowSCC class.
     *
     * @param directedGraph the graph to inspect
     *
     * @throws IllegalArgumentException
     */
    public GabowSCC(DirectedGraph<V, E> directedGraph)
    {
        assert (directedGraph != null);
        
        graph = directedGraph;
        vertexToVertexNumber = null;
        
        stronglyConnectedSets = null;        
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the graph inspected 
     *
     * @return the graph inspected 
     */
    public DirectedGraph<V, E> getGraph() {
        return graph;
    }

    /**
     * Returns true if the graph instance is strongly connected.
     *
     * @return true if the graph is strongly connected, false otherwise
     */
    public boolean isStronglyConnected() {
        return stronglyConnectedSets().size() == 1;
    }

    /**
     * Computes a {@link List} of {@link Set}s, where each set contains vertices
     * which together form a strongly connected component within the given
     * graph.
     *
     * @return <code>List</code> of <code>EquivalanceClass</code>es containing the strongly
     * connected components
     */
    public List<Equivalences<V>> stronglyConnectedSets()
    {
        if (stronglyConnectedSets == null) {
            
            stronglyConnectedSets = new Vector<>();

            // create VertexData objects for all vertices, store them
            createVertexNumber();

            // perform  DFS
            for (VertexNumber<V> data : vertexToVertexNumber.values()) {           	
                if (data.getNumber() == 0) {
                    dfsVisit(graph, data);
                }
            }
                  
            vertexToVertexNumber = null;
            stack = null;
            B = null;
        }

        return stronglyConnectedSets;
    }

   
    /*
     * Creates a VertexNumber object for every vertex in the graph and stores
     * them in a HashMap.
     */
    
    private void createVertexNumber()
    {
    	c = graph.vertexSet().size();
        vertexToVertexNumber = new HashMap<>(c);

        for (V vertex : graph.vertexSet()) {
            vertexToVertexNumber.put(vertex, new VertexNumber<V>(vertex, 0));            
        }
        
        stack = new ArrayDeque<>(c);
        B = new ArrayDeque<>(c);
    }

    /*
     * The subroutine of DFS. 
     */
    private void dfsVisit(DirectedGraph<V, E> visitedGraph, VertexNumber<V> v) 
    {	
    	 VertexNumber<V> w;
    	 stack.add(v);
    	 B.add(v.setNumber(stack.size() - 1));
    	
         // follow all edges
         for (E edge : visitedGraph.outgoingEdgesOf(v.getVertex())) {
        	 
        	 w =  vertexToVertexNumber.get(visitedGraph.getEdgeTarget(edge));

             if (w.getNumber() == 0) {
            	 dfsVisit(graph, w);
             }
             else { /*contract if necessary*/
            	 while (w.getNumber() < B.getLast()) 
            		 B.removeLast(); 
             } 
         }
         
         ImmutableSet.Builder<V> L = new ImmutableSet.Builder<>(); 
         if (v.getNumber() == (B.getLast())) { 
        	 /* number vertices of the next strong component */
        	 B.removeLast(); 
              
             c++;
             while (v.getNumber() <= (stack.size()-1)) {
            	 VertexNumber<V> r = stack.removeLast();
                 L.add(r.getVertex()); 
                 r.setNumber(c);
             } 
             stronglyConnectedSets.add(new Equivalences<V>(L.build())); 
         } 
    }

  
   
    
    private static final class VertexNumber<V>
    {
    	V vertex;
    	int number;
    	
    	private VertexNumber(V vertex, int number) {
    		this.vertex = vertex;
    		this.number = number;
    	}

    	int getNumber() {
    		return number;
    	}

    	V getVertex() {
    		return vertex;
    	}
    	
    	Integer setNumber(int n) {
    		return number=n;
    	}
    }
}

// End 
