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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Translates an OWLOntology into ontop's internal ontology representation. 
 * It performs a check whether the ontology belongs to OWL 2 QL and reports 
 * all axioms that do not belong to the profile.
 * 
 * @author Roman Kontchakov
 * 
 */
public class OWLAPI3TranslatorUtility {

	private static final Logger log = LoggerFactory.getLogger(OWLAPI3TranslatorUtility.class);

	/***
	 * Load all the imports of the ontology and merges into a single ontop internal representation
	 * 
	 * @param ontologies
	 * @return
	 */
	
	public static Ontology translateImportsClosure(OWLOntology ontology) {
		Set<OWLOntology> clousure = ontology.getOWLOntologyManager().getImportsClosure(ontology);
		return mergeTranslateOntologies(clousure);		
	}
	
	/***
	 * Load all the ontologies into a single translated merge.
	 * 
	 * @param ontologies
	 * @return
	 */
	
	@Deprecated
	public static Ontology mergeTranslateOntologies(Collection<OWLOntology> ontologies)   {
		log.debug("Load ontologies called. Translating {} ontologies.", ontologies.size());

		OWLAPI3TranslatorOWL2QL translator = new OWLAPI3TranslatorOWL2QL(ontologies);
		for (OWLOntology owl : ontologies) {
			translator.setCurrentOWLOntology(owl);
			for (OWLAxiom axiom : owl.getAxioms()) 
				axiom.accept(translator);
		}
		
		log.debug("Ontology loaded: {}", translator.getOntology());

		return translator.getOntology();
	}

	/**
	 * USE FOR TESTS ONLY
	 * 
	 * @param owl
	 * @return
	 */
	
	public static Ontology translate(OWLOntology owl) {
		OWLAPI3TranslatorOWL2QL translator = new OWLAPI3TranslatorOWL2QL(Collections.singleton(owl));
		translator.setCurrentOWLOntology(owl);

		for (OWLAxiom axiom : owl.getAxioms()) 
			axiom.accept(translator);
		return translator.getOntology();	
	}
	
	/**
	 * USE FOR TESTS ONLY
	 * 
	 * @param filename
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	
	public static Ontology loadOntologyFromFile(String filename) throws OWLOntologyCreationException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owl = man.loadOntologyFromOntologyDocument(new File(filename));
		OWLAPI3TranslatorOWL2QL translator = new OWLAPI3TranslatorOWL2QL(Collections.singleton(owl));
		translator.setCurrentOWLOntology(owl);

		for (OWLAxiom axiom : owl.getAxioms()) 
			axiom.accept(translator);
	
		return translator.getOntology();	
	}
	
}
