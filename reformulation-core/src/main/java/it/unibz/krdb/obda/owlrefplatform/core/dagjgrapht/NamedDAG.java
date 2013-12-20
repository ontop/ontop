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
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.DirectedGraph;
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
	
	private final DAGImpl originalDag;
	private final SimpleDirectedGraph <Description,DefaultEdge> dag;
	
	// constructor is accessible within the class only
	private NamedDAG(SimpleDirectedGraph<Description,DefaultEdge> dag, DAGImpl originalDag) {
		this.dag = dag;
		this.originalDag = originalDag;
	}
	
	
	/**
	 * Allows to have all named roles in the DAG even the equivalent named roles
	 * @return  set of all property (not inverse) in the DAG
	 */
	public Set<Property> getRoles(){
		return originalDag.getPropertyNames();
	}

	/**
	 * Allows to have all named classes in the DAG even the equivalent named classes
	 * @return  set of all named concepts in the DAG
	 */
	
	public Set<OClass> getClasses(){
		return originalDag.getClassNames();
	}


	/**
	 * Allows to have the  map with equivalences
	 * @return  a map between the node and the set of all its equivalent nodes
	 */
	public EquivalenceClass<Description> getEquivalenceClass(Description desc) {
		return originalDag.getEquivalenceClass(desc);
	}

	public Description getRepresentativeFor(Description v) {
		return originalDag.getRepresentativeFor(v);
	}

	/**
	 * Allows to obtain the node present in the DAG. 
	 * @param  node a node that we want to know if it is part of the DAG
	 * @return the node, or its representative, or null if it is not present in the DAG
	 */
	
	public Description getNode(Description node){
		Description rep = getRepresentativeFor(node);
		if (!dag.containsVertex(rep))
			return null;
		
		return rep;	
	}

	@Override
	public String toString() {
		return dag.toString();
	}
	
	public DirectedGraph<Description, DefaultEdge> getReversedDag() {
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(dag); // WOULD IT NOT BE BETTER TO CACHE?
		return reversed;
	}

	public Set<Description> vertexSet() {
		return dag.vertexSet();
	}

	
	
	@Deprecated // USED ONLY IN TESTS
	public SimpleDirectedGraph <Description,DefaultEdge> getDag() {
		return dag;
	}
	
	
	
	
	/**
	 * Constructor for the NamedDAGBuilder
	 * @param dag the DAG from which we want to maintain only the named descriptions
	 */
	
	public static NamedDAG getNamedDAG(DAGImpl dag) {

		SimpleDirectedGraph <Description,DefaultEdge>  namedDag = dag.getCopy(); 

		OntologyFactory descFactory = OntologyFactoryImpl.getInstance();
				
		/*
		 * Test with a reversed graph so that the incoming edges 
		 * represent the parents of the node
		 */
		
		DirectedGraph<Description, DefaultEdge> reversed = dag.getReversedDag();

		LinkedList<Description> roots = new LinkedList<Description>();
		for (Description n : reversed.vertexSet()) {
			if ((reversed.incomingEdgesOf(n)).isEmpty()) {
				roots.add(n);
			}
		}
		
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
			
				if (dag.isNamed(node)) {
					processedNodes.add(node);
					continue;
				}
			
				if (node instanceof Property) {
					Property posNode = descFactory.createProperty(((Property)node).getPredicate(), false);
					if (processedNodes.contains(posNode)) {
						eliminateNode(namedDag, node);
						processedNodes.add(node);					
						continue;
					}
				}
			
				Set<Description> namedEquivalences = new LinkedHashSet<Description>();
				for (Description vertex : dag.getEquivalenceClass(node)) {
					if (dag.isNamed(vertex)) 
						namedEquivalences.add(vertex);
				}
							
				if(!namedEquivalences.isEmpty()) {
					Description newReference = namedEquivalences.iterator().next();
					newReference = dag.getRepresentativeFor(newReference);
					
					//replacements.remove(newReference);
					namedDag.addVertex(newReference);
				
					/*
					 * Re-pointing all links to and from the eliminated node to the new
					 * representative node
					 */
					
					for (DefaultEdge incEdge : namedDag.incomingEdgesOf(node)) {
						Description source = namedDag.getEdgeSource(incEdge);
						namedDag.removeAllEdges(source, node);
					
						if (!source.equals(newReference))
							namedDag.addEdge(source, newReference);
					}
					
					for (DefaultEdge outEdge : namedDag.outgoingEdgesOf(node)) {
						Description target = namedDag.getEdgeTarget(outEdge);
						namedDag.removeAllEdges(node, target);
					
						if (!target.equals(newReference))
							namedDag.addEdge(newReference, target);
					}
					
					namedDag.removeVertex(node);
					processedNodes.add(node);
					 
					if (node instanceof Property) {
						/*remove the inverse */
						Property posNode = descFactory.createProperty(((Property)node).getPredicate(), false);
						eliminateNode(namedDag, posNode);
					}
				}
				else {
					eliminateNode(namedDag, node);
					processedNodes.add(node);
				}
			} // end while
		} // end for each root
		
		NamedDAG dagImpl = new NamedDAG(namedDag, dag);
		return dagImpl;
	}
	
	private static void eliminateNode(SimpleDirectedGraph <Description,DefaultEdge>  namedDag, Description node) {
		Set<DefaultEdge> incomingEdges = new HashSet<DefaultEdge>(
				namedDag.incomingEdgesOf(node));

		// I do a copy of the dag not to remove edges that I still need to
		// consider in the loops
		SimpleDirectedGraph <Description,DefaultEdge> copyDAG = 
					(SimpleDirectedGraph <Description,DefaultEdge>) namedDag.clone();
		Set<DefaultEdge> outgoingEdges = new HashSet<DefaultEdge>(
				copyDAG.outgoingEdgesOf(node));
		for (DefaultEdge incEdge : incomingEdges) {

			Description source = namedDag.getEdgeSource(incEdge);
			namedDag.removeAllEdges(source, node);

			for (DefaultEdge outEdge : outgoingEdges) {
				Description target = copyDAG.getEdgeTarget(outEdge);
				namedDag.removeAllEdges(node, target);

				if (!source.equals(target))
					namedDag.addEdge(source, target);
			}

		}
		namedDag.removeVertex(node);		
	}	
	
}
