package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Test_TBoxReasonerImplOnGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

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
			Test_TBoxReasonerImplOnGraph graphReasoner = new Test_TBoxReasonerImplOnGraph(reasoner);

			
			log.debug("Input number {}", i+1 );
			log.info("First graph {}", reasoner.getClassGraph());
			log.info("First graph {}", reasoner.getPropertyGraph());
			log.info("Second dag {}", reasoner);

			assertTrue(testDescendants(graphReasoner, reasoner));
			assertTrue(testDescendants(reasoner, graphReasoner));
			assertTrue(testChildren(graphReasoner, reasoner));
			assertTrue(testChildren(reasoner, graphReasoner));
			assertTrue(testAncestors(graphReasoner.getClasses(), reasoner.getClasses()));
			assertTrue(testAncestors(graphReasoner.getProperties(), reasoner.getProperties()));
			assertTrue(testAncestors(reasoner.getClasses(), graphReasoner.getClasses()));
			assertTrue(testAncestors(reasoner.getProperties(), graphReasoner.getProperties()));
			assertTrue(testParents(graphReasoner ,reasoner));
			assertTrue(testParents(reasoner, graphReasoner));
			assertTrue(checkVertexReduction(graphReasoner, reasoner));
			assertTrue(checkEdgeReduction(graphReasoner, reasoner));

		}

	}
	
	private static <T> boolean coincide(Set<Equivalences<T>> setd1, Set<Equivalences<T>> setd2) {
		Set<T> set2 = new HashSet<T>();
		Iterator<Equivalences<T>> it1 =setd2.iterator();
		while (it1.hasNext()) {
			set2.addAll(it1.next().getMembers());	
		}
		Set<T> set1 = new HashSet<T>();
		Iterator<Equivalences<T>> it2 =setd1.iterator();
		while (it2.hasNext()) {
			set1.addAll(it2.next().getMembers());	
		}
		return set2.equals(set1);
		
	}

	private boolean testDescendants(TBoxReasoner d1, TBoxReasoner d2) {

		for(Equivalences<Property> vertex: d1.getProperties()) {
			Property p = vertex.getRepresentative();
			Set<Equivalences<Property>> setd1 = d1.getSubProperties(p);
			log.info("vertex {}", vertex);
			log.debug("descendants {} ", setd1);
			Set<Equivalences<Property>> setd2 = d2.getSubProperties(p);
			log.debug("descendants {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		for(Equivalences<BasicClassDescription> vertex: d1.getClasses()) {
			BasicClassDescription p = vertex.getRepresentative();
			Set<Equivalences<BasicClassDescription>> setd1 = d1.getSubClasses(p);
			log.info("vertex {}", vertex);
			log.debug("descendants {} ", setd1);
			Set<Equivalences<BasicClassDescription>> setd2 = d2.getSubClasses(p);
			log.debug("descendants {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}

		
	private boolean testChildren(TBoxReasoner d1, TBoxReasoner d2) {
		
		for(Equivalences<Property> vertex: d1.getProperties()){
			Property p = vertex.getRepresentative();
			Set<Equivalences<Property>> setd1 = d1.getDirectSubProperties(p);
			log.info("vertex {}", vertex);
			log.debug("children {} ", setd1);
			Set<Equivalences<Property>> setd2 = d2.getDirectSubProperties(p);
			log.debug("children {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		for(Equivalences<BasicClassDescription> vertex: d1.getClasses()){
			BasicClassDescription p = vertex.getRepresentative();
			Set<Equivalences<BasicClassDescription>> setd1 = d1.getDirectSubClasses(p);
			log.info("vertex {}", vertex);
			log.debug("children {} ", setd1);
			Set<Equivalences<BasicClassDescription>> setd2 = d2.getDirectSubClasses(p);
			log.debug("children {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}


	private <T> boolean testAncestors(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2) {

		for(Equivalences<T> vertex: d1){
			Set<Equivalences<T>> setd1 = d1.getSuper(vertex);
			log.info("vertex {}", vertex);
			log.debug("ancestors {} ", setd1);
			Set<Equivalences<T>> setd2 = d2.getSuper(vertex);
			log.debug("ancestors {} ", setd2);
			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}


	private boolean testParents(TBoxReasoner d1, TBoxReasoner d2) {
		
		for(Equivalences<Property> vertex: d1.getProperties()){
			Property p = vertex.getRepresentative();
			Set<Equivalences<Property>> setd1 = d1.getDirectSuperProperties(p);
			log.info("vertex {}", vertex);
			log.debug("children {} ", setd1);
			Set<Equivalences<Property>> setd2 = d2.getDirectSuperProperties(p);
			log.debug("children {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		for(Equivalences<BasicClassDescription> vertex: d1.getClasses()){
			BasicClassDescription p = vertex.getRepresentative();
			Set<Equivalences<BasicClassDescription>> setd1 = d1.getDirectSuperClasses(p);
			log.info("vertex {}", vertex);
			log.debug("children {} ", setd1);
			Set<Equivalences<BasicClassDescription>> setd2 = d2.getDirectSuperClasses(p);
			log.debug("children {} ", setd2);

			if (!coincide(setd1, setd2))
				return false;
		}
		return true;
	}


	private boolean checkVertexReduction(Test_TBoxReasonerImplOnGraph reasonerd1, TBoxReasonerImpl d2){

		//number of vertexes in the graph
		int numberVertexesD1= reasonerd1.vertexSetSize();
		//number of vertexes in the dag
		int numberVertexesD2 = d2.vertexSetSize();

		//number of vertexes in the equivalent mapping
		int numberEquivalents=0;

		Set<Description> set2 = new HashSet<Description>();
		
		for (Equivalences<Property> equivalents : d2.getProperties()) {
			numberEquivalents += equivalents.size()-1;
			set2.addAll(equivalents.getMembers());	
		}
		
		for (Equivalences<BasicClassDescription> equivalents : d2.getClasses()) {
			numberEquivalents += equivalents.size()-1;
			set2.addAll(equivalents.getMembers());				
		}

		log.info("vertex dag {}", numberVertexesD2);
		log.info("equivalents {} ", numberEquivalents);

		return numberVertexesD1== set2.size() & numberEquivalents== (numberVertexesD1-numberVertexesD2);

	}

	private boolean checkEdgeReduction(Test_TBoxReasonerImplOnGraph reasonerd1, TBoxReasonerImpl d2){
		//number of edges in the graph
		int numberEdgesD1= reasonerd1.edgeSetSize();
		//number of edges in the dag
		int numberEdgesD2 = d2.edgeSetSize();

		//number of edges between the equivalent nodes
		int numberEquivalents=0;

		for (Equivalences<Property> equivalents : d2.getProperties())
			//two nodes have two edges, three nodes have three edges...
			if(equivalents.size() >= 2)
				numberEquivalents += equivalents.size();
		
		for (Equivalences<BasicClassDescription> equivalents : d2.getClasses())
			//two nodes have two edges, three nodes have three edges...
			if(equivalents.size() >= 2)
				numberEquivalents += equivalents.size();
			

		log.info("edges graph {}", numberEdgesD1);
		log.info("edges dag {}", numberEdgesD2);
		log.info("equivalents {} ", numberEquivalents);

		return numberEdgesD1 >= (numberEquivalents+ numberEdgesD2);

	}

}
