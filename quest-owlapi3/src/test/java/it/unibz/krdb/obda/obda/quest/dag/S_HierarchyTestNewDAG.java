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


import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
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
			
			assertTrue(testDescendants(dag1.getClasses(), dag2.getClasses()));
			assertTrue(testDescendants(dag1.getProperties(), dag2.getProperties()));
			assertTrue(testAncestors(dag1.getClasses(),dag2.getClasses()));
			assertTrue(testAncestors(dag1.getProperties(),dag2.getProperties()));
			assertTrue(checkforNamedVertexesOnly(dag2, reasoner));
			assertTrue(testDescendants(dag2.getClasses(), dag1.getClasses()));
			assertTrue(testDescendants(dag2.getProperties(), dag1.getProperties()));
			assertTrue(testAncestors(dag2.getClasses(), dag1.getClasses()));
			assertTrue(testAncestors(dag2.getProperties(), dag1.getProperties()));
		}
	}


	private <T> boolean testDescendants(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2) {

		for(Equivalences<T> node : d1) {
			Set<Equivalences<T>> setd1 = d1.getSub(node);
			Set<Equivalences<T>> setd2 = d2.getSub(node);

			if(!setd1.equals(setd2))
				return false;
		}
		return true;
	}


	private <T> boolean testAncestors(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2){

		for (Equivalences<T> node : d1) {
			Set<Equivalences<T>> setd1	= d1.getSuper(node);
			Set<Equivalences<T>> setd2	= d2.getSuper(node);
			if (!setd1.equals(setd2))
				return false;
		}
		return true;
	}
	

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
