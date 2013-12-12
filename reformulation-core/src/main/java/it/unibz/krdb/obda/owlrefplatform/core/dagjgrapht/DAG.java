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

import java.util.Map;
import java.util.Set;


/** 
 * Interface to the DAG and the named DAG
 *  */
public interface DAG {

	//set the map of replacements
	@Deprecated
	public void setReplacements(Map<Description,Description> replacements);

	//return the map set of equivalences
	public Map<Description, Set<Description>> getMapEquivalences();

	//return the map set of replacements
	public Map<Description,Description> getReplacements();

	//set the graph is a dag
	public void setIsaDAG(boolean d);

	//check if the graph is a dag
	public boolean isaDAG();

	//return the named properties in the dag
	public Set<Property> getRoles();

	//return the named classed in the dag
	public Set<OClass> getClasses();

	//return the node considering replacements
	public Description getNode(Description node);
}
