package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/**
 * Builds a graph starting from the axioms of a TBox.
 * 
 *
 */



public class TBoxGraphImpl implements TBoxGraph{
	

	GraphImpl<Description, DefaultEdge> graph = new  GraphImpl<Description, DefaultEdge>(DefaultEdge.class);
	private static final OntologyFactory descFactory = new OntologyFactoryImpl();
	

	/**
	 * Build the graph from the TBox axioms
	 */
	public TBoxGraphImpl (Ontology ontology){
		
		for (Predicate conceptp : ontology.getConcepts()) {
			ClassDescription concept = descFactory.createClass(conceptp);
			graph.addVertex(concept);
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		for (Predicate rolep : ontology.getRoles()) {
			Property role = descFactory.createProperty(rolep);
			Property roleInv = descFactory.createProperty(role.getPredicate(), !role.isInverse());
			PropertySomeRestriction existsRole = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
			graph.addVertex(role);
			graph.addVertex(roleInv);
			graph.addVertex(existsRole);
			graph.addVertex(existsRoleInv);
		}

		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addEdge(child, parent);
			} else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();
				Property parentInv = descFactory.createProperty(parent.getPredicate(), !parent.isInverse());
				Property childInv = descFactory.createProperty(child.getPredicate(), !child.isInverse());

				// This adds the direct edge and the inverse, e.g., R ISA S and
				// R- ISA S-,
				// R- ISA S and R ISA S-
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addVertex(childInv);
				graph.addVertex(parentInv);
				graph.addEdge(child, parent);
				graph.addEdge(childInv, parentInv);
				
			}
		}


	


	}

	//we build a graph starting from assertions of a TBox
	public TBoxGraphImpl (Collection<Axiom> assertions, Set<Predicate> concepts, Set<Predicate> roles){
		for (Predicate conceptp : concepts) {
			ClassDescription concept = descFactory.createClass(conceptp);
			graph.addVertex(concept);
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		for (Predicate rolep : roles) {
			Property role = descFactory.createProperty(rolep);
			Property roleInv = descFactory.createProperty(role.getPredicate(), !role.isInverse());
			PropertySomeRestriction existsRole = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
			graph.addVertex(role);
			graph.addVertex(roleInv);
			graph.addVertex(existsRole);
			graph.addVertex(existsRoleInv);
		}

		for (Axiom assertion : assertions) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addEdge(child, parent);
			} else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();
				Property parentInv = descFactory.createProperty(parent.getPredicate(), !parent.isInverse());
				Property childInv = descFactory.createProperty(child.getPredicate(), !child.isInverse());

				// This adds the direct edge and the inverse, e.g., R ISA S and
				// R- ISA S-,
				// R- ISA S and R ISA S-
				graph.addVertex(child);
				graph.addVertex(parent);
				graph.addVertex(childInv);
				graph.addVertex(parentInv);
				graph.addEdge(child, parent);
				graph.addEdge(childInv, parentInv);
				
			}
		}

	}


	public  GraphImpl<Description, DefaultEdge>  getGraph(){

		return graph;
	}





}
