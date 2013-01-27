package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;

import org.jgrapht.graph.DefaultEdge;

/** Interface for the GraphDAG class that starting from a graph build a DAG 
 * considering equivalences, redundancies and transitive reduction */

public interface GraphDAG {


	//obtain nodes
	public void getNodes();

	//obtain Edges
	public void getEdges();

	//obtain the graph
	public GraphImpl<Description, DefaultEdge> getDAG();


}
