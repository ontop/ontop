/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class OntologyGraph {

	private static OntologyFactory fac = OntologyFactoryImpl.getInstance();
	
	/**
	 *  graph representation of property inclusions in the ontology
	 *  
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 * 
	 * @param an ontology 
	 * @return the graph of the property inclusions 
	 */
	
	private static DefaultDirectedGraph<Property,DefaultEdge> getPropertyGraph (Ontology ontology) {
		
		DefaultDirectedGraph<Property,DefaultEdge> graph 
							= new  DefaultDirectedGraph<Property,DefaultEdge>(DefaultEdge.class);
				
		for (Predicate rolep : ontology.getRoles()) {
			Property role = fac.createProperty(rolep);
			graph.addVertex(role);
			Property roleInv = fac.createProperty(role.getPredicate(), !role.isInverse());
			graph.addVertex(roleInv);
		}

		// property inclusions
		for (Axiom assertion : ontology.getAssertions()) 
			if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				// adds the direct edge and the inverse 
				// e.g., R ISA S and R- ISA S-,
				//    or R- ISA S and R ISA S-

				Property child = roleIncl.getSub();
				graph.addVertex(child);
				Property parent = roleIncl.getSuper();
				graph.addVertex(parent);
				graph.addEdge(child, parent);
				
				Property childInv = fac.createProperty(child.getPredicate(), !child.isInverse());
				graph.addVertex(childInv);
				Property parentInv = fac.createProperty(parent.getPredicate(), !parent.isInverse());
				graph.addVertex(parentInv);
				graph.addEdge(childInv, parentInv);
			}
		
		return graph;
	}
	
	
	/**
	 * graph representation of the class inclusions in the ontology
	 * 
	 * adds inclusions of the domain of R in the domain of S if
	 *           the provided property graph has an edge from R to S
	 *           (given the getPropertyGraph algorithm, this also 
	 *           implies inclusions of the range of R in the range of S
	 * 
	 * @param ontology
	 * @param propertyGraph obtained by getPropertyGraph
	 * @param chain adds all equivalences \exists R = \exists R-, so that 
	 *              the result can be used to construct Sigma chains
	 * @return the graph of the concept inclusions
	 */
	
	public static DefaultDirectedGraph<BasicClassDescription,DefaultEdge> getClassGraph (Ontology ontology, 
													DefaultDirectedGraph<Property,DefaultEdge> propertyGraph, boolean chain) {
		
		DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph 
									= new  DefaultDirectedGraph<BasicClassDescription,DefaultEdge>(DefaultEdge.class);
		
		for (Predicate conceptp : ontology.getConcepts()) {
			BasicClassDescription concept = fac.createClass(conceptp);
			classGraph.addVertex(concept);
		}

		// domains and ranges of roles
		for (Property role : propertyGraph.vertexSet()) {
			PropertySomeRestriction existsRole = fac.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			classGraph.addVertex(existsRole);			
		}

		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : propertyGraph.edgeSet()) {
			Property child = propertyGraph.getEdgeSource(edge);
			Property parent = propertyGraph.getEdgeTarget(edge);
			BasicClassDescription existChild = fac.getPropertySomeRestriction(child.getPredicate(), child.isInverse());
			BasicClassDescription existsParent = fac.getPropertySomeRestriction(parent.getPredicate(), parent.isInverse());
			classGraph.addVertex(existChild);
			classGraph.addVertex(existsParent);
			classGraph.addEdge(existChild, existsParent);		
		}
		
		// edges between the domain and the range of each property for the chain graph
		if (chain) 
			for (Property role : propertyGraph.vertexSet()) {
				PropertySomeRestriction existsRole = fac.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
				PropertySomeRestriction existsRoleInv = fac.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
				
				classGraph.addEdge(existsRoleInv, existsRole);				
				classGraph.addEdge(existsRole, existsRoleInv);				
			}
		
		// class inclusions from the ontology
		for (Axiom assertion : ontology.getAssertions()) 
			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				BasicClassDescription parent = (BasicClassDescription)clsIncl.getSuper();
				BasicClassDescription child = (BasicClassDescription)clsIncl.getSub();
				classGraph.addVertex(child);
				classGraph.addVertex(parent);
				classGraph.addEdge(child, parent);
			} 
		return classGraph;
	}
	
	
	
	public static DefaultDirectedGraph<Description,DefaultEdge> getGraph (Ontology ontology, boolean chain) {
		
		DefaultDirectedGraph<Property,DefaultEdge> propertyGraph = getPropertyGraph(ontology);
		
		DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph = getClassGraph(ontology, propertyGraph, chain);
		
		DefaultDirectedGraph<Description,DefaultEdge> graph = new DefaultDirectedGraph<Description,DefaultEdge>(DefaultEdge.class);
		Graphs.addGraph(graph, propertyGraph);
		Graphs.addGraph(graph, classGraph);
		return graph;
	}

}
