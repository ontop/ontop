package it.unibz.inf.ontop.owlrefplatform.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.utils.MappingVocabularyExtractor;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlapi.QuestOWLIndividualAxiomIterator;
import it.unibz.inf.ontop.owlrefplatform.core.abox.QuestMaterializer;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * TODO: refactor (remove the exceptions from the constructors and create a Result object)
 */
public class OWLAPIMaterializer implements AutoCloseable {

	private final Iterator<Assertion> assertions;
	private final QuestMaterializer materializer;

	public OWLAPIMaterializer(QuestConfiguration configuration, boolean doStreamResults) throws Exception {
		Ontology tbox = extractTBox(configuration);
		materializer = new QuestMaterializer(configuration, tbox, doStreamResults);
		assertions = materializer.getAssertionIterator();
	}

	/**
 	 * Only materializes the predicates in `predicates`
  	 */
	public OWLAPIMaterializer(QuestConfiguration configuration, ImmutableSet<Predicate> selectedVocabulary, boolean doStreamResults) throws Exception {
		materializer = new QuestMaterializer(configuration, extractTBox(configuration), selectedVocabulary, doStreamResults);
		assertions = materializer.getAssertionIterator();
	}

	public OWLAPIMaterializer(QuestConfiguration configuration, Predicate selectedPredicate, boolean doStreamResults) throws Exception {
		this(configuration, ImmutableSet.of(selectedPredicate), doStreamResults);
	}

	public QuestOWLIndividualAxiomIterator getIterator() {
		return new QuestOWLIndividualAxiomIterator(assertions);
	}
	
	public void disconnect() {
		materializer.disconnect();
	}
	
	public long getTriplesCount() { 
		try {
			return materializer.getTriplesCount();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getVocabularySize() {
		return materializer.getVocabSize();
	}

    @Override
    public void close() throws Exception {
        disconnect();
    }

	private static Ontology extractTBox(QuestConfiguration configuration) throws OWLOntologyCreationException,
			IOException, InvalidMappingException {

		Optional<Ontology> inputOntology =  configuration.loadInputOntology()
				.map(OWLAPITranslatorUtility::translate);

		if (inputOntology.isPresent())
			return inputOntology.get();

		return MappingVocabularyExtractor.extractOntology(configuration.loadProvidedMapping());
	}
}
