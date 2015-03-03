package org.semanticweb.ontop.reformulation.semindex.tests;

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




import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import org.semanticweb.ontop.ontology.ClassExpression;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.DataRangeExpression;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.Equivalences;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import junit.framework.TestCase;

public class DAGTest extends TestCase {

	SemanticIndexHelper	helper	= new SemanticIndexHelper();

	private void test_dag_index_nodes(String testname) throws Exception {
		TBoxReasoner reasoner = helper.load_dag(testname);
		List<List<Description>> exp_idx = helper.get_results(testname);

		List<Description> classes= new LinkedList<Description>();
		for(Equivalences<ClassExpression> node : reasoner.getClassDAG()) {
			for(ClassExpression c: node)
				classes.add(c);
		}
		for(Equivalences<DataRangeExpression> node : reasoner.getDataRanges()) {
			for(DataRangeExpression c: node)
				classes.add(c);
		}
		
		List<Description> roles= new LinkedList<Description>();
		for (Equivalences<ObjectPropertyExpression> node : reasoner.getObjectPropertyDAG()) {
			for (ObjectPropertyExpression r: node)
				roles.add(r);
		}
		for (Equivalences<DataPropertyExpression> node : reasoner.getDataPropertyDAG()) {
			for (DataPropertyExpression r: node) {
				roles.add(r);
				roles.add(r); // ROMAN: hacky way of double-counting data properties (which have no inverses)
			}
		}
		
		System.out.println(classes);
		System.out.println(roles);
		assertEquals(exp_idx.get(0).size(), classes.size());
		assertEquals(exp_idx.get(1).size(), roles.size());

		for (Description node : exp_idx.get(0)) {
			classes.contains(node);
		}
		for (Description node : exp_idx.get(1)) {
			roles.contains(node);
		}
		
		
	}

	public void test_1_0_0() throws Exception {
		String testname = "test_1_0_0";
		test_dag_index_nodes(testname);

	}

	public void test_1_0_1() throws Exception {
		String testname = "test_1_0_1";
		test_dag_index_nodes(testname);
	}

	public void test_1_1_0() throws Exception {
		String testname = "test_1_1_0";
		test_dag_index_nodes(testname);
	}

	public void test_1_2_0() throws Exception {
		String testname = "test_1_2_0";
		test_dag_index_nodes(testname);
	}

	public void test_1_3_0() throws Exception {
		String testname = "test_1_3_0";
		test_dag_index_nodes(testname);
	}

	// public void test_1_4_0() throws Exception {
	// String testname = "test_1_4_0";
	// test_dag_index_nodes(testname);
	// }

	public void test_1_5_0() throws Exception {
		String testname = "test_1_5_0";
		test_dag_index_nodes(testname);
	}
}
