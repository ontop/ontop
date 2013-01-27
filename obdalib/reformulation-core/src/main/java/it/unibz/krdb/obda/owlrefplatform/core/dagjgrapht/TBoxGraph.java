package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;

import org.jgrapht.graph.DefaultEdge;



/**
 * Interface to create a graph starting from the axioms of a TBox.
 * @author Sarah
 *
 */

public interface TBoxGraph {




	//obtain the node
	public void getNode();

	//obtain the edge
	public void getEdge();

	//obtain the graph built
	public GraphImpl<Description, DefaultEdge> getGraph();

}
