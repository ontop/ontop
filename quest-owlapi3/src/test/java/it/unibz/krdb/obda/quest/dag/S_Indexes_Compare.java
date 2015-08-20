package it.unibz.krdb.obda.quest.dag;

/*
 * #%L
 * ontop-quest-owlapi3
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



import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_Indexes_Compare extends TestCase {
	
	ArrayList<String> input= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_Indexes_Compare (String name){
		super(name);
	}
	
	public void setUp() {
		input.add("src/test/resources/test/equivalence/test_404.owl");
	}

	public void testIndexes() throws Exception {
	//for each file in the input
	for (int i=0; i<input.size(); i++){
		String fileInput=input.get(i);

		TBoxReasoner dag = TBoxReasonerImpl.create(S_InputOWL.createOWL(fileInput));

		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		
		log.debug("Input number {}", i+1 );
		
		testIndexes(engine, dag);

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(fileInput));
		Ontology onto = OWLAPI3TranslatorUtility.translate(owlonto);
		DAG dag2 = DAGConstructor.getISADAG(onto);
		dag2.clean();
        DAGOperations.buildDescendants(dag2);
        DAGOperations.buildAncestors(dag2);
		DAG pureIsa = DAGConstructor.filterPureISA(dag2, onto.getVocabulary());
		 pureIsa.clean();
			pureIsa.index();
			 DAGOperations.buildDescendants(pureIsa);
		        DAGOperations.buildAncestors(pureIsa);
		 testOldIndexes(pureIsa, engine);
		
	}
}

private void testOldIndexes(DAG d1, SemanticIndexBuilder d2){
	
	
	
	for(DAGNode d: d1.getClasses()){
		System.out.println(d + "\n "+ d.getEquivalents());
		System.out.println(d1.equi_mappings.values());
		
	}
	
	
	for(DAGNode d: d1.getRoles()){
		System.out.println(d );
		for(DAGNode dd: d.getEquivalents()){
		System.out.println(d1.getRoleNode(((ObjectPropertyExpression)dd.getDescription())));
		;
		}
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		System.out.println(d1.getRoleNode(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2")));
		;
	}
		
	}


	private boolean testIndexes(SemanticIndexBuilder engine, TBoxReasoner reasoner) {
		
		boolean result = false;
		
		//check that the index of the node is contained in the intervals of the parent node
		SimpleDirectedGraph<ObjectPropertyExpression, DefaultEdge> namedOP 
							= SemanticIndexBuilder.getNamedDAG(reasoner.getObjectPropertyDAG());
		for (Entry<ObjectPropertyExpression, SemanticIndexRange> vertex: engine.getIndexedObjectProperties()) { // .getNamedDAG().vertexSet()
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);
			for (ObjectPropertyExpression parent: Graphs.successorListOf(namedOP, vertex.getKey())){
				result = engine.getRange(parent).contained(new SemanticIndexRange(index));			
				if (result)
					return result;
			}
		}
		SimpleDirectedGraph<DataPropertyExpression, DefaultEdge> namedDP 
					= SemanticIndexBuilder.getNamedDAG(reasoner.getDataPropertyDAG());
		for (Entry<DataPropertyExpression, SemanticIndexRange> vertex: engine.getIndexedDataProperties()) { // .getNamedDAG().vertexSet()
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);
			for (DataPropertyExpression parent: Graphs.successorListOf(namedDP, vertex.getKey())) {
				result = engine.getRange(parent).contained(new SemanticIndexRange(index));			
				if (result)
					return result;
			}
		}
		SimpleDirectedGraph<ClassExpression, DefaultEdge> namedCL 
						= SemanticIndexBuilder.getNamedDAG(reasoner.getClassDAG());
		for (Entry<ClassExpression, SemanticIndexRange> vertex: engine.getIndexedClasses()) { 
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);			
			for (ClassExpression parent: Graphs.successorListOf(namedCL, vertex.getKey())) {
				result = engine.getRange((OClass)parent).contained(new SemanticIndexRange(index));			
				if (result)
					return result;
			}
		}
		
		return result;
	}






}
