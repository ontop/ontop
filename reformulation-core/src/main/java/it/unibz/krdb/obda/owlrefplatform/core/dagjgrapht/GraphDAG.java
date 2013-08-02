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

import org.jgrapht.graph.DefaultEdge;

/** Interface for the GraphDAG class that starting from a graph build a DAG 
 * considering equivalences, redundancies and transitive reduction */

public interface GraphDAG {


	//obtain nodes
	public void getNodes();

	//obtain Edges
	public void getEdges();

	//obtain the graph
	public GraphImpl<Description, DefaultEdge> getDAG();


}
