package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;

import org.jgrapht.graph.DefaultEdge;


/**Interface for a DAG with only the named description. */

public interface NamedDescriptionDAG {



	public void constructor (TBoxReasonerNamedImpl r);
	public DAGImpl<Description, DefaultEdge> getDAG();

}
