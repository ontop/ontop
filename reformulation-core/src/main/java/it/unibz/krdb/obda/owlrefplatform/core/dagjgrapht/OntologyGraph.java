package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

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
	
	public static DefaultDirectedGraph<Property,DefaultEdge> getPropertyGraph (Ontology ontology) {
		
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
			PropertySomeRestriction existsRole = fac.createPropertySomeRestriction(role);
			classGraph.addVertex(existsRole);			
		}

		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : propertyGraph.edgeSet()) {
			Property child = propertyGraph.getEdgeSource(edge);
			Property parent = propertyGraph.getEdgeTarget(edge);
			BasicClassDescription existChild = fac.createPropertySomeRestriction(child);
			BasicClassDescription existsParent = fac.createPropertySomeRestriction(parent);
			classGraph.addVertex(existChild);
			classGraph.addVertex(existsParent);
			classGraph.addEdge(existChild, existsParent);		
		}
		
		// edges between the domain and the range of each property for the chain graph
		if (chain) 
			for (Property role : propertyGraph.vertexSet()) {
				PropertySomeRestriction existsRole = fac.createPropertySomeRestriction(role);
				PropertySomeRestriction existsRoleInv = fac.createPropertySomeRestriction(role.getPredicate(), !role.isInverse());
				
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
	
}
