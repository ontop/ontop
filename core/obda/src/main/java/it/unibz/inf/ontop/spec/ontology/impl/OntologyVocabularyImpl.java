package it.unibz.inf.ontop.spec.ontology.impl;


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


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implements OntologyVocabulary 
 * by providing look-up tables for classes, object and data properties
 * (checks whether the name has been declared)
 * 
 * NOTE: the sets of classes, object and data properties DO NOT contain 
 *       top/bottom elements
 *       HOWEVER, they are recognized as valid class and property names
 * 
 * @author Roman Kontchakov
 */

public class OntologyVocabularyImpl implements OntologyVocabulary {

	private static final class OntologyVocabularyCategoryImpl<T> implements OntologyVocabularyCategory<T> {
		private final Map<String, T> entities = new HashMap<>();

		private final ImmutableMap<String, T> builtins;
		private final Function<String, ? extends T> ctor;
		private final String NOT_FOUND_MESSAGE;

		OntologyVocabularyCategoryImpl(ImmutableMap<String, T> builtins, Function<String, ? extends T> ctor, String NOT_FOUND_MESSAGE) {
			this.builtins = builtins;
			this.ctor = ctor;
			this.NOT_FOUND_MESSAGE = NOT_FOUND_MESSAGE;
		}

		@Override
		public T get(String uri) {
			T e = entities.get(uri);
			if (e != null)
				return e;
			e = builtins.get(uri);
			if (e != null)
				return e;
			throw new RuntimeException(NOT_FOUND_MESSAGE + uri);
		}

		@Override
		public boolean contains(String uri) {
			return entities.containsKey(uri);
		}

		@Override
		public Collection<T> all() {
			return entities.values();
		}

		@Override
		public T create(String uri) {
			if (builtins.containsKey(uri))
				return builtins.get(uri);

			T e = ctor.apply(uri);
			entities.put(uri, e);
			return e;
		}

		@Override
		public void remove(String uri) { entities.remove(uri); }
	}

	private final OntologyVocabularyCategoryImpl<OClass> classes = new OntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(ClassImpl.owlThingIRI, ClassImpl.owlThing,
					ClassImpl.owlNothingIRI, ClassImpl.owlNothing),
			s -> new ClassImpl(s), "Class not found: ");

	private final OntologyVocabularyCategoryImpl<ObjectPropertyExpression> objectProperties = new OntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(ObjectPropertyExpressionImpl.owlBottomObjectPropertyIRI, ObjectPropertyExpressionImpl.owlBottomObjectProperty,
					ObjectPropertyExpressionImpl.owlTopObjectPropertyIRI, ObjectPropertyExpressionImpl.owlTopObjectProperty),
			s -> new ObjectPropertyExpressionImpl(s), "ObjectProperty not found: ");

	private final OntologyVocabularyCategoryImpl<DataPropertyExpression> dataProperties = new OntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(DataPropertyExpressionImpl.owlBottomDataPropertyIRI, DataPropertyExpressionImpl.owlBottomDataProperty,
					DataPropertyExpressionImpl.owlTopDataPropertyIRI, DataPropertyExpressionImpl.owlTopDataProperty),
			s -> new DataPropertyExpressionImpl(s), "DataProperty not found: ");

	private final OntologyVocabularyCategoryImpl<AnnotationProperty> annotationProperties = new OntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(),
			s -> new AnnotationPropertyImpl(s), "AnnotationProperty not found: ");

	private static final String DATATYPE_NOT_FOUND = "Datatype not found: ";
	
	public OntologyVocabularyImpl() {		
	}


	@Override
	public Datatype getDatatype(String uri) {
		Datatype dt = OntologyImpl.OWL2QLDatatypes.get(uri);
		if (dt == null)
			throw new RuntimeException(DATATYPE_NOT_FOUND + uri);
		return dt;
	}
	
	
	@Override
	public OntologyVocabularyCategory<OClass> classes() {
		return classes;
	}

	@Override
	public OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties() {
		return objectProperties;
	}

	@Override
	public OntologyVocabularyCategory<DataPropertyExpression> dataProperties() {
		return dataProperties;
	}

	@Override
	public OntologyVocabularyCategory<AnnotationProperty> annotationProperties() { return annotationProperties; }

	
	@Override
	public void merge(Ontology ontology) {
		OntologyImpl imp = (OntologyImpl)ontology;

		classes.entities.putAll(imp.vocabulary.concepts.entrySet().stream()
				.filter(e -> !classes.builtins.containsKey(e.getKey()))
				.collect(ImmutableCollectors.toMap()));
		objectProperties.entities.putAll(imp.vocabulary.objectProperties.entrySet().stream()
				.filter(e -> !objectProperties.builtins.containsKey(e.getKey()))
				.collect(ImmutableCollectors.toMap()));
		dataProperties.entities.putAll(imp.vocabulary.dataProperties.entrySet().stream()
				.filter(e -> !dataProperties.builtins.containsKey(e.getKey()))
				.collect(ImmutableCollectors.toMap()));
		annotationProperties.entities.putAll(imp.vocabulary.annotationProperties);
	}
}
