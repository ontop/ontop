package it.unibz.inf.ontop.materialization.impl;

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
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ABoxAssertionSupplier;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.NamedAssertion;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Triple;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;


/**
 * Allows you to materialize the virtual RDF graph of an OBDA specification.
 * 
 * @author Mariano Rodriguez Muro (initial version was called QuestMaterializer)
 * 
 */
public class DefaultOntopRDFMaterializer implements OntopRDFMaterializer {

	private final MaterializationParams params;
	private final InputQueryFactory inputQueryFactory;
	private final OntopQueryEngine queryEngine;

	private final ImmutableMap<IRI, VocabularyEntry> vocabulary;

	private static final class VocabularyEntry {
        private final IRI name;
        private final int arity;

        VocabularyEntry(IRI predicate, int arity) {

                this.name = predicate;
            	this.arity = arity;
        }

		private static final String PROPERTY_QUERY = "CONSTRUCT {?s <%s> ?o} WHERE {?s <%s> ?o}";
        private static final String CLASS_QUERY = "CONSTRUCT {?s a <%s>} WHERE {?s a <%s>}";

        private static final String SELECT_PROPERTY_QUERY_CONTEXT = "SELECT DISTINCT ?s ?o ?g WHERE {GRAPH ?g {?s <%s> ?o}}"; // Davide> TODO
		private static final String SELECT_CLASS_QUERY_CONTEXT = "SELECT DISTINCT ?s ?g WHERE {GRAPH ?g {?s a <%s>}}"; // Davide> TODO

		private static final String SELECT_PROPERTY_QUERY = "SELECT DISTINCT ?s ?o WHERE {?s <%s> ?o}"; // Davide> TODO
		private static final String SELECT_CLASS_QUERY = "SELECT DISTINCT ?s WHERE {?s a <%s>}"; // Davide> TODO
		boolean isClass(){
			return arity == 1;
		}

		String getIRIString(){
			return name.getIRIString();
		}

        String getQuery() {
            return String.format((arity == 1) ? CLASS_QUERY : PROPERTY_QUERY, name.getIRIString(), name.getIRIString());
        }

//        String getQueryQuad () {
//        	return String.format((arity == 1) ? CLASS_QUERY_GRAPH : PROPERTY_QUERY_GRAPH, name.getIRIString(), name.getIRIString());
//		}

		String getSelectQuadQuery() {
        	return String.format((arity == 1) ? SELECT_CLASS_QUERY_CONTEXT : SELECT_PROPERTY_QUERY_CONTEXT, name.getIRIString(), name.getIRIString());
		}

		String getSelectQuery() {
        	return String.format((arity == 1) ? SELECT_CLASS_QUERY : SELECT_PROPERTY_QUERY, name.getIRIString(), name.getIRIString());
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

	private static ImmutableMap<IRI, VocabularyEntry> extractVocabulary(@Nonnull Mapping mapping) {
		Map<IRI, VocabularyEntry> result = new HashMap<>();
		for (RDFAtomPredicate predicate : mapping.getRDFAtomPredicates()){
			if( predicate instanceof TriplePredicate || predicate instanceof QuadPredicate )
				result.putAll(extractTripleVocabulary(mapping, predicate)
						.collect(ImmutableCollectors.toMap(e -> e.name, e -> e)));
		}
		return ImmutableMap.copyOf(result);
	}

    private static Stream<VocabularyEntry> extractTripleVocabulary(Mapping mapping, RDFAtomPredicate tripleOrQuadPredicate) {
		Stream<VocabularyEntry> vocabularyPropertyStream = mapping.getRDFProperties(tripleOrQuadPredicate).stream()
				.map(p -> new VocabularyEntry(p, 2));

		Stream<VocabularyEntry> vocabularyClassStream = mapping.getRDFClasses(tripleOrQuadPredicate).stream()
				.map(p -> new VocabularyEntry(p, 1));
		return Stream.concat(vocabularyClassStream, vocabularyPropertyStream);
	}


	private static class DefaultMaterializedGraphResultSet implements MaterializedGraphResultSet {

		private final ImmutableMap<IRI, VocabularyEntry> vocabulary;
		private final InputQueryFactory inputQueryFactory;
		private final boolean canBeIncomplete;

		private final OntopQueryEngine queryEngine;
		private final UnmodifiableIterator<VocabularyEntry> vocabularyIterator;

		private int counter;
		@Nullable
		private OntopConnection ontopConnection;
		@Nullable
		private OntopStatement tmpStatement;
		@Nullable
		private TupleResultSet tmpContextResultSet = null;
		@Nullable
//		private Assertion nextAssertion;

		private Logger LOGGER = LoggerFactory.getLogger(DefaultMaterializedGraphResultSet.class);
		private final List<IRI> possiblyIncompleteClassesAndProperties;
		private VocabularyEntry lastSeenPredicate;


		DefaultMaterializedGraphResultSet(ImmutableMap<IRI, VocabularyEntry> vocabulary, MaterializationParams params,
										  OntopQueryEngine queryEngine, InputQueryFactory inputQueryFactory) {

			this.vocabulary = vocabulary;
			this.vocabularyIterator = vocabulary.values().iterator();

			this.queryEngine = queryEngine;
			this.canBeIncomplete = params.canMaterializationBeIncomplete();
			this.inputQueryFactory = inputQueryFactory;
			this.possiblyIncompleteClassesAndProperties = new ArrayList<>();

			counter = 0;
			// Lately initiated
			ontopConnection = null;
			tmpStatement = null;
			tmpContextResultSet = null;
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

			if ((tmpContextResultSet != null) && tmpContextResultSet.hasNext()) {
				return true;
			}

			// Davide> If there is no next, we need to go to the next vocabulary predicate

			while(vocabularyIterator.hasNext()) {
				/*
			 	* Closes the previous result set and statement (if open)
			 	*/
				if (tmpContextResultSet != null) {
					try {
						tmpContextResultSet.close();
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

				SelectQuery query = inputQueryFactory.createSelectQuery(predicate.getSelectQuadQuery());

				try {
					tmpStatement = ontopConnection.createStatement();
					tmpContextResultSet = tmpStatement.execute(query);

					if (tmpContextResultSet.hasNext()) {
						lastSeenPredicate = predicate;
						return true;
					}else{
						// Davide> Try triples (ugly, TODO refactor)
						tmpContextResultSet.close();
						tmpStatement.close();

						query = inputQueryFactory.createSelectQuery(predicate.getSelectQuery());
						tmpStatement = ontopConnection.createStatement();
						tmpContextResultSet = tmpStatement.execute(query);

						if (tmpContextResultSet.hasNext()) {
							lastSeenPredicate = predicate;
							return true;
						}
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

		// Davide> Builds (named) assertions out of (quad) results
		private Assertion toConstruct(OntopBindingSet tuple) throws OntopReformulationException, OntopConnectionException, OntopResultConversionException, OntopQueryEvaluationException {

			String s = tuple.getBinding("s").getValue().toString();
			String p = lastSeenPredicate.isClass() ? "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" : lastSeenPredicate.getIRIString();
			// String o = lastSeenPredicate.isClass() ? "<" + lastSeenPredicate.getIRIString() + ">" : "\"" + tuple.getBinding("o").getValue().getValue() + "\""; //+"\"^^"+tuple.getBinding("o").getValue().getType();
			String o = lastSeenPredicate.isClass() ? "<" + lastSeenPredicate.getIRIString() + ">" : tuple.getBinding("o").getValue().toString(); //+"\"^^"+tuple.getBinding("o").getValue().getType();
			String g = tuple.hasBinding("g") ? tuple.getBinding("g").getValue().getValue() : null;
			String constructTemplateContext = "CONSTRUCT {%s <%s> %s} WHERE {}"; // {GRAPH <%s> {<%s> <%s> %s}}";
			String constructTemplate = "CONSTRUCT {%s <%s> %s} WHERE {}";

			// Davide> Clean language tag: "aaa"^^@en -> "aaa"@en
			if( o.contains("@") ){
				String temp = o.replace("^^","");
				o = temp;
			}


			if( g != null ){
				// Quad
				// String queryString = String.format(constructTemplateContext,s,p,o,g,s,p,o);
				String queryString = String.format(constructTemplateContext,s,p,o);
				ConstructQuery query = inputQueryFactory.createConstructQuery(queryString);
				OntopStatement tmpStatement = ontopConnection.createStatement();
				SimpleGraphResultSet tmpGraphResultSet = tmpStatement.execute(query);

				Assertion a = tmpGraphResultSet.hasNext() ? tmpGraphResultSet.next() : null; // TODO Davide> Add Exception
				NamedAssertion aN = NamedAssertion.decorate(a, g);

				tmpGraphResultSet.close();
				tmpStatement.close();
				return aN;
			}

			String queryString = String.format(constructTemplate,s,p,o);

			ConstructQuery query = inputQueryFactory.createConstructQuery(queryString);
			OntopStatement tmpStatement = ontopConnection.createStatement();
			SimpleGraphResultSet tmpGraphResultSet = tmpStatement.execute(query);
			Assertion a = tmpGraphResultSet.hasNext() ? tmpGraphResultSet.next() : null; // TODO Davide> Add Exception

			tmpGraphResultSet.close();
			tmpStatement.close();

			return a;
		}

		@Override
		public Assertion next()throws OntopQueryAnsweringException {
			counter++;

			OntopBindingSet resultTuple = null;
			try {
				resultTuple = tmpContextResultSet.next();
				Assertion result = toConstruct(resultTuple);
				return result;
			} catch (OntopConnectionException e) {
				try {
					tmpContextResultSet.close();
				} catch (OntopConnectionException ex) {
					ex.printStackTrace();
				}
				e.printStackTrace();
			}
			return null;
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
