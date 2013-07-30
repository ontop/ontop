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



/**
 * Interface to create a graph starting from the axioms of a TBox.
 * @author Sarah
 *
 */

public interface TBoxGraph {


	//obtain the graph built
	public GraphImpl<Description, DefaultEdge> getGraph();

}
