package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

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
	
	public QuestMaterializer(OBDAModel modell, Ontology onto) throws Exception {
		this(modell, onto, getDefaultPreferences());
	}
	
	/***
	 * 
	 * 
	 * @param model
	 * @throws Exception
	 */
	public QuestMaterializer(OBDAModel modell, Ontology onto, QuestPreferences preferences) throws Exception {
		this.model = modell;
		this.ontology = onto;
		this.vocabulary = new HashSet<Predicate>();
		
		if (model.getSources()!= null && model.getSources().size() > 1)
			throw new Exception("Cannot materialize with multiple data sources!");
		
		//add all class/data/object predicates to vocabulary
		//add declared predicates in model
		for (Predicate p: model.getDeclaredPredicates()) {
			if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#"))
				vocabulary.add(p);
		}
		if (onto != null) {
			//from ontology
			for (Predicate p : onto.getVocabulary()) {
				if (!p.toString().startsWith("http://www.w3.org/2002/07/owl#")
						&& !vocabulary.contains(p))
					vocabulary.add(p);
			}
		} else
		{
			//from mapping undeclared predicates (can happen)
			for (URI uri : model.getMappings().keySet()){
				for (OBDAMappingAxiom axiom : model.getMappings(uri))
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
			ontology.addEntities(model.getDeclaredPredicates());
		}
		
		
		preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		questInstance = new Quest(ontology, model, preferences);
					
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
						results = (GraphResultSet) stm.execute(query);
					}
					else
						break;
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
						results = (GraphResultSet) stm.execute(getQuery(vocabularyIterator.next()));
						if (results!=null)
							hasNext = results.hasNext();
					
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
