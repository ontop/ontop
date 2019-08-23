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


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;
import junit.framework.TestCase;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;

import static it.unibz.inf.ontop.utils.SITestingTools.getIRI;
import static it.unibz.inf.ontop.utils.SITestingTools.loadOntologyFromFileAndClassify;

public class S_TransitiveReductionTest extends TestCase {

	Logger log = LoggerFactory.getLogger(S_TransitiveReductionTest.class);

	public S_TransitiveReductionTest (String name){
		super(name);
	}


	public void testR() throws OWLOntologyCreationException {
		ClassifiedTBox dag = loadOntologyFromFileAndClassify("src/test/resources/test/newDag/transitive.owl");

		ClassExpression A = dag.classes().get(getIRI("http://www.kro.com/ontologies/", "A"));
		ClassExpression B = dag.classes().get(getIRI("http://www.kro.com/ontologies/", "B"));
		ClassExpression C = dag.classes().get(getIRI("http://www.kro.com/ontologies/", "C"));
		
		EquivalencesDAG<ClassExpression> classes = dag.classesDAG();
		
		Equivalences<ClassExpression> vA = classes.getVertex(A);
		Equivalences<ClassExpression> vB = classes.getVertex(B);
		Equivalences<ClassExpression> vC = classes.getVertex(C);
		
		assertEquals(ImmutableSet.of(vB), classes.getDirectSuper(vC));
		assertEquals(ImmutableSet.of(vA), classes.getDirectSuper(vB));
	}

	public void testR2() throws Exception {
		ClassifiedTBox dag = loadOntologyFromFileAndClassify("src/test/resources/test/newDag/transitive2.owl");

		ClassExpression A = dag.classes().get(getIRI("http://www.kro.com/ontologies/", "A"));
		ClassExpression B = dag.classes().get(getIRI("http://www.kro.com/ontologies/", "B"));
		ClassExpression C = dag.classes().get(getIRI("http://www.kro.com/ontologies/", "C"));
		ClassExpression D = dag.classes().get(getIRI("http://www.kro.com/ontologies/", "D"));
		
		EquivalencesDAG<ClassExpression> classes = dag.classesDAG();
		
		Equivalences<ClassExpression> vA = classes.getVertex(A);
		Equivalences<ClassExpression> vB = classes.getVertex(B);
		Equivalences<ClassExpression> vC = classes.getVertex(C);
		Equivalences<ClassExpression> vD = classes.getVertex(D);
		
		assertEquals(ImmutableSet.of(vB, vD), classes.getDirectSuper(vC));
		assertEquals(ImmutableSet.of(vA), classes.getDirectSuper(vB));
		assertEquals(ImmutableSet.of(vA), classes.getDirectSuper(vD));
	}
	
	
	public void testSimplification() throws Exception {
		ArrayList<String> input= new ArrayList<>();
		input.add("src/test/resources/test/dag/test-equivalence-roles-inverse.owl");
		input.add("src/test/resources/test/dag/test-role-hierarchy.owl");
		input.add("src/test/resources/test/stockexchange-unittest.owl");
		input.add("src/test/resources/test/dag/role-equivalence.owl");

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
		/** P-> ER- =B -> A  C=L ->ES- -> ER- */
		input.add("src/test/resources/test/newDag/inverseEquivalents7.owl");
		/** B->A=ET- ->ER- C->ES- = D->A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents8.owl");

		for (String fileInput: input) {
			ClassifiedTBox dag2 = loadOntologyFromFileAndClassify(fileInput);
			TestClassifiedTBoxImpl_OnGraph reasonerd1 = new TestClassifiedTBoxImpl_OnGraph((ClassifiedTBoxImpl)dag2);

			log.debug("Input number {}", fileInput);
			log.info("First graph {}", ((ClassifiedTBoxImpl)dag2).getObjectPropertyGraph());
			log.info("First graph {}", ((ClassifiedTBoxImpl)dag2).getClassGraph());
			log.info("Second dag {}", dag2);
						
			assertTrue(testRedundantEdges(reasonerd1, (ClassifiedTBoxImpl)dag2));
		}
	}


	private boolean testRedundantEdges(TestClassifiedTBoxImpl_OnGraph reasonerd1, ClassifiedTBoxImpl d2){
		//number of edges in the graph
		int  numberEdgesD1= reasonerd1.edgeSetSize();
		//number of edges in the dag
		int numberEdgesD2 = d2.edgeSetSize();

		//number of edges between the equivalent nodes
		int numberEquivalents = 0;

		//number of redundant edges 
		int numberRedundants = 0;

		for (Equivalences<ObjectPropertyExpression> equivalents: d2.objectPropertiesDAG())
			if (equivalents.size() >= 2)
				numberEquivalents += equivalents.size();
		
		for (Equivalences<ClassExpression> equivalents: d2.classesDAG())
			if (equivalents.size() >= 2)
				numberEquivalents += equivalents.size();


		{
			DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> g1 = reasonerd1.getObjectPropertyGraph();
			for (Equivalences<ObjectPropertyExpression> equivalents: reasonerd1.objectPropertiesDAG()) {
				
				log.info("equivalents {} ", equivalents);
				
				//check if there are redundant edges
				for (ObjectPropertyExpression vertex: equivalents) {
					if(g1.incomingEdgesOf(vertex).size() != g1.inDegreeOf(vertex)) //check that there aren't two edges pointing twice to the same nodes
						numberRedundants += g1.inDegreeOf(vertex)- g1.incomingEdgesOf(vertex).size();
				
					
					//descendants of the vertex
					Set<Equivalences<ObjectPropertyExpression>> descendants = d2.objectPropertiesDAG().getSub(equivalents);
					Set<Equivalences<ObjectPropertyExpression>> children = d2.objectPropertiesDAG().getDirectSub(equivalents);

					log.info("descendants{} ", descendants);
					log.info("children {} ", children);

					for(DefaultEdge edge: g1.incomingEdgesOf(vertex)) {
						ObjectPropertyExpression source = g1.getEdgeSource(edge);
						for(Equivalences<ObjectPropertyExpression> descendant:descendants) {
							if (!children.contains(descendant) & ! equivalents.contains(descendant.iterator().next()) &descendant.contains(source))
								numberRedundants++;
						}
					}
				}
			}
		}
		{
			DefaultDirectedGraph<ClassExpression,DefaultEdge> g1 = reasonerd1.getClassGraph();

			for (Equivalences<ClassExpression> equivalents : reasonerd1.classesDAG()) {
				
				log.info("equivalents {} ", equivalents);
				
				//check if there are redundant edges
				for (ClassExpression vertex: equivalents) {
					if(g1.incomingEdgesOf(vertex).size()!= g1.inDegreeOf(vertex)) //check that there aren't two edges pointing twice to the same nodes
						numberRedundants +=g1.inDegreeOf(vertex)- g1.incomingEdgesOf(vertex).size();

					//descendants of the vertex
					Set<Equivalences<ClassExpression>> descendants = d2.classesDAG().getSub(equivalents);
					Set<Equivalences<ClassExpression>> children = d2.classesDAG().getDirectSub(equivalents);

					log.info("descendants{} ", descendants);
					log.info("children {} ", children);

					for(DefaultEdge edge: g1.incomingEdgesOf(vertex)) {
						ClassExpression source = g1.getEdgeSource(edge);
						for(Equivalences<ClassExpression> descendant : descendants) {
							if (!children.contains(descendant) & ! equivalents.contains(descendant.iterator().next()) &descendant.contains(source))
								numberRedundants +=1;	
						}
					}
				}
			}
		}
		log.info("edges graph {}", numberEdgesD1);
		log.info("edges dag {}", numberEdgesD2);
		log.info("equivalents {} ", numberEquivalents);
		log.info("redundants {} ", numberRedundants);

		return numberEdgesD1>= (numberRedundants+ numberEquivalents+ numberEdgesD2);
	}
}

