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


import it.unibz.inf.ontop.si.repository.impl.Interval;
import it.unibz.inf.ontop.si.repository.impl.SemanticIndex;
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

public class S_Indexes_CompareTest extends TestCase {
	
	private final Logger log = LoggerFactory.getLogger(S_Indexes_CompareTest.class);

	public S_Indexes_CompareTest (String name){
		super(name);
	}
	
	public void testIndexes() throws Exception {
		ArrayList<String> input = new ArrayList<>();
		input.add("src/test/resources/completeness/equivalence/test_404.owl");

		for (String fileInput: input) {
			ClassifiedTBox dag = loadOntologyFromFileAndClassify(fileInput);

			SemanticIndex engine = new SemanticIndex(dag);

			log.debug("Input {}", fileInput);

			assertTrue(testIndexes(engine, dag));
		}
	}


	private boolean testIndexes(SemanticIndex engine, ClassifiedTBox reasoner) {
		
		boolean result = true;
		
		//check that the index of the node is contained in the intervals of the parent node
		SimpleDirectedGraph<ObjectPropertyExpression, DefaultEdge> namedOP 
							= SemanticIndexBuilder.getNamedDAG(reasoner.objectPropertiesDAG());
		for (Entry<ObjectPropertyExpression, SemanticIndexRange> vertex: engine.getIndexedObjectProperties()) { // .getNamedDAG().vertexSet()
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);
			for (ObjectPropertyExpression parent: Graphs.successorListOf(namedOP, vertex.getKey())){
				result = contains(engine.getRange(parent), new SemanticIndexRange(index));
				if (!result)
					return false;
			}
		}
		SimpleDirectedGraph<DataPropertyExpression, DefaultEdge> namedDP
					= SemanticIndexBuilder.getNamedDAG(reasoner.dataPropertiesDAG());
		for (Entry<DataPropertyExpression, SemanticIndexRange> vertex: engine.getIndexedDataProperties()) { // .getNamedDAG().vertexSet()
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);
			for (DataPropertyExpression parent: Graphs.successorListOf(namedDP, vertex.getKey())) {
				result = contains(engine.getRange(parent), new SemanticIndexRange(index));
				if (!result)
					return false;
			}
		}
		SimpleDirectedGraph<ClassExpression, DefaultEdge> namedCL 
						= SemanticIndexBuilder.getNamedDAG(reasoner.classesDAG());
		for (Entry<OClass, SemanticIndexRange> vertex: engine.getIndexedClasses()) {
			int index = vertex.getValue().getIndex();
			log.info("vertex {} index {}", vertex, index);			
			for (ClassExpression parent: Graphs.successorListOf(namedCL, vertex.getKey())) {
				result = contains(engine.getRange((OClass)parent), new SemanticIndexRange(index));
				if (!result)
					return false;
			}
		}
		
		return true;
	}

	public static boolean contains(SemanticIndexRange r1, SemanticIndexRange r2) {
		for (Interval it2: r2.getIntervals()) {
			boolean contained = false;
			for (Interval it1 : r1.getIntervals()) {
				if ((it1.getStart() <= it2.getStart()) && (it1.getEnd() >= it2.getEnd())) {
					contained = true;
					break;
				}
			}
			if (!contained)
				return false;
		}
		return true;
	}

}
