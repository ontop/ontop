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

import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Translates an OWLOntology into ontop's internal ontology representation. It
 * does a syntactic approximation of the ontology, dropping anything not supported 
 * by Quest during inference.
 * 
 */
public class OWLAPI3TranslatorUtility {

	private static final Logger log = LoggerFactory.getLogger(OWLAPI3TranslatorUtility.class);

	private static Class<?extends OWLAPI3TranslatorBase> factory = OWLAPI3TranslatorDLLiteA.class;

	public static void setTranslator(Class<?extends OWLAPI3TranslatorBase> factory) {
		OWLAPI3TranslatorUtility.factory = factory;
	}
	
	/***
	 * Load all the ontologies into a single translated merge.
	 * 
	 * @param ontologies
	 * @return
	 */
	public static Ontology mergeTranslateOntologies(Set<OWLOntology> ontologies)   {
		
		// We will keep track of the loaded ontologies and translate the TBox
		// part of them into our internal representation
		
		log.debug("Load ontologies called. Translating ontologies.");
		
		try {
			OWLAPI3TranslatorBase translator = factory.newInstance();
			for (OWLOntology onto : ontologies) 
				translateInto(translator, onto);

			log.debug("Ontology loaded: {}", translator.getOntology());

			return translator.getOntology();
		} catch (InstantiationException e) {
			log.debug("Failed to instantiate the factory {}.", e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (IllegalAccessException e) {
			log.debug("Failed to instantiate the factory {}.", e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	
	
	public static Ontology translate(OWLOntology owl) {
		try {
			OWLAPI3TranslatorBase translator = factory.newInstance();
			translateInto(translator, owl);
			return translator.getOntology();	
		} 
		catch (InstantiationException e) {
			log.debug("Failed to instantiate the factory {}.", e.getMessage());
			throw new RuntimeException(e.getMessage());
		} 
		catch (IllegalAccessException e) {
			log.debug("Failed to instantiate the factory {}.", e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private static void translateInto(OWLAPI3TranslatorBase translator, OWLOntology owl) {

		// add all definitions for classes and roles
					
		for (OWLClass entity : owl.getClassesInSignature()) 
			translator.declare(entity);

		for (OWLObjectProperty prop : owl.getObjectPropertiesInSignature()) 
			translator.declare(prop);
		
		for (OWLDataProperty prop : owl.getDataPropertiesInSignature()) 
			translator.declare(prop);

		// process all axioms
		
		for (OWLAxiom axiom : owl.getAxioms()) 
			axiom.accept(translator);
	}
}
