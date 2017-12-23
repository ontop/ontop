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
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


/***
 * Allows you to materialize the virtual RDF graph of an OBDA specification.
 * 
 * @author Mariano Rodriguez Muro (initial version, was called QuestMaterializer)
 * 
 */
public class DefaultOntopRDFMaterializer implements OntopRDFMaterializer {

	private static int FETCH_SIZE = 50000;

	public DefaultOntopRDFMaterializer() {
	}

	@Override
	public MaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
												  @Nonnull MaterializationParams params)
			throws OBDASpecificationException {
		OBDASpecification obdaSpecification = configuration.loadSpecification();
		ImmutableSet<Predicate> vocabulary = extractVocabulary(obdaSpecification.getSaturatedMapping());
		return apply(obdaSpecification, vocabulary, params, configuration);
	}

	@Override
	public MaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
												  @Nonnull ImmutableSet<URI> selectedVocabulary,
												  @Nonnull MaterializationParams params)
			throws OBDASpecificationException {
		OBDASpecification obdaSpecification = configuration.loadSpecification();

		ImmutableMap<URI, Predicate> vocabularyMap = extractVocabulary(obdaSpecification.getSaturatedMapping()).stream()
				.collect(ImmutableCollectors.toMap(
						DefaultOntopRDFMaterializer::convertIntoURI,
						p -> p));

		ImmutableSet<Predicate> internalVocabulary = selectedVocabulary.stream()
				.filter(vocabularyMap::containsKey)
				.map(vocabularyMap::get)
				.collect(ImmutableCollectors.toSet());

		return apply(obdaSpecification, internalVocabulary, params, configuration);
	}

	private MaterializedGraphResultSet apply(OBDASpecification obdaSpecification, ImmutableSet<Predicate> selectedVocabulary,
											 MaterializationParams params, OntopSystemConfiguration configuration) {

		Injector injector = configuration.getInjector();
		OntopSystemFactory engineFactory = injector.getInstance(OntopSystemFactory.class);
		OntopQueryEngine queryEngine = engineFactory.create(obdaSpecification, configuration.getExecutorRegistry());
		InputQueryFactory inputQueryFactory = injector.getInstance(InputQueryFactory.class);

		return new DefaultMaterializedGraphResultSet(selectedVocabulary, params, queryEngine, inputQueryFactory);
	}

	private static ImmutableSet<Predicate> extractVocabulary(@Nonnull Mapping mapping) {
        Set<Predicate> predicates = new HashSet<>();

        for (AtomPredicate a : mapping.getPredicates()) {
        	if (a.isClass())
        		predicates.add(TERM_FACTORY.getClassPredicate(a.getName()));
        	else if (a.isObjectProperty())
        		predicates.add(TERM_FACTORY.getObjectPropertyPredicate(a.getName()));
        	else if (!a.isTriplePredicate())
        		predicates.add(TERM_FACTORY.getDataPropertyPredicate(a.getName()));
        	//else if (a.isAnnotationProperty())
        	//	predicates.add(TERM_FACTORY.getAnnotationPropertyPredicate(a.getName()));
		}
/*
        // collect all class/data/object predicates to selectedVocabulary
        for (OClass cl : vocabulary.classes()) {
            Predicate p = cl.getPredicate();
            if (!isBuiltin(p))
                predicates.add(p);
        }
        for (ObjectPropertyExpression role : vocabulary.objectProperties()) {
            Predicate p = role.getPredicate();
            if (!isBuiltin(p))
                predicates.add(p);
        }
        for (DataPropertyExpression role : vocabulary.dataProperties()) {
            Predicate p = role.getPredicate();
            if (!isBuiltin(p))
                predicates.add(p);
        }
        for (AnnotationProperty role : vocabulary.annotationProperties()) {
            Predicate p = role.getPredicate();
            if (!isBuiltin(p))
                predicates.add(p);
        }
//        else {
//            //from mapping undeclared predicates (can happen)
//			for (OBDAMappingAxiom axiom : this.model.getMappings()) {
//				List<Function> rule = axiom.getTargetQuery();
//				for (Function f : rule)
//					selectedVocabulary.add(f.getFunctionSymbol());
//			}
//        }
*/
        return ImmutableSet.copyOf(predicates);
    }

    private static boolean isBuiltin(Predicate p) { return p.toString().startsWith(IriConstants.NS_OWL); }

	private static URI convertIntoURI(Predicate vocabularyPredicate) {
		try {
			return new URI(vocabularyPredicate.getName());
		} catch (URISyntaxException e) {
			throw new NonURIPredicateInVocabularyException(vocabularyPredicate);
		}
	}



	private static class DefaultMaterializedGraphResultSet implements MaterializedGraphResultSet {

		private static final String PROPERTY_QUERY = "CONSTRUCT {?s <%s> ?o} WHERE {?s <%s> ?o}";
		private static final String CLASS_QUERY = "CONSTRUCT {?s a <%s>} WHERE {?s a <%s>}";

		private final ImmutableSet<URI> vocabulary;
		private final InputQueryFactory inputQueryFactory;
		private final boolean doStreamResults, canBeIncomplete;

		private final OntopQueryEngine queryEngine;
		private final UnmodifiableIterator<Predicate> vocabularyIterator;

		private int counter;
		@Nullable
		private OntopConnection ontopConnection;
		@Nullable
		private OntopStatement tmpStatement;
		@Nullable
		private SimpleGraphResultSet tmpGraphResultSet;
		@Nullable
		private Assertion nextAssertion;

		private Logger LOGGER = LoggerFactory.getLogger(DefaultMaterializedGraphResultSet.class);
		private final List<URI> possiblyIncompleteClassesAndProperties;


		DefaultMaterializedGraphResultSet(ImmutableSet<Predicate> vocabulary, MaterializationParams params,
										  OntopQueryEngine queryEngine, InputQueryFactory inputQueryFactory) {

			this.vocabulary = vocabulary.stream()
					.map(DefaultOntopRDFMaterializer::convertIntoURI)
					.collect(ImmutableCollectors.toSet());
			this.vocabularyIterator = vocabulary.iterator();

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
			nextAssertion = null;
		}

		@Override
		public ImmutableSet<URI> getSelectedVocabulary() {
			return vocabulary;
		}


		@Override
		public boolean hasNext() throws OntopQueryAnsweringException, OntopConnectionException {
			// Initialization
			if (ontopConnection == null)
				ontopConnection = queryEngine.getConnection();

			if ((tmpGraphResultSet != null) && tmpGraphResultSet.hasNext()) {

				// Two times in a row (can be tolerated)
				if (nextAssertion != null)
					return true;

				nextAssertion = tmpGraphResultSet.next();
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
				Predicate predicate = vocabularyIterator.next();
				ConstructQuery query = getConstructQuery(predicate);

				try {
					tmpStatement = ontopConnection.createStatement();
					if (doStreamResults) {
						tmpStatement.setFetchSize(FETCH_SIZE);
					}
					tmpGraphResultSet = tmpStatement.execute(query);

					if (tmpGraphResultSet.hasNext()) {
						nextAssertion = tmpGraphResultSet.next();
						return true;
					}
				} catch (OntopQueryAnsweringException | OntopConnectionException e) {
					if (canBeIncomplete) {
						LOGGER.warn("Possibly incomplete class/property " + predicate + " (materialization problem).\n"
								+ "Details: " + e);
						possiblyIncompleteClassesAndProperties.add(convertIntoURI(predicate));
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
		public Assertion next() {
			if (nextAssertion != null) {
				Assertion assertion = nextAssertion;
				counter++;
				nextAssertion = null;
				return assertion;
			}
			throw new NoSuchElementException("Please call hasNext() before calling next()");
		}

		private ConstructQuery getConstructQuery(Predicate p)
		{
			try {
				if (p.getArity() == 1)
					return inputQueryFactory.createConstructQuery(getClassQuery(p));
				else if (p.getArity() == 2)
					return inputQueryFactory.createConstructQuery(getPredicateQuery(p));
				else
					throw new NonRDFPredicateException(p);
			} catch (OntopInvalidInputQueryException e) {
				throw new InvalidMaterializationConstructQueryException(e);
			}

		}

		private String getPredicateQuery(Predicate p) {
			return String.format(PROPERTY_QUERY, p.toString(), p.toString()); }

		private String getClassQuery(Predicate p) {
			return String.format(CLASS_QUERY, p.toString(), p.toString()); }
			

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

		public ImmutableList<URI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar() {
			return ImmutableList.copyOf(possiblyIncompleteClassesAndProperties);
		}
	}



	private static class NonURIPredicateInVocabularyException extends OntopInternalBugException {

		NonURIPredicateInVocabularyException(Predicate vocabularyPredicate) {
			super("A non-URI predicate has been found in the vocabulary: " + vocabularyPredicate);
		}
	}



	private static class NonRDFPredicateException extends OntopInternalBugException {

		NonRDFPredicateException(Predicate predicate) {
			super("This predicate is not a RDF predicate: " + predicate);
		}
	}



	private static class InvalidMaterializationConstructQueryException extends OntopInternalBugException {

		InvalidMaterializationConstructQueryException(OntopInvalidInputQueryException e) {
			super("Invalid materialization construct query: \n" + e);
		}
	}
}
