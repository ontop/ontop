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
