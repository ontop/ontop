package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
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
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OWLAPI3IndividualTranslator {

	private OWLDataFactory dataFactory = null;

	public OWLAPI3IndividualTranslator() {
		dataFactory = new OWLDataFactoryImpl();
	}

	public OWLIndividualAxiom translate(Assertion assertion) {

		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;

			IRI conceptIRI = IRI.create(ca.getConcept().getName());
			IRI individualIRI = IRI.create(ca.getObject().getURI());

			OWLClass description = dataFactory.getOWLClass(conceptIRI);
			OWLIndividual individual = dataFactory.getOWLNamedIndividual(individualIRI);

			return dataFactory.getOWLClassAssertionAxiom(description, individual);
		} else if (assertion instanceof ObjectPropertyAssertion) {
			ObjectPropertyAssertion opa = (ObjectPropertyAssertion) assertion;

			IRI roleIRI = IRI.create(opa.getRole().getName());
			IRI subjectIRI = IRI.create(opa.getFirstObject().getURI());
			IRI objectIRI = IRI.create(opa.getSecondObject().getURI());

			OWLObjectProperty property = dataFactory.getOWLObjectProperty(roleIRI);
			OWLIndividual subject = dataFactory.getOWLNamedIndividual(subjectIRI);
			OWLIndividual object = dataFactory.getOWLNamedIndividual(objectIRI);

			return dataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
		} else if (assertion instanceof DataPropertyAssertion) {
			DataPropertyAssertion dpa = (DataPropertyAssertion) assertion;

			IRI attributeIRI = IRI.create(dpa.getAttribute().getName());
			IRI subjectIRI = IRI.create(dpa.getObject().getURI());

			OWLDataProperty property = dataFactory.getOWLDataProperty(attributeIRI);
			OWLIndividual subject = dataFactory.getOWLNamedIndividual(subjectIRI);
			String value = dpa.getValue().getValue();

			return dataFactory.getOWLDataPropertyAssertionAxiom(property, subject, value);
		}
		return null;
	}

	/***
	 * Translates from assertion objects into
	 * 
	 * @param constant
	 * @return
	 */
	public OWLPropertyAssertionObject translate(Constant constant) {
		OWLPropertyAssertionObject result = null;
		if (constant instanceof URIConstant) {
			result = dataFactory.getOWLNamedIndividual(IRI.create(((URIConstant) constant).getURI()));
		} else if (constant instanceof BNode) {
			result = dataFactory.getOWLAnonymousIndividual(((BNode) constant).getName());
		} else if (constant instanceof ValueConstant) {
			ValueConstant v = (ValueConstant) constant;
			if (v.getType() == COL_TYPE.BOOLEAN) {
				result = dataFactory.getOWLLiteral(v.getValue(), OWL2Datatype.XSD_BOOLEAN);
			} else if (v.getType() == COL_TYPE.DATETIME) {
				result = dataFactory.getOWLLiteral(v.getValue(), OWL2Datatype.XSD_DATE_TIME);
			} else if (v.getType() == COL_TYPE.DECIMAL) {
				result = dataFactory.getOWLLiteral(v.getValue(), OWL2Datatype.XSD_DECIMAL);
			} else if (v.getType() == COL_TYPE.DOUBLE) {
				result = dataFactory.getOWLLiteral(v.getValue(), OWL2Datatype.XSD_DOUBLE);
			} else if (v.getType() == COL_TYPE.INTEGER) {
				result = dataFactory.getOWLLiteral(v.getValue(), OWL2Datatype.XSD_INTEGER);
			} else if (v.getType() == COL_TYPE.LITERAL) {
				if (v.getLanguage() == null || !v.getLanguage().equals("")) {
					result = dataFactory.getOWLLiteral(v.getValue(), v.getLanguage());
				} else {
					result = dataFactory.getOWLLiteral(v.getValue(), OWL2Datatype.RDF_PLAIN_LITERAL);
				}
			} else if (v.getType() == COL_TYPE.STRING) {
				result = dataFactory.getOWLLiteral(v.getValue(), OWL2Datatype.XSD_STRING);
			} else {
				throw new IllegalArgumentException(v.getType().toString());
			}
		} else {
			throw new IllegalArgumentException(constant.getClass().toString());
		}
		return result;
	}

}
