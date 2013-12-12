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
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import junit.framework.TestCase;

public class SigmaTest extends TestCase {

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = new OntologyFactoryImpl();

    public void test_exists_simple() {
        Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

        Predicate a = predicateFactory.getPredicate("a", 1);
        Predicate c = predicateFactory.getPredicate("c", 1);
        Predicate r = predicateFactory.getPredicate("r", 2);
        OClass ac = descFactory.createClass(a);
        OClass cc = descFactory.createClass(c);
        PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
        ontology.addConcept(ac.getPredicate());
        ontology.addConcept(cc.getPredicate());
        ontology.addRole(er.getPredicate());

        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, er));

        
       
        Ontology ontologySigma =  TBoxReasonerImpl.getSigma(ontology);
        TBoxReasonerImpl sigma= new TBoxReasonerImpl(ontologySigma, false);
        DAGImpl res= sigma.getDAG();

        assertTrue(sigma.getDescendants(ac, false).contains(sigma.getEquivalences(er, false)));

        assertEquals(1, sigma.getDescendants(ac, false).size());

        assertEquals(0, sigma.getDescendants(er, false).size());

        assertEquals(0, sigma.getDescendants(cc, false).size());

    }
}
