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


/**Interface for a DAG with only the named description. */

public interface NamedDescriptionDAG {



	public void constructor (TBoxReasonerNamedImpl r);
	public DAGImpl<Description, DefaultEdge> getDAG();

}
