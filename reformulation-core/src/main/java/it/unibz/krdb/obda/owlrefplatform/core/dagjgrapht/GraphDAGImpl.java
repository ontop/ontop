/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import org.jgrapht.graph.DefaultEdge;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;

public class GraphDAGImpl implements GraphDAG{

	/** Starting from a graph build a DAG 
	 * considering equivalences, redundancies and transitive reduction */

	GraphDAGImpl (GraphImpl<Description, DefaultEdge> graph){

	}

	public GraphDAGImpl (Ontology ontology){

	}
	@Override
	public void getNodes() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getEdges() {
		// TODO Auto-generated method stub

	}

	@Override
	public GraphImpl<Description, DefaultEdge> getDAG() {
		// TODO Auto-generated method stub
		return null;
	}

	//implement the private method for redundancies 
	//implement the private method for equivalences 	
	//implement the private method for transitive reduction







}
