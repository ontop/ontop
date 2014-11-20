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
import org.semanticweb.ontop.ontology.BinaryAxiom;
import org.semanticweb.ontop.ontology.ClassExpression;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.DataRangeExpression;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.ObjectSomeValuesFrom;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
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
	
	public static DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> getObjectPropertyGraph (Ontology ontology) {
		
		DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> graph 
							= new  DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge>(DefaultEdge.class);
				
		for (ObjectPropertyExpression role : ontology.getVocabulary().getObjectProperties()) {
			graph.addVertex(role);
			graph.addVertex(role.getInverse());
		}
		
		for (ObjectPropertyExpression role : ontology.getVocabulary().getAuxiliaryObjectProperties()) {
			graph.addVertex(role);
			graph.addVertex(role.getInverse());
		}
		
		// property inclusions
		for (BinaryAxiom<ObjectPropertyExpression> roleIncl : ontology.getSubObjectPropertyAxioms()) {
			// adds the direct edge and the inverse (e.g., R ISA S and R- ISA S-)
			graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());			
			graph.addEdge(roleIncl.getSub().getInverse(), roleIncl.getSuper().getInverse());
		}
		
		return graph;
	}
	
	/**
	 *  graph representation of property inclusions in the ontology
	 *  
	 *  adds inclusions between the inverses of R and S if
	 *         R is declared a sub-property of S in the ontology
	 * 
	 * @param an ontology 
	 * @return the graph of the property inclusions 
	 */
	
	public static DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> getDataPropertyGraph (Ontology ontology) {
		
		DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> graph 
							= new  DefaultDirectedGraph<DataPropertyExpression,DefaultEdge>(DefaultEdge.class);
				
		for (DataPropertyExpression role : ontology.getVocabulary().getDataProperties()) 
			graph.addVertex(role);
		
		for (DataPropertyExpression role : ontology.getVocabulary().getAuxiliaryDataProperties()) 
			graph.addVertex(role);

		for (BinaryAxiom<DataPropertyExpression> roleIncl : ontology.getSubDataPropertyAxioms()) 
			graph.addEdge(roleIncl.getSub(), roleIncl.getSuper());
		
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
	
	public static DefaultDirectedGraph<ClassExpression,DefaultEdge> getClassGraph (Ontology ontology,
													DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> objectPropertyGraph, 
													DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph,
													boolean chain) {
		
		DefaultDirectedGraph<ClassExpression,DefaultEdge> classGraph 
									= new  DefaultDirectedGraph<ClassExpression,DefaultEdge>(DefaultEdge.class);
		
		for (OClass concept : ontology.getVocabulary().getClasses())
			classGraph.addVertex(concept);
	
		// domains and ranges of roles
		for (ObjectPropertyExpression role : objectPropertyGraph.vertexSet()) 
			classGraph.addVertex(role.getDomain());			
		
		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : objectPropertyGraph.edgeSet()) {
			ObjectPropertyExpression child = objectPropertyGraph.getEdgeSource(edge);
			ObjectPropertyExpression parent = objectPropertyGraph.getEdgeTarget(edge);
			classGraph.addEdge(child.getDomain(), parent.getDomain());		
		}
		
		// domains and ranges of roles
		for (DataPropertyExpression role : dataPropertyGraph.vertexSet()) 
			classGraph.addVertex(role.getDomain());			
		
		// edges between the domains and ranges for sub-properties
		for (DefaultEdge edge : dataPropertyGraph.edgeSet()) {
			DataPropertyExpression child = dataPropertyGraph.getEdgeSource(edge);
			DataPropertyExpression parent = dataPropertyGraph.getEdgeTarget(edge);
			classGraph.addEdge(child.getDomain(), parent.getDomain());		
		}

		
		// edges between the domain and the range of each property for the chain graph
		if (chain)  {
			for (ObjectPropertyExpression role : objectPropertyGraph.vertexSet()) {
				ObjectSomeValuesFrom existsRole = role.getDomain();
				ObjectSomeValuesFrom existsRoleInv = role.getRange();
				
				classGraph.addEdge(existsRoleInv, existsRole);				
				classGraph.addEdge(existsRole, existsRoleInv);				
			}
		}
		
		// class inclusions from the ontology
		for (BinaryAxiom<ClassExpression> clsIncl : ontology.getSubClassAxioms()) 
			classGraph.addEdge(clsIncl.getSub(), clsIncl.getSuper());
		 
		return classGraph;
	}

	public static DefaultDirectedGraph<DataRangeExpression,DefaultEdge> getDataRangeGraph (Ontology ontology,
							DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph) {

		DefaultDirectedGraph<DataRangeExpression,DefaultEdge> dataRangeGraph 
					= new  DefaultDirectedGraph<DataRangeExpression,DefaultEdge>(DefaultEdge.class);

		// ranges of roles
		for (DataPropertyExpression role : dataPropertyGraph.vertexSet()) 
			dataRangeGraph.addVertex(role.getRange());			
		
		// edges between the ranges for sub-properties
		for (DefaultEdge edge : dataPropertyGraph.edgeSet()) {
			DataPropertyExpression child = dataPropertyGraph.getEdgeSource(edge);
			DataPropertyExpression parent = dataPropertyGraph.getEdgeTarget(edge);
			dataRangeGraph.addEdge(child.getRange(), parent.getRange());		
		}

		// data range inclusions from the ontology
		for (BinaryAxiom<DataRangeExpression> clsIncl : ontology.getSubDataRangeAxioms()) {
			dataRangeGraph.addVertex(clsIncl.getSuper()); // Datatype is not among the vertices from the start
			dataRangeGraph.addEdge(clsIncl.getSub(), clsIncl.getSuper());
		}

		return dataRangeGraph;
	}
	
	
}
