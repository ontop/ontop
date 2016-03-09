package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Return empty concepts and roles, based on the mappings. Given an ontology,
 * which is connected to a database via mappings, generate a suitable set of
 * queries that test if there are empty concepts, concepts that are no populated
 * to anything.
 */
public class QuestOWLEmptyEntitiesChecker {

	private Ontology onto;
	private QuestOWLConnection conn;

	private int nEmptyConcepts = 0;
	private int nEmptyRoles = 0;


	/**
	 * Generate SPARQL queries to check if there are instances for each concept and role in the ontology
	 * 
	 * @param translatedOntologyMerge the OWLAPI3 ontology, conn QuestOWL connection
	 * @throws Exception
	 */
	public QuestOWLEmptyEntitiesChecker(Ontology translatedOntologyMerge, QuestOWLConnection conn) throws Exception {
		this.onto = translatedOntologyMerge;
		this.conn = conn;

	}

	public Iterator<Predicate> iEmptyConcepts() {
		return new EmptyEntitiesIterator( onto.getVocabulary().getClasses().iterator(), conn);
	}


	public Iterator<Predicate> iEmptyRoles() {
		return new EmptyEntitiesIterator(onto.getVocabulary().getObjectProperties().iterator(), onto.getVocabulary().getDataProperties().iterator(), conn);
	}

	public int getEConceptsSize() {
		return nEmptyConcepts;
	}

	public int getERolesSize() {
		return  nEmptyRoles ;
	}


	@Override
	public String toString() {
		String str = new String();

		str += String.format("- %s Empty %s ", nEmptyConcepts, (nEmptyConcepts == 1) ? "concept" : "concepts");

		str += String.format("- %s Empty %s\n", nEmptyRoles, (nEmptyRoles == 1) ? "role" : "roles");
		return str;
	}


	/***
	 * An iterator that will dynamically construct ABox assertions for the given
	 * predicate based on the results of executing the mappings for the
	 * predicate in each data source.
	 *
	 */
	private class EmptyEntitiesIterator implements Iterator<Predicate> {


		private String queryConcepts = "SELECT ?x WHERE {?x a <%s>.} LIMIT 1";
		private String queryRoles = "SELECT * WHERE {?x <%s> ?y.} LIMIT 1";

		private QuestOWLConnection questConn;
		private boolean hasNext = false;
		private Predicate nextConcept;

		Iterator<OClass> classIterator;
		Iterator<ObjectPropertyExpression> objectRoleIterator;
		Iterator<DataPropertyExpression> dataRoleIterator;

		private Logger log = LoggerFactory.getLogger(EmptyEntitiesIterator.class);

		/** iterator for classes  of the ontologies */
		public EmptyEntitiesIterator(Iterator<OClass> classIterator, QuestOWLConnection questConn) {

				this.questConn = questConn;

				this.classIterator = classIterator;

				this.objectRoleIterator = new Iterator<ObjectPropertyExpression>() {
					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public ObjectPropertyExpression next() {
						return null;
					}
				};

				this.dataRoleIterator = new Iterator<DataPropertyExpression>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public DataPropertyExpression next() {
					return null;
				}
			};
		}

		/** iterator for roles of the ontologies */
		public EmptyEntitiesIterator(Iterator<ObjectPropertyExpression> objectRoleIterator, Iterator<DataPropertyExpression> dataRoleIterator, QuestOWLConnection questConn) {

			this.questConn = questConn;

			this.classIterator = new Iterator<OClass>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public OClass next() {
					return null;
				}
			};

			this.objectRoleIterator = objectRoleIterator;

			this.dataRoleIterator = dataRoleIterator;

		}

		private String getPredicateQuery(Predicate p) {
			return String.format(queryRoles, p.toString()); }

		private String getClassQuery(Predicate p) {
			return String.format(queryConcepts, p.toString()); }

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
			while (classIterator.hasNext()){
				OClass next = classIterator.next();
				if (!next.isTop() && !next.isBottom()) {
					Predicate entity = next.getPredicate();
					if (nextEmptyEntity(entity)) {
						nEmptyConcepts++;
						return hasNext;
					}
				}

			}
			log.debug( "No more empty concepts" );

			while (objectRoleIterator.hasNext()){
				ObjectPropertyExpression next = objectRoleIterator.next();
				if (!next.isTop() && !next.isBottom()) {
					Predicate entity = next.getPredicate();
					if (nextEmptyEntity(entity)) {
						nEmptyRoles++;
						return hasNext;
					}
				}
			}
			log.debug( "No more empty object roles" );

			while (dataRoleIterator.hasNext()){
				DataPropertyExpression next = dataRoleIterator.next();
				if (!next.isTop() && !next.isBottom()) {
					Predicate entity = next.getPredicate();
					if (nextEmptyEntity(entity)) {
						nEmptyRoles++;
						return hasNext;
					}
				}

			}
			log.debug( "No more empty data roles" );
			hasNext = false;

			return hasNext;
		}

		private boolean nextEmptyEntity(Predicate entity) {

			String query =getQuery(entity);

			//execute next query
			try (QuestOWLStatement stm = questConn.createStatement()){

                try (QuestOWLResultSet rs = stm.executeTuple(query)) {

                    if (!rs.nextRow()) {

						nextConcept = entity;
						log.debug( "Empty " + entity );

						hasNext = true;
						return true;

                    }

					return false;

                }
            } catch (OWLException e) {
                e.printStackTrace();
            }

			return false;
		}

		@Override
		public Predicate next() {

			if(hasNext) {
				return nextConcept;
			}

			return null;


		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
