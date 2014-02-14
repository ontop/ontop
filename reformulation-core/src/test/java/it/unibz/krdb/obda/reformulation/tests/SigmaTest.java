/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.tests;


import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.SigmaTBoxOptimizer;
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

        ontology.addAssertion(descFactory.createSubClassAxiom(er, ac));
        ontology.addAssertion(descFactory.createSubClassAxiom(cc, er));

        
       
		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(ontology);
		Ontology ontologySigma = SigmaTBoxOptimizer.getSigmaOntology(reasoner);
        TBoxReasonerImpl sigma = new TBoxReasonerImpl(ontologySigma);
        EquivalencesDAG<BasicClassDescription> classes = sigma.getClasses();

        assertTrue(classes.getSub(classes.getVertex(ac)).contains(classes.getVertex(er)));

     // Roman: was 1, which, I think, is wrong: A has two subclasses, ER and C (now 3 because it's reflexive)
        assertEquals(3, classes.getSub(classes.getVertex(ac)).size());   // getDescendants is reflexive

        assertEquals(1, classes.getSub(classes.getVertex(er)).size());  // getDescendants is reflexive

        assertEquals(1, classes.getSub(classes.getVertex(cc)).size());  // getDescendants is reflexive

    }
}
