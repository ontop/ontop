package it.unibz.inf.ontop.reformulation.tests;

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


import it.unibz.inf.ontop.ontology.ClassExpression;
import it.unibz.inf.ontop.ontology.OClass;
import it.unibz.inf.ontop.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.ontology.ObjectSomeValuesFrom;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.owlrefplatform.core.tboxprocessing.TBoxReasonerToOntology;
import junit.framework.TestCase;

public class SigmaTest extends TestCase {

    private static final OntologyFactory descFactory = OntologyFactoryImpl.getInstance();

    public void test_exists_simple() {
        Ontology ontology = descFactory.createOntology();

        OClass ac = ontology.getVocabulary().createClass("a");
        OClass cc = ontology.getVocabulary().createClass("c");
        ObjectPropertyExpression rprop = ontology.getVocabulary().createObjectProperty("r");
        ObjectSomeValuesFrom er = rprop.getDomain();
 
        ontology.addSubClassOfAxiom(er, ac);
        ontology.addSubClassOfAxiom(cc, er);

       
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);
		TBoxReasoner sigmaReasoner = new TBoxReasonerImpl(TBoxReasonerToOntology.getOntology(reasoner, true));						

		EquivalencesDAG<ClassExpression> classes = sigmaReasoner.getClassDAG();

        assertTrue(classes.getSub(classes.getVertex(ac)).contains(classes.getVertex(er)));

     // Roman: was 1, which, I think, is wrong: A has two subclasses, ER and C (now 3 because it's reflexive)
        assertEquals(3, classes.getSub(classes.getVertex(ac)).size());   // getDescendants is reflexive

        assertEquals(1, classes.getSub(classes.getVertex(er)).size());  // getDescendants is reflexive

        assertEquals(1, classes.getSub(classes.getVertex(cc)).size());  // getDescendants is reflexive

    }
}
