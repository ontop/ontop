package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TestTBoxReasonerImplOnNamedDAG;
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

			TBoxReasonerImpl d1= new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			//		DAGImpl dag2= InputOWL.createDAG(fileOutput);

			//transform in a named graph
			TestTBoxReasonerImplOnNamedDAG dag2= new TestTBoxReasonerImplOnNamedDAG(d1);
			log.debug("Input number {}", i+1 );
			log.info("First dag {}", d1);
			log.info("Second dag {}", dag2);
			Test_NamedTBoxReasonerImpl dag1 = new Test_NamedTBoxReasonerImpl(d1);
			
			assertTrue(testDescendants(dag1,dag2,true));
			assertTrue(testAncestors(dag1,dag2,true));
			assertTrue(checkforNamedVertexesOnly(dag2));
			assertTrue(testDescendants(dag2,dag1,true));
			assertTrue(testAncestors(dag2,dag1,true));
		}
	}


	private boolean testDescendants(Test_NamedTBoxReasonerImpl d1, TestTBoxReasonerImplOnNamedDAG d2, boolean named){
		boolean result = false;

		for(Equivalences<Description> node : d1.getNodes()) {
			Description vertex = node.getRepresentative();
			if(named){

				if(d1.isNamed(vertex)) {
					Set<Equivalences<Description>> setd1	= d1.getDescendants(vertex);
					Set<Equivalences<Description>> setd2	= d2.getDescendants(vertex);

					result= setd1.equals(setd2);
				}
			}
			else
				result = d1.getDescendants(vertex).equals(d2.getDescendants(vertex));
			if(!result)
				break;
		}

		return result;

	}

	private boolean testDescendants(TestTBoxReasonerImplOnNamedDAG d1, Test_NamedTBoxReasonerImpl d2, boolean named){
		boolean result = false;

		for(Description vertex: d1.vertexSet()){
			if(named){

				if(d1.reasoner().isNamed(vertex)){
					Set<Equivalences<Description>> setd1	= d1.getDescendants(vertex);
					Set<Equivalences<Description>> setd2	= d2.getDescendants(vertex);

					result= setd1.equals(setd2);
				}
			}
			else
				result=d1.getDescendants(vertex).equals(d2.getDescendants(vertex));
			if(!result)
				break;
		}

		return result;

	}

	private boolean testAncestors(Test_NamedTBoxReasonerImpl d1, TestTBoxReasonerImplOnNamedDAG d2, boolean named){
		boolean result = false;

		for(Equivalences<Description> node : d1.getNodes()) {
			Description vertex = node.getRepresentative();
			if(named){

				if(d1.isNamed(vertex)) {
					Set<Equivalences<Description>> setd1	= d1.getAncestors(vertex);
					Set<Equivalences<Description>> setd2	= d2.getAncestors(vertex);

					result= setd1.equals(setd2);
				}
			}
			else
				result=d1.getAncestors(vertex).equals(d2.getAncestors(vertex));
			if(!result)
				break;
		}
		return result;
	}
	
	private boolean testAncestors(TestTBoxReasonerImplOnNamedDAG d1, Test_NamedTBoxReasonerImpl d2, boolean named){
		boolean result = false;

		for(Description vertex: d1.vertexSet()){
			if(named){

				if(d1.reasoner().isNamed(vertex)){
					Set<Equivalences<Description>> setd1	= d1.getAncestors(vertex);
					Set<Equivalences<Description>> setd2	= d2.getAncestors(vertex);

					result= setd1.equals(setd2);
				}
			}
			else
				result=d1.getAncestors(vertex).equals(d2.getAncestors(vertex));
			if(!result)
				break;
		}
		return result;
	}

	private boolean checkforNamedVertexesOnly(TestTBoxReasonerImplOnNamedDAG dag){
		boolean result = false;
		for(Description vertex: dag.vertexSet()){

			if(dag.reasoner().isNamed(vertex)){
				result=true;

				if(!result)
					break;
			}
		}
		return result;
	}

}
