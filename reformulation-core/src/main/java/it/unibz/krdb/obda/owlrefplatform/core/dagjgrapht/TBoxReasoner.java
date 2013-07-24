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

	public Set<Set<Description>> getDirectChildren(Description desc, boolean named);

	public Set<Set<Description>> getDirectParents(Description desc, boolean named);

	public Set<Set<Description>> getDescendants(Description desc, boolean named);

	public Set<Set<Description>> getAncestors(Description desc, boolean named);

	public Set<Description> getEquivalences(Description desc, boolean named);
	
	public DAG getDAG ();

	public Ontology getSigmaOntology();

	
}
