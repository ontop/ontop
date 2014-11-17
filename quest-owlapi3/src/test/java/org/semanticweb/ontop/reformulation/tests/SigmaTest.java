package org.semanticweb.ontop.reformulation.tests;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.*;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import junit.framework.TestCase;
import org.semanticweb.ontop.owlrefplatform.core.tboxprocessing.TBoxReasonerToOntology;

public class SigmaTest extends TestCase {

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = OntologyFactoryImpl.getInstance();

    public void test_exists_simple() {
        Ontology ontology = descFactory.createOntology();

        OClass ac = descFactory.createClass("a");
        OClass cc = descFactory.createClass("c");
        PropertyExpression rprop = descFactory.createObjectProperty("r");
        SomeValuesFrom er = descFactory.createPropertySomeRestriction(rprop);
 
        ontology.addSubClassOfAxiomWithReferencedEntities(er, ac);
        ontology.addSubClassOfAxiomWithReferencedEntities(cc, er);

       
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);
		TBoxReasoner sigmaReasoner = new TBoxReasonerImpl(TBoxReasonerToOntology.getOntology(reasoner, true));

		EquivalencesDAG<BasicClassDescription> classes = sigmaReasoner.getClasses();

        assertTrue(classes.getSub(classes.getVertex(ac)).contains(classes.getVertex(er)));

     // Roman: was 1, which, I think, is wrong: A has two subclasses, ER and C (now 3 because it's reflexive)
        assertEquals(3, classes.getSub(classes.getVertex(ac)).size());   // getDescendants is reflexive

        assertEquals(1, classes.getSub(classes.getVertex(er)).size());  // getDescendants is reflexive

        assertEquals(1, classes.getSub(classes.getVertex(cc)).size());  // getDescendants is reflexive

    }
}
