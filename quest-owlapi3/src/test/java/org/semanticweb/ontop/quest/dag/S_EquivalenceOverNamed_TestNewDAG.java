package org.semanticweb.ontop.quest.dag;

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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.ontop.ontology.BasicClassDescription;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_EquivalenceOverNamed_TestNewDAG extends TestCase {

	ArrayList<String> input= new ArrayList<String>();
	ArrayList<String> output= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_EquivalenceOverNamed_TestNewDAG (String name){
		super(name);
	}

	public void setUp(){
		
		
		
		input.add("src/test/resources/test/stockexchange-unittest.owl");
		input.add("src/test/resources/test/dag/test-equivalence-roles-inverse.owl");
		input.add("src/test/resources/test/dag/test-role-hierarchy.owl");
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
		/** D-> ER  ER- =C=B -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents6.owl");
		/** D->  ER- =C=B -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents6b.owl");
		/** P-> ER- =B -> A  C=L ->ES- -> ER- */
		input.add("src/test/resources/test/newDag/inverseEquivalents7.owl");
		/** B->A=ET- ->ER- C->ES- = D->A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents8.owl");
		



	}
	
	public void testNamedAndEquivalences() throws Exception{
		//for each file in the input
		for (int i=0; i<input.size(); i++){
			String fileInput=input.get(i);

			TBoxReasonerImpl reasoner = new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			//transform in a named graph
			Test_TBoxReasonerImplOnNamedDAG namedDag2 = new Test_TBoxReasonerImplOnNamedDAG(reasoner);
			log.debug("Input number {}", i+1 );
			log.info("First graph {}", reasoner.getClassGraph());
			log.info("First graph {}", reasoner.getPropertyGraph());
			log.info("Second dag {}", namedDag2);
			Test_NamedTBoxReasonerImpl dag2 = new Test_NamedTBoxReasonerImpl(reasoner);

			assertTrue(testDescendants(dag2.getClasses(), namedDag2.getClasses()));
			assertTrue(testDescendants(dag2.getProperties(), namedDag2.getProperties()));
			assertTrue(testDescendants(namedDag2.getClasses(), dag2.getClasses()));
			assertTrue(testDescendants(namedDag2.getProperties(), dag2.getProperties()));
			assertTrue(testChildren(dag2.getClasses(), namedDag2.getClasses()));
			assertTrue(testChildren(dag2.getProperties(), namedDag2.getProperties()));
			assertTrue(testChildren(namedDag2.getClasses(), dag2.getClasses()));
			assertTrue(testChildren(namedDag2.getProperties(), dag2.getProperties()));
			assertTrue(testAncestors(dag2.getClasses(), namedDag2.getClasses()));
			assertTrue(testAncestors(dag2.getProperties(), namedDag2.getProperties()));
			assertTrue(testAncestors(namedDag2.getClasses(), dag2.getClasses()));
			assertTrue(testAncestors(namedDag2.getProperties(), dag2.getProperties()));
			assertTrue(testParents(dag2.getClasses(), namedDag2.getClasses()));
			assertTrue(testParents(dag2.getProperties(), namedDag2.getProperties()));
			assertTrue(testParents(namedDag2.getClasses(), dag2.getClasses()));
			assertTrue(testParents(namedDag2.getProperties(), dag2.getProperties()));
//			assertTrue(checkVertexReduction(graph1, namedDag2, true));
			//check only if the number of edges is smaller
			//assertTrue(checkEdgeReduction(graph1, namedDag2, true)); COMMENTED OUT BY ROMAN
			assertTrue(checkforNamedVertexesOnly(namedDag2, reasoner));
	
		}
	}

	
	private static <T> boolean coincide(Set<Equivalences<T>> setd1, Set<Equivalences<T>> setd2) {
		
		Set<T> set1 = new HashSet<T>();
		Iterator<Equivalences<T>> it1 =setd1.iterator();
		while (it1.hasNext()) {
			set1.addAll(it1.next().getMembers());	
		}
		
		Set<T> set2 = new HashSet<T>();
		Iterator<Equivalences<T>> it2 =setd2.iterator();
		while (it2.hasNext()) {
			set2.addAll(it2.next().getMembers());	
		}
		return set1.equals(set2);		
	}
	
	private <T> boolean testDescendants(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2) {
		
		for(Equivalences<T> node : d1) {
			Set<Equivalences<T>> setd1 = d1.getSub(node);
			log.info("vertex {}", node);
			log.debug("descendants {} ", setd1);
			Set<Equivalences<T>> setd2 = d2.getSub(node);
			log.debug("descendants {} ", setd2);
			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

/*			
			private boolean testDescendants(Test_NamedTBoxReasonerImpl d1, DefaultDirectedGraph<Description,DefaultEdge> d2, boolean named){
				boolean result = false;
				TestTBoxReasonerImplOnGraph reasonerd2 = new TestTBoxReasonerImplOnGraph(d2);
				Set<Equivalences<Description>> setd1 = null;
				Set<Equivalences<Description>> setd2 = null;

				for(Equivalences<Description> node : d1.getNodes()) {
					Description vertex = node.getRepresentative();
					if(named){

						if(d1.isNamed(vertex)) {
							setd1	= d1.getDescendants(vertex);
							log.info("vertex {}", vertex);
							log.debug("descendants {} ", setd1);
							setd2	=reasonerd2.getDescendants(vertex, named);
							log.debug("descendants {} ", setd2);
						}
					}
					else{
						setd1	= d1.getDescendants(vertex);
						log.info("vertex {}", vertex);
						log.debug("descendants {} ", setd1);


						setd2	=reasonerd2.getDescendants(vertex, named);
						log.debug("descendants {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean testChildren(DefaultDirectedGraph<Description,DefaultEdge> d1, Test_NamedTBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TestTBoxReasonerImplOnGraph reasonerd1 = new TestTBoxReasonerImplOnGraph(d1);
				Set<Equivalences<Description>> setd1 = new HashSet<Equivalences<Description>>();
				Set<Equivalences<Description>> setd2 = new HashSet<Equivalences<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(reasonerd1.isNamed(vertex)){

							setd1	=reasonerd1.getDirectChildren(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("children {} ", setd1);

							setd2	= d2.getDirectChildren(vertex);
							log.debug("children {} ", setd2);


						}
					}
					else{
						setd1	=reasonerd1.getDirectChildren(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("children {} ", setd1);

						setd2	= d2.getDirectChildren(vertex);
						log.debug("children {} ", setd2);

					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}
*/
	private <T> boolean testChildren(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2){

		for(Equivalences<T> node : d1) {
			Set<Equivalences<T>> setd1	= d1.getDirectSub(node);
			log.info("vertex {}", node);
			log.debug("children {} ", setd1);
			Set<Equivalences<T>> setd2	= d2.getDirectSub(node);
			log.debug("children {} ", setd2);
			if (!coincide(setd1, setd2))
				return false;
		}

		return true;
	}
			
	
/*
			private boolean testAncestors(DefaultDirectedGraph<Description,DefaultEdge> d1, Test_NamedTBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TestTBoxReasonerImplOnGraph reasonerd1= new TestTBoxReasonerImplOnGraph(d1);
				Set<Equivalences<Description>> setd1 = new HashSet<Equivalences<Description>>();
				Set<Equivalences<Description>> setd2 = new HashSet<Equivalences<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(reasonerd1.isNamed(vertex)){

							setd1	=reasonerd1.getAncestors(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("ancestors {} ", setd1);

							setd2	= d2.getAncestors(vertex);

							log.debug("ancestors {} ", setd2);



						}
					}
					else{
						setd1	=reasonerd1.getAncestors(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("ancestors {} ", setd1);

						setd2	= d2.getAncestors(vertex);

						log.debug("ancestors {} ", setd2);


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}
*/
	private <T> boolean testAncestors(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2) {
		
		for(Equivalences<T> v: d1){
			Set<Equivalences<T>> setd1 = d1.getSuper(v);
			log.info("vertex {}", v);
			log.debug("ancestors {} ", setd1);
			Set<Equivalences<T>> setd2 = d2.getSuper(v);
			log.debug("ancestors {} ", setd2);
			
			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

/*
			private boolean testParents(DefaultDirectedGraph<Description,DefaultEdge> d1, Test_NamedTBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TestTBoxReasonerImplOnGraph reasonerd1 = new TestTBoxReasonerImplOnGraph(d1);
				Set<Equivalences<Description>> setd1 = new HashSet<Equivalences<Description>>();
				Set<Equivalences<Description>> setd2 = new HashSet<Equivalences<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(reasonerd1.isNamed(vertex)){

							setd1	=reasonerd1.getDirectParents(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("parents {} ", setd1);

							setd2	= d2.getDirectParents(vertex);
							log.debug("parents {} ", setd2);


						}
					}
					else{
						setd1	=reasonerd1.getDirectParents(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("parents {} ", setd1);

						setd2	= d2.getDirectParents(vertex);
						log.debug("parents {} ", setd2);


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Equivalences<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}
*/
	private <T> boolean testParents(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2) {
	
		for(Equivalences<T> node : d1) {
			Set<Equivalences<T>> setd1	= d1.getDirectSuper(node);
			log.info("vertex {}", node);
			log.debug("parents {} ", setd1);
			Set<Equivalences<T>> setd2	= d2.getDirectSuper(node);
			log.debug("parents {} ", setd2);
			if (!coincide(setd1, setd2))  
				return false;
		}
		return true;
	}

/*
			private boolean checkVertexReduction(DefaultDirectedGraph<Description,DefaultEdge> d1, Test_NamedTBoxReasonerImpl d2, boolean named){

				//number of vertexes in the graph
				int numberVertexesD1= 0;
				//
				//if(d2.isaNamedDAG()){
				//	for (Description v: d1.vertexSet()){
				//		if(d1.getRoles().contains(v)| d1.getClasses().contains(v)){	
				//			numberVertexesD1++;
				//			System.out.println(v);
				//		}
				//	}
				//}
				//else			 
					numberVertexesD1= d1.vertexSet().size();
				
				//number of vertexes in the dag
				int numberVertexesD2 = d2.getNodes().size();

				//number of vertexes in the equivalent mapping
				int numberEquivalents=0;


				Set<Equivalences<Description>> nodesd2= d2.getNodes();
				Set<Description> set2 = new HashSet<Description>();
				Iterator<Equivalences<Description>> it1 =nodesd2.iterator();
				while (it1.hasNext()) {
					Equivalences<Description> equivalents=it1.next();
					numberEquivalents += equivalents.size()-1;
					set2.addAll(equivalents.getMembers());	
				}
				
				log.info("vertex graph {}", numberVertexesD1);
				log.info("set {}", set2.size());

				log.info("vertex dag {}", numberVertexesD2);
				log.info("equivalents {} ", numberEquivalents);

				return numberVertexesD1== set2.size() & numberEquivalents== (numberVertexesD1-numberVertexesD2);

			}

			private boolean checkEdgeReduction(DefaultDirectedGraph<Description,DefaultEdge> d1, TestTBoxReasonerImplOnNamedDAG d2, boolean named){
				
				//number of edges in the graph
				int  numberEdgesD1= d1.edgeSet().size();
				System.out.println(numberEdgesD1);
				//number of edges in the dag
				int numberEdgesD2 = d2.getEdgesSize();
				System.out.println(numberEdgesD2);

				//number of edges between the equivalent nodes
				int numberEquivalents=0;
				
				if(named){
					TestTBoxReasonerImplOnGraph reasonerd1 = new TestTBoxReasonerImplOnGraph(d1);
					for(Description vertex: d1.vertexSet()) {
						if(!reasonerd1.isNamed(vertex)) {
							if(d1.inDegreeOf(vertex)>=1 || d1.outDegreeOf(vertex)>=1){
								numberEdgesD1 -=1;
							}	
						}
					}
				}				

				Set<Equivalences<Description>> nodesd2= d2.getNodes();
				Iterator<Equivalences<Description>> it1 =nodesd2.iterator();
				while (it1.hasNext()) {
					Equivalences<Description> equivalents=it1.next();
					//System.out.println(equivalents);
					//two nodes have two edges, three nodes have three edges...
					if(equivalents.size()>=2){
						numberEquivalents += equivalents.size();
					}
				}
				
				log.info("edges graph {}", numberEdgesD1);
				log.info("edges dag {}", numberEdgesD2);
				log.info("equivalents {} ", numberEquivalents);

				return numberEdgesD1>= (numberEquivalents+ numberEdgesD2);

			}
*/			
	private boolean checkforNamedVertexesOnly(Test_TBoxReasonerImplOnNamedDAG dag, TBoxReasonerImpl reasoner){
		for(Equivalences<Property> node: dag.getProperties()) {
			Property vertex = node.getRepresentative();
			if(!reasoner.getProperties().getVertex(vertex).isIndexed())
				return false;
		}
		for(Equivalences<BasicClassDescription> node: dag.getClasses()) {
			BasicClassDescription vertex = node.getRepresentative();
			if(!reasoner.getClasses().getVertex(vertex).isIndexed())
				return false;
		}
		return true;
	}

}


