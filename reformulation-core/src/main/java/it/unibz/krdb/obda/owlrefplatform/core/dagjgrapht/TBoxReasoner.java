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

import java.util.Set;


/**
 *  This is the interface for the class TBoxReasoner where we are able to retrieve all the connection built in our DAG 
 * 
 *
 */
public interface TBoxReasoner {

	public Set<EquivalenceClass<Description>> getDirectChildren(Description desc);

	public Set<EquivalenceClass<Description>> getDirectParents(Description desc);

	/**
	 * Reflexive and transitive closure of the sub-description relation
	 * @param desc: a class or a property
	 * @return equivalence classes for all sub-descriptions (including desc)
	 */
	public Set<EquivalenceClass<Description>> getDescendants(Description desc);

	/**
	 * Reflexive and transitive closure of the super-description relation
	 * @param desc: a class or a property
	 * @return equivalence classes for all super-descriptions (including desc)
	 */
	public Set<EquivalenceClass<Description>> getAncestors(Description desc);

	public EquivalenceClass<Description> getEquivalences(Description desc);
	
	public Set<EquivalenceClass<Description>> getNodes();
	

}
