package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAGBuilderImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

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

			DAGImpl dag1= S_InputOWL.createDAG(fileInput);
			//		DAGImpl dag2= InputOWL.createDAG(fileOutput);

			//transform in a named graph
			DAGImpl dag2 = NamedDAGBuilderImpl.getNamedDAG(dag1);
			log.debug("Input number {}", i+1 );
			log.info("First dag {}", dag1);
			log.info("Second dag {}", dag2);

			assertTrue(testDescendants(dag1,dag2,true));
			assertTrue(testAncestors(dag1,dag2,true));
			assertTrue(checkforNamedVertexesOnly(dag2));
			assertTrue(testDescendants(dag2,dag1,true));
			assertTrue(testAncestors(dag2,dag1,true));





		}
	}


	private boolean testDescendants(DAGImpl d1, DAGImpl d2, boolean named){
		boolean result = false;
		TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
		TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);

		for(Description vertex: d1.vertexSet()){
			if(named){

				if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
					Set<Set<Description>> setd1	=reasonerd1.getDescendants(vertex, named);
					Set<Set<Description>> setd2	=reasonerd2.getDescendants(vertex, named);

					result= setd1.equals(setd2);
				}
			}
			else
				result=reasonerd1.getDescendants(vertex, named).equals(reasonerd2.getDescendants(vertex, named));
			if(!result)
				break;
		}

		return result;

	}

	private boolean testAncestors(DAGImpl d1, DAGImpl d2, boolean named){
		boolean result = false;
		TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
		TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);

		for(Description vertex: d1.vertexSet()){
			if(named){

				if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
					Set<Set<Description>> setd1	=reasonerd1.getAncestors(vertex, named);
					Set<Set<Description>> setd2	=reasonerd2.getAncestors(vertex, named);

					result= setd1.equals(setd2);
				}
			}
			else
				result=reasonerd1.getAncestors(vertex, named).equals(reasonerd2.getAncestors(vertex, named));
			if(!result)
				break;
		}

		return result;

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
