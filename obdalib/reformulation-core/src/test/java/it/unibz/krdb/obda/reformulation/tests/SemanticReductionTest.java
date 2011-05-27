package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.SemanticIndex.SemanticIndexHelper;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SDAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticReduction;
import it.unibz.krdb.obda.owlrefplatform.core.abox.TDAG;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;

import java.util.List;

import junit.framework.TestCase;

import org.semanticweb.owl.model.OWLOntologyCreationException;

public class SemanticReductionTest extends TestCase {
    SemanticIndexHelper helper = new SemanticIndexHelper();

    public void test_1_0_0() throws OWLOntologyCreationException {
        DAG dag = helper.load_dag("test_1_0_0");
        TDAG tdag = new TDAG(dag);
        SDAG sdag = new SDAG(tdag);
        SemanticReduction reduction = new SemanticReduction(dag, tdag, sdag);
        List<Assertion> rv = reduction.reduce();
        assertEquals(0, rv.size());
    }
}
