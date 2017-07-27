package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-obdalib-owlapi
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

import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.ontology.AnnotationAssertion;
import it.unibz.inf.ontop.ontology.ClassAssertion;
import it.unibz.inf.ontop.ontology.DataPropertyAssertion;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE;
import it.unibz.inf.ontop.ontology.ObjectPropertyAssertion;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/***
 * Translates a ontop ABox assertion into an OWLIndividualAxiom. Used in the
 * result sets.
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */
public class OWLAPIIndividualTranslator {

	private final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	public OWLIndividualAxiom translate(ClassAssertion ca) {
		IRI conceptIRI = IRI.create(ca.getConcept().getName());

		OWLClass description = dataFactory.getOWLClass(conceptIRI);
		OWLIndividual object = translate(ca.getIndividual());
		return dataFactory.getOWLClassAssertionAxiom(description, object);
	} 
	
	public OWLIndividualAxiom translate(ObjectPropertyAssertion opa) {
		IRI roleIRI = IRI.create(opa.getProperty().getName());

		OWLObjectProperty property = dataFactory.getOWLObjectProperty(roleIRI);
		OWLIndividual subject = translate(opa.getSubject());
		OWLIndividual object = translate(opa.getObject());
		return dataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);				
	}
	
	public OWLIndividualAxiom translate(DataPropertyAssertion opa) {
		IRI roleIRI = IRI.create(opa.getProperty().getName());

		OWLDataProperty property = dataFactory.getOWLDataProperty(roleIRI);
		OWLIndividual subject = translate(opa.getSubject());
		OWLLiteral object = translate(opa.getValue());
		return dataFactory.getOWLDataPropertyAssertionAxiom(property, subject, object);			
	}

	public OWLAnnotationAssertionAxiom translate(AnnotationAssertion opa) {
		IRI roleIRI = IRI.create(opa.getProperty().getName());

		OWLAnnotationProperty property = dataFactory.getOWLAnnotationProperty(roleIRI);
		OWLAnnotationSubject subject = translateAnnotationSubject(opa.getSubject());
		OWLAnnotationValue object = translateAnnotationValue(opa.getValue());
		return dataFactory.getOWLAnnotationAssertionAxiom(property, subject, object);
	}

	/***
	 * Translates from assertion objects into
	 * 
	 * @param constant
	 * @return
	 */
	public OWLIndividual translate(ObjectConstant constant) {
		if (constant instanceof URIConstant)
			return dataFactory.getOWLNamedIndividual(IRI.create(((URIConstant)constant).getURI()));		

		else /*if (constant instanceof BNode)*/ 
			return dataFactory.getOWLAnonymousIndividual(((BNode) constant).getName());
	}
	
	public OWLLiteral translate(ValueConstant v) {
		if (v == null)
			return null;
		
		String value = v.getValue();
		if (value == null) {
			return null;
		} 
		else if (v.getType() == COL_TYPE.LITERAL_LANG) {
			return dataFactory.getOWLLiteral(value, v.getLanguage());
		} 
		else {
			OWL2Datatype datatype = OWLTypeMapper.getOWLType(v.getType());
			if (datatype != null)
				return dataFactory.getOWLLiteral(value, datatype);
			else 
				throw new IllegalArgumentException(v.getType().toString());
		}
	}

	public OWLAnnotationSubject translateAnnotationSubject(ObjectConstant subject) {
		if (subject instanceof URIConstant)
			return IRI.create(((URIConstant) subject).getURI());
		else if (subject instanceof BNode)
			return dataFactory.getOWLAnonymousIndividual(((BNode) subject).getName());
		else
			throw new UnexceptedAssertionTermException(subject);

	}

	public OWLAnnotationValue translateAnnotationValue(Constant constant) {
		if (constant instanceof ValueConstant)
			return translate((ValueConstant) constant);
		else if (constant instanceof URIConstant)
			return IRI.create(((URIConstant) constant).getURI());
		else if (constant instanceof BNode)
			return dataFactory.getOWLAnonymousIndividual(((BNode) constant).getName());
		else
			throw new UnexceptedAssertionTermException(constant);
	}



	private static class UnexceptedAssertionTermException extends OntopInternalBugException {
		UnexceptedAssertionTermException(Term term) {
			super("Unexpected term in an assertion (cannot be converted to OWLAPI): " + term);
		}
	}
}
