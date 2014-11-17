package org.semanticweb.ontop.owlrefplatform.core.dagjgrapht;

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


import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.ontop.ontology.*;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;

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
	
	public static DefaultDirectedGraph<PropertyExpression,DefaultEdge> getPropertyGraph (Ontology ontology) {
		
		DefaultDirectedGraph<PropertyExpression,DefaultEdge> graph 
							= new  DefaultDirectedGraph<PropertyExpression,DefaultEdge>(DefaultEdge.class);
				
		for (ObjectPropertyExpression role : ontology.getVocabulary().getObjectProperties()) {
			graph.addVertex(role);
			graph.addVertex(role.getInverse());
		}
		
		for (DataPropertyExpression role : ontology.getVocabulary().getDataProperties()) {
			graph.addVertex(role);
			graph.addVertex(role.getInverse());
		}

		// property inclusions
		for (SubPropertyOfAxiom roleIncl : ontology.getSubPropertyAxioms()) {
			// adds the direct edge and the inverse 
			// e.g., R ISA S and R- ISA S-,
			//    or R- ISA S and R ISA S-

			PropertyExpression child = roleIncl.getSub();
			graph.addVertex(child);
			PropertyExpression parent = roleIncl.getSuper();
			graph.addVertex(parent);
			graph.addEdge(child, parent);
			
			PropertyExpression childInv = child.getInverse();
			graph.addVertex(childInv);
			PropertyExpression parentInv = parent.getInverse();
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
													DefaultDirectedGraph<PropertyExpression,DefaultEdge> propertyGraph, boolean chain) {
		
		DefaultDirectedGraph<BasicClassDescription,DefaultEdge> classGraph 
									= new  DefaultDirectedGraph<BasicClassDescription,DefaultEdge>(DefaultEdge.class);
		
		for (OClass concept : ontology.getVocabulary().getClasses()) {
		//	BasicClassDescription concept = fac.createClass(conceptp.getName()); // TODO: careful with datatypes
			classGraph.addVertex(concept);
		}

		// domains and ranges of roles
		for (PropertyExpression role : propertyGraph.vertexSet()) {
			SomeValuesFrom existsRole = fac.createPropertySomeRestriction(role);
			classGraph.addVertex(existsRole);			
		}

		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : propertyGraph.edgeSet()) {
			PropertyExpression child = propertyGraph.getEdgeSource(edge);
			PropertyExpression parent = propertyGraph.getEdgeTarget(edge);
			BasicClassDescription existChild = fac.createPropertySomeRestriction(child);
			BasicClassDescription existsParent = fac.createPropertySomeRestriction(parent);
			classGraph.addVertex(existChild);
			classGraph.addVertex(existsParent);
			classGraph.addEdge(existChild, existsParent);		
		}
		
		// edges between the domain and the range of each property for the chain graph
		if (chain) 
			for (PropertyExpression role : propertyGraph.vertexSet()) {
				SomeValuesFrom existsRole = fac.createPropertySomeRestriction(role);
				PropertyExpression inv = role.getInverse();
				SomeValuesFrom existsRoleInv = fac.createPropertySomeRestriction(inv);
				
				classGraph.addEdge(existsRoleInv, existsRole);				
				classGraph.addEdge(existsRole, existsRoleInv);				
			}
		
		// class inclusions from the ontology
		for (SubClassOfAxiom clsIncl : ontology.getSubClassAxioms()) {
			BasicClassDescription parent = (BasicClassDescription)clsIncl.getSuper();
			BasicClassDescription child = (BasicClassDescription)clsIncl.getSub();
			classGraph.addVertex(child);
			classGraph.addVertex(parent);
			classGraph.addEdge(child, parent);
		} 
		return classGraph;
	}
	
}
