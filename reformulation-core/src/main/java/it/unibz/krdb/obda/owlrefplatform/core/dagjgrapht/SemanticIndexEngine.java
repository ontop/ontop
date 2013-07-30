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

/** An interface for the class SemanticIndexEngine that build the indexes for the DAG
 * 
 */

public interface SemanticIndexEngine {



	public void construct(DAGImpl<Description, DefaultEdge> dag);
	public void getIndex(Description d);
	public void getIntervals();
}
