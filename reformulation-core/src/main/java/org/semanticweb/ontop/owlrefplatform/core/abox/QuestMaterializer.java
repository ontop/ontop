package org.semanticweb.ontop.owlrefplatform.core.abox;

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

import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.GraphResultSet;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.ResultSet;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.owlrefplatform.core.QuestConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.core.QuestStatement;
import org.semanticweb.ontop.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final OBDAModel model;
	private final Quest questInstance;
	private Ontology ontology;
	
	/**
	 * Puts the JDBC connection in streaming mode.
	 */
	private final boolean doStreamResults;
	
	private final Set<Predicate> vocabulary;

	private long counter = 0;
	private VirtualTripleIterator iterator;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private static int FETCH_SIZE = 50000;

	/***
	 * 
	 * 
	 * @param model
	 * @throws Exception
	 */
	public QuestMaterializer(OBDAModel model, boolean doStreamResults) throws Exception {
		this(model, null, null, getDefaultPreferences(), doStreamResults);
	}
	
	public QuestMaterializer(OBDAModel model, QuestPreferences pref, boolean doStreamResults) throws Exception {
		this(model, null, null, pref, doStreamResults);
		
	}
		
	public QuestMaterializer(OBDAModel model, Ontology onto, boolean doStreamResults) throws Exception {
		this(model, onto, null, getDefaultPreferences(), doStreamResults);
	}
	
    public QuestMaterializer(OBDAModel model, Ontology onto, Collection<Predicate> predicates, boolean doStreamResults) throws Exception {
        this(model, onto, predicates, getDefaultPreferences(), doStreamResults);
	}
	
	/***
	 * 
	 * 
	 * @param model
	 * @throws Exception
	 */
	public QuestMaterializer(OBDAModel model, Ontology onto, Collection<Predicate> predicates, QuestPreferences preferences,
							 boolean doStreamResults) throws Exception {
		this.doStreamResults = doStreamResults;
		this.model = model;
		this.ontology = onto;

        if(predicates != null && !predicates.isEmpty()){
            this.vocabulary = new HashSet<>(predicates);
        } else {
           this.vocabulary = extractVocabulary(model, onto);
        }
		
		if (this.model.getSources()!= null && this.model.getSources().size() > 1)
			throw new Exception("Cannot materialize with multiple data sources!");
		

        //start a quest instance
		if (ontology == null) {
			ontology = ofac.createOntology();
			
			// TODO: use Vocabulary for OBDAModel as well
			
			for (OClass pred : model.getDeclaredClasses()) 
				ontology.getVocabulary().createClass(pred.getPredicate().getName());				
			
			for (ObjectPropertyExpression prop : model.getDeclaredObjectProperties()) 
				ontology.getVocabulary().createObjectProperty(prop.getPredicate().getName());

			for (DataPropertyExpression prop : model.getDeclaredDataProperties()) 
				ontology.getVocabulary().createDataProperty(prop.getPredicate().getName());
		}
		
		
		preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		questInstance = new Quest(ontology, this.model, preferences);
					
		questInstance.setupRepository();
	}

    public QuestMaterializer(OBDAModel model, Ontology onto, QuestPreferences prefs, boolean doStreamResults) throws Exception {
        this(model, onto, null, prefs, doStreamResults);
    }

    private Set<Predicate> extractVocabulary(OBDAModel model, Ontology onto) {
        Set<Predicate> vocabulary = new HashSet<Predicate>();

		//add all class/data/object predicates to vocabulary
		//add declared predicates in model
		for (OClass cl : model.getDeclaredClasses()) {
			Predicate p = cl.getPredicate();
			if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#"))
				vocabulary.add(p);
		}
		for (ObjectPropertyExpression prop : model.getDeclaredObjectProperties()) {
			Predicate p = prop.getPredicate();
			if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#"))
				vocabulary.add(p);
		}
		for (DataPropertyExpression prop : model.getDeclaredDataProperties()) {
			Predicate p = prop.getPredicate();
			if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#"))
				vocabulary.add(p);
		}
		if (onto != null) {
			//from ontology
			for (OClass cl : onto.getVocabulary().getClasses()) {
				Predicate p = cl.getPredicate(); 
				if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
						&& !vocabulary.contains(p))
					vocabulary.add(p);
			}
			for (ObjectPropertyExpression role : onto.getVocabulary().getObjectProperties()) {
				Predicate p = role.getPredicate();
				if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
						&& !vocabulary.contains(p))
					vocabulary.add(p);
			}
			for (DataPropertyExpression role : onto.getVocabulary().getDataProperties()) {
				Predicate p = role.getPredicate();
				if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
						&& !vocabulary.contains(p))
					vocabulary.add(p);
			}
		} 
		else {
			//from mapping undeclared predicates (can happen)
			for (URI uri : this.model.getMappings().keySet()){
				for (OBDAMappingAxiom axiom : this.model.getMappings(uri))
				{
					if (axiom.getTargetQuery() instanceof CQIE)
					{
						CQIE rule = (CQIE)axiom.getTargetQuery();
						for (Function f: rule.getBody())
						{
							vocabulary.add(f.getFunctionSymbol());
						}
					}
					
				}
			
			}
		}
			
        return vocabulary;
		}
		
/*    public QuestMaterializer(OBDAModel model, Ontology onto, List<Predicate> predicates, QuestPreferences defaultPreferences) {
	}
*/

	private static QuestPreferences getDefaultPreferences() {
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		return p;
	}


	public Iterator<Assertion> getAssertionIterator() throws Exception {
		//return the inner class  iterator
		iterator = new VirtualTripleIterator(questInstance, vocabulary.iterator());
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
		return vocabulary.size();
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

		private QuestConnection questConn;
		private QuestStatement stm;
		
		private boolean read = false, hasNext = false;

		private GraphResultSet results;
		
		private Iterator<Predicate> vocabularyIterator;
		
		private Logger log = LoggerFactory.getLogger(VirtualTripleIterator.class);

		public VirtualTripleIterator(Quest questInstance, Iterator<Predicate> vocabIter)
				throws SQLException {
			try{
				questConn = questInstance.getNonPoolConnection();

				if (doStreamResults) {
					// Autocommit must be OFF (needed for autocommit)
					questConn.setAutoCommit(false);
				}

				vocabularyIterator = vocabIter;
				//execute first query to start the process
				counter = 0;
				stm = questConn.createStatement();

				if (doStreamResults) {
					// Fetch 50 000 lines at the same time
					stm.setFetchSize(FETCH_SIZE);
				}
				if (!vocabularyIterator.hasNext())
					throw new NullPointerException("Vocabulary is empty!");
				while (results == null) {
					if (vocabularyIterator.hasNext()) {
						Predicate pred = vocabularyIterator.next();
						String query = getQuery(pred);
						ResultSet execute = stm.execute(query);

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
						stm = questConn.createStatement();
						if (doStreamResults) {
							stm.setFetchSize(FETCH_SIZE);
						}
						Predicate next = vocabularyIterator.next();
						String query = getQuery(next);
						ResultSet execute = stm.execute(query);

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
				
			} catch (OBDAException e) {
				e.printStackTrace();
				log.warn("Exception in Assertion Iterator next");
			}
			return null;
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

			if (questConn != null) {
				try {
					questConn.close();
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
