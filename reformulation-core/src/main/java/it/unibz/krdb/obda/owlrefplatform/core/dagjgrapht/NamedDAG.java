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
import it.unibz.krdb.obda.ontology.Property;

import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

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

	
	
	@Deprecated // USED ONLY IN TESTS (3 calls)
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

		SimpleDirectedGraph <Description,DefaultEdge>  namedDag 
					= new SimpleDirectedGraph <Description,DefaultEdge> (DefaultEdge.class); 

		for (Equivalences<Property> v : reasoner.getProperties()) 
			namedDag.addVertex(v.getRepresentative());
		
		for (Equivalences<BasicClassDescription> v : reasoner.getClasses()) 
			namedDag.addVertex(v.getRepresentative());
		
		for (Equivalences<Property> s : reasoner.getProperties()) 
			for (Equivalences<Property> t : reasoner.getDirectSuperProperties(s.getRepresentative())) 
				namedDag.addEdge(s.getRepresentative(), t.getRepresentative());
		
		for (Equivalences<BasicClassDescription> s : reasoner.getClasses()) 
			for (Equivalences<BasicClassDescription> t : reasoner.getDirectSuperClasses(s.getRepresentative())) 
				namedDag.addEdge(s.getRepresentative(), t.getRepresentative());

		for (Equivalences<Property> v : reasoner.getProperties()) 
			if (!v.isIndexed()) {
				// eliminate node
				for (DefaultEdge incEdge : namedDag.incomingEdgesOf(v.getRepresentative())) { 
					Description source = namedDag.getEdgeSource(incEdge);

					for (DefaultEdge outEdge : namedDag.outgoingEdgesOf(v.getRepresentative())) {
						Description target = namedDag.getEdgeTarget(outEdge);

						namedDag.addEdge(source, target);
					}
				}
				namedDag.removeVertex(v.getRepresentative());		// removes all adjacent edges as well				
			}
		for (Equivalences<BasicClassDescription> v : reasoner.getClasses()) 
			if (!v.isIndexed()) {
				// eliminate node
				for (DefaultEdge incEdge : namedDag.incomingEdgesOf(v.getRepresentative())) { 
					Description source = namedDag.getEdgeSource(incEdge);

					for (DefaultEdge outEdge : namedDag.outgoingEdgesOf(v.getRepresentative())) {
						Description target = namedDag.getEdgeTarget(outEdge);

						namedDag.addEdge(source, target);
					}
				}
				namedDag.removeVertex(v.getRepresentative());		// removes all adjacent edges as well				
			}
				
		NamedDAG dagImpl = new NamedDAG(namedDag);
		return dagImpl;
	}
		
}
