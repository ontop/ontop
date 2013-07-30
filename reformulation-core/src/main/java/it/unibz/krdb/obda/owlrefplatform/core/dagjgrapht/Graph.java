/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Set;

import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;

import org.jgrapht.graph.DefaultEdge;


/**
 * Interface to build a simple graph
 *
 */
public interface Graph {


	public Set<Property> getRoles();
	 public Set<ClassDescription> getClasses();


}
