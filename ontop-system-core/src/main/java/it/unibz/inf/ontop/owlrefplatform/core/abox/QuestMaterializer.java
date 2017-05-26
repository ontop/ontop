package it.unibz.inf.ontop.owlrefplatform.core.abox;

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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.input.ConstructQuery;
import it.unibz.inf.ontop.answering.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.injection.OntopEngineFactory;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.model.GraphResultSet;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.OBDAResultSet;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlrefplatform.core.*;

import java.io.IOException;
import java.util.*;

import it.unibz.inf.ontop.spec.OBDASpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/***
 * Allows you to work with the virtual triples defined by an OBDA model. In
 * particular you will be able to generate ABox assertions from it and get
 * statistics.
 * 
 * The class works "online", that is, the triples are never kept in memory by
 * this class, instead a connection to the data sources will be established when
 * needed and the data will be streamed or retrieved on request.
 * 
 * In order to compute the SQL queries relevant for each predicate we use the
 * ComplexMapping Query unfolder. This allow us to reuse all the infrastructure
 * for URI generation and to avoid manually processing each mapping. This also
 * means that all features and limitations of the complex unfolder are present
 * here.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class QuestMaterializer {

	/**
	 * Puts the JDBC connection in streaming mode.
	 */
	private final boolean doStreamResults;
	
	private final ImmutableSet<Predicate> selectedVocabulary;
	private final OntopQueryEngine queryEngine;
	private final InputQueryFactory inputQueryFactory;

	private long counter = 0;
	private VirtualTripleIterator iterator;

	private static int FETCH_SIZE = 50000;

	public QuestMaterializer(@Nonnull OntopSystemConfiguration configuration,
							 @Nonnull ImmutableSet<Predicate> selectedVocabulary,
							 boolean doStreamResults) throws OBDASpecificationException {

		this.doStreamResults = doStreamResults;

		this.selectedVocabulary = selectedVocabulary;

		Injector injector = configuration.getInjector();
		OntopEngineFactory engineFactory = injector.getInstance(OntopEngineFactory.class);

		this.queryEngine = engineFactory.create(configuration.loadProvidedSpecification(),
				configuration.getExecutorRegistry());
		this.inputQueryFactory = injector.getInstance(InputQueryFactory.class);
	}

	public QuestMaterializer(@Nonnull OntopSystemConfiguration configuration,
							 boolean doStreamResults) throws IOException, OBDASpecificationException {

		this.doStreamResults = doStreamResults;

		Injector injector = configuration.getInjector();
		OntopEngineFactory engineFactory = injector.getInstance(OntopEngineFactory.class);

		OBDASpecification obdaSpecification = configuration.loadProvidedSpecification();

		this.selectedVocabulary = extractVocabulary(obdaSpecification.getVocabulary());
		this.queryEngine = engineFactory.create(obdaSpecification, configuration.getExecutorRegistry());
		this.inputQueryFactory = injector.getInstance(InputQueryFactory.class);

		// Was an ugly way to ask for also querying the annotations
	}

    private static ImmutableSet<Predicate> extractVocabulary(@Nonnull ImmutableOntologyVocabulary vocabulary) {
        Set<Predicate> predicates = new HashSet<>();

        //add all class/data/object predicates to selectedVocabulary
            //from ontology
            for (OClass cl : vocabulary.getClasses()) {
                Predicate p = cl.getPredicate();
                if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
                        && !predicates.contains(p))
                    predicates.add(p);
            }
            for (ObjectPropertyExpression role : vocabulary.getObjectProperties()) {
                Predicate p = role.getPredicate();
                if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
                        && !predicates.contains(p))
                    predicates.add(p);
            }
            for (DataPropertyExpression role : vocabulary.getDataProperties()) {
                Predicate p = role.getPredicate();
                if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
                        && !predicates.contains(p))
                    predicates.add(p);
            }
			for (AnnotationProperty role : vocabulary.getAnnotationProperties()) {
				Predicate p = role.getPredicate();
				if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
							&& !predicates.contains(p))
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

        return ImmutableSet.copyOf(predicates);
    }


	public Iterator<Assertion> getAssertionIterator() throws Exception {
		//return the inner class  iterator
		iterator = new VirtualTripleIterator(queryEngine, selectedVocabulary.iterator());
		return iterator;
		
	}

	public List<Assertion> getAssertionList() throws Exception {
		Iterator<Assertion> it = getAssertionIterator();
		List<Assertion> assertions = new LinkedList<Assertion>();
		while (it.hasNext()) {
			assertions.add(it.next());
		}
		return assertions;
	}

	public int getTripleCount() throws Exception {
		int counter = 0;
		getAssertionIterator();
		while(iterator.hasNext) {
			counter++;
			iterator.next();
		}
		return counter;
	}

	public long getTriplesCount() throws Exception {
		if (iterator != null)
			return counter;
		else  
		return getTripleCount();
	}

	public int getVocabSize() {
		return selectedVocabulary.size();
	}
	public void disconnect() {
		iterator.disconnect();
	}

	/***
	 * An iterator that will dynamically construct ABox assertions for the given
	 * predicate based on the results of executing the mappings for the
	 * predicate in each data source.
	 * 
	 */
	private class VirtualTripleIterator implements Iterator<Assertion> {


		private String query1 = "CONSTRUCT {?s <%s> ?o} WHERE {?s <%s> ?o}";
		private String query2 = "CONSTRUCT {?s a <%s>} WHERE {?s a <%s>}";

		private OntopConnection queryEngine;
		private OntopStatement stm;
		
		private boolean read = false, hasNext = false;

		private GraphResultSet results;
		
		private Iterator<Predicate> vocabularyIterator;
		
		private Logger log = LoggerFactory.getLogger(VirtualTripleIterator.class);

		public VirtualTripleIterator(OntopQueryEngine queryEngine, Iterator<Predicate> vocabIter) {
			try{
				this.queryEngine = queryEngine.getNonPoolConnection();

				if (doStreamResults) {
					// Autocommit must be OFF (needed for autocommit)
					//queryEngine.setAutoCommit(false);
				}

				vocabularyIterator = vocabIter;
				//execute first query to start the process
				counter = 0;
				stm = this.queryEngine.createStatement();

				if (doStreamResults) {
					// Fetch 50 000 lines at the same time
					stm.setFetchSize(FETCH_SIZE);
				}
				if (!vocabularyIterator.hasNext())
					throw new NullPointerException("Vocabulary is empty!");
				while (results == null) {
					if (vocabularyIterator.hasNext()) {
						Predicate pred = vocabularyIterator.next();
						ConstructQuery query = inputQueryFactory.createConstructQuery(getQuery(pred));
						OBDAResultSet execute = stm.execute(query);

						results = (GraphResultSet) execute;
//						if (results!=null){
//							hasNext = results.hasNext();
//
//						}					
					}else{
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private String getPredicateQuery(Predicate p) {
			return String.format(query1, p.toString(), p.toString()); }
		
		private String getClassQuery(Predicate p) {
			return String.format(query2, p.toString(), p.toString()); }
		
		private String getQuery(Predicate p)
		{
			if (p.getArity() == 1)
				return getClassQuery(p);
			else if (p.getArity() == 2)
				return getPredicateQuery(p);
			return "";
		}

		@Override
		public boolean hasNext() {
			try{
			if (!read && results!=null) {
				hasNext = results.hasNext();
				while (vocabularyIterator.hasNext() && hasNext == false)
				{
						//close previous statement if open
						if (stm!= null && results!=null)
							{stm.close(); results.close(); }

						//execute next query
						stm = queryEngine.createStatement();
						if (doStreamResults) {
							stm.setFetchSize(FETCH_SIZE);
						}
						Predicate predicate = vocabularyIterator.next();
						ConstructQuery query = inputQueryFactory.createConstructQuery(getQuery(predicate));
						OBDAResultSet execute = stm.execute(query);

						results = (GraphResultSet) execute;
						if (results!=null){
							hasNext = results.hasNext();

						}


				}
				read = true;
				
			}
			} catch(Exception e)
			{e.printStackTrace();}


			return hasNext;
		}

		@Override
		public Assertion next() {
			try {
				counter+=1;
				if (read && hasNext)
				{
					read = false;
					return results.next().get(0);
				}
				else if (!read){
					hasNext();
					return next();
				}
				else {
					throw new IllegalStateException("You cannot call next() twice in a row without calling hasNext()");
				}
				
			} catch (OntopConnectionException | OntopResultConversionException e) {
				log.warn("Exception in Assertion Iterator next: " + e.getMessage());
				throw new NoSuchElementException(e.getMessage());
			}
		}
			

		/**
		 * Releases all the connection resources
		 */
		public void disconnect() {
			if (stm != null) {
				try {
					stm.close();
				} catch (Exception e) {
					// NO-OP
				}
			}

			if (queryEngine != null) {
				try {
					queryEngine.close();
				} catch (Exception e) {
					// NO-OP
				}
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
