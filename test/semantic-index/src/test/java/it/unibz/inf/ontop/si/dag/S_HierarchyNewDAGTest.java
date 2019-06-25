package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-quest-owlapi
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


import it.unibz.inf.ontop.spec.ontology.*;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;

import static it.unibz.inf.ontop.utils.SITestingTools.loadOntologyFromFileAndClassify;

public class S_HierarchyNewDAGTest extends TestCase {

	Logger log = LoggerFactory.getLogger(S_HierarchyNewDAGTest.class);

	public S_HierarchyNewDAGTest(String name){
		super(name);
	}

	public void testReachability() throws Exception {
		ArrayList<String> input = new ArrayList<>();
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

		for (String fileInput: input) {
			ClassifiedTBox reasoner = loadOntologyFromFileAndClassify(fileInput);

			//transform in a named graph
			TestClassifiedTBoxImpl_OnNamedDAG dag2= new TestClassifiedTBoxImpl_OnNamedDAG(reasoner);
			TestClassifiedTBoxImpl_Named dag1 = new TestClassifiedTBoxImpl_Named(reasoner);
			log.debug("Input {}", fileInput);
			log.info("First dag {}", dag1);
			log.info("Second dag {}", dag2);
			
			assertTrue(testDescendants(dag1.classesDAG(), dag2.classesDAG()));
			assertTrue(testDescendants(dag1.objectPropertiesDAG(), dag2.objectPropertiesDAG()));
			assertTrue(testAncestors(dag1.classesDAG(),dag2.classesDAG()));
			assertTrue(testAncestors(dag1.objectPropertiesDAG(),dag2.objectPropertiesDAG()));
			assertTrue(checkforNamedVertexesOnly(dag2, reasoner));
			assertTrue(testDescendants(dag2.classesDAG(), dag1.classesDAG()));
			assertTrue(testDescendants(dag2.objectPropertiesDAG(), dag1.objectPropertiesDAG()));
			assertTrue(testAncestors(dag2.classesDAG(), dag1.classesDAG()));
			assertTrue(testAncestors(dag2.objectPropertiesDAG(), dag1.objectPropertiesDAG()));
		}
	}


	private <T> boolean testDescendants(EquivalencesDAG<T> d1, EquivalencesDAG<T> d2) {

		for (Equivalences<T> node : d1) {
			Set<Equivalences<T>> setd1 = d1.getSub(node);
			Set<Equivalences<T>> setd2 = d2.getSub(node);
			if (!setd1.equals(setd2))
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
	

	private boolean checkforNamedVertexesOnly(TestClassifiedTBoxImpl_OnNamedDAG dag, ClassifiedTBox reasoner){
		for (Equivalences<ObjectPropertyExpression> node: dag.objectPropertiesDAG()) {
			ObjectPropertyExpression vertex = node.getRepresentative();
			if (!reasoner.objectPropertiesDAG().getVertex(vertex).isIndexed())
				return false;
		}
		for (Equivalences<DataPropertyExpression> node: dag.dataPropertiesDAG()) {
			DataPropertyExpression vertex = node.getRepresentative();
			if (!reasoner.dataPropertiesDAG().getVertex(vertex).isIndexed())
				return false;
		}
		for (Equivalences<ClassExpression> node: dag.classesDAG()) {
			ClassExpression vertex = node.getRepresentative();
			if (!reasoner.classesDAG().getVertex(vertex).isIndexed())
				return false;
		}
		return true;
	}
}
