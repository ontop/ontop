package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;

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
        OClass ac = descFactory.createClass(a);
        OClass cc = descFactory.createClass(c);
        PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
        ontology.addConcept(ac.getPredicate());
        ontology.addConcept(cc.getPredicate());
        ontology.addRole(er.getPredicate());

        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, er));

        DAG res = DAGConstructor.getSigma(ontology);
        res.clean();

        assertTrue(res.getClassNode(ac).getDescendants().contains(res.getClassNode(er)));

        assertEquals(1, res.getClassNode(ac).getDescendants().size());

        assertEquals(0, res.getClassNode(er).getDescendants().size());

        assertEquals(0, res.getClassNode(cc).getDescendants().size());

    }
}
