package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.owlapi2.OWLAPI2Translator;

import java.net.URI;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLAPI2ABoxTranslatorTest extends TestCase {

	OWLOntologyManager	manager		= OWLManager.createOWLOntologyManager();
	OWLDataFactory		factory		= manager.getOWLDataFactory();
	OWLAPI2Translator	translator	= new OWLAPI2Translator();

	
	public void setUp() throws Exception {
	}

	public void testClassAssertion() {
		OWLIndividualAxiom a1 = factory.getOWLClassAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLClass(URI.create("person")));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof ClassAssertion);
		ClassAssertion c = (ClassAssertion) a2;
		assertTrue(c.getConcept().getArity() == 1);
		assertTrue(c.getConcept().getName().toString().equals("person"));
		assertTrue(c.getObject().getURI().toString().equals("a"));

	}

	public void testObjectPropertyAssertion() {
		OWLIndividualAxiom a1 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLObjectProperty(URI.create("hasfather")), factory.getOWLIndividual(URI.create("b")));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof ObjectPropertyAssertion);
		ObjectPropertyAssertion r = (ObjectPropertyAssertion) a2;
		assertTrue(r.getRole().getArity() == 2);
		assertTrue(r.getRole().getName().toString().equals("hasfather"));
		assertTrue(r.getFirstObject().getURI().toString().equals("a"));
		assertTrue(r.getSecondObject().getURI().toString().equals("b"));
	}

	public void testDataPropertyAssertion1() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLUntypedConstant("23"));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().equals("23"));
	}

	public void testDataPropertyAssertion2() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant("23"));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("23"));
	}

	public void testDataPropertyAssertion3() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant(23));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("23"));
	}

	public void testDataPropertyAssertion4() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant(true));
	    Assertion a2 = translator.translate(a1);
	    
	    assertTrue(a2 instanceof DataPropertyAssertion);
	    DataPropertyAssertion r = (DataPropertyAssertion) a2;
	    assertTrue(r.getAttribute().getArity() == 2);
	    assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
	    assertTrue(r.getObject().getURI().toString().equals("a"));
	    assertTrue(r.getValue().getValue().toString().equals("true"));
	}

	public void testDataPropertyAssertion5() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant((double)2.34));
	    Assertion a2 = translator.translate(a1);
	    
	    assertTrue(a2 instanceof DataPropertyAssertion);
	    DataPropertyAssertion r = (DataPropertyAssertion) a2;
	    assertTrue(r.getAttribute().getArity() == 2);
	    assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
	    assertTrue(r.getObject().getURI().toString().equals("a"));
	    assertTrue(r.getValue().getValue().toString().equals("2.34"));
	}

	public void testDataPropertyAssertion6() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant((float) 2.34));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("2.34"));
	}
}
