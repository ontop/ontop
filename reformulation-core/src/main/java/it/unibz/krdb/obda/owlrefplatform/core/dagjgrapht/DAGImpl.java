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

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/** 
 * Used to represent a DAG and a named DAG.
 * Extend SimpleDirectedGraph from the JGrapht library
 * It can be constructed using the class DAGBuilder.
 * 
 * 
 */

public class DAGImpl extends SimpleDirectedGraph <Description,DefaultEdge> {

	private static final long serialVersionUID = 4466539952784531284L;
	
	boolean dag; // true if DAG, false if NamedDAG
	
	private Set<OClass> classes = new LinkedHashSet<OClass> ();
	private Set<Property> roles = new LinkedHashSet<Property> ();
	
	//map between an element  and the representative between the equivalent elements
	private Map<Description, Description> replacements; 
	
	//map of the equivalent elements of an element
	private Map<Description, Set<Description>> equivalencesMap; 

	public DAGImpl(Map<Description, Set<Description>> equivalencesMap, Map<Description, Description> replacements, boolean dag) {
		super(DefaultEdge.class);
		this.equivalencesMap = equivalencesMap;
		this.replacements = replacements;
		this.dag = dag;
	}

	
	/**
	 * check if we are working with a DAG
	 * 
	 * @return boolean <code> true</code> if DAG and not named DAG
	 */

	public boolean isaDAG(){
		return dag;
	}

	/**
	 * check if we are working with a DAG and not a named DAG
	 * 
	 * @return boolean <code> true</code> if named DAG and not DAG
	 */
	public boolean isaNamedDAG(){
		return !dag; 
	}
	
	/**
	 * Allows to have all named roles in the DAG even the equivalent named roles
	 * @return  set of all property (not inverse) in the DAG
	 */
	public Set<Property> getRoles(){
		for (Description r: this.vertexSet()){
			
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
		for (Description c: this.vertexSet()) {			
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
	public Map<Description, Set<Description>> getMapEquivalences() {
		return equivalencesMap;
	}

	/**
	 * Allows to have the map with replacements
	 * @return  a map between the node and its representative node
	 */
	public Map<Description, Description> getReplacements() {
		return replacements;
	}

	/**
	 * Allows to set the map with replacements
	 * @param  replacements a map between the node and its representative node
	 */
	public void setReplacements(Map<Description, Description> replacements) {
		this.replacements = replacements;	
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
			if(!this.vertexSet().contains(node))
				node = null;
		return node;
		
	}
}
