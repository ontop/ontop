/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

/** 
 * Used to represent a DAG and a named DAG.
 * Extend SimpleDirectedGraph from the JGrapht library
 * It can be constructed using the class DAGBuilder.
 * 
 * 
 */

public class NamedDAG  {
	
	private final SimpleDirectedGraph <Description,DefaultEdge> dag;
	
	// constructor is accessible within the class only
	private NamedDAG(SimpleDirectedGraph<Description,DefaultEdge> dag) {
		this.dag = dag;
	}
	
	@Override
	public String toString() {
		return dag.toString();
	}
	
	// the real working method (used in the SemanticIndexEngineImple)
	public DirectedGraph<Description, DefaultEdge> getReversedDag() {
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(dag); // WOULD IT NOT BE BETTER TO CACHE?
		return reversed;
	}

	public Set<Description> vertexSet() {
		return dag.vertexSet();
	}

	
	
	@Deprecated // USED ONLY IN TESTS (2 calls)
	public SimpleDirectedGraph <Description,DefaultEdge> getDag() {
		return dag;
	}
	
	public List<Description> getSuccessors(Description desc) {
		return Graphs.successorListOf(dag, desc);		
	}
	
	public List<Description> getPredecessors(Description desc) {
		return Graphs.predecessorListOf(dag, desc);		
	}
	
	
	/**
	 * Constructor for the NamedDAGBuilder
	 * @param dag the DAG from which we want to maintain only the named descriptions
	 */
	
	public static NamedDAG getNamedDAG(TBoxReasonerImpl reasoner) {

		DirectedGraph<Description, DefaultEdge> dag = reasoner.getDag();

		SimpleDirectedGraph <Description,DefaultEdge>  namedDag 
					= new SimpleDirectedGraph <Description,DefaultEdge> (DefaultEdge.class); 

		for (Description v : dag.vertexSet()) {
			namedDag.addVertex(v);
		}
		for (DefaultEdge e : dag.edgeSet()) {
			Description s = dag.getEdgeSource(e);
			Description t = dag.getEdgeTarget(e);
			namedDag.addEdge(s, t, e);
		}

		/*
		 * Test with a reversed graph so that the incoming edges 
		 * represent the parents of the node
		 */
		
		DirectedGraph<Description, DefaultEdge> reversed = new EdgeReversedGraph<Description, DefaultEdge>(dag);

		LinkedList<Description> roots = new LinkedList<Description>();
		for (Description n : reversed.vertexSet()) {
			if ((reversed.incomingEdgesOf(n)).isEmpty()) {
				roots.add(n);
			}
		}

		OntologyFactory descFactory = OntologyFactoryImpl.getInstance();
		
		
		Set<Description> processedNodes = new HashSet<Description>();
		
		for (Description root: roots) {
		
			/* 
			 * A depth first sort from each root of the DAG.
			 * If the node is named we keep it, otherwise we remove it and 
			 * we connect all its descendants to all its ancestors.
			 */
			GraphIterator<Description, DefaultEdge> orderIterator =
				new DepthFirstIterator<Description, DefaultEdge>(reversed, root);	
		
			while (orderIterator.hasNext()) {
				Description node = orderIterator.next();
			
				if (processedNodes.contains(node))
					continue;
			
				if (reasoner.isNamed(node)) {
					processedNodes.add(node);
					continue;
				}
			
				if (node instanceof Property) {
					// node is the inverse of a property
					Property posNode = descFactory.createProperty(((Property)node).getPredicate(), false);
					if (processedNodes.contains(posNode)) {
						eliminateNode(namedDag, node);   // eliminates inverse property only if the property has need processed
						processedNodes.add(node);					
						continue;
					}
				}
				
				// obtain a representative for the equivalence class
				Description nodeRep = reasoner.getRepresentativeFor(node); 
							
				if(reasoner.isNamed(nodeRep)) { 
					// node is not named, but has a named equivalence
					// ROMAN: i'm not sure how this can happen
					namedDag.addVertex(nodeRep);
				
					// redirect all links to and from the eliminated node to the representative node
					
					for (DefaultEdge incEdge : namedDag.incomingEdgesOf(node)) {
						Description source = namedDag.getEdgeSource(incEdge);
					
						if (!source.equals(nodeRep))
							namedDag.addEdge(source, nodeRep);
					}
					
					for (DefaultEdge outEdge : namedDag.outgoingEdgesOf(node)) {
						Description target = namedDag.getEdgeTarget(outEdge);
					
						if (!target.equals(nodeRep))
							namedDag.addEdge(nodeRep, target);
					}
					
					namedDag.removeVertex(node); // removed all adjacent edges as well

					if (node instanceof Property) {
						// node is the inverse of a property
						// remove the property because its inverse has a named representative
						Property posNode = descFactory.createProperty(((Property)node).getPredicate(), false);
						eliminateNode(namedDag, posNode);
					}
				}
				else {
					// no named equivalences
					eliminateNode(namedDag, node);
				}
				
				processedNodes.add(node);
				
			} // end while
		} // end for each root
		
		NamedDAG dagImpl = new NamedDAG(namedDag);
		return dagImpl;
	}
	
	private static void eliminateNode(SimpleDirectedGraph <Description,DefaultEdge>  namedDag, Description node) {

		// no need to copy -- the node is removed after all the connections are made
		
		for (DefaultEdge incEdge : namedDag.incomingEdgesOf(node)) { 
			Description source = namedDag.getEdgeSource(incEdge);

			for (DefaultEdge outEdge : namedDag.outgoingEdgesOf(node)) {
				Description target = namedDag.getEdgeTarget(outEdge);

				if (!source.equals(target))
					namedDag.addEdge(source, target);
			}

		}
		namedDag.removeVertex(node);		// removes all adjacent edges as well
	}	
	
}
