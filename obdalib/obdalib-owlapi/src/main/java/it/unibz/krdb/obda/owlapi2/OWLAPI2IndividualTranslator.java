package it.unibz.krdb.obda.owlapi2;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ObjectPropertyAssertion;

import java.net.URI;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.OWLDataFactoryImpl;

public class OWLAPI2IndividualTranslator {

	private OWLDataFactory dataFactory = null;
	
	public OWLAPI2IndividualTranslator() {
		dataFactory = new OWLDataFactoryImpl();
	}
	
	public OWLIndividualAxiom translate(Assertion assertion) {
		
		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion)assertion;
			
			URI conceptUri = ca.getConcept().getName();
			URI individualUri = ca.getObject().getURI();

			OWLClass description = dataFactory.getOWLClass(conceptUri);
			OWLIndividual individual = dataFactory.getOWLIndividual(individualUri);
			
			return dataFactory.getOWLClassAssertionAxiom(individual, description);
		}
		else if (assertion instanceof ObjectPropertyAssertion) {
			ObjectPropertyAssertion opa = (ObjectPropertyAssertion)assertion;
			
			URI roleUri = opa.getRole().getName();
			URI subjectUri = opa.getFirstObject().getURI();
			URI objectUri = opa.getSecondObject().getURI();
			
			OWLObjectProperty property = dataFactory.getOWLObjectProperty(roleUri);
			OWLIndividual subject = dataFactory.getOWLIndividual(subjectUri);
			OWLIndividual object = dataFactory.getOWLIndividual(objectUri);
					
			return dataFactory.getOWLObjectPropertyAssertionAxiom(subject, property, object);
		}
		else if (assertion instanceof DataPropertyAssertion) {
			DataPropertyAssertion dpa = (DataPropertyAssertion)assertion;
			
			URI attributeUri = dpa.getAttribute().getName();
			URI subjectUri = dpa.getObject().getURI();
			
			OWLDataProperty property = dataFactory.getOWLDataProperty(attributeUri);
			OWLIndividual subject = dataFactory.getOWLIndividual(subjectUri);
			String value = dpa.getValue().getValue();
			
			return dataFactory.getOWLDataPropertyAssertionAxiom(subject, property, value);
		}
		return null;
	}
	
}
