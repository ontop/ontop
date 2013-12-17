/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/** 
 * Used to represent a DAG and a named DAG.
 * Extend SimpleDirectedGraph from the JGrapht library
 * It can be constructed using the class DAGBuilder.
 * 
 * 
 */

public class DAGImpl extends SimpleDirectedGraph <Description,DefaultEdge> implements DAG {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4466539952784531284L;
	
	boolean dag = false;
	boolean namedDAG = false;
	
	private Set<OClass> classes = new LinkedHashSet<OClass> ();
	private Set<Property> roles = new LinkedHashSet<Property> ();
	
	//map between an element  and the representative between the equivalent elements
	private Map<Description, Description> replacements = new HashMap<Description, Description>();
	
	//map of the equivalent elements of an element
	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	public DAGImpl(Class<? extends DefaultEdge> arg0) {
		super(arg0);
		dag=true;
	}

	public DAGImpl(EdgeFactory<Description,DefaultEdge> ef) {
		super(ef);
		dag=true;
	}
	
	
	/**
	 * set we are working with a DAG and not a named DAG
	 * 
	 * @param d boolean <code> true</code> if DAG
	 */
	
	public void setIsaDAG(boolean d){
		
		dag=d;
		namedDAG=!d;

	}

	/**
	 * set we are working with a named DAG and not with a DAG
	 * 
	 * @param nd boolean <code> true</code> if named DAG
	 */

	public void setIsaNamedDAG(boolean nd){
		
		namedDAG=nd;
		dag=!nd;

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
		return namedDAG;


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
		for (Description c: this.vertexSet()){
			
			//check in the equivalent nodes if there are named classes
			if(replacements.containsValue(c)){
			if(equivalencesMap.get(c)!=null){
				
				for (Description e: equivalencesMap.get(c))	{
					if (e instanceof OClass){
//						System.out.println("classes: "+ e +" "+ e.getClass());
						classes.add((OClass)e);
				}
				}
			}
			}
			
			if (c instanceof OClass){
//				System.out.println("classes: "+ c+ " "+ c.getClass());
				classes.add((OClass)c);
			}

		}
		return classes;

	}


	@Override
	/**
	 * Allows to have the  map with equivalences
	 * @return  a map between the node and the set of all its equivalent nodes
	 */
	public Map<Description, Set<Description>> getMapEquivalences() {
		
		return equivalencesMap;
	}

	@Override
	/**
	 * Allows to have the map with replacements
	 * @return  a map between the node and its representative node
	 */
	public Map<Description, Description> getReplacements() {
		return replacements;
	}

	@Override
	/**
	 * Allows to set the map with equivalences
	 * @param  equivalences a map between the node and the set of all its equivalent nodes
	 */
	public void setMapEquivalences(Map<Description, Set<Description>> equivalences) {
		this.equivalencesMap= equivalences;
		
	}
	
	/**
	 * Allows to set the map with replacements
	 * @param  replacements a map between the node and its representative node
	 */
	@Override
	public void setReplacements(Map<Description, Description> replacements) {
		this.replacements=replacements;
		
	}
	
	@Override
	/**
	 * Allows to obtain the node present in the DAG. 
	 * @param  node a node that we want to know if it is part of the DAG
	 * @return the node, or its representative, or null if it is not present in the DAG
	 */
	public Description getNode(Description node){
		if(replacements.containsKey(node))
			node= replacements.get(node);
		else
		if(!this.vertexSet().contains(node))
			node=null;
		return node;
		
	}

	






}
