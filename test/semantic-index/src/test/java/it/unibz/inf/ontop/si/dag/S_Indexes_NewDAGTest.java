package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-quest-owlapi
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


import it.unibz.inf.ontop.si.repository.impl.SemanticIndexBuilder;
import it.unibz.inf.ontop.si.repository.impl.SemanticIndexRange;
import it.unibz.inf.ontop.spec.ontology.*;
import junit.framework.TestCase;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map.Entry;

import static it.unibz.inf.ontop.utils.SITestingTools.loadOntologyFromFileAndClassify;

public class S_Indexes_NewDAGTest extends TestCase {
	

	Logger log = LoggerFactory.getLogger(S_Indexes_NewDAGTest.class);

	public S_Indexes_NewDAGTest (String name){
		super(name);
	}
	
	public void testIndexes() throws Exception {
		ArrayList<String> input= new ArrayList<>();
		/** C -> B  -> A  C->A*/
		input.add("src/test/resources/test/newDag/transitive.owl");
		/** C -> B  -> A  C->D ->A C->A */
		input.add("src/test/resources/test/newDag/transitive2.owl");

		/** C = B -> ER -> A*/
		input.add("src/test/resources/test/newDag/equivalents1.owl");
		/** B -> A -> ER=C */
		input.add("src/test/resources/test/newDag/equivalents2.owl");
		/** C->B = ER -> A*/
		input.add("src/test/resources/test/newDag/equivalents3.owl");
		/** ER-> A=B=C */
		input.add("src/test/resources/test/newDag/equivalents4.owl");
		/** C=ER=A->B */
		input.add("src/test/resources/test/newDag/equivalents5.owl");
		/** D-> ER=C=B -> A*/
		input.add("src/test/resources/test/newDag/equivalents6.owl");
		/** P-> ER=B -> A  C=L ->ES-> ER */
		input.add("src/test/resources/test/newDag/equivalents7.owl");
		/** B->A=ET->ER C->ES=D->A*/
		input.add("src/test/resources/test/newDag/equivalents8.owl");

		/** C = B -> ER- -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents1.owl");
		/** B -> A -> ER- = C */
		input.add("src/test/resources/test/newDag/inverseEquivalents2.owl");
		/** C->B = ER- -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents3.owl");
		/** ER- -> A=B=C */
		input.add("src/test/resources/test/newDag/inverseEquivalents4.owl");
		/** C=ER- =A->B */
		input.add("src/test/resources/test/newDag/inverseEquivalents5.owl");
		/** D-> ER- =C=B -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents6.owl");
		/** D->  ER- =C=B -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents6b.owl");
		/** P-> ER- =B -> A  C=L ->ES- -> ER- */
		input.add("src/test/resources/test/newDag/inverseEquivalents7.owl");
		/** B->A=ET- ->ER- C->ES- = D->A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents8.owl");

		//for each file in the input
		for (String fileInput : input) {
			ClassifiedTBox dag = loadOntologyFromFileAndClassify(fileInput);

			//add input named graph
			SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		
			log.debug("Input {}", fileInput);
			log.info("named graph {}", engine);
		
			assertTrue(testIndexes(engine, dag));
		}
	}

	private boolean testIndexes(SemanticIndexBuilder engine, ClassifiedTBox reasoner){
		boolean result = true;
		
		//classify semantic index
		//check that the index of the node is contained in the intervals of the parent node
		SimpleDirectedGraph<ObjectPropertyExpression, DefaultEdge> namedOP
						= SemanticIndexBuilder.getNamedDAG(reasoner.objectPropertiesDAG());
		for (Entry<ObjectPropertyExpression, SemanticIndexRange> vertex: engine.getIndexedObjectProperties()) { // .getNamedDAG().vertexSet()
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);
			for(ObjectPropertyExpression parent: Graphs.successorListOf(namedOP, vertex.getKey())){
				result = engine.getRange(parent).contained(new SemanticIndexRange(index));
				if (!result)
					return result;
			}
		}
		SimpleDirectedGraph<DataPropertyExpression, DefaultEdge> namedDP
						= SemanticIndexBuilder.getNamedDAG(reasoner.dataPropertiesDAG());
		for (Entry<DataPropertyExpression, SemanticIndexRange> vertex: engine.getIndexedDataProperties()) { 
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);
			for(DataPropertyExpression parent: Graphs.successorListOf(namedDP, vertex.getKey())){
				result = engine.getRange(parent).contained(new SemanticIndexRange(index));
				if (!result)
					return result;
			}
		}
		SimpleDirectedGraph<ClassExpression, DefaultEdge> namedCL
						= SemanticIndexBuilder.getNamedDAG(reasoner.classesDAG());
		for (Entry<ClassExpression, SemanticIndexRange> vertex: engine.getIndexedClasses()) { 
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);
			for(ClassExpression parent: Graphs.successorListOf(namedCL, vertex.getKey())) {
				result = engine.getRange((OClass)parent).contained(new SemanticIndexRange(index));
				if (!result)
					return result;
			}			
		}
		
		return result;
	}
}
