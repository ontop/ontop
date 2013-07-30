/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.semindex.tests;

import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGEdgeIterator;
import junit.framework.TestCase;

public class DAGEdgeIteratorTest extends TestCase {

	SemanticIndexHelper	helper	= null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		helper = new SemanticIndexHelper();
	}

	public void testEnumerationOfSubClass() throws Exception {
		Ontology ontology = helper.load_onto("equivalence-classes");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.clean();
		
		DAGEdgeIterator it = new DAGEdgeIterator(isa);
		int count = 0;
		while (it.hasNext()) {
			count += 1;
			it.next();
		}
		assertEquals(9,count);
	}

	public void testEnumerationOfSubProperty() throws Exception {
		Ontology ontology = helper.load_onto("equivalence-roles");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.clean();
		
		DAGEdgeIterator it = new DAGEdgeIterator(isa);
		int count = 0;
		while (it.hasNext()) {
			count += 1;
			it.next();
		}
		
		/* 9 for role inclusions, 9 for exists R, 9 for exists Rinv */
		assertEquals(36,count);
	}
}
