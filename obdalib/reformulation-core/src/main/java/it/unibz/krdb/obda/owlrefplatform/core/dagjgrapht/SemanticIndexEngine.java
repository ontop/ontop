package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import org.jgrapht.graph.DefaultEdge;

import it.unibz.krdb.obda.ontology.Description;

/** An interface for the class SemanticIndexEngine that build the indexes for the DAG
 * 
 */

public interface SemanticIndexEngine {



	public void construct(DAGImpl<Description, DefaultEdge> dag);
	public void getIndex(Description d);
	public void getIntervals();
}
