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

import org.semanticweb.ontop.ontology.*;
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
		
		Set<SubClassOfAxiom> ass = dlliteonto.getSubClassAxioms();
		Iterator<SubClassOfAxiom> assit = ass.iterator();
		assertEquals(1, ass.size());
		
		SubClassOfAxiom a = assit.next();
		SomeValuesFrom ex = (SomeValuesFrom) a.getSub();
		assertEquals(true, ex.getProperty().isInverse());
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
		
		Set<SubClassOfAxiom> ass = dlliteonto.getSubClassAxioms();
		Iterator<SubClassOfAxiom> assit = ass.iterator();
		assertEquals(1, ass.size());
		
		SubClassOfAxiom a = assit.next();
		SomeValuesFrom ex = (SomeValuesFrom) a.getSub();
		assertEquals(false, ex.getProperty().isInverse());
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
		
		Set<SubPropertyOfAxiom> ass = dlliteonto.getSubPropertyAxioms();
		Iterator<SubPropertyOfAxiom> assit = ass.iterator();
		assertEquals(2, ass.size());
		
		SubPropertyOfAxiom a = assit.next();
		SubPropertyOfAxiom b = assit.next();
		PropertyExpression included =a.getSub();
		assertEquals(false, included.isInverse());
		assertEquals("http://example/R", included.getPredicate().getName().toString());
		
		PropertyExpression indlucing = a.getSuper();
		assertEquals(true, indlucing.isInverse());
		assertEquals("http://example/S", indlucing.getPredicate().getName().toString());
		
		included = b.getSub();
		assertEquals(false, included.isInverse());
		assertEquals("http://example/S", included.getPredicate().getName().toString());
		
		indlucing = b.getSuper();
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
		
		Set<SubClassOfAxiom> ass = dlliteonto.getSubClassAxioms();
		Iterator<SubClassOfAxiom> assit = ass.iterator();
		assertEquals(2, ass.size());
		
		SubClassOfAxiom c1 = assit.next();
		SubClassOfAxiom c2 = assit.next();
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
