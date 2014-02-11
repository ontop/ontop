package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Test_TBoxReasonerImplOnNamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Test_NamedTBoxReasonerImpl;

import java.util.ArrayList;
import java.util.Set;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_HierarchyTestNewDAG extends TestCase {
	ArrayList<String> input= new ArrayList<String>();
	ArrayList<String> output= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_HierarchyTestNewDAG(String name){
		super(name);
	}

	public void setUp(){

		input.add("src/test/resources/test/dag/test-role-hierarchy.owl");
		input.add("src/test/resources/test/dag/role-equivalence.owl");
		input.add("src/test/resources/test/dag/test-class-hierarchy.owl");

		/**Graph1 B-> ER -> A */
		input.add("src/test/resources/test/newDag/ancestor1.owl");
		/**Graph B-> A ->ER */
		input.add("src/test/resources/test/newDag/ancestors2.owl");
		/**Graph B->ER->A and C->ES->ER->A */
		input.add("src/test/resources/test/newDag/ancestors3.owl");
		/**Graph B->A->ER C->ES->A->ER */
		input.add("src/test/resources/test/newDag/ancestors4.owl");
		/**Graph1 B-> ER -> A */
		input.add("src/test/resources/test/newDag/inverseAncestor1.owl");
		/**Graph B-> A ->ER */
		input.add("src/test/resources/test/newDag/inverseAncestor2.owl");
		/**Graph B->ER->A and C->ES->ER->A */
		input.add("src/test/resources/test/newDag/inverseAncestor3.owl");
		/**Graph B->A->ER C->ES->A->ER */
		input.add("src/test/resources/test/newDag/inverseAncestor4.owl");



	}


	public void testReachability() throws Exception{

		//for each file in the input
		for (int i=0; i<input.size(); i++){
			String fileInput=input.get(i);

			TBoxReasonerImpl reasoner = new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			//		DAGImpl dag2= InputOWL.createDAG(fileOutput);

			//transform in a named graph
			Test_TBoxReasonerImplOnNamedDAG dag2= new Test_TBoxReasonerImplOnNamedDAG(reasoner);
			Test_NamedTBoxReasonerImpl dag1 = new Test_NamedTBoxReasonerImpl(reasoner);
			log.debug("Input number {}", i+1 );
			log.info("First dag {}", dag1);
			log.info("Second dag {}", dag2);
			
			assertTrue(testDescendants(dag1,dag2, reasoner));
			assertTrue(testAncestors(dag1,dag2, reasoner));
			assertTrue(checkforNamedVertexesOnly(dag2, reasoner));
			assertTrue(testDescendants(dag2,dag1, reasoner));
			assertTrue(testAncestors(dag2,dag1, reasoner));
		}
	}


	private boolean testDescendants(TBoxReasoner d1, TBoxReasoner d2, TBoxReasonerImpl reasoner){

		for(Equivalences<Property> node : d1.getProperties()) {
			Property vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<Property>> setd1 = d1.getSubProperties(vertex);
				Set<Equivalences<Property>> setd2 = d2.getSubProperties(vertex);

				if(!setd1.equals(setd2))
					return false;
			}
		}
		for(Equivalences<BasicClassDescription> node : d1.getClasses()) {
			BasicClassDescription vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<BasicClassDescription>> setd1 = d1.getSubClasses(vertex);
				Set<Equivalences<BasicClassDescription>> setd2 = d2.getSubClasses(vertex);

				if(!setd1.equals(setd2))
					return false;
			}
		}
		return true;
	}


	private boolean testAncestors(TBoxReasoner d1, TBoxReasoner d2, TBoxReasonerImpl reasoner){

		for(Equivalences<Property> node : d1.getProperties()) {
			Property vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<Property>> setd1	= d1.getSuperProperties(vertex);
				Set<Equivalences<Property>> setd2	= d2.getSuperProperties(vertex);

				if (!setd1.equals(setd2))
					return false;
			}
		}
		for(Equivalences<BasicClassDescription> node : d1.getClasses()) {
			BasicClassDescription vertex = node.getRepresentative();
			if(reasoner.isNamed(vertex)) {
				Set<Equivalences<BasicClassDescription>> setd1	= d1.getSuperClasses(vertex);
				Set<Equivalences<BasicClassDescription>> setd2	= d2.getSuperClasses(vertex);

				if (!setd1.equals(setd2))
					return false;
			}
		}
		return true;
	}
	

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
