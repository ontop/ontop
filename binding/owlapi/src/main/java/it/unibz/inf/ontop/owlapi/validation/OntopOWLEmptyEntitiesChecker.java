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
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Return empty concepts and roles, based on the mappings. Given an ontology,
 * which is connected to a database via mappings, generate a suitable set of
 * queries that test if there are empty concepts, concepts that are no populated
 * to anything.
 */
public class OntopOWLEmptyEntitiesChecker {

	private static final String CLASS_QUERY = "SELECT ?x WHERE {?x a <%s>.} LIMIT 1";
	private static final String PROPERTY_QUERY = "SELECT * WHERE {?x <%s> ?y.} LIMIT 1";

	private static final Logger LOG = LoggerFactory.getLogger(OntopOWLEmptyEntitiesChecker.class);

	private final ClassifiedTBox onto;
	private final OWLConnection conn;

	/**
	 * Generate SPARQL queries to check if there are instances for each concept and role in the ontology
	 *
	 * @param tbox the ontology, conn QuestOWL connection
	 */
	public OntopOWLEmptyEntitiesChecker(ClassifiedTBox tbox, OWLConnection conn) {
		this.onto = tbox;
		this.conn = conn;
	}

	public Iterable<IRI> emptyClasses() {
		return () -> new FunctionIterator<>(
				new FilterIterator<>(onto.classes().iterator(), this::isEmpty),
				OClass::getIRI);
	}

	public Iterable<IRI> emptyProperties() {
		return () -> new ChainIterator<>(
				new FunctionIterator<>(
						new FilterIterator<>(onto.objectProperties().iterator(), this::isEmpty),
						ObjectPropertyExpression::getIRI),
				new FunctionIterator<>(
						new FilterIterator<>(onto.dataProperties().iterator(), this::isEmpty),
						DataPropertyExpression::getIRI));
	}

	private boolean isEmpty(OClass c) {
		return !c.isTop() && !c.isBottom()
				&& !isResultNonEmpty(String.format(CLASS_QUERY, c.getIRI().getIRIString()), conn);
	}

	private boolean isEmpty(ObjectPropertyExpression c) {
		return !c.isTop() && !c.isBottom()
				&& !isResultNonEmpty(String.format(PROPERTY_QUERY, c.getIRI().getIRIString()), conn);
	}

	private boolean isEmpty(DataPropertyExpression c) {
		return !c.isTop() && !c.isBottom()
				&& !isResultNonEmpty(String.format(PROPERTY_QUERY, c.getIRI().getIRIString()), conn);
	}


	private static boolean isResultNonEmpty(String query, OWLConnection conn) {
		try (OWLStatement stm = conn.createStatement();
			 TupleOWLResultSet rs = stm.executeSelectQuery(query)) {
			return rs.hasNext();
		}
		catch (OWLException e) {
			LOG.debug("Error executing SPARQL query", e);
			return false;
		}
	}

	private static class ChainIterator<E> implements Iterator<E> {
		private final Iterator<E> first, second;

		ChainIterator(Iterator<E> first, Iterator<E> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean hasNext() {
			return first.hasNext() || second.hasNext();
		}

		@Override
		public E next() {
			return first.hasNext() ? first.next() : second.next();
		}
	}

	private static class FunctionIterator<E, F> implements Iterator<F> {
		private final Iterator<E> iterator;
		private final Function<E, F> function;

		FunctionIterator(Iterator<E> iterator, Function<E, F> function) {
			this.iterator = iterator;
			this.function = function;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public F next() {
			return function.apply(iterator.next());
		}
	}

	private static class FilterIterator<E> implements Iterator<E> {

		private final Iterator<E> iterator;
		private final Predicate<E> filter;

		private E next;
		private boolean atNext;

		FilterIterator(Iterator<E> iterator, Predicate<E> filter) {
			this.iterator = iterator;
			this.filter = filter;
		}

		@Override
		public boolean hasNext() {
			if (!atNext)
				atNext = moveToNext();

			return atNext;
		}

		@Override
		public E next() {
			if (!atNext) {
				atNext = !moveToNext();
				if (!atNext)
					throw new NoSuchElementException();
			}

			atNext = false;
			return next;
		}

		private boolean moveToNext() {
			while (iterator.hasNext()) {
				next = iterator.next();
				if (filter.test(next)) {
					return true;
				}
			}
			return false;
		}
	}
}

