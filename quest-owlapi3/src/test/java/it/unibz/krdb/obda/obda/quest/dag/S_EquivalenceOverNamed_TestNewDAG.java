package it.unibz.krdb.obda.obda.quest.dag;

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


import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAGBuilderImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

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

			GraphImpl graph1= S_InputOWL.createGraph(fileInput);

			DAGImpl dag2= S_InputOWL.createDAG(fileInput);
			//transform in a named graph
			NamedDAGBuilderImpl transform = new NamedDAGBuilderImpl(dag2);
			DAGImpl namedDag2= transform.getDAG();
			log.debug("Input number {}", i+1 );
			log.info("First graph {}", graph1);
			log.info("Second dag {}", namedDag2);

			assertTrue(testDescendants(dag2,namedDag2,true));
			assertTrue(testDescendants(namedDag2,dag2,true));
			assertTrue(testChildren(dag2,namedDag2,true));
			assertTrue(testChildren(namedDag2, dag2,true));
			assertTrue(testAncestors(dag2,namedDag2,true));
			assertTrue(testAncestors(namedDag2,dag2,true));
			assertTrue(testParents(dag2,namedDag2,true));
			assertTrue(testParents(namedDag2, dag2,true));
//			assertTrue(checkVertexReduction(graph1, namedDag2, true));
			//check only if the number of edges is smaller
			assertTrue(checkEdgeReduction(graph1, namedDag2, true));
			assertTrue(checkforNamedVertexesOnly(namedDag2));
		
			
		}
	}

			private boolean testDescendants(DAGImpl d1, DAGImpl d2, boolean named){
				
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = new HashSet<Set<Description>>();
				Set<Set<Description>> setd2 = new HashSet<Set<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getDescendants(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("descendants {} ", setd1);

							setd2	=reasonerd2.getDescendants(vertex, named);

							log.debug("descendants {} ", setd2);



						}
					}
					else{
						setd1	=reasonerd1.getDescendants(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("descendants {} ", setd1);
						for(Description v: d2.vertexSet()){
					
						}
						setd2	=reasonerd2.getDescendants(vertex, named);
						log.debug("descendants {} ", setd2);
						


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testDescendants( DAGImpl d1, GraphImpl d2, boolean named){
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = null;
				Set<Set<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
							setd1	=reasonerd1.getDescendants(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("descendants {} ", setd1);
							setd2	=reasonerd2.getDescendants(vertex, named);
							log.debug("descendants {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDescendants(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("descendants {} ", setd1);


						setd2	=reasonerd2.getDescendants(vertex, named);
						log.debug("descendants {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean testChildren(GraphImpl d1, DAGImpl d2, boolean named){
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = new HashSet<Set<Description>>();
				Set<Set<Description>> setd2 = new HashSet<Set<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getDirectChildren(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("children {} ", setd1);

							setd2	=reasonerd2.getDirectChildren(vertex, named);
							log.debug("children {} ", setd2);


						}
					}
					else{
						setd1	=reasonerd1.getDirectChildren(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("children {} ", setd1);

						setd2	=reasonerd2.getDirectChildren(vertex, named);
						log.debug("children {} ", setd2);

					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testChildren( DAGImpl d1, DAGImpl d2, boolean named){
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = null;
				Set<Set<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
							setd1	=reasonerd1.getDirectChildren(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("children {} ", setd1);
							setd2	=reasonerd2.getDirectChildren(vertex, named);
							log.debug("children {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDirectChildren(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("children {} ", setd1);
						setd2	=reasonerd2.getDirectChildren(vertex, named);
						log.debug("children {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean testAncestors(GraphImpl d1, DAGImpl d2, boolean named){
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = new HashSet<Set<Description>>();
				Set<Set<Description>> setd2 = new HashSet<Set<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getAncestors(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("ancestors {} ", setd1);

							setd2	=reasonerd2.getAncestors(vertex, named);

							log.debug("ancestors {} ", setd2);



						}
					}
					else{
						setd1	=reasonerd1.getAncestors(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("ancestors {} ", setd1);

						setd2	=reasonerd2.getAncestors(vertex, named);

						log.debug("ancestors {} ", setd2);


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testAncestors( DAGImpl d1, DAGImpl d2, boolean named){
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = null;
				Set<Set<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
							setd1	=reasonerd1.getAncestors(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("ancestors {} ", setd1);
							setd2	=reasonerd2.getAncestors(vertex, named);
							log.debug("ancestors {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getAncestors(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("ancestors {} ", setd1);


						setd2	=reasonerd2.getAncestors(vertex, named);
						log.debug("ancestors {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean testParents(GraphImpl d1, DAGImpl d2, boolean named){
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = new HashSet<Set<Description>>();
				Set<Set<Description>> setd2 = new HashSet<Set<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getDirectParents(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("parents {} ", setd1);

							setd2	=reasonerd2.getDirectParents(vertex, named);
							log.debug("parents {} ", setd2);


						}
					}
					else{
						setd1	=reasonerd1.getDirectParents(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("parents {} ", setd1);

						setd2	=reasonerd2.getDirectParents(vertex, named);
						log.debug("parents {} ", setd2);


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testParents( DAGImpl d1, DAGImpl d2, boolean named){
				boolean result = false;
				TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
				Set<Set<Description>> setd1 = null;
				Set<Set<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
							setd1	=reasonerd1.getDirectParents(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("parents {} ", setd1);
							setd2	=reasonerd2.getDirectParents(vertex, named);
							log.debug("parents {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDirectParents(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("parents {} ", setd1);
						setd2	=reasonerd2.getDirectParents(vertex, named);
						log.debug("parents {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean checkVertexReduction(GraphImpl d1, DAGImpl d2, boolean named){

				//number of vertexes in the graph
				int numberVertexesD1= 0;
				if(d2.isaNamedDAG()){
					for (Description v: d1.vertexSet()){
						if(d1.getRoles().contains(v)| d1.getClasses().contains(v)){	
							numberVertexesD1++;
							System.out.println(v);
						}
					}
				}
				else 
					numberVertexesD1= d1.vertexSet().size();
				
				//number of vertexes in the dag
				int numberVertexesD2 = d2.vertexSet().size();

				//number of vertexes in the equivalent mapping
				int numberEquivalents=0;

				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);

				Set<Set<Description>> nodesd2= reasonerd2.getNodes(named);
				Set<Description> set2 = new HashSet<Description>();
				Iterator<Set<Description>> it1 =nodesd2.iterator();
				while (it1.hasNext()) {
					Set<Description> equivalents=it1.next();
					numberEquivalents += equivalents.size()-1;
					set2.addAll(equivalents);	
				}
				
				log.info("vertex graph {}", numberVertexesD1);
				log.info("set {}", set2.size());

				log.info("vertex dag {}", numberVertexesD2);
				log.info("equivalents {} ", numberEquivalents);

				return numberVertexesD1== set2.size() & numberEquivalents== (numberVertexesD1-numberVertexesD2);

			}

			private boolean checkEdgeReduction(GraphImpl d1, DAGImpl d2, boolean named){
				
				//number of edges in the graph
				int  numberEdgesD1= d1.edgeSet().size();
				System.out.println(numberEdgesD1);
				System.out.println(d1.edgeSet());
				//number of edges in the dag
				int numberEdgesD2 = d2.edgeSet().size();

				//number of edges between the equivalent nodes
				int numberEquivalents=0;
				
				if(named){
					TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
				for(Description vertex: d1.vertexSet()){
					if(!(d1.getClasses().contains(vertex)| d1.getRoles().contains(vertex))){
						if(d1.inDegreeOf(vertex)>=1| d1.outDegreeOf(vertex)>=1){
					numberEdgesD1 -=1;
					
					}
				
				}
				}
				}
				
				TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);

				Set<Set<Description>> nodesd2= reasonerd2.getNodes(named);
				Iterator<Set<Description>> it1 =nodesd2.iterator();
				while (it1.hasNext()) {
					Set<Description> equivalents=it1.next();
					System.out.println(equivalents);
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
			
			private boolean checkforNamedVertexesOnly(DAGImpl dag){
				boolean result = false;
				for(Description vertex: dag.vertexSet()){

					if(dag.getRoles().contains(vertex)| dag.getClasses().contains(vertex)){
						result=true;

						if(!result)
							break;
					}


				}

				return result;
			}

		}


