package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Test_TBoxReasonerImplOnNamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Test_NamedTBoxReasonerImpl;

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

			TBoxReasonerImpl reasoner = new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			//transform in a named graph
			Test_TBoxReasonerImplOnNamedDAG namedDag2 = new Test_TBoxReasonerImplOnNamedDAG(reasoner);
			log.debug("Input number {}", i+1 );
			log.info("First graph {}", reasoner.getClassGraph());
			log.info("First graph {}", reasoner.getPropertyGraph());
			log.info("Second dag {}", namedDag2);
			Test_NamedTBoxReasonerImpl dag2 = new Test_NamedTBoxReasonerImpl(reasoner);

			assertTrue(testDescendants(dag2, namedDag2, reasoner));
			assertTrue(testDescendants(namedDag2, dag2, reasoner));
			assertTrue(testChildren(dag2, namedDag2, reasoner));
			assertTrue(testChildren(namedDag2, dag2, reasoner));
			assertTrue(testAncestors(dag2, namedDag2, reasoner));
			assertTrue(testAncestors(namedDag2, dag2, reasoner));
			assertTrue(testParents(dag2, namedDag2, reasoner));
			assertTrue(testParents(namedDag2, dag2, reasoner));
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
	
	private boolean testDescendants(TBoxReasoner d1, TBoxReasoner d2, TBoxReasonerImpl reasoner){
		
		for(Equivalences<Property> node : d1.getProperties()) {
			Property vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<Property>> setd1 = d1.getSubProperties(vertex);
				log.info("vertex {}", vertex);
				log.debug("descendants {} ", setd1);
				Set<Equivalences<Property>> setd2 = d2.getSubProperties(vertex);
				log.debug("descendants {} ", setd2);
				if (!coincide(setd1, setd2))
					return false;
			}
		}
		for(Equivalences<BasicClassDescription> node : d1.getClasses()) {
			BasicClassDescription vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<BasicClassDescription>> setd1 = d1.getSubClasses(vertex);
				log.info("vertex {}", vertex);
				log.debug("descendants {} ", setd1);
				Set<Equivalences<BasicClassDescription>> setd2 = d2.getSubClasses(vertex);
				log.debug("descendants {} ", setd2);
				if (!coincide(setd1, setd2))
					return false;
			}
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
	private boolean testChildren(TBoxReasoner d1, TBoxReasoner d2, TBoxReasonerImpl reasoner){

		for(Equivalences<Property> node : d1.getProperties()) {
			Property vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<Property>> setd1	= d1.getDirectSubProperties(vertex);
				log.info("vertex {}", vertex);
				log.debug("children {} ", setd1);
				Set<Equivalences<Property>> setd2	= d2.getDirectSubProperties(vertex);
				log.debug("children {} ", setd2);
				if (!coincide(setd1, setd2))
					return false;
			}
		}
		for(Equivalences<BasicClassDescription> node : d1.getClasses()) {
			BasicClassDescription vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<BasicClassDescription>> setd1	= d1.getDirectSubClasses(vertex);
				log.info("vertex {}", vertex);
				log.debug("children {} ", setd1);
				Set<Equivalences<BasicClassDescription>> setd2	= d2.getDirectSubClasses(vertex);
				log.debug("children {} ", setd2);
				if (!coincide(setd1, setd2))
					return false;
			}
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
	private boolean testAncestors(TBoxReasoner d1, TBoxReasoner d2, TBoxReasonerImpl reasoner) {
		
		for(Equivalences<Property> v: d1.getProperties()){
			Property vertex = v.getRepresentative();
			if(reasoner.isNamed(vertex)){
				Set<Equivalences<Property>> setd1 = d1.getSuperProperties(vertex);
				log.info("vertex {}", vertex);
				log.debug("ancestors {} ", setd1);
				Set<Equivalences<Property>> setd2 = d2.getSuperProperties(vertex);
				log.debug("ancestors {} ", setd2);
				
				if (!coincide(setd1, setd2))
					return false;
			}
		}
		for(Equivalences<BasicClassDescription> v: d1.getClasses()){
			BasicClassDescription vertex = v.getRepresentative();
			if(reasoner.isNamed(vertex)){
				Set<Equivalences<BasicClassDescription>> setd1 = d1.getSuperClasses(vertex);
				log.info("vertex {}", vertex);
				log.debug("ancestors {} ", setd1);
				Set<Equivalences<BasicClassDescription>> setd2 = d2.getSuperClasses(vertex);
				log.debug("ancestors {} ", setd2);
				
				if (!coincide(setd1, setd2))
					return false;
			}
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
	private boolean testParents(TBoxReasoner d1, TBoxReasoner d2, TBoxReasonerImpl reasoner){
	
		for(Equivalences<Property> node : d1.getProperties()) {
			Property vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)){
				Set<Equivalences<Property>> setd1	= d1.getDirectSuperProperties(vertex);
				log.info("vertex {}", vertex);
				log.debug("parents {} ", setd1);
				Set<Equivalences<Property>> setd2	= d2.getDirectSuperProperties(vertex);
				log.debug("parents {} ", setd2);
				if (!coincide(setd1, setd2))  
					return false;
			}
		}
		for(Equivalences<BasicClassDescription> node : d1.getClasses()) {
			BasicClassDescription vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)){
				Set<Equivalences<BasicClassDescription>> setd1	= d1.getDirectSuperClasses(vertex);
				log.info("vertex {}", vertex);
				log.debug("parents {} ", setd1);
				Set<Equivalences<BasicClassDescription>> setd2	= d2.getDirectSuperClasses(vertex);
				log.debug("parents {} ", setd2);
				if (!coincide(setd1, setd2))  
					return false;
			}
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
					if(!reasoner.isNamed(vertex))
						return false;
				}
				for(Equivalences<BasicClassDescription> node: dag.getClasses()) {
					BasicClassDescription vertex = node.getRepresentative();
					if(!reasoner.isNamed(vertex))
						return false;
				}
				return true;
			}

		}


