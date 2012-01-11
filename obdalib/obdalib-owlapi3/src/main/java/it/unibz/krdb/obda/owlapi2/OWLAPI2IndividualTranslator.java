package it.unibz.krdb.obda.owlapi2;

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OWLAPI2IndividualTranslator {

	private OWLDataFactory dataFactory = null;
	
	public OWLAPI2IndividualTranslator() {
		dataFactory = new OWLDataFactoryImpl();
	}
	
	public OWLIndividualAxiom translate(Assertion assertion) {
		
		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion)assertion;
			
			IRI conceptIRI = IRI.create(ca.getConcept().getName());
			IRI individualIRI = IRI.create(ca.getObject().getURI());

			OWLClass description = dataFactory.getOWLClass(conceptIRI);
			OWLIndividual individual = dataFactory.getOWLNamedIndividual(individualIRI);
			
			return dataFactory.getOWLClassAssertionAxiom(description,individual);
		}
		else if (assertion instanceof ObjectPropertyAssertion) {
			ObjectPropertyAssertion opa = (ObjectPropertyAssertion)assertion;
			
			IRI roleIRI = IRI.create(opa.getRole().getName());
			IRI subjectIRI = IRI.create(opa.getFirstObject().getURI());
			IRI objectIRI = IRI.create(opa.getSecondObject().getURI());
			
			OWLObjectProperty property = dataFactory.getOWLObjectProperty(roleIRI);
			OWLIndividual subject = dataFactory.getOWLNamedIndividual(subjectIRI);
			OWLIndividual object = dataFactory.getOWLNamedIndividual(objectIRI);
					
			return dataFactory.getOWLObjectPropertyAssertionAxiom(property,subject,object);
		}
		else if (assertion instanceof DataPropertyAssertion) {
			DataPropertyAssertion dpa = (DataPropertyAssertion)assertion;
			
			IRI attributeIRI = IRI.create(dpa.getAttribute().getName());
			IRI subjectIRI = IRI.create(dpa.getObject().getURI());
			
			OWLDataProperty property = dataFactory.getOWLDataProperty(attributeIRI);
			OWLIndividual subject = dataFactory.getOWLNamedIndividual(subjectIRI);
			String value = dpa.getValue().getValue();
			
			return dataFactory.getOWLDataPropertyAssertionAxiom(property, subject, value);
		}
		return null;
	}
	
}
