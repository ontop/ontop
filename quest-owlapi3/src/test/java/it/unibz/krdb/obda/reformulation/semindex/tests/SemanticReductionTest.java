package it.unibz.krdb.obda.reformulation.semindex.tests;

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


import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.SigmaTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxReasonerToOntology;

import java.util.Set;

import junit.framework.TestCase;

public class SemanticReductionTest extends TestCase {
	
	SemanticIndexHelper	helper	= new SemanticIndexHelper();

	public void test_2_0_0() throws Exception {
		Ontology ontology = helper.load_onto("test_2_0_0");
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);

		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(reasoner);
		Set<Axiom> rv = reduction.getReducedOntology().getAssertions();
		assertEquals(0, rv.size());
	}

	public void test_2_0_1() throws Exception {
		Ontology ontology = helper.load_onto("test_2_0_1");
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);

		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(reasoner);
		Set<Axiom> rv = reduction.getReducedOntology().getAssertions();
		assertEquals(0, rv.size());
	}

	public void test_2_1_0() throws Exception {
		Ontology ontology = helper.load_onto("test_2_1_0");
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);

		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(reasoner);
		Set<Axiom> rv = reduction.getReducedOntology().getAssertions();
		assertEquals(1, rv.size());
	}

	public void test_1_2_0() throws Exception {
		Ontology ontology = helper.load_onto("test_1_2_0");
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);

		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(reasoner);
		Set<Axiom> rv = reduction.getReducedOntology().getAssertions();
		assertEquals(0, rv.size());
	}

	public void test_equivalence() throws Exception {

		/*
		 * The ontology contains A1 = A2 = A3, B1 ISA A1, B1 = B2 = B3, this
		 * gives 9 inferences and R1 = R2 = R3, S1 ISA R1, S1 = S2 = S3, this
		 * gives 36 inferences (counting inverse related inferences, and exist
		 * related inferences. Total, 45 inferences
		 */

		Ontology ontology = helper.load_onto("equivalence-test");
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);

		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(reasoner, null);
		Set<Axiom> rv = reduction.getReducedOntology().getAssertions();
		//System.out.println(rv);
		assertEquals(45, rv.size());
	}
}
