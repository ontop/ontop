package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.SemanticIndex.SemanticIndexHelper;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGChain;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;

import java.net.URI;
import java.util.Map;

import junit.framework.TestCase;

public class DAGChainTest extends TestCase {

    SemanticIndexHelper helper = new SemanticIndexHelper();

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = new BasicDescriptionFactory();

    public void test_simple_isa() {
        Ontology ontology = new OntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate b = predicateFactory.getPredicate(URI.create("b"), 1);
        Predicate c = predicateFactory.getPredicate(URI.create("c"), 1);

        Class ac = descFactory.getClass(a);
        Class bc = descFactory.getClass(b);
        Class cc = descFactory.getClass(c);
        ontology.addAssertion(new SubClassAxiomImpl(bc, ac));
        ontology.addAssertion(new SubClassAxiomImpl(cc, bc));
        ontology.addConcept(ac);
        ontology.addConcept(bc);
        ontology.addConcept(cc);

        DAGChain resChain = new DAGChain(DAGConstructor.getISADAG(ontology));
        Map<Description, DAGNode> res = resChain.chain();

        assertTrue(res.get(ac).descendans.contains(res.get(bc)));
        assertTrue(res.get(ac).descendans.contains(res.get(cc)));
        assertEquals(res.get(ac).descendans.size(), 2);

        assertTrue(res.get(bc).descendans.contains(res.get(cc)));
        assertEquals(res.get(bc).descendans.size(), 1);

        assertEquals(res.get(cc).descendans.size(), 0);
    }

    public void test_exists_simple() {
        Ontology ontology = new OntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate r = predicateFactory.getPredicate(URI.create("r"), 2);
        Predicate c = predicateFactory.getPredicate(URI.create("c"), 1);
        Class ac = descFactory.getClass(a);
        PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
        PropertySomeRestriction ier = descFactory.getPropertySomeRestriction(r, true);
        Class cc = descFactory.getClass(c);

        ontology.addAssertion(new SubClassAxiomImpl(er, ac));
        ontology.addAssertion(new SubClassAxiomImpl(cc, ier));
        ontology.addConcept(ac);
        ontology.addConcept(er);
        ontology.addConcept(ier);
        ontology.addConcept(cc);

        DAGChain resChain = new DAGChain(DAGConstructor.getISADAG(ontology));

        Map<Description, DAGNode> res = resChain.chain();

        assertTrue(res.get(ac).descendans.contains(res.get(er)));
        assertTrue(res.get(ac).descendans.contains(res.get(ier)));
        assertTrue(res.get(ac).descendans.contains(res.get(cc)));
        assertEquals(res.get(ac).descendans.size(), 3);

        assertTrue(res.get(er).descendans.contains(res.get(cc)));
        assertEquals(res.get(er).descendans.size(), 1);

        assertTrue(res.get(ier).descendans.contains(res.get(cc)));
        assertEquals(res.get(ier).descendans.size(), 1);

        assertEquals(res.get(cc).descendans.size(), 0);
    }

    public void test_exists_complex() {

        Ontology ontology = new OntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate r = predicateFactory.getPredicate(URI.create("r"), 2);
        Predicate c = predicateFactory.getPredicate(URI.create("c"), 1);
        Predicate b = predicateFactory.getPredicate(URI.create("b"), 1);
        Predicate d = predicateFactory.getPredicate(URI.create("d"), 1);

        Class ac = descFactory.getClass(a);
        PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
        PropertySomeRestriction ier = descFactory.getPropertySomeRestriction(r, true);
        Class cc = descFactory.getClass(c);
        Class bc = descFactory.getClass(b);
        Class dc = descFactory.getClass(d);

        ontology.addAssertion(new SubClassAxiomImpl(er, ac));
        ontology.addAssertion(new SubClassAxiomImpl(cc, ier));
        ontology.addAssertion(new SubClassAxiomImpl(bc, er));
        ontology.addAssertion(new SubClassAxiomImpl(ier, dc));

        ontology.addConcept(ac);
        ontology.addConcept(er);
        ontology.addConcept(ier);
        ontology.addConcept(cc);
        ontology.addConcept(bc);
        ontology.addConcept(dc);

        DAGChain resChain = new DAGChain(DAGConstructor.getISADAG(ontology));

        Map<Description, DAGNode> res = resChain.chain();

        assertTrue(res.get(ac).descendans.contains(res.get(er)));
        assertTrue(res.get(ac).descendans.contains(res.get(ier)));
        assertTrue(res.get(ac).descendans.contains(res.get(bc)));
        assertTrue(res.get(ac).descendans.contains(res.get(cc)));
        assertEquals(res.get(ac).descendans.size(), 4);

        assertTrue(res.get(dc).descendans.contains(res.get(er)));
        assertTrue(res.get(dc).descendans.contains(res.get(ier)));
        assertTrue(res.get(dc).descendans.contains(res.get(bc)));
        assertTrue(res.get(dc).descendans.contains(res.get(cc)));
        assertEquals(res.get(dc).descendans.size(), 4);

        assertTrue(res.get(er).descendans.contains(res.get(bc)));
        assertTrue(res.get(er).descendans.contains(res.get(cc)));
        assertEquals(res.get(er).descendans.size(), 2);

        assertTrue(res.get(ier).descendans.contains(res.get(bc)));
        assertTrue(res.get(ier).descendans.contains(res.get(cc)));
        assertEquals(res.get(ier).descendans.size(), 2);

        assertEquals(res.get(bc).descendans.size(), 0);
        assertEquals(res.get(cc).descendans.size(), 0);

    }


    public void test_exists_complex_2() {

        Ontology ontology = new OntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate r = predicateFactory.getPredicate(URI.create("r"), 2);


        Class ac = descFactory.getClass(a);
        PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
        PropertySomeRestriction ier = descFactory.getPropertySomeRestriction(r, true);

        ontology.addAssertion(new SubClassAxiomImpl(ier, ac));
        ontology.addAssertion(new SubClassAxiomImpl(ier, er));
        ontology.addAssertion(new SubClassAxiomImpl(ac, er));

        DAGChain resChain = new DAGChain(DAGConstructor.getISADAG(ontology));
        Map<Description, DAGNode> res = resChain.chain();


        assertTrue(res.get(ac).descendans.contains(res.get(er)));
        assertTrue(res.get(ac).descendans.contains(res.get(ier)));
        assertTrue(res.get(ac).descendans.contains(res.get(ac)));
        assertEquals(res.get(ac).descendans.size(), 3);

        assertTrue(res.get(er).descendans.contains(res.get(er)));
        assertTrue(res.get(er).descendans.contains(res.get(ier)));
        assertTrue(res.get(er).descendans.contains(res.get(ac)));
        assertEquals(res.get(er).descendans.size(), 3);

        assertTrue(res.get(ier).descendans.contains(res.get(er)));
        assertTrue(res.get(ier).descendans.contains(res.get(ier)));
        assertTrue(res.get(ier).descendans.contains(res.get(ac)));
        assertEquals(res.get(ier).descendans.size(), 3);
    }


}
