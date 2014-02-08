/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;

import java.util.Set;


/**
 *  This is the interface for the class TBoxReasoner where we are able to retrieve all the connection built in our DAG 
 * 
 *
 */
public interface TBoxReasoner {

	public Set<Equivalences<Description>> getDirectChildren(Description desc);

	public Set<Equivalences<Description>> getDirectParents(Description desc);

	/**
	 * Reflexive and transitive closure of the sub-description relation
	 * @param desc: a class or a property
	 * @return equivalence classes for all sub-descriptions (including desc)
	 */
	@Deprecated
	public Set<Equivalences<Description>> getDescendants(Description desc);
	public Set<Equivalences<Property>> getSubProperties(Property desc);
	public Set<Equivalences<BasicClassDescription>> getSubClasses(BasicClassDescription desc);

	/**
	 * Reflexive and transitive closure of the super-description relation
	 * @param desc: a class or a property
	 * @return equivalence classes for all super-descriptions (including desc)
	 */
	public Set<Equivalences<Property>> getSuperProperties(Property desc);
	public Set<Equivalences<BasicClassDescription>> getSuperClasses(BasicClassDescription desc);

	public Equivalences<Property> getEquivalences(Property desc);
	public Equivalences<BasicClassDescription> getEquivalences(BasicClassDescription desc);
	@Deprecated
	public Equivalences<Description> getEquivalences(Description desc);
	
	public Set<Equivalences<Description>> getNodes();
	

}
