package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalenceClass;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TestTBoxReasonerImplOnNamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TestTBoxReasonerImplOnDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TestTBoxReasonerImplOnGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
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

			TBoxReasonerImpl dag2= new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			DefaultDirectedGraph<Description,DefaultEdge> graph1= dag2.getGraph(); // S_InputOWL.createGraph(fileInput);
			//transform in a named graph
			NamedDAG namedDag2 = NamedDAG.getNamedDAG(dag2);
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

	private static TBoxReasoner getReasoner(TBoxReasonerImpl dag) {
		return new TestTBoxReasonerImplOnDAG(dag);
	}

	private static TBoxReasoner getReasoner(NamedDAG dag) {
		return new TestTBoxReasonerImplOnNamedDAG(dag);
	}
	
			private boolean testDescendants(TBoxReasonerImpl d1, NamedDAG d2, boolean named){
				
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = new HashSet<EquivalenceClass<Description>>();
				Set<EquivalenceClass<Description>> setd2 = new HashSet<EquivalenceClass<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getPropertyNames().contains(vertex)| d1.getClassNames().contains(vertex)){

							setd1	=reasonerd1.getDescendants(vertex);
							log.info("vertex {}", vertex);
							log.debug("descendants {} ", setd1);

							setd2	=reasonerd2.getDescendants(vertex);

							log.debug("descendants {} ", setd2);



						}
					}
					else{
						setd1	=reasonerd1.getDescendants(vertex);
						log.info("vertex {}", vertex);
						log.debug("descendants {} ", setd1);
						for(Description v: d2.vertexSet()){
					
						}
						setd2	=reasonerd2.getDescendants(vertex);
						log.debug("descendants {} ", setd2);
						


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testDescendants(NamedDAG d1, TBoxReasonerImpl d2, boolean named){
				
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = new HashSet<EquivalenceClass<Description>>();
				Set<EquivalenceClass<Description>> setd2 = new HashSet<EquivalenceClass<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getDescendants(vertex);
							log.info("vertex {}", vertex);
							log.debug("descendants {} ", setd1);

							setd2	=reasonerd2.getDescendants(vertex);

							log.debug("descendants {} ", setd2);



						}
					}
					else{
						setd1	=reasonerd1.getDescendants(vertex);
						log.info("vertex {}", vertex);
						log.debug("descendants {} ", setd1);
						for(Description v: d2.vertexSet()){
					
						}
						setd2	=reasonerd2.getDescendants(vertex);
						log.debug("descendants {} ", setd2);
						


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testDescendants( TBoxReasonerImpl d1, DefaultDirectedGraph<Description,DefaultEdge> d2, boolean named){
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TestTBoxReasonerImplOnGraph reasonerd2 = new TestTBoxReasonerImplOnGraph(d2);
				Set<EquivalenceClass<Description>> setd1 = null;
				Set<EquivalenceClass<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getPropertyNames().contains(vertex)| d1.getClassNames().contains(vertex)){
							setd1	=reasonerd1.getDescendants(vertex);
							log.info("vertex {}", vertex);
							log.debug("descendants {} ", setd1);
							setd2	=reasonerd2.getDescendants(vertex, named);
							log.debug("descendants {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDescendants(vertex);
						log.info("vertex {}", vertex);
						log.debug("descendants {} ", setd1);


						setd2	=reasonerd2.getDescendants(vertex, named);
						log.debug("descendants {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean testChildren(DefaultDirectedGraph<Description,DefaultEdge> d1, TBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TestTBoxReasonerImplOnGraph reasonerd1 = new TestTBoxReasonerImplOnGraph(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = new HashSet<EquivalenceClass<Description>>();
				Set<EquivalenceClass<Description>> setd2 = new HashSet<EquivalenceClass<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(reasonerd1.getRoles().contains(vertex)| reasonerd1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getDirectChildren(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("children {} ", setd1);

							setd2	=reasonerd2.getDirectChildren(vertex);
							log.debug("children {} ", setd2);


						}
					}
					else{
						setd1	=reasonerd1.getDirectChildren(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("children {} ", setd1);

						setd2	=reasonerd2.getDirectChildren(vertex);
						log.debug("children {} ", setd2);

					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testChildren( TBoxReasonerImpl d1, NamedDAG d2, boolean named){
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = null;
				Set<EquivalenceClass<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getPropertyNames().contains(vertex)| d1.getClassNames().contains(vertex)){
							setd1	=reasonerd1.getDirectChildren(vertex);
							log.info("vertex {}", vertex);
							log.debug("children {} ", setd1);
							setd2	=reasonerd2.getDirectChildren(vertex);
							log.debug("children {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDirectChildren(vertex);
						log.info("vertex {}", vertex);
						log.debug("children {} ", setd1);
						setd2	=reasonerd2.getDirectChildren(vertex);
						log.debug("children {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}
			private boolean testChildren(NamedDAG d1, TBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = null;
				Set<EquivalenceClass<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
							setd1	=reasonerd1.getDirectChildren(vertex);
							log.info("vertex {}", vertex);
							log.debug("children {} ", setd1);
							setd2	=reasonerd2.getDirectChildren(vertex);
							log.debug("children {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDirectChildren(vertex);
						log.info("vertex {}", vertex);
						log.debug("children {} ", setd1);
						setd2	=reasonerd2.getDirectChildren(vertex);
						log.debug("children {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean testAncestors(DefaultDirectedGraph<Description,DefaultEdge> d1, TBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TestTBoxReasonerImplOnGraph reasonerd1= new TestTBoxReasonerImplOnGraph(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = new HashSet<EquivalenceClass<Description>>();
				Set<EquivalenceClass<Description>> setd2 = new HashSet<EquivalenceClass<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(reasonerd1.getRoles().contains(vertex)| reasonerd1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getAncestors(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("ancestors {} ", setd1);

							setd2	=reasonerd2.getAncestors(vertex);

							log.debug("ancestors {} ", setd2);



						}
					}
					else{
						setd1	=reasonerd1.getAncestors(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("ancestors {} ", setd1);

						setd2	=reasonerd2.getAncestors(vertex);

						log.debug("ancestors {} ", setd2);


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testAncestors( TBoxReasonerImpl d1, NamedDAG d2, boolean named){
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = null;
				Set<EquivalenceClass<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getPropertyNames().contains(vertex)| d1.getClassNames().contains(vertex)){
							setd1	=reasonerd1.getAncestors(vertex);
							log.info("vertex {}", vertex);
							log.debug("ancestors {} ", setd1);
							setd2	=reasonerd2.getAncestors(vertex);
							log.debug("ancestors {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getAncestors(vertex);
						log.info("vertex {}", vertex);
						log.debug("ancestors {} ", setd1);


						setd2	=reasonerd2.getAncestors(vertex);
						log.debug("ancestors {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}
			private boolean testAncestors(NamedDAG d1, TBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = null;
				Set<EquivalenceClass<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
							setd1	=reasonerd1.getAncestors(vertex);
							log.info("vertex {}", vertex);
							log.debug("ancestors {} ", setd1);
							setd2	=reasonerd2.getAncestors(vertex);
							log.debug("ancestors {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getAncestors(vertex);
						log.info("vertex {}", vertex);
						log.debug("ancestors {} ", setd1);


						setd2	=reasonerd2.getAncestors(vertex);
						log.debug("ancestors {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean testParents(DefaultDirectedGraph<Description,DefaultEdge> d1, TBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TestTBoxReasonerImplOnGraph reasonerd1 = new TestTBoxReasonerImplOnGraph(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = new HashSet<EquivalenceClass<Description>>();
				Set<EquivalenceClass<Description>> setd2 = new HashSet<EquivalenceClass<Description>>();

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(reasonerd1.getRoles().contains(vertex)| reasonerd1.getClasses().contains(vertex)){

							setd1	=reasonerd1.getDirectParents(vertex, named);
							log.info("vertex {}", vertex);
							log.debug("parents {} ", setd1);

							setd2	=reasonerd2.getDirectParents(vertex);
							log.debug("parents {} ", setd2);


						}
					}
					else{
						setd1	=reasonerd1.getDirectParents(vertex, named);
						log.info("vertex {}", vertex);
						log.debug("parents {} ", setd1);

						setd2	=reasonerd2.getDirectParents(vertex);
						log.debug("parents {} ", setd2);


					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
						set1.addAll(it1.next().getMembers());	
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
						set2.addAll(it2.next().getMembers());	
					}
					result=set1.equals(set2);


					if(!result)
						break;
				}

				return result;

			}

			private boolean testParents( TBoxReasonerImpl d1, NamedDAG d2, boolean named){
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = null;
				Set<EquivalenceClass<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getPropertyNames().contains(vertex)| d1.getClassNames().contains(vertex)){
							setd1	=reasonerd1.getDirectParents(vertex);
							log.info("vertex {}", vertex);
							log.debug("parents {} ", setd1);
							setd2	=reasonerd2.getDirectParents(vertex);
							log.debug("parents {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDirectParents(vertex);
						log.info("vertex {}", vertex);
						log.debug("parents {} ", setd1);
						setd2	=reasonerd2.getDirectParents(vertex);
						log.debug("parents {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}
			private boolean testParents(NamedDAG d1, TBoxReasonerImpl d2, boolean named){
				boolean result = false;
				TBoxReasoner reasonerd1 = getReasoner(d1);
				TBoxReasoner reasonerd2 = getReasoner(d2);
				Set<EquivalenceClass<Description>> setd1 = null;
				Set<EquivalenceClass<Description>> setd2 = null;

				for(Description vertex: d1.vertexSet()){
					if(named){

						if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
							setd1	=reasonerd1.getDirectParents(vertex);
							log.info("vertex {}", vertex);
							log.debug("parents {} ", setd1);
							setd2	=reasonerd2.getDirectParents(vertex);
							log.debug("parents {} ", setd2);
						}
					}
					else{
						setd1	=reasonerd1.getDirectParents(vertex);
						log.info("vertex {}", vertex);
						log.debug("parents {} ", setd1);
						setd2	=reasonerd2.getDirectParents(vertex);
						log.debug("parents {} ", setd2);
					}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
						set2.addAll(it1.next().getMembers());	
					}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<EquivalenceClass<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
						set1.addAll(it2.next().getMembers());	
					}
					result=set2.equals(set1);



					if(!result)
						break;
				}

				return result;

			}

			private boolean checkVertexReduction(DefaultDirectedGraph<Description,DefaultEdge> d1, TBoxReasonerImpl d2, boolean named){

				//number of vertexes in the graph
				int numberVertexesD1= 0;
				/*
				if(d2.isaNamedDAG()){
					for (Description v: d1.vertexSet()){
						if(d1.getRoles().contains(v)| d1.getClasses().contains(v)){	
							numberVertexesD1++;
							System.out.println(v);
						}
					}
				}
				else
				*/ 
					numberVertexesD1= d1.vertexSet().size();
				
				//number of vertexes in the dag
				int numberVertexesD2 = d2.vertexSet().size();

				//number of vertexes in the equivalent mapping
				int numberEquivalents=0;

				TBoxReasoner reasonerd2a= getReasoner(d2);

				Set<EquivalenceClass<Description>> nodesd2= reasonerd2a.getNodes();
				Set<Description> set2 = new HashSet<Description>();
				Iterator<EquivalenceClass<Description>> it1 =nodesd2.iterator();
				while (it1.hasNext()) {
					EquivalenceClass<Description> equivalents=it1.next();
					numberEquivalents += equivalents.size()-1;
					set2.addAll(equivalents.getMembers());	
				}
				
				log.info("vertex graph {}", numberVertexesD1);
				log.info("set {}", set2.size());

				log.info("vertex dag {}", numberVertexesD2);
				log.info("equivalents {} ", numberEquivalents);

				return numberVertexesD1== set2.size() & numberEquivalents== (numberVertexesD1-numberVertexesD2);

			}

			private boolean checkEdgeReduction(DefaultDirectedGraph<Description,DefaultEdge> d1, NamedDAG d2, boolean named){
				
				//number of edges in the graph
				int  numberEdgesD1= d1.edgeSet().size();
				System.out.println(numberEdgesD1);
				System.out.println(d1.edgeSet());
				//number of edges in the dag
				int numberEdgesD2 = d2.getDag().edgeSet().size();

				//number of edges between the equivalent nodes
				int numberEquivalents=0;
				
				if(named){
					TestTBoxReasonerImplOnGraph reasonerd1= new TestTBoxReasonerImplOnGraph(d1);
				for(Description vertex: d1.vertexSet()){
					if(!(reasonerd1.getClasses().contains(vertex)| reasonerd1.getRoles().contains(vertex))){
						if(d1.inDegreeOf(vertex)>=1| d1.outDegreeOf(vertex)>=1){
					numberEdgesD1 -=1;
					
					}
				
				}
				}
				}
				
				TBoxReasoner reasonerd2a = getReasoner(d2);

				Set<EquivalenceClass<Description>> nodesd2= reasonerd2a.getNodes();
				Iterator<EquivalenceClass<Description>> it1 =nodesd2.iterator();
				while (it1.hasNext()) {
					EquivalenceClass<Description> equivalents=it1.next();
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
			
			private boolean checkforNamedVertexesOnly(NamedDAG dag){
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


