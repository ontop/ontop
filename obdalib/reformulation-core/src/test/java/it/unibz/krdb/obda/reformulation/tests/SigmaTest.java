package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;

import java.net.URI;

import junit.framework.TestCase;

public class SigmaTest extends TestCase {

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = new OntologyFactoryImpl();

    public void test_exists_simple() {
        Ontology ontology = OntologyFactoryImpl.getInstance().createOntology(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate c = predicateFactory.getPredicate(URI.create("c"), 1);
        Predicate r = predicateFactory.getPredicate(URI.create("r"), 2);
        Class ac = descFactory.createClass(a);
        Class cc = descFactory.createClass(c);
        PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);

        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, er));
        ontology.addConcept(ac);
        ontology.addConcept(cc);
        ontology.addConcept(er);

        DAG res = DAGConstructor.getSigma(ontology);
        res.clean();

        assertTrue(res.getClassNode(ac).descendans.contains(res.getClassNode(er)));

        assertEquals(1, res.getClassNode(ac).descendans.size());

        assertEquals(0, res.getClassNode(er).descendans.size());

        assertEquals(0, res.getClassNode(cc).descendans.size());

    }

//    }
//
//    public void test_exists_complex() {
//        DAGNode a = new DAGNode("a");
//        DAGNode er = new DAGNode(DAG.owl_exists_obj + "r");
//        DAGNode ier = new DAGNode(DAG.owl_inverse_exists_obj + "r");
//        DAGNode c = new DAGNode("c");
//        DAGNode b = new DAGNode("b");
//        DAGNode d = new DAGNode("d");
//
//        a.getChildren().add(er);
//        er.getParents().add(a);
//
//        ier.getChildren().add(c);
//        c.getParents().add(ier);
//
//        er.getChildren().add(b);
//        b.getParents().add(er);
//
//        ier.getParents().add(d);
//        d.getChildren().add(ier);
//
//        List<DAGNode> ll = new LinkedList<DAGNode>();
//        ll.add(a);
//        ll.add(er);
//        ll.add(ier);
//        ll.add(c);
//        ll.add(b);
//        ll.add(d);
//
//        DAG dag = new DAG(ll, new LinkedList<DAGNode>(), new LinkedList<DAGNode>());
//        TDAG tdag = new TDAG(dag);
//        SDAG sdag = new SDAG(tdag);
//        Map<String, DAGNode> res = sdag.getTDAG();
//
//        assertTrue(res.get("a").descendans.contains(res.get(DAG.owl_exists_obj + "r")));
//        assertTrue(res.get("a").descendans.contains(res.get(DAG.owl_inverse_exists_obj + "r")));
//        assertEquals(res.get("a").descendans.size(), 2);
//
//        assertTrue(res.get("d").descendans.contains(res.get(DAG.owl_exists_obj + "r")));
//        assertTrue(res.get("d").descendans.contains(res.get(DAG.owl_inverse_exists_obj + "r")));
//        assertEquals(res.get("d").descendans.size(), 2);
//
//        assertEquals(res.get(DAG.owl_exists_obj + "r").descendans.size(), 0);
//
//        assertEquals(res.get(DAG.owl_inverse_exists_obj + "r").descendans.size(), 0);
//
//        assertEquals(res.get("b").descendans.size(), 0);
//
//        assertEquals(res.get("c").descendans.size(), 0);
//
//    }
}
