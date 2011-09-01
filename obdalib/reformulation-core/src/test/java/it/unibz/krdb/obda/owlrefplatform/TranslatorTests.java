package it.unibz.krdb.obda.owlrefplatform;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.ClassImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.PropertySomeRestrictionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubPropertyAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;

import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;


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
		
		Class included = (Class) c1.getSub();
		assertEquals("A", included.getPredicate().getName().toString());
		Class indlucing = (Class) c1.getSuper();
		assertEquals("B", indlucing.getPredicate().getName().toString());
		
		included = (Class) c2.getSub();
		assertEquals("B", included.getPredicate().getName().toString());
		indlucing = (Class) c2.getSuper();
		assertEquals("A", indlucing.getPredicate().getName().toString());
		
	}
	
}
