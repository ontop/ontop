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

import java.util.Collections;
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
 * Used to represent a DAG.
 * Uses SimpleDirectedGraph from the JGrapht library
 * It can be constructed using the class DAGBuilder.
 * 
 * 
 */

public class DAGImpl  {
	
	private final SimpleDirectedGraph <Description,DefaultEdge> dag;
	
	//map between an element  and the representative between the equivalent elements
	private Map<Description, Description> replacements; 
	
	//map of the equivalent elements of an element
	private Map<Description, EquivalenceClass<Description>> equivalencesClasses; 

	
	// constructor is accessible within the package only
	DAGImpl(DefaultDirectedGraph<Description,DefaultEdge> dag, Map<Description, 
			EquivalenceClass<Description>> equivalencesMap, 
			Map<Description, Description> replacements) {
		
		this.dag = new SimpleDirectedGraph <Description,DefaultEdge> (DefaultEdge.class);
		Graphs.addGraph(this.dag, dag);
		this.equivalencesClasses = equivalencesMap;
		this.replacements = replacements;
	}

		

	public EquivalenceClass<Description> getEquivalenceClass(Description desc) {
		EquivalenceClass<Description> c = equivalencesClasses.get(desc);
		if (c == null)
			c = new EquivalenceClass<Description>(Collections.singleton(desc));
		return c;
	}
	
	public Description getRepresentativeFor(Description v) {
		Description rep = replacements.get(v);
		if (rep != null)   // there is a proper replacement
			return rep;
		return v;		   // no replacement -- return the node
	}

	public boolean hasReplacementFor(Description v) {
		return (replacements.get(v) != null);
	}

	@Deprecated 
	public Description getReplacementFor(Description v) {
		return replacements.get(v);
	}
	
	
	
	/**
	 * Allows to obtain the node present in the DAG. 
	 * @param  node a node that we want to know if it is part of the DAG
	 * @return the node, or its representative, or null if it is not present in the DAG
	 */
	public Description getNode(Description node) {
		if(replacements.containsKey(node))
			node = replacements.get(node);
		else
			if(!dag.containsVertex(node))
				node = null;
		return node;
	}

	@Override
	public String toString() {
		return dag.toString() + 
				"\n\nReplacements\n" + replacements.toString() + 
				"\n\nEquivalenceMap\n" + equivalencesClasses;
	}
	
	
	

		
	@Deprecated 
	public SimpleDirectedGraph <Description,DefaultEdge> getDag() {
		return dag;
	}
}
