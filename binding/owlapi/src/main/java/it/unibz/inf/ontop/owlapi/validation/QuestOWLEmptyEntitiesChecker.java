package it.unibz.inf.ontop.owlapi.validation;

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

import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.ontology.*;
import org.apache.commons.rdf.api.IRI;
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

	private final ClassifiedTBox onto;
	private final OWLConnection conn;

	private int nEmptyConcepts = 0;
	private int nEmptyRoles = 0;

	/**
	 * Generate SPARQL queries to check if there are instances for each concept and role in the ontology
	 *
	 * @param tbox the ontology, conn QuestOWL connection
	 */
	public QuestOWLEmptyEntitiesChecker(ClassifiedTBox tbox, OWLConnection conn) {
		this.onto = tbox;
		this.conn = conn;
	}

	public Iterator<IRI> iEmptyConcepts() {
		return new EmptyEntitiesIterator(onto.classes().iterator(), conn);
	}

	public Iterator<IRI> iEmptyRoles() {
		return new EmptyEntitiesIterator(onto.objectProperties().iterator(), onto.dataProperties().iterator(), conn);
	}

	public int getEConceptsSize() {
		return nEmptyConcepts;
	}

	public int getERolesSize() {
		return  nEmptyRoles;
	}


	@Override
	public String toString() {
		return String.format("- %s Empty %s ", nEmptyConcepts, (nEmptyConcepts == 1) ? "concept" : "concepts") +
		String.format("- %s Empty %s\n", nEmptyRoles, (nEmptyRoles == 1) ? "role" : "roles");
	}


	/***
	 * An iterator that will dynamically construct ABox assertions for the given
	 * predicate based on the results of executing the mappings for the
	 * predicate in each data source.
	 *
	 */
	private class EmptyEntitiesIterator implements Iterator<IRI> {


		private static final String queryConcepts = "SELECT ?x WHERE {?x a <%s>.} LIMIT 1";
		private static final String queryRoles = "SELECT * WHERE {?x <%s> ?y.} LIMIT 1";

		private final OWLConnection questConn;

		private boolean hasNext = false;
		private IRI nextConcept;

		private final Iterator<OClass> classIterator;
		private final Iterator<ObjectPropertyExpression> objectRoleIterator;
		private final Iterator<DataPropertyExpression> dataRoleIterator;

		private final Logger log = LoggerFactory.getLogger(EmptyEntitiesIterator.class);

		/** iterator for classes  of the ontologies */
		public EmptyEntitiesIterator(Iterator<OClass> classIterator, OWLConnection questConn) {

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
		public EmptyEntitiesIterator(Iterator<ObjectPropertyExpression> objectRoleIterator, Iterator<DataPropertyExpression> dataRoleIterator, OWLConnection questConn) {

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

		private String getPredicateQuery(IRI p) {
			return String.format(queryRoles, p.getIRIString()); }

		private String getClassQuery(IRI p) {
			return String.format(queryConcepts, p.getIRIString()); }

		private String getQuery(int arity, IRI iri)
		{
			switch(arity) {
				case 1:
					return getClassQuery(iri);
				case 2:
					return getPredicateQuery(iri);
				default:
					return "";
			}
		}

		@Override
		public boolean hasNext() {
			while (classIterator.hasNext()){
				OClass next = classIterator.next();
				if (!next.isTop() && !next.isBottom()) {
					if (nextEmptyEntity(next.getIRI(), 1)) {
						nEmptyConcepts++;
						return hasNext;
					}
				}
			}
			log.debug( "No more empty concepts" );

			while (objectRoleIterator.hasNext()){
				ObjectPropertyExpression next = objectRoleIterator.next();
				if (!next.isTop() && !next.isBottom()) {
					if (nextEmptyEntity(next.getIRI(), 2)) {
						nEmptyRoles++;
						return hasNext;
					}
				}
			}
			log.debug( "No more empty object roles" );

			while (dataRoleIterator.hasNext()){
				DataPropertyExpression next = dataRoleIterator.next();
				if (!next.isTop() && !next.isBottom()) {
					if (nextEmptyEntity(next.getIRI(), 2)) {
						nEmptyRoles++;
						return hasNext;
					}
				}
			}
			log.debug( "No more empty data roles" );
			hasNext = false;

			return hasNext;
		}

		private boolean nextEmptyEntity(IRI entity, int arity) {

			String query =getQuery(arity, entity);

			//execute next query
			try (OWLStatement stm = questConn.createStatement()){
				try (TupleOWLResultSet rs = stm.executeSelectQuery(query)) {
					if (!rs.hasNext()) {
						nextConcept = entity;
						log.debug( "Empty " + entity );

						hasNext = true;
						return true;
					}

					return false;
				}
			}
			catch (OWLException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public IRI next() {
			if (hasNext) {
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

