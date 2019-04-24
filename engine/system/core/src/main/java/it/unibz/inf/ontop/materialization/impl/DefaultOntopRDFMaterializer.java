package it.unibz.inf.ontop.materialization.impl;

/*
 * #%L
 * ontop-reformulation-core
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Allows you to materialize the virtual RDF graph of an OBDA specification.
 * 
 * @author Mariano Rodriguez Muro (initial version was called QuestMaterializer)
 * 
 */
public class DefaultOntopRDFMaterializer implements OntopRDFMaterializer {

	private static int FETCH_SIZE = 50000;
	private final MaterializationParams params;
	private final InputQueryFactory inputQueryFactory;
	private final OntopQueryEngine queryEngine;

	private final ImmutableMap<IRI, VocabularyEntry> vocabulary;

	private static final class VocabularyEntry {
        private final IRI name;
        private final int arity;

        VocabularyEntry(IRI predicate, int arity) {

                this.name =predicate;

            	this.arity = arity;
        }

        private static final String PROPERTY_QUERY = "CONSTRUCT {?s <%s> ?o} WHERE {?s <%s> ?o}";
        private static final String CLASS_QUERY = "CONSTRUCT {?s a <%s>} WHERE {?s a <%s>}";

        String getQuery() {
            return String.format((arity == 1) ? CLASS_QUERY : PROPERTY_QUERY, name.getIRIString(), name.getIRIString());
        }
    }

	public DefaultOntopRDFMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
		Injector injector = configuration.getInjector();
		OntopSystemFactory engineFactory = injector.getInstance(OntopSystemFactory.class);
		OBDASpecification specification = configuration.loadSpecification();
		this.queryEngine = engineFactory.create(specification, configuration.getExecutorRegistry());
		this.inputQueryFactory = injector.getInstance(InputQueryFactory.class);
		this.vocabulary = extractVocabulary(specification.getSaturatedMapping());
		this.params = materializationParams;
	}

	@Override
	public MaterializedGraphResultSet materialize() {
		return new DefaultMaterializedGraphResultSet(vocabulary, params, queryEngine, inputQueryFactory);
	}

	@Override
	public MaterializedGraphResultSet materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary) {
		return new DefaultMaterializedGraphResultSet(filterVocabularyEntries(selectedVocabulary), params, queryEngine, inputQueryFactory);
	}

	private ImmutableMap<IRI,VocabularyEntry> filterVocabularyEntries(ImmutableSet<IRI> selectedVocabulary) {
		return vocabulary.entrySet().stream()
				.filter(e -> selectedVocabulary.contains(e.getKey()))
				.collect(ImmutableCollectors.toMap());
	}

	@Override
	public ImmutableSet<IRI> getClasses() {
		return vocabulary.entrySet().stream()
				.filter(e -> e.getValue().arity == 1)
				.map(Map.Entry::getKey)
				.collect(ImmutableCollectors.toSet());
	}

	@Override
	public ImmutableSet<IRI> getProperties() {
		return vocabulary.entrySet().stream()
				.filter(e -> e.getValue().arity == 2)
				.map(Map.Entry::getKey)
				.collect(ImmutableCollectors.toSet());
	}

	/**
	 * TODO: refactor so as to work with quads
	 */
	private static ImmutableMap<IRI, VocabularyEntry> extractVocabulary(@Nonnull Mapping mapping) {
		return mapping.getRDFAtomPredicates().stream()
				.filter(p -> p instanceof TriplePredicate)
				.map(p -> (TriplePredicate) p)
				.findFirst()
				.map(p -> extractTripleVocabulary(mapping, p)
						.collect(ImmutableCollectors.toMap(e -> e.name, e -> e)))
				.orElseGet(ImmutableMap::of);
    }

    private static Stream<VocabularyEntry> extractTripleVocabulary(Mapping mapping, TriplePredicate triplePredicate) {
		Stream<VocabularyEntry> vocabularyPropertyStream = mapping.getRDFProperties(triplePredicate).stream()
				.map(p -> new VocabularyEntry(p, 2));

		Stream<VocabularyEntry> vocabularyClassStream = mapping.getRDFClasses(triplePredicate).stream()
				.map(p -> new VocabularyEntry(p, 1));

		return Stream.concat(vocabularyClassStream,vocabularyPropertyStream);
	}


	private static class DefaultMaterializedGraphResultSet implements MaterializedGraphResultSet {

		private final ImmutableMap<IRI, VocabularyEntry> vocabulary;
		private final InputQueryFactory inputQueryFactory;
		private final boolean doStreamResults, canBeIncomplete;

		private final OntopQueryEngine queryEngine;
		private final UnmodifiableIterator<VocabularyEntry> vocabularyIterator;

		private int counter;
		@Nullable
		private OntopConnection ontopConnection;
		@Nullable
		private OntopStatement tmpStatement;
		@Nullable
		private SimpleGraphResultSet tmpGraphResultSet;
		@Nullable
//		private Assertion nextAssertion;

		private Logger LOGGER = LoggerFactory.getLogger(DefaultMaterializedGraphResultSet.class);
		private final List<IRI> possiblyIncompleteClassesAndProperties;


		DefaultMaterializedGraphResultSet(ImmutableMap<IRI, VocabularyEntry> vocabulary, MaterializationParams params,
										  OntopQueryEngine queryEngine, InputQueryFactory inputQueryFactory) {

			this.vocabulary = vocabulary;
			this.vocabularyIterator = vocabulary.values().iterator();

			this.queryEngine = queryEngine;
			this.doStreamResults = params.isDBResultStreamingEnabled();
			this.canBeIncomplete = params.canMaterializationBeIncomplete();
			this.inputQueryFactory = inputQueryFactory;
			this.possiblyIncompleteClassesAndProperties = new ArrayList<>();

			if (doStreamResults) {
				// Autocommit must be OFF (needed for autocommit)
				//ontopConnection.setAutoCommit(false);
			}

			counter = 0;
			// Lately initiated
			ontopConnection = null;
			tmpStatement = null;
			tmpGraphResultSet = null;
//			nextAssertion = null;
		}

		@Override
		public ImmutableSet<IRI> getSelectedVocabulary() {
			return vocabulary.keySet();
		}


		@Override
		public boolean hasNext() throws OntopQueryAnsweringException, OntopConnectionException {
			// Initialization
			if (ontopConnection == null)
				ontopConnection = queryEngine.getConnection();

			if ((tmpGraphResultSet != null) && tmpGraphResultSet.hasNext()) {

				// Two times in a row (can be tolerated)
//				if (nextAssertion != null)
//					return true;

//				nextAssertion = tmpGraphResultSet.next();
				return true;
			}

			while(vocabularyIterator.hasNext()) {
				/*
			 	* Closes the previous result set and statement (if open)
			 	*/
				if (tmpGraphResultSet != null) {
					try {
						tmpGraphResultSet.close();
					} catch (OntopConnectionException e) {
						LOGGER.warn("Non-critical exception while closing the graph result set: " + e);
						// Not critical, continue
					}
				}
				if (tmpStatement != null) {
					try {
						tmpStatement.close();
					} catch (OntopConnectionException e) {
						LOGGER.warn("Non-critical exception while closing the statement: " + e);
						// Not critical, continue
					}
				}

				/*
				 * New query for the next RDF property/class
				 */
                VocabularyEntry predicate = vocabularyIterator.next();
				ConstructQuery query = inputQueryFactory.createConstructQuery(predicate.getQuery());

				try {
					tmpStatement = ontopConnection.createStatement();
					if (doStreamResults) {
						tmpStatement.setFetchSize(FETCH_SIZE);
					}
					tmpGraphResultSet = tmpStatement.execute(query);

					if (tmpGraphResultSet.hasNext()) {
//						nextAssertion = tmpGraphResultSet.next();
						return true;
					}
				} catch (OntopQueryAnsweringException | OntopConnectionException e) {
					if (canBeIncomplete) {
						LOGGER.warn("Possibly incomplete class/property " + predicate + " (materialization problem).\n"
								+ "Details: " + e);
						possiblyIncompleteClassesAndProperties.add(predicate.name);
					}
					else {
						LOGGER.error("Problem materialiing the class/property " + predicate);
						throw e;
					}
				}
			}

			return false;
		}

		@Override
		public Assertion next() throws OntopQueryAnsweringException {
			counter++;
			try {
				return tmpGraphResultSet.next();
			} catch (OntopResultConversionException e) {
			    throw new OntopQueryAnsweringException(e);
			}
		}

		/**
		 * Releases all the connection resources
		 */
		public void close() throws OntopConnectionException {
			if (tmpStatement != null) {
				tmpStatement.close();
			}
			if (ontopConnection != null) {
				ontopConnection.close();
			}
		}

		public long getTripleCountSoFar() {
			return counter;
		}

		public ImmutableList<IRI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar() {
			return ImmutableList.copyOf(possiblyIncompleteClassesAndProperties);
		}
	}


	private static class NonURIPredicateInVocabularyException extends OntopInternalBugException {

		NonURIPredicateInVocabularyException(String vocabularyPredicate) {
			super("A non-URI predicate has been found in the vocabulary: " + vocabularyPredicate);
		}
	}
}
