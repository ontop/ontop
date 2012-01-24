package it.unibz.krdb.obda.reformulation.tests;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.ClassImpl;
import it.unibz.krdb.obda.ontology.impl.PropertySomeRestrictionImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class TranslatorTests extends TestCase {

	public void test_1() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass class1 = factory.getOWLClass(URI.create("A"));
		OWLObjectProperty prop =  factory.getOWLObjectProperty(URI.create("prop1"));
		
		OWLObjectPropertyRangeAxiom ax = factory.getOWLObjectPropertyRangeAxiom(prop, class1);
		
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPI2Translator translator = new OWLAPI2Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();
		
		assertEquals(1, ass.size());
		SubClassAxiomImpl a = (SubClassAxiomImpl) assit.next();
		PropertySomeRestrictionImpl ex = (PropertySomeRestrictionImpl) a.getSub();
		assertEquals(true, ex.isInverse());
		ClassImpl con = (ClassImpl) a.getSuper();
//		assertEquals(false, con.isInverse());
		
	}
	
	public void test_2() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass class1 = factory.getOWLClass(URI.create("A"));
		OWLObjectProperty prop =  factory.getOWLObjectProperty(URI.create("prop1"));
		
		OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom(prop, class1);
		
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPI2Translator translator = new OWLAPI2Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();

		
		assertEquals(1, ass.size());
		SubClassAxiomImpl a = (SubClassAxiomImpl) assit.next();
		PropertySomeRestrictionImpl ex = (PropertySomeRestrictionImpl) a.getSub();
		assertEquals(false, ex.isInverse());
		ClassImpl con = (ClassImpl) a.getSuper();
//		assertEquals(false, con.isInverse());
		
	}
	
	public void test_3() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLObjectProperty prop =  factory.getOWLObjectProperty(URI.create("R"));
		OWLObjectProperty invofprop =  factory.getOWLObjectProperty(URI.create("S"));
		
		OWLInverseObjectPropertiesAxiom ax = factory.getOWLInverseObjectPropertiesAxiom(prop, invofprop);
		
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPI2Translator translator = new OWLAPI2Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();

		
		assertEquals(2, ass.size());
		SubPropertyAxiomImpl a = (SubPropertyAxiomImpl) assit.next();
		SubPropertyAxiomImpl b = (SubPropertyAxiomImpl) assit.next();
		
		
		Property included = (Property) a.getSub();
		assertEquals(false, included.isInverse());
		assertEquals("R", included.getPredicate().getName().toString());
		Property indlucing = (Property) a.getSuper();
		assertEquals(true, indlucing.isInverse());
		assertEquals("S", indlucing.getPredicate().getName().toString());
		
		included = (Property) b.getSub();
		assertEquals(false, included.isInverse());
		assertEquals("S", included.getPredicate().getName().toString());
		indlucing = (Property) b.getSuper();
		assertEquals(true, indlucing.isInverse());
		assertEquals("R", indlucing.getPredicate().getName().toString());
		
	}
	
	public void test_4() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass clsA = factory.getOWLClass(URI.create("A"));
		OWLClass clsB = factory.getOWLClass(URI.create("B"));
		
		OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(clsA, clsB);
				
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPI2Translator translator = new OWLAPI2Translator();
		Ontology dlliteonto = translator.translate(onto);
		
		Set<Axiom> ass = dlliteonto.getAssertions();
		Iterator<Axiom> assit = ass.iterator();

		
		assertEquals(2, ass.size());
		SubClassAxiomImpl c1 = (SubClassAxiomImpl) assit.next();
		SubClassAxiomImpl c2 = (SubClassAxiomImpl) assit.next();
		
		OClass included = (OClass) c1.getSub();
		assertEquals("A", included.getPredicate().getName().toString());
		OClass indlucing = (OClass) c1.getSuper();
		assertEquals("B", indlucing.getPredicate().getName().toString());
		
		included = (OClass) c2.getSub();
		assertEquals("B", included.getPredicate().getName().toString());
		indlucing = (OClass) c2.getSuper();
		assertEquals("A", indlucing.getPredicate().getName().toString());
		
	}
	
}
