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
	
	private final SimpleDirectedGraph <Property,DefaultEdge> propertyDAG;
	private final SimpleDirectedGraph <BasicClassDescription,DefaultEdge> classDAG;
	
	// constructor is accessible within the class only
	private NamedDAG(SimpleDirectedGraph<Property,DefaultEdge> propertyDAG, SimpleDirectedGraph<BasicClassDescription,DefaultEdge> classDAG) {
		this.propertyDAG = propertyDAG;
		this.classDAG = classDAG;
	}
	
	@Override
	public String toString() {
		return propertyDAG.toString() + classDAG.toString();
	}
	
	// the real working method (used in the SemanticIndexEngineImple)
	public DirectedGraph<Property, DefaultEdge> getReversedPropertyDag() {
		DirectedGraph<Property, DefaultEdge> reversed =
				new EdgeReversedGraph<Property, DefaultEdge>(propertyDAG); // WOULD IT NOT BE BETTER TO CACHE?
		return reversed;
	}
	public DirectedGraph<BasicClassDescription, DefaultEdge> getReversedClassDag() {
		DirectedGraph<BasicClassDescription, DefaultEdge> reversed =
				new EdgeReversedGraph<BasicClassDescription, DefaultEdge>(classDAG); // WOULD IT NOT BE BETTER TO CACHE?
		return reversed;
	}

	
	
	@Deprecated // USED ONLY IN TESTS (3 calls)
	public SimpleDirectedGraph <Property,DefaultEdge> getPropertyDag() {
		return propertyDAG;
	}
	@Deprecated // USED ONLY IN TESTS (3 calls)
	public SimpleDirectedGraph <BasicClassDescription,DefaultEdge> getClassDag() {
		return classDAG;
	}
	
	public List<Property> getSuccessors(Property desc) {
		return Graphs.successorListOf(propertyDAG, desc);		
	}
	public List<BasicClassDescription> getSuccessors(BasicClassDescription desc) {
		return Graphs.successorListOf(classDAG, desc);		
	}
	
	public List<Property> getPredecessors(Property desc) {
		return Graphs.predecessorListOf(propertyDAG, desc);		
	}
	public List<BasicClassDescription> getPredecessors(BasicClassDescription desc) {
		return Graphs.predecessorListOf(classDAG, desc);		
	}
	
	
	/**
	 * Constructor for the NamedDAGBuilder
	 * @param dag the DAG from which we want to maintain only the named descriptions
	 */

	
	public static NamedDAG getNamedDAG(TBoxReasonerImpl reasoner) {

		SimpleDirectedGraph <Property,DefaultEdge>  propertyDAG 
					= new SimpleDirectedGraph <Property,DefaultEdge> (DefaultEdge.class); 

		for (Equivalences<Property> v : reasoner.getProperties()) 
			propertyDAG.addVertex(v.getRepresentative());

		for (Equivalences<Property> s : reasoner.getProperties()) 
			for (Equivalences<Property> t : reasoner.getDirectSuperProperties(s.getRepresentative())) 
				propertyDAG.addEdge(s.getRepresentative(), t.getRepresentative());
		
		for (Equivalences<Property> v : reasoner.getProperties()) 
			if (!v.isIndexed()) {
				// eliminate node
				for (DefaultEdge incEdge : propertyDAG.incomingEdgesOf(v.getRepresentative())) { 
					Property source = propertyDAG.getEdgeSource(incEdge);

					for (DefaultEdge outEdge : propertyDAG.outgoingEdgesOf(v.getRepresentative())) {
						Property target = propertyDAG.getEdgeTarget(outEdge);

						propertyDAG.addEdge(source, target);
					}
				}
				propertyDAG.removeVertex(v.getRepresentative());		// removes all adjacent edges as well				
			}
	
		
		
		
		
		SimpleDirectedGraph <BasicClassDescription,DefaultEdge>  classDAG 
		= new SimpleDirectedGraph <BasicClassDescription,DefaultEdge> (DefaultEdge.class); 
		
		for (Equivalences<BasicClassDescription> v : reasoner.getClasses()) 
			classDAG.addVertex(v.getRepresentative());
		
		for (Equivalences<BasicClassDescription> s : reasoner.getClasses()) 
			for (Equivalences<BasicClassDescription> t : reasoner.getDirectSuperClasses(s.getRepresentative())) 
				classDAG.addEdge(s.getRepresentative(), t.getRepresentative());

		for (Equivalences<BasicClassDescription> v : reasoner.getClasses()) 
			if (!v.isIndexed()) {
				// eliminate node
				for (DefaultEdge incEdge : classDAG.incomingEdgesOf(v.getRepresentative())) { 
					BasicClassDescription source = classDAG.getEdgeSource(incEdge);

					for (DefaultEdge outEdge : classDAG.outgoingEdgesOf(v.getRepresentative())) {
						BasicClassDescription target = classDAG.getEdgeTarget(outEdge);

						classDAG.addEdge(source, target);
					}
				}
				classDAG.removeVertex(v.getRepresentative());		// removes all adjacent edges as well				
			}
				
		NamedDAG dagImpl = new NamedDAG(propertyDAG, classDAG);
		return dagImpl;
	}

	public DirectedGraph<Description, DefaultEdge> getReversedDag() {
		SimpleDirectedGraph <Description,DefaultEdge>  dag 
			= new SimpleDirectedGraph <Description,DefaultEdge> (DefaultEdge.class); 
		Graphs.addGraph(dag, propertyDAG);
		Graphs.addGraph(dag, classDAG);
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(dag); // WOULD IT NOT BE BETTER TO CACHE?
		return reversed;
	}
		
}
