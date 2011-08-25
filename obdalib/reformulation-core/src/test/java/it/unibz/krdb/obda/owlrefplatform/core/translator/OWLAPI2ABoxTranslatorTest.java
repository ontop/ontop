package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.ABoxAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AttributeABoxAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptABoxAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleABoxAssertion;

import java.net.URI;

import junit.framework.TestCase;

import org.junit.Before;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLAPI2ABoxTranslatorTest extends TestCase {

	OWLOntologyManager	manager		= OWLManager.createOWLOntologyManager();
	OWLDataFactory		factory		= manager.getOWLDataFactory();
	OWLAPI2Translator	translator	= new OWLAPI2Translator();

	@Before
	public void setUp() throws Exception {
	}

	public void testClassAssertion() {
		OWLIndividualAxiom a1 = factory.getOWLClassAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLClass(URI.create("person")));
		ABoxAssertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof ConceptABoxAssertion);
		ConceptABoxAssertion c = (ConceptABoxAssertion) a2;
		assertTrue(c.getConcept().getArity() == 1);
		assertTrue(c.getConcept().getName().toString().equals("person"));
		assertTrue(c.getObject().getURI().toString().equals("a"));

	}

	public void testObjectPropertyAssertion() {
		OWLIndividualAxiom a1 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLObjectProperty(URI.create("hasfather")), factory.getOWLIndividual(URI.create("b")));
		ABoxAssertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof RoleABoxAssertion);
		RoleABoxAssertion r = (RoleABoxAssertion) a2;
		assertTrue(r.getRole().getArity() == 2);
		assertTrue(r.getRole().getName().toString().equals("hasfather"));
		assertTrue(r.getFirstObject().getURI().toString().equals("a"));
		assertTrue(r.getSecondObject().getURI().toString().equals("b"));
	}

	public void testDataPropertyAssertion1() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLUntypedConstant("23"));
		ABoxAssertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof AttributeABoxAssertion);
		AttributeABoxAssertion r = (AttributeABoxAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().equals("23"));
	}

	public void testDataPropertyAssertion2() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant("23"));
		ABoxAssertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof AttributeABoxAssertion);
		AttributeABoxAssertion r = (AttributeABoxAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("23"));
	}

	public void testDataPropertyAssertion3() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant(23));
		ABoxAssertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof AttributeABoxAssertion);
		AttributeABoxAssertion r = (AttributeABoxAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("23"));
	}

	public void testDataPropertyAssertion4() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant(true));
	    ABoxAssertion a2 = translator.translate(a1);
	    
	    assertTrue(a2 instanceof AttributeABoxAssertion);
	    AttributeABoxAssertion r = (AttributeABoxAssertion) a2;
	    assertTrue(r.getAttribute().getArity() == 2);
	    assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
	    assertTrue(r.getObject().getURI().toString().equals("a"));
	    assertTrue(r.getValue().getValue().toString().equals("true"));
	}

	public void testDataPropertyAssertion5() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant((double)2.34));
	    ABoxAssertion a2 = translator.translate(a1);
	    
	    assertTrue(a2 instanceof AttributeABoxAssertion);
	    AttributeABoxAssertion r = (AttributeABoxAssertion) a2;
	    assertTrue(r.getAttribute().getArity() == 2);
	    assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
	    assertTrue(r.getObject().getURI().toString().equals("a"));
	    assertTrue(r.getValue().getValue().toString().equals("2.34"));
	}

	public void testDataPropertyAssertion6() {
		OWLIndividualAxiom a1 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLIndividual(URI.create("a")),
				factory.getOWLDataProperty(URI.create("hasvalue")), factory.getOWLTypedConstant((float) 2.34));
		ABoxAssertion a2 = translator.translate(a1);

		assertTrue(a2 instanceof AttributeABoxAssertion);
		AttributeABoxAssertion r = (AttributeABoxAssertion) a2;
		assertTrue(r.getAttribute().getArity() == 2);
		assertTrue(r.getAttribute().getName().toString().equals("hasvalue"));
		assertTrue(r.getObject().getURI().toString().equals("a"));
		assertTrue(r.getValue().getValue().toString().equals("2.34"));
	}
}
