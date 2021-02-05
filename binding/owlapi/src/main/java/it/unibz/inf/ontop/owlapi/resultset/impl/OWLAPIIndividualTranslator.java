package it.unibz.inf.ontop.owlapi.resultset.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.spec.ontology.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;


/***
 * Translates a ontop ABox assertion into an OWLIndividualAxiom. Used in the
 * result sets.
 *
 * Treats assertions opportunistically as data or object properties, given the type of the object.
 * Has no information whether the property is actually an annotation property or not (not provided by SPARQL).
 * 
 */
public class OWLAPIIndividualTranslator {

	private final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	/**
	 * Distinguishes class assertions from property assertions.
	 *
	 * As we are not having access to the distinction between object/data/annotation OWL properties,
	 * does some approximation: if the property is used like an object property, then declare it as such.
	 * Otherwise, declares it as a data property.
	 */
	public OWLAxiom translate(RDFFact assertion, byte[] salt) {
		IRIConstant factProperty = assertion.getProperty();
		ObjectConstant classOrProperty = assertion.getClassOrProperty();
		OWLIndividual subject = translate(assertion.getSubject(), salt);

		if (assertion.getGraph().isPresent())
			throw new MinorOntopInternalBugException("Quads are not supported by OWLAPI so that method " +
					"should not used with them");

		/*
		 * For regular property assertions and when the object is a b-node (not working fine with OWLAPI)
		 */
		if (factProperty.equals(classOrProperty) || (!(classOrProperty instanceof IRIConstant))) {

			RDFConstant assertionObject = assertion.getObject();
			if (assertionObject instanceof ObjectConstant) {

				OWLObjectProperty property = dataFactory.getOWLObjectProperty(
						IRI.create(factProperty.getIRI().getIRIString()));
				OWLIndividual object = translate((ObjectConstant) assertionObject, salt);
				return dataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
			}
			else {
				OWLDataProperty property = dataFactory.getOWLDataProperty(
						IRI.create(factProperty.getIRI().getIRIString()));
				OWLLiteral literal = translate((RDFLiteralConstant) assertionObject);
				return dataFactory.getOWLDataPropertyAssertionAxiom(property, subject, literal);
			}
		}
		else {
			IRI classIRI = IRI.create(((IRIConstant)classOrProperty).getIRI().getIRIString());

			OWLClass description = dataFactory.getOWLClass(classIRI);
			return dataFactory.getOWLClassAssertionAxiom(description, subject);
		}
	}

	/***
	 * Translates from assertion objects into
	 * 
	 * @param constant
	 * @return
	 */
	public OWLIndividual translate(ObjectConstant constant, byte[] salt) {
		if (constant instanceof IRIConstant)
			return dataFactory.getOWLNamedIndividual(IRI.create(((IRIConstant)constant).getIRI().getIRIString()));

		else /*if (constant instanceof BNode)*/ 
			return dataFactory.getOWLAnonymousIndividual(((BNode) constant).getAnonymizedLabel(salt));
	}
	
	public OWLLiteral translate(RDFLiteralConstant v) {
		if (v == null)
			return null;
		
		String value = v.getValue();
		if (value == null) {
			return null;
		}

		RDFDatatype type = v.getType();
		if (type.getLanguageTag().isPresent()) {
			return dataFactory.getOWLLiteral(value, type.getLanguageTag().get().getFullString());
		} 
		else {
			OWLDatatype owlDatatype = new OWLDatatypeImpl(IRI.create(type.getIRI().getIRIString()));
			return dataFactory.getOWLLiteral(value, owlDatatype);
		}
	}
}
