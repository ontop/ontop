package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
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

public class S_Equivalences_TestNewDAG extends TestCase{

	ArrayList<String> input= new ArrayList<String>();
	ArrayList<String> output= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_Equivalences_TestNewDAG(String name){
		super(name);
	}

	public void setUp(){
//		
		input.add("src/test/resources/test/dag/test-role-hierarchy.owl");
		input.add("src/test/resources/test/stockexchange-unittest.owl");
		input.add("src/test/resources/test/dag/role-equivalence.owl");
		input.add("src/test/resources/test/dag/test-equivalence-classes.owl");
		input.add("src/test/resources/test/dag/test-equivalence-roles-inverse.owl");
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

	public void testEquivalences() throws Exception{
		//for each file in the input
		for (int i=0; i<input.size(); i++){
			String fileInput=input.get(i);

			TBoxReasonerImpl reasoner = new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			DefaultDirectedGraph<Description,DefaultEdge> graph = reasoner.getGraph();
			TestTBoxReasonerImplOnGraph graphReasoner = new TestTBoxReasonerImplOnGraph(graph);

			
			log.debug("Input number {}", i+1 );
			log.info("First graph {}", graph);
			log.info("Second dag {}", reasoner);

			assertTrue(testDescendants(graphReasoner, reasoner));
			assertTrue(testDescendants(reasoner, graphReasoner));
			assertTrue(testChildren(graphReasoner, reasoner));
			assertTrue(testChildren(reasoner, graphReasoner));
			assertTrue(testAncestors(graphReasoner, reasoner));
			assertTrue(testAncestors(reasoner, graphReasoner));
			assertTrue(testParents(graphReasoner ,reasoner));
			assertTrue(testParents(reasoner, graphReasoner));
			assertTrue(checkVertexReduction(graphReasoner, reasoner));
			assertTrue(checkEdgeReduction(graph, reasoner));

		}

	}
	
	private static <T> boolean coincide(Set<Equivalences<Description>> setd1, Set<Equivalences<Description>> setd2) {
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
		return set2.equals(set1);
		
	}

	private boolean testDescendants(TestTBoxReasonerImplOnGraph reasonerd1, TBoxReasonerImpl d2) {

		for(Description vertex: reasonerd1.vertexSet()){
			Set<Equivalences<Description>> setd1 = reasonerd1.getDescendants(vertex);
			log.info("vertex {}", vertex);
			log.debug("descendants {} ", setd1);
			Set<Equivalences<Description>> setd2 = d2.getDescendants(vertex);
			log.debug("descendants {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

	private boolean testDescendants(TBoxReasonerImpl d1, TestTBoxReasonerImplOnGraph reasonerd2) {
		
		for(Equivalences<Description> node : d1.getNodes()) {
			Description vertex = node.getRepresentative();
			Set<Equivalences<Description>> setd1 = d1.getDescendants(vertex);
			log.info("vertex {}", vertex);
			log.debug("descendants {} ", setd1);
			Set<Equivalences<Description>>  setd2 = reasonerd2.getDescendants(vertex);
			log.debug("descendants {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}
		
	private boolean testChildren(TestTBoxReasonerImplOnGraph reasonerd1, TBoxReasonerImpl d2) {
		
		for(Description vertex: reasonerd1.vertexSet()){
			Set<Equivalences<Description>> setd1 = reasonerd1.getDirectChildren(vertex);
			log.info("vertex {}", vertex);
			log.debug("children {} ", setd1);
			Set<Equivalences<Description>> setd2 = d2.getDirectChildren(vertex);
			log.debug("children {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

	private boolean testChildren(TBoxReasonerImpl d1, TestTBoxReasonerImplOnGraph reasonerd2) {

		for(Equivalences<Description> node : d1.getNodes()) {
			Description vertex = node.getRepresentative();
			Set<Equivalences<Description>> setd1 = d1.getDirectChildren(vertex);
			log.info("vertex {}", vertex);
			log.debug("children {} ", setd1);
			Set<Equivalences<Description>> setd2 =reasonerd2.getDirectChildren(vertex);
			log.debug("children {} ", setd2);
			
			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

	private boolean testAncestors(TestTBoxReasonerImplOnGraph reasonerd1, TBoxReasonerImpl d2) {

		for(Description vertex: reasonerd1.vertexSet()){
			Set<Equivalences<Description>> setd1 = reasonerd1.getAncestors(vertex);
			log.info("vertex {}", vertex);
			log.debug("ancestors {} ", setd1);
			Set<Equivalences<Description>> setd2 = d2.getAncestors(vertex);
			log.debug("ancestors {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

	private boolean testAncestors(TBoxReasonerImpl d1, TestTBoxReasonerImplOnGraph reasonerd2) {

		for(Equivalences<Description> node : d1.getNodes()) {
			Description vertex = node.getRepresentative();
			Set<Equivalences<Description>> setd1 = d1.getAncestors(vertex);
			log.info("vertex {}", vertex);
			log.debug("ancestors {} ", setd1);
			Set<Equivalences<Description>> setd2 = reasonerd2.getAncestors(vertex);
			log.debug("ancestors {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

	private boolean testParents(TestTBoxReasonerImplOnGraph reasonerd1, TBoxReasonerImpl d2) {
		
		for(Description vertex: reasonerd1.vertexSet()){
			Set<Equivalences<Description>> setd1 =reasonerd1.getDirectParents(vertex);
			log.info("vertex {}", vertex);
			log.debug("parents {} ", setd1);
			Set<Equivalences<Description>> setd2 = d2.getDirectParents(vertex);
			log.debug("parents {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

	private boolean testParents(TBoxReasonerImpl d1, TestTBoxReasonerImplOnGraph d2){

		for(Equivalences<Description> node : d1.getNodes()) {
			Description vertex = node.getRepresentative();
			Set<Equivalences<Description>> setd1 = d1.getDirectParents(vertex);
			log.info("vertex {}", vertex);
			log.debug("parents {} ", setd1);
			Set<Equivalences<Description>> setd2 = d2.getDirectParents(vertex);
			log.debug("parents {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

	private boolean checkVertexReduction(TestTBoxReasonerImplOnGraph reasonerd1, TBoxReasonerImpl d2){

		//number of vertexes in the graph
		int numberVertexesD1= reasonerd1.vertexSet().size();
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

		log.info("vertex dag {}", numberVertexesD2);
		log.info("equivalents {} ", numberEquivalents);

		return numberVertexesD1== set2.size() & numberEquivalents== (numberVertexesD1-numberVertexesD2);

	}

	private boolean checkEdgeReduction(DefaultDirectedGraph<Description,DefaultEdge> d1, TBoxReasonerImpl d2){
		//number of edges in the graph
		int numberEdgesD1= d1.edgeSet().size();
		//number of edges in the dag
		int numberEdgesD2 = d2.getDAG().edgeSetSize();

		//number of edges between the equivalent nodes
		int numberEquivalents=0;

		Set<Equivalences<Description>> nodesd2= d2.getNodes();
		Iterator<Equivalences<Description>> it1 =nodesd2.iterator();
		while (it1.hasNext()) {
			Equivalences<Description> equivalents=it1.next();

			//two nodes have two edges, three nodes have three edges...
			if(equivalents.size()>=2){
				numberEquivalents += equivalents.size();
			}
		}

		log.info("edges graph {}", numberEdgesD1);
		log.info("edges dag {}", numberEdgesD2);
		log.info("equivalents {} ", numberEquivalents);

		return numberEdgesD1 >= (numberEquivalents+ numberEdgesD2);

	}

}
