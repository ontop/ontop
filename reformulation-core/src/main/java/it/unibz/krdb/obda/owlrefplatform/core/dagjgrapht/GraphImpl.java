/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/** Use to build a simple graph.
 * 
 * We probably don't need this class we can simply used DefaultDirectedGraph<V,E>
 * A directed graph multiple edges are not permitted, but loops are. 
 *
 */

public class GraphImpl <V, E> extends DefaultDirectedGraph<V,E> implements Graph {
	
	private Set<ClassDescription> classes = new LinkedHashSet<ClassDescription> ();

	private Set<Property> roles = new LinkedHashSet<Property> ();

	

	public GraphImpl(Class<? extends E> arg0) {
		super(arg0);
		
	
		
		initialize();
	}
	

	private void initialize(){
		
		
	}
	
	
	//add Vertex in the graph
	 public boolean addVertex(Description d, V v)
	    {
	        boolean added = super.addVertex(v);

//	        if (added) {
//	        	
//	            // add to class or roles
//	        	allnodes.put(d, v);
//	         
//	        }

	        return added;
	    }

	 //remove Vertex from the graph
	 public boolean removeVertex(V v){
		 
		 boolean remove = super.removeVertex(v);

//	        if (remove) {
//	            // remove to class or roles
//	        	
////	        	classes.remove(v);
////	        	roles.remove(v);
//	        	allnodes.remove(v);
//	         
//	        }

	        return remove;
	 }
	 
	 
	 //remove all vertices in the graph
	  public boolean removeAllVertices(Collection<? extends V> arg0)
	    {
	        boolean removed = super.removeAllVertices(arg0);

//	        if (removed) {
//	            // remove all classes and roles
//	        	classes.clear();
//	        	roles.clear();
//	        	allnodes.clear();
//	         
//	        }
	        return removed;
	    }


	  //return all roles in the graph
	  public Set<Property> getRoles(){
		  for (V r: this.vertexSet()){
			  if (r.getClass().equals(Property.class))
				  roles.add((Property)r);
				  
				  
		  }
		  return roles;
		  
	  }
	  
	  //return all classes in the graph
	  public Set<ClassDescription> getClasses(){
		  for (V c: this.vertexSet()){
			  if (c.getClass().equals(ClassDescription.class))
				  classes.add((ClassDescription)c);
				  
				  
		  }
		  return classes;
		  
	  }


	
	
	


}
