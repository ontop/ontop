package it.unibz.krdb.obda.owlapi3;

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

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
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

	private final OWLDataFactory dataFactory;

	public OWLAPI3IndividualTranslator() {
		dataFactory = new OWLDataFactoryImpl();
	}

	public OWLIndividualAxiom translate(ClassAssertion ca) {
		IRI conceptIRI = IRI.create(ca.getConcept().getPredicate().getName().toString());

		OWLClass description = dataFactory.getOWLClass(conceptIRI);
		OWLIndividual object = translate(ca.getIndividual());
		return dataFactory.getOWLClassAssertionAxiom(description, object);
	} 
	
	public OWLIndividualAxiom translate(ObjectPropertyAssertion opa) {
		IRI roleIRI = IRI.create(opa.getProperty().getPredicate().getName().toString());

		OWLObjectProperty property = dataFactory.getOWLObjectProperty(roleIRI);
		OWLIndividual subject = translate(opa.getSubject());
		OWLIndividual object = translate(opa.getObject());
		return dataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);				
	}
	
	public OWLIndividualAxiom translate(DataPropertyAssertion opa) {
		IRI roleIRI = IRI.create(opa.getProperty().getPredicate().getName().toString());

		OWLDataProperty property = dataFactory.getOWLDataProperty(roleIRI);
		OWLIndividual subject = translate(opa.getSubject());
		OWLLiteral object = translate(opa.getValue());
		return dataFactory.getOWLDataPropertyAssertionAxiom(property, subject, object);			
	}

	/***
	 * Translates from assertion objects into
	 * 
	 * @param constant
	 * @return
	 */
	public OWLIndividual translate(ObjectConstant constant) {
		if (constant instanceof URIConstant)
			return dataFactory.getOWLNamedIndividual(IRI.create(constant.getValue()));		

		else /*if (constant instanceof BNode)*/ 
			return dataFactory.getOWLAnonymousIndividual(((BNode) constant).getName());
	}
	
	public OWLLiteral translate(ValueConstant v) {
		OWLLiteral result = null;
		if (v == null)
			return null;
		
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
		return result;
	}
}
