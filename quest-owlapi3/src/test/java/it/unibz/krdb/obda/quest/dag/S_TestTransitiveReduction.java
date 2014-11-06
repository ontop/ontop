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



import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.ArrayList;
import java.util.Set;

import junit.framework.TestCase;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_TestTransitiveReduction extends TestCase {

	ArrayList<String> input= new ArrayList<String>();
	ArrayList<String> output= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_TestTransitiveReduction (String name){
		super(name);
	}

	public void setUp(){
		
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



	}

	public void testSimplification() throws Exception{
		//for each file in the input
		for (int i=0; i<input.size(); i++){
			String fileInput=input.get(i);

			TBoxReasonerImpl dag2 = new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			TestTBoxReasonerImpl_OnGraph reasonerd1 = new TestTBoxReasonerImpl_OnGraph(dag2);

			log.debug("Input number {}", i+1 );
			log.info("First graph {}", dag2.getPropertyGraph());
			log.info("First graph {}", dag2.getClassGraph());
			log.info("Second dag {}", dag2);
						
			assertTrue(testRedundantEdges(reasonerd1,dag2));


		}
	}


	private boolean testRedundantEdges(TestTBoxReasonerImpl_OnGraph reasonerd1, TBoxReasonerImpl d2){
		//number of edges in the graph
		int  numberEdgesD1= reasonerd1.edgeSetSize();
		//number of edges in the dag
		int numberEdgesD2 = d2.edgeSetSize();

		//number of edges between the equivalent nodes
		int numberEquivalents=0;

		//number of redundant edges 
		int numberRedundants=0;

		for(Equivalences<PropertyExpression> equivalents: d2.getProperties()) 
			if(equivalents.size()>=2)
				numberEquivalents += equivalents.size();
			
		for(Equivalences<BasicClassDescription> equivalents: d2.getClasses()) 
			if(equivalents.size()>=2)
				numberEquivalents += equivalents.size();


		{
			DefaultDirectedGraph<PropertyExpression,DefaultEdge> g1 = 	reasonerd1.getPropertyGraph();	
			for (Equivalences<PropertyExpression> equivalents: reasonerd1.getProperties()) {
				
				log.info("equivalents {} ", equivalents);
				
				//check if there are redundant edges
				for (PropertyExpression vertex: equivalents) {
					if(g1.incomingEdgesOf(vertex).size()!= g1.inDegreeOf(vertex)) //check that there anren't two edges pointing twice to the same nodes
						numberRedundants +=g1.inDegreeOf(vertex)- g1.incomingEdgesOf(vertex).size();
				
					
					//descendants of the vertex
					Set<Equivalences<PropertyExpression>> descendants = d2.getProperties().getSub(equivalents);
					Set<Equivalences<PropertyExpression>> children = d2.getProperties().getDirectSub(equivalents);

					log.info("descendants{} ", descendants);
					log.info("children {} ", children);

					for(DefaultEdge edge: g1.incomingEdgesOf(vertex)) {
						PropertyExpression source=g1.getEdgeSource(edge);
						for(Equivalences<PropertyExpression> descendant:descendants) {
							if (!children.contains(descendant) & ! equivalents.contains(descendant.iterator().next()) &descendant.contains(source))
								numberRedundants +=1;	
						}
					}
				}
			}
		}
		{
			DefaultDirectedGraph<BasicClassDescription,DefaultEdge> g1 = 	reasonerd1.getClassGraph();	

			for (Equivalences<BasicClassDescription> equivalents : reasonerd1.getClasses()) {
				
				log.info("equivalents {} ", equivalents);
				
				//check if there are redundant edges
				for (BasicClassDescription vertex: equivalents) {
					if(g1.incomingEdgesOf(vertex).size()!= g1.inDegreeOf(vertex)) //check that there anren't two edges pointing twice to the same nodes
						numberRedundants +=g1.inDegreeOf(vertex)- g1.incomingEdgesOf(vertex).size();
				
					
					//descendants of the vertex
					Set<Equivalences<BasicClassDescription>> descendants = d2.getClasses().getSub(equivalents);
					Set<Equivalences<BasicClassDescription>> children = d2.getClasses().getDirectSub(equivalents);

					log.info("descendants{} ", descendants);
					log.info("children {} ", children);

					for(DefaultEdge edge: g1.incomingEdgesOf(vertex)) {
						BasicClassDescription source=g1.getEdgeSource(edge);
						for(Equivalences<BasicClassDescription> descendant:descendants) {
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

