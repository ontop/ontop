package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.Set;


/**
 * Interface to build a simple graph
 *
 */
public interface Graph {


	public Set<Property> getRoles();
	
	public Set<OClass> getClasses();


}
