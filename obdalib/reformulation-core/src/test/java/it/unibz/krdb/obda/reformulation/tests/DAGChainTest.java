package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.SemanticIndex.SemanticIndexHelper;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGChain;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;

import java.net.URI;
import java.util.Map;

import junit.framework.TestCase;

public class DAGChainTest extends TestCase {

    SemanticIndexHelper helper = new SemanticIndexHelper();

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();

    public void test_simple_isa() {
        DLLiterOntology ontology = new DLLiterOntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate b = predicateFactory.getPredicate(URI.create("b"), 1);
        Predicate c = predicateFactory.getPredicate(URI.create("c"), 1);

        AtomicConceptDescription ac = descFactory.getAtomicConceptDescription(a);
        AtomicConceptDescription bc = descFactory.getAtomicConceptDescription(b);
        AtomicConceptDescription cc = descFactory.getAtomicConceptDescription(c);
        ontology.addAssertion(new DLLiterConceptInclusionImpl(bc, ac));
        ontology.addAssertion(new DLLiterConceptInclusionImpl(cc, bc));
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
        DLLiterOntology ontology = new DLLiterOntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate r = predicateFactory.getPredicate(URI.create("r"), 2);
        Predicate c = predicateFactory.getPredicate(URI.create("c"), 1);
        AtomicConceptDescription ac = descFactory.getAtomicConceptDescription(a);
        ExistentialConceptDescription er = descFactory.getExistentialConceptDescription(r, false);
        ExistentialConceptDescription ier = descFactory.getExistentialConceptDescription(r, true);
        AtomicConceptDescription cc = descFactory.getAtomicConceptDescription(c);

        ontology.addAssertion(new DLLiterConceptInclusionImpl(er, ac));
        ontology.addAssertion(new DLLiterConceptInclusionImpl(cc, ier));
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

        DLLiterOntology ontology = new DLLiterOntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate r = predicateFactory.getPredicate(URI.create("r"), 2);
        Predicate c = predicateFactory.getPredicate(URI.create("c"), 1);
        Predicate b = predicateFactory.getPredicate(URI.create("b"), 1);
        Predicate d = predicateFactory.getPredicate(URI.create("d"), 1);

        AtomicConceptDescription ac = descFactory.getAtomicConceptDescription(a);
        ExistentialConceptDescription er = descFactory.getExistentialConceptDescription(r, false);
        ExistentialConceptDescription ier = descFactory.getExistentialConceptDescription(r, true);
        AtomicConceptDescription cc = descFactory.getAtomicConceptDescription(c);
        AtomicConceptDescription bc = descFactory.getAtomicConceptDescription(b);
        AtomicConceptDescription dc = descFactory.getAtomicConceptDescription(d);

        ontology.addAssertion(new DLLiterConceptInclusionImpl(er, ac));
        ontology.addAssertion(new DLLiterConceptInclusionImpl(cc, ier));
        ontology.addAssertion(new DLLiterConceptInclusionImpl(bc, er));
        ontology.addAssertion(new DLLiterConceptInclusionImpl(ier, dc));

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

        DLLiterOntology ontology = new DLLiterOntologyImpl(URI.create(""));

        Predicate a = predicateFactory.getPredicate(URI.create("a"), 1);
        Predicate r = predicateFactory.getPredicate(URI.create("r"), 2);


        AtomicConceptDescription ac = descFactory.getAtomicConceptDescription(a);
        ExistentialConceptDescription er = descFactory.getExistentialConceptDescription(r, false);
        ExistentialConceptDescription ier = descFactory.getExistentialConceptDescription(r, true);

        ontology.addAssertion(new DLLiterConceptInclusionImpl(ier, ac));
        ontology.addAssertion(new DLLiterConceptInclusionImpl(ier, er));
        ontology.addAssertion(new DLLiterConceptInclusionImpl(ac, er));

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
