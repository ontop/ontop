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
import it.unibz.krdb.obda.ontology.Property;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
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

public class NamedDAGImpl  {
	
	private SimpleDirectedGraph <Description,DefaultEdge> dag;
	
	private Set<OClass> classes = new LinkedHashSet<OClass> ();
	private Set<Property> roles = new LinkedHashSet<Property> ();
	
	//map between an element  and the representative between the equivalent elements
	private Map<Description, Description> replacements; 
	
	//map of the equivalent elements of an element
	private Map<Description, Set<Description>> equivalencesMap; 

	// constructor is accessible within the package only
	NamedDAGImpl(SimpleDirectedGraph<Description,DefaultEdge> dag, Map<Description, Set<Description>> equivalencesMap, Map<Description, Description> replacements) {
		this.dag = dag;
		this.equivalencesMap = equivalencesMap;
		this.replacements = replacements;
	}
	
	
	
	/**
	 * Allows to have all named roles in the DAG even the equivalent named roles
	 * @return  set of all property (not inverse) in the DAG
	 */
	public Set<Property> getRoles(){
		for (Description r: dag.vertexSet()){
			
			//check in the equivalent nodes if there are properties
			if(replacements.containsValue(r)){
			if(equivalencesMap.get(r)!=null){
				for (Description e: equivalencesMap.get(r))	{
					if (e instanceof Property){
//						System.out.println("roles: "+ e +" "+ e.getClass());
						if(!((Property) e).isInverse())
							roles.add((Property)e);
						
				}
				}
			}
			}
			if (r instanceof Property){
//				System.out.println("roles: "+ r +" "+ r.getClass());
				if(!((Property) r).isInverse())
					roles.add((Property)r);
			}
		}
		return roles;
	}

	/**
	 * Allows to have all named classes in the DAG even the equivalent named classes
	 * @return  set of all named concepts in the DAG
	 */
	
	public Set<OClass> getClasses(){
		for (Description c: dag.vertexSet()) {			
			//check in the equivalent nodes if there are named classes
			if (replacements.containsValue(c)) {
				if (equivalencesMap.get(c)!=null) {
				
					for (Description e: equivalencesMap.get(c))	{
						if (e instanceof OClass) {
//							System.out.println("classes: "+ e +" "+ e.getClass());
							classes.add((OClass)e);
						}
					}
				}
			}
			
			if (c instanceof OClass) {
//				System.out.println("classes: "+ c+ " "+ c.getClass());
				classes.add((OClass)c);
			}

		}
		return classes;
	}


	/**
	 * Allows to have the  map with equivalences
	 * @return  a map between the node and the set of all its equivalent nodes
	 */
	public Set<Description> getEquivalenceClass(Description desc) {
		return equivalencesMap.get(desc);
	}

	public Description getReplacementFor(Description v) {
		return replacements.get(v);
	}

	/**
	 * Allows to set the map with replacements
	 */
	@Deprecated
	public void setReplacementFor(Description key, Description value) {
		replacements.put(key, value);	
	}

	/**
	 * Allows to obtain the node present in the DAG. 
	 * @param  node a node that we want to know if it is part of the DAG
	 * @return the node, or its representative, or null if it is not present in the DAG
	 */
	public Description getNode(Description node){
		if(replacements.containsKey(node))
			node = replacements.get(node);
		else
			if(!dag.vertexSet().contains(node))
				node = null;
		return node;	
	}

	@Override
	public String toString() {
		return dag.toString();
	}
	
	
	
	public SimpleDirectedGraph <Description,DefaultEdge> getDag() {
		return dag;
	}

	public DirectedGraph<Description, DefaultEdge> getReversedDag() {
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(dag);
		return reversed;
	}


	
	public Set<Description> vertexSet() {
		return dag.vertexSet();
	}

	public Set<DefaultEdge> edgeSet() {
		return dag.edgeSet();
	}	

	public Description getEdgeSource(DefaultEdge edge) {
		return dag.getEdgeSource(edge);
	}

	public Description getEdgeTarget(DefaultEdge edge) {
		return dag.getEdgeTarget(edge);
	}

	public Set<DefaultEdge> incomingEdgesOf(Description node) {
		return dag.incomingEdgesOf(node);
	}

	public Set<DefaultEdge> outgoingEdgesOf(Description node) {
		return dag.outgoingEdgesOf(node);
	}
}
