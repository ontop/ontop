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
import it.unibz.krdb.obda.ontology.Ontology;

import java.util.Set;


/**
 *  This is the interface for the class TBoxReasoner where we are able to retrieve all the connection built in our DAG 
 * 
 *
 */
public interface TBoxReasoner {

	public Set<Set<Description>> getDirectChildren(Description desc);

	public Set<Set<Description>> getDirectParents(Description desc);

	public Set<Set<Description>> getDescendants(Description desc);

	public Set<Set<Description>> getAncestors(Description desc);

	public Set<Description> getEquivalences(Description desc);
	
	public Set<Set<Description>> getNodes();
	

}
