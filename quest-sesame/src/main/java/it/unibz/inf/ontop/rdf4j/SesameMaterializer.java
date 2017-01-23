package it.unibz.inf.ontop.rdf4j;

/*
 * #%L
 * ontop-quest-sesame
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

import com.google.common.collect.Iterators;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.utils.MappingVocabularyExtractor;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.abox.QuestMaterializer;
import org.eclipse.rdf4j.model.Statement;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * TODO: refactor (no exception in the constructor) + complete
 */
public class SesameMaterializer {
	
		private Iterator<Assertion> assertions = null;
		private QuestMaterializer materializer;
		
    public SesameMaterializer(QuestConfiguration configuration, boolean doStreamResults) throws Exception {
        materializer = new QuestMaterializer(configuration, extractOntology(configuration), doStreamResults);
        assertions = materializer.getAssertionIterator();
	}
		
		public Iterator<Statement> getIterator() {
            return Iterators.transform(assertions, RDF4JHelper::createStatement);
		}
		
		public void disconnect() {
			materializer.disconnect();
		}
		
    public long getTriplesCount() {
        try {
			return materializer.getTriplesCount();
		} catch (Exception e) {
			e.printStackTrace();
        }
        return -1;
		}
	
		public int getVocabularySize() {
			return materializer.getVocabSize();
		}

	private static Ontology extractOntology(QuestConfiguration configuration) throws OWLOntologyCreationException,
			IOException, InvalidMappingException {

		Optional<Ontology> inputOntology =  configuration.loadInputOntology()
				.map(OWLAPITranslatorUtility::translate);

		if (inputOntology.isPresent())
			return inputOntology.get();

		return MappingVocabularyExtractor.extractOntology(configuration.loadProvidedSpecification());
	}
}
