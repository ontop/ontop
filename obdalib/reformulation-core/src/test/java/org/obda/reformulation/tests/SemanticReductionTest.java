package org.obda.reformulation.tests;


import junit.framework.TestCase;
import org.obda.SemanticIndex.SemanticIndexHelper;
import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.SemanticReduction;
import org.obda.owlrefplatform.core.abox.TDAG;
import org.obda.owlrefplatform.core.ontology.Assertion;
import org.semanticweb.owl.model.OWLOntologyCreationException;

import java.util.List;

public class SemanticReductionTest extends TestCase {
    SemanticIndexHelper helper = new SemanticIndexHelper();

    public void test_1_0_0() throws OWLOntologyCreationException {
        DAG dag = helper.load_dag("test_1_0_0");
        TDAG tdag = new TDAG(dag);
        SemanticReduction reduction = new SemanticReduction(dag, tdag, tdag);
        List<Assertion> rv = reduction.reduce();
        assertEquals(0, rv.size());
    }
}
