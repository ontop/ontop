package it.unibz.inf.ontop.protege.core;


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
import it.unibz.inf.ontop.spec.ontology.impl.AnnotationPropertyImpl;
import it.unibz.inf.ontop.spec.ontology.impl.ClassImpl;
import it.unibz.inf.ontop.spec.ontology.impl.DataPropertyExpressionImpl;
import it.unibz.inf.ontop.spec.ontology.impl.ObjectPropertyExpressionImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implements MutableOntologyVocabulary
 * by providing look-up tables for classes, object and data properties
 * (checks whether the name has been declared)
 * 
 * NOTE: the sets of classes, object and data properties DO NOT contain 
 *       top/bottom elements
 *       HOWEVER, they are recognized as valid class and property names
 * 
 * @author Roman Kontchakov
 */

public class MutableOntologyVocabularyImpl implements MutableOntologyVocabulary {

	private static final class MutableOntologyVocabularyCategoryImpl<T> implements MutableOntologyVocabularyCategory<T> {
		private final Map<String, T> entities = new HashMap<>();

		private final ImmutableMap<String, T> builtins;
		private final Function<String, ? extends T> ctor;
		private final String NOT_FOUND_MESSAGE;

		MutableOntologyVocabularyCategoryImpl(ImmutableMap<String, T> builtins, Function<String, ? extends T> ctor, String NOT_FOUND_MESSAGE) {
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

	private final MutableOntologyVocabularyCategoryImpl<OClass> classes = new MutableOntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(ClassImpl.owlThingIRI, ClassImpl.owlThing,
					ClassImpl.owlNothingIRI, ClassImpl.owlNothing),
			ClassImpl::new, "Class not found: ");

	private final MutableOntologyVocabularyCategoryImpl<ObjectPropertyExpression> objectProperties = new MutableOntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(ObjectPropertyExpressionImpl.owlBottomObjectPropertyIRI, ObjectPropertyExpressionImpl.owlBottomObjectProperty,
					ObjectPropertyExpressionImpl.owlTopObjectPropertyIRI, ObjectPropertyExpressionImpl.owlTopObjectProperty),
			ObjectPropertyExpressionImpl::new, "ObjectProperty not found: ");

	private final MutableOntologyVocabularyCategoryImpl<DataPropertyExpression> dataProperties = new MutableOntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(DataPropertyExpressionImpl.owlBottomDataPropertyIRI, DataPropertyExpressionImpl.owlBottomDataProperty,
					DataPropertyExpressionImpl.owlTopDataPropertyIRI, DataPropertyExpressionImpl.owlTopDataProperty),
			DataPropertyExpressionImpl::new, "DataProperty not found: ");

	private final MutableOntologyVocabularyCategoryImpl<AnnotationProperty> annotationProperties = new MutableOntologyVocabularyCategoryImpl<>(
			ImmutableMap.of(),
			AnnotationPropertyImpl::new, "AnnotationProperty not found: ");

	public MutableOntologyVocabularyImpl() {
	}


	@Override
	public MutableOntologyVocabularyCategory<OClass> classes() {
		return classes;
	}

	@Override
	public MutableOntologyVocabularyCategory<ObjectPropertyExpression> objectProperties() {
		return objectProperties;
	}

	@Override
	public MutableOntologyVocabularyCategory<DataPropertyExpression> dataProperties() {
		return dataProperties;
	}

	@Override
	public MutableOntologyVocabularyCategory<AnnotationProperty> annotationProperties() { return annotationProperties; }
}
