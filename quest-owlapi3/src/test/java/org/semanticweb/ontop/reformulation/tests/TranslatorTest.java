package org.semanticweb.ontop.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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

import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.ontop.ontology.Axiom;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.impl.PropertySomeRestrictionImpl;
import org.semanticweb.ontop.ontology.impl.SubClassAxiomImpl;
import org.semanticweb.ontop.ontology.impl.SubPropertyAxiomImpl;
import org.semanticweb.ontop.owlapi3.OWLAPI3Translator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TranslatorTest extends TestCase {

	public void test_1() throws Exception{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass class1 = factory.getOWLClass(IRI.create(URI.create("http://example/A")));
		OWLObjectProperty prop =  factory.getOWLObjectProperty(IRI.create(URI.create("http://example/prop1")));
		
		OWLObjectPropertyRangeAxiom ax = factory.getOWLObjectPropertyRangeAxiom(prop, class1);
		
		OWLOntology onto = manager.createOntology(IRI.create(URI.create("http://example/testonto")));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(prop));
		manager.addAxiom(onto, ax);
		
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();
		assertEquals(1, ass.size());
		
		SubClassAxiomImpl a = (SubClassAxiomImpl) assit.next();
		PropertySomeRestrictionImpl ex = (PropertySomeRestrictionImpl) a.getSub();
		assertEquals(true, ex.isInverse());
	}
	
	public void test_2() throws Exception{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass class1 = factory.getOWLClass(IRI.create(URI.create("http://example/A")));
		OWLObjectProperty prop =  factory.getOWLObjectProperty(IRI.create(URI.create("http://example/prop1")));
		
		OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom(prop, class1);
		
		OWLOntology onto = manager.createOntology(IRI.create(URI.create("http://example/testonto")));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		
		manager.addAxiom(onto, ax);
		
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();
		assertEquals(1, ass.size());
		
		SubClassAxiomImpl a = (SubClassAxiomImpl) assit.next();
		PropertySomeRestrictionImpl ex = (PropertySomeRestrictionImpl) a.getSub();
		assertEquals(false, ex.isInverse());
	}
	
	public void test_3() throws Exception{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLObjectProperty prop =  factory.getOWLObjectProperty(IRI.create(URI.create("http://example/R")));
		OWLObjectProperty invofprop =  factory.getOWLObjectProperty(IRI.create(URI.create("http://example/S")));
		
		OWLInverseObjectPropertiesAxiom ax = factory.getOWLInverseObjectPropertiesAxiom(prop, invofprop);
		
		OWLOntology onto = manager.createOntology(IRI.create(URI.create("http://example/testonto")));
		manager.addAxiom(onto, ax);
		
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();
		assertEquals(2, ass.size());
		
		SubPropertyAxiomImpl a = (SubPropertyAxiomImpl) assit.next();
		SubPropertyAxiomImpl b = (SubPropertyAxiomImpl) assit.next();
		Property included = (Property) a.getSub();
		assertEquals(false, included.isInverse());
		assertEquals("http://example/R", included.getPredicate().getName().toString());
		
		Property indlucing = (Property) a.getSuper();
		assertEquals(true, indlucing.isInverse());
		assertEquals("http://example/S", indlucing.getPredicate().getName().toString());
		
		included = (Property) b.getSub();
		assertEquals(false, included.isInverse());
		assertEquals("http://example/S", included.getPredicate().getName().toString());
		
		indlucing = (Property) b.getSuper();
		assertEquals(true, indlucing.isInverse());
		assertEquals("http://example/R", indlucing.getPredicate().getName().toString());
	}
	
	public void test_4() throws Exception{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass clsA = factory.getOWLClass(IRI.create(URI.create("http://example/A")));
		OWLClass clsB = factory.getOWLClass(IRI.create(URI.create("http://example/B")));
		
		OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(clsA, clsB);
				
		OWLOntology onto = manager.createOntology(IRI.create(URI.create("http://example/testonto")));
		manager.addAxiom(onto, ax);
		
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();
		assertEquals(2, ass.size());
		
		SubClassAxiomImpl c1 = (SubClassAxiomImpl) assit.next();
		SubClassAxiomImpl c2 = (SubClassAxiomImpl) assit.next();
		OClass included = (OClass) c1.getSub();
		assertEquals("http://example/A", included.getPredicate().getName().toString());
		
		OClass indlucing = (OClass) c1.getSuper();
		assertEquals("http://example/B", indlucing.getPredicate().getName().toString());
		
		included = (OClass) c2.getSub();
		assertEquals("http://example/B", included.getPredicate().getName().toString());
		
		indlucing = (OClass) c2.getSuper();
		assertEquals("http://example/A", indlucing.getPredicate().getName().toString());
	}
}
