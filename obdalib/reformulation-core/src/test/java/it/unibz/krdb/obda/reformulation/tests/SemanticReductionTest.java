package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.SemanticIndex.SemanticIndexHelper;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticReduction;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;

import java.util.List;

import junit.framework.TestCase;

public class SemanticReductionTest extends TestCase {
    SemanticIndexHelper helper = new SemanticIndexHelper();


    public void test_2_0_0() throws Exception {
        Ontology ontology = helper.load_onto("test_2_0_0");
        DAG isa = DAGConstructor.getISADAG(ontology);
        isa.index();
        SemanticReduction reduction = new SemanticReduction(ontology, DAGConstructor.getSigmaOntology(ontology));
        List<Axiom> rv = reduction.reduce();
        assertEquals(0, rv.size());
    }

    public void test_2_0_1() throws Exception {
        Ontology ontology = helper.load_onto("test_2_0_1");
        DAG isa = DAGConstructor.getISADAG(ontology);
        isa.index();
        SemanticReduction reduction = new SemanticReduction(ontology, DAGConstructor.getSigmaOntology(ontology));
        List<Axiom> rv = reduction.reduce();
        assertEquals(0, rv.size());
    }

    public void test_2_1_0() throws Exception {
        Ontology ontology = helper.load_onto("test_2_1_0");
        DAG isa = DAGConstructor.getISADAG(ontology);
        isa.index();
        SemanticReduction reduction = new SemanticReduction(ontology, DAGConstructor.getSigmaOntology(ontology));
        List<Axiom> rv = reduction.reduce();
        assertEquals(1, rv.size());
    }

    public void test_1_2_0() throws Exception {
        Ontology ontology = helper.load_onto("test_1_2_0");
        DAG isa = DAGConstructor.getISADAG(ontology);
        isa.index();
        SemanticReduction reduction = new SemanticReduction(ontology, DAGConstructor.getSigmaOntology(ontology));
        List<Axiom> rv = reduction.reduce();
        assertEquals(0, rv.size());
    }

}
