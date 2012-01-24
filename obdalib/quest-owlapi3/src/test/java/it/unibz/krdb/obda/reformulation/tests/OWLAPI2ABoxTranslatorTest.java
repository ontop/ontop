package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;

import java.net.URI;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLAPI2ABoxTranslatorTest extends TestCase {

	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLDataFactory factory = manager.getOWLDataFactory();
	OWLAPI3Translator translator = new OWLAPI3Translator();

	public void setUp() throws Exception {
	}

	public void testClassAssertion() {
		OWLIndividualAxiom a1 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(URI.create("person"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof ClassAssertion);
		ClassAssertion c = (ClassAssertion) a2;
		assertTrue(c.getConcept().getArity() == 1);
		assertTrue(c.getConcept().getName().toString().equals("person"));
		assertTrue(c.getObject().getURI().toString().equals("a"));

	}

	public void testObjectPropertyAssertion() {
		OWLIndividualAxiom a1 = factory.getOWLObjectPropertyAssertionAxiom(
				factory.getOWLObjectProperty(IRI.create(URI.create("hasfather"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))), factory.getOWLNamedIndividual(IRI.create(URI.create("b"))));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof ObjectPropertyAssertion);
		ObjectPropertyAssertion r = (ObjectPropertyAssertion) a2;
		assertTrue(r.getRole().getArity() == 2);
		assertTrue(r.getRole().getName().toString().equals("hasfather"));
		assertTrue(r.getFirstObject().getURI().toString().equals("a"));
		assertTrue(r.getSecondObject().getURI().toString().equals("b"));
	}

	public void testDataPropertyAssertion1() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(IRI.create(URI.create("hasvalue"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))), factory.getOWLLiteral("23"));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().equals("23"));
	}

	public void testDataPropertyAssertion2() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(IRI.create(URI.create("hasvalue"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))), factory.getOWLLiteral("23"));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("23"));
	}

	public void testDataPropertyAssertion3() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(IRI.create(URI.create("hasvalue"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))), factory.getOWLLiteral(23));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("23"));
	}

	public void testDataPropertyAssertion4() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(IRI.create(URI.create("hasvalue"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))), factory.getOWLLiteral(true));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("true"));
	}

	public void testDataPropertyAssertion5() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(IRI.create(URI.create("hasvalue"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))), factory.getOWLLiteral((double) 2.34));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("2.34"));
	}

	public void testDataPropertyAssertion6() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(IRI.create(URI.create("hasvalue"))),
				factory.getOWLNamedIndividual(IRI.create(URI.create("a"))), factory.getOWLLiteral((float) 2.34));
		Assertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof DataPropertyAssertion);
		DataPropertyAssertion r = (DataPropertyAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("2.34"));
	}
}
