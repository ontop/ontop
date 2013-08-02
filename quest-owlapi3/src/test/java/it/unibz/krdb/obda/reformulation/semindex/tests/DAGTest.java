/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.semindex.tests;

import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;

import java.util.List;

import junit.framework.TestCase;

public class DAGTest extends TestCase {

	SemanticIndexHelper	helper	= new SemanticIndexHelper();

	private void test_dag_index_nodes(String testname) throws Exception {
		DAG res = helper.load_dag(testname);
		List<List<DAGNode>> exp_idx = helper.get_results(testname);

		
		assertEquals(exp_idx.get(0).size(), res.getClasses().size());
		assertEquals(exp_idx.get(1).size(), res.getRoles().size());

		for (DAGNode node : exp_idx.get(0)) {
			res.getClasses().contains(node);
		}
		for (DAGNode node : exp_idx.get(1)) {
			res.getRoles().contains(node);
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
