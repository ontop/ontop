/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.semindex.tests;

import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class DAGTest extends TestCase {

	SemanticIndexHelper	helper	= new SemanticIndexHelper();

	private void test_dag_index_nodes(String testname) throws Exception {
		DAGImpl res = helper.load_dag(testname);
		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(res);
		List<List<Description>> exp_idx = helper.get_results(testname);

		Set<Description> classes= new HashSet<Description>();
		for(Description node:((DAGImpl)res).vertexSet()){
			if(node instanceof ClassDescription){
				for(Description c: reasoner.getEquivalences(node) )
				classes.add(c);
			}
		}
		
		Set<Description> roles= new HashSet<Description>();
		for(Description node:((DAGImpl)res).vertexSet()){
			if(node instanceof Property){
				for(Description r: reasoner.getEquivalences(node) )
					roles.add(r);
				}
			
		}
		
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
