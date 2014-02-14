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
import it.unibz.krdb.obda.ontology.Property;


/**
 *  This is the interface for the class TBoxReasoner where we are able to retrieve all the connection built in our DAG 
 */
public interface TBoxReasoner {
	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<Property> getProperties();
	
	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<BasicClassDescription> getClasses();
}
