package it.unibz.krdb.obda.owlrefplatform.core.abox;

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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ResultSet;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.BooleanOWLOBDARefResultSet;

import java.net.URI;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	private OBDAModel model;
	private Quest questInstance;
	private Ontology ontology;
	
	private Set<Predicate> vocabulary;

	protected long counter = 0;
	private VirtualTripleIterator iterator;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	/***
	 * 
	 * 
	 * @param model
	 * @throws Exception
	 */
	public QuestMaterializer(OBDAModel model) throws Exception {
		this(model, null, getDefaultPreferences());
	}
	
	public QuestMaterializer(OBDAModel model, QuestPreferences pref) throws Exception {
		this(model, null, pref);
		
	}
	
	public QuestMaterializer(OBDAModel model, Ontology onto) throws Exception {
		this(model, onto, getDefaultPreferences());
	}
	
	/***
	 * 
	 * 
	 * @param model
	 * @throws Exception
	 */
	public QuestMaterializer(OBDAModel model, Ontology onto, QuestPreferences preferences) throws Exception {
		this.model = model;
		this.ontology = onto;
		this.vocabulary = new HashSet<Predicate>();
		
		if (this.model.getSources()!= null && this.model.getSources().size() > 1)
			throw new Exception("Cannot materialize with multiple data sources!");
		
		//add all class/data/object predicates to vocabulary
		//add declared predicates in model
		for (Predicate p: model.getDeclaredClasses()) {
			if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#"))
				vocabulary.add(p);
		}
		for (Predicate p: model.getDeclaredObjectProperties()) {
			if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#"))
				vocabulary.add(p);
		}
		for (Predicate p: model.getDeclaredDataProperties()) {
			if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#"))
				vocabulary.add(p);
		}
		if (onto != null) {
			//from ontology
			for (OClass cl : onto.getConcepts()) {
				Predicate p = cl.getPredicate(); 
				if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
						&& !vocabulary.contains(p))
					vocabulary.add(p);
			}
			for (PropertyExpression role : onto.getRoles()) {
				Predicate p = role.getPredicate();
				if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
						&& !vocabulary.contains(p))
					vocabulary.add(p);
			}
		} else
		{
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
		//start a quest instance
		if (ontology == null) {
			ontology = ofac.createOntology();
			
			for (Predicate pred : model.getDeclaredClasses()) {
				ontology.addConcept(ofac.createClass(pred.getName()));				
			}
			
			for (Predicate pred : model.getDeclaredObjectProperties()) {
				ontology.addRole(ofac.createObjectProperty(pred.getName()));
			}

			for (Predicate pred : model.getDeclaredDataProperties()) {
				ontology.addRole(ofac.createDataProperty(pred.getName()));
			}
						
		}
		
		
		preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		questInstance = new Quest(ontology, this.model, preferences);
					
		questInstance.setupRepository();
	}
	

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
				vocabularyIterator = vocabIter;
				//execute first query to start the process
				counter = 0;
				stm = questConn.createStatement();
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
