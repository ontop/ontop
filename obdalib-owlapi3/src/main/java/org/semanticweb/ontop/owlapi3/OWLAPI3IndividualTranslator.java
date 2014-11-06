package org.semanticweb.ontop.owlapi3;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import org.semanticweb.ontop.model.BNode;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.PropertyAssertion;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/***
 * Translates a ontop ABox assertion into an OWLIndividualAxiom. Used in the
 * result sets.
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */
public class OWLAPI3IndividualTranslator {

	private OWLDataFactory dataFactory = null;

	public OWLAPI3IndividualTranslator() {
		dataFactory = new OWLDataFactoryImpl();
	}

	public OWLIndividualAxiom translate(Assertion assertion) {

		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;
			IRI conceptIRI = IRI.create(ca.getConcept().getPredicate().getName().toString());

			OWLClass description = dataFactory.getOWLClass(conceptIRI);
			OWLIndividual object = (OWLNamedIndividual) translate(ca.getIndividual());

			return dataFactory.getOWLClassAssertionAxiom(description, object);

		} else if (assertion instanceof PropertyAssertion) {
			PropertyAssertion opa = (PropertyAssertion) assertion;
			IRI roleIRI = IRI.create(opa.getProperty().getPredicate().getName().toString());

			OWLIndividual subject = (OWLNamedIndividual) translate(opa.getSubject());
			if (opa.getValue2() instanceof ValueConstant) {
				OWLDataProperty property = dataFactory.getOWLDataProperty(roleIRI);
				OWLLiteral object = (OWLLiteral) translate(opa.getValue2());
				return dataFactory.getOWLDataPropertyAssertionAxiom(property, subject, object);			
			}
			else {
				OWLObjectProperty property = dataFactory.getOWLObjectProperty(roleIRI);
				OWLIndividual object = (OWLNamedIndividual) translate(opa.getValue2());
				return dataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);				
			}

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

			result = dataFactory.getOWLNamedIndividual(IRI.create(constant.getValue()));
		} else if (constant instanceof BNode) {
			result = dataFactory.getOWLAnonymousIndividual(((BNode) constant).getName());
		} else if (constant instanceof ValueConstant) {
			ValueConstant v = (ValueConstant) constant;

			String value = v.getValue();
			if (value == null) {
				result = null;
			} else if (v.getType() == COL_TYPE.BOOLEAN) {
				result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_BOOLEAN);
			} else if (v.getType() == COL_TYPE.DATETIME) {
				result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_DATE_TIME);
            } else if (v.getType() == COL_TYPE.DATE) {
                //result = dataFactory.getOWLLiteral(value, this.dataFactory.getOWLDatatype(IRI.create(OBDAVocabulary.XSD_DATE_URI)));
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.RDF_PLAIN_LITERAL);
            }else if (v.getType() == COL_TYPE.TIME) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.RDF_PLAIN_LITERAL);
            } else if (v.getType() == COL_TYPE.YEAR) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.RDF_PLAIN_LITERAL);
			} else if (v.getType() == COL_TYPE.DECIMAL) {
				result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_DECIMAL);
			} else if (v.getType() == COL_TYPE.DOUBLE) {
				result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_DOUBLE);
			} else if (v.getType() == COL_TYPE.INTEGER) {
				result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_INTEGER);
            } else if (v.getType() == COL_TYPE.NEGATIVE_INTEGER) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_NEGATIVE_INTEGER);
            } else if (v.getType() == COL_TYPE.NON_NEGATIVE_INTEGER) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
            } else if (v.getType() == COL_TYPE.POSITIVE_INTEGER) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_POSITIVE_INTEGER);
            } else if (v.getType() == COL_TYPE.NON_POSITIVE_INTEGER) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_NON_POSITIVE_INTEGER);
            } else if (v.getType() == COL_TYPE.INT) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_INT);
            } else if (v.getType() == COL_TYPE.UNSIGNED_INT) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_UNSIGNED_INT);
            } else if (v.getType() == COL_TYPE.FLOAT) {
                result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_FLOAT);
			} else if (v.getType() == COL_TYPE.LONG) {
                 result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_LONG);
            } else if (v.getType() == COL_TYPE.LITERAL) {
				result = dataFactory.getOWLLiteral(value, OWL2Datatype.RDF_PLAIN_LITERAL);
			} else if (v.getType() == COL_TYPE.LITERAL_LANG) {
				result = dataFactory.getOWLLiteral(value, v.getLanguage());
			} else if (v.getType() == COL_TYPE.STRING) {
				result = dataFactory.getOWLLiteral(value, OWL2Datatype.XSD_STRING);
			} else {
				throw new IllegalArgumentException(v.getType().toString());
			}
		} else if (constant == null) {
			return null;
		} else {
			throw new IllegalArgumentException(constant.getClass().toString());
		}
		return result;
	}
}
