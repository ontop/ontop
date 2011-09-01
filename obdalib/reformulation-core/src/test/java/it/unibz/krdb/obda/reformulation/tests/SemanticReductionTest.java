package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.SemanticIndex.SemanticIndexHelper;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SigmaTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;

import java.util.List;

import junit.framework.TestCase;

public class SemanticReductionTest extends TestCase {
	SemanticIndexHelper	helper	= new SemanticIndexHelper();

	public void test_2_0_0() throws Exception {
		Ontology ontology = helper.load_onto("test_2_0_0");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.index();
		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(ontology, DAGConstructor.getSigmaOntology(ontology));
		List<Axiom> rv = reduction.reduce();
		assertEquals(0, rv.size());
	}

	public void test_2_0_1() throws Exception {
		Ontology ontology = helper.load_onto("test_2_0_1");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.index();
		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(ontology, DAGConstructor.getSigmaOntology(ontology));
		List<Axiom> rv = reduction.reduce();
		assertEquals(0, rv.size());
	}

	public void test_2_1_0() throws Exception {
		Ontology ontology = helper.load_onto("test_2_1_0");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.index();
		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(ontology, DAGConstructor.getSigmaOntology(ontology));
		List<Axiom> rv = reduction.reduce();
		assertEquals(1, rv.size());
	}

	public void test_1_2_0() throws Exception {
		Ontology ontology = helper.load_onto("test_1_2_0");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.index();
		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(ontology, DAGConstructor.getSigmaOntology(ontology));
		List<Axiom> rv = reduction.reduce();
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
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.index();
		SigmaTBoxOptimizer reduction = new SigmaTBoxOptimizer(ontology, OntologyFactoryImpl.getInstance().createOntology());
		List<Axiom> rv = reduction.reduce();
		//System.out.println(rv);
		assertEquals(45, rv.size());
	}
}
