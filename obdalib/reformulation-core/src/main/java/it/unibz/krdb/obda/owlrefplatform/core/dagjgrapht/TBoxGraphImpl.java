package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/**
 * Builds a graph starting from the axioms of a TBox.
 * 
 *
 */

//build classes, allnodes and roles

public class TBoxGraphImpl implements TBoxGraph{

	GraphImpl<Description, DefaultEdge> graph = new  GraphImpl<Description, DefaultEdge>(DefaultEdge.class);

	/**
	 * Build the graph from the  TBox axioms
	 */
	public TBoxGraphImpl (Ontology ontology){

	}

	//we build a graph starting from assertions of a Tbox
	public TBoxGraphImpl (Collection<Axiom> assertions, Set<Predicate> concepts, Set<Predicate> roles){


	}

	//buld a graph starting from nodes
	public TBoxGraphImpl(Map<Description, DefaultEdge> classes, Map<Description, DefaultEdge> roles, Map<Description, Description> equiMap,
			Map<Description, DefaultEdge> allnodes) {

	}

	public  GraphImpl<Description, DefaultEdge>  getGraph(){

		return graph;
	}

	@Override
	public void getNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getEdge() {
		// TODO Auto-generated method stub

	}


}
