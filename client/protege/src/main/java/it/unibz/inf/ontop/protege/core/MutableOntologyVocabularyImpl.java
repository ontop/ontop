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


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import org.apache.commons.rdf.api.IRI;

import java.util.*;

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

	private static final class MutableOntologyVocabularyCategoryImpl implements MutableOntologyVocabularyCategory {
		private final Set<IRI> entities;
		private final ImmutableSet<IRI> builtins;

		MutableOntologyVocabularyCategoryImpl(ImmutableSet<IRI> builtins) {
            this.builtins = builtins;
            entities = new HashSet<>(builtins);
		}

		@Override
		public boolean contains(IRI iri) { return entities.contains(iri); }

		@Override
		public void declare(IRI iri) {
			entities.add(iri);
		}

		@Override
		public void remove(IRI iri) {
		    if (!builtins.contains(iri))
		        entities.remove(iri);
		}

		@Override
		public Iterator<IRI> iterator() { return entities.iterator(); }
	}

	private final MutableOntologyVocabularyCategoryImpl classes;

	private final MutableOntologyVocabularyCategoryImpl objectProperties;
	private final MutableOntologyVocabularyCategoryImpl dataProperties;
	private final MutableOntologyVocabularyCategoryImpl annotationProperties;

	// package only
	MutableOntologyVocabularyImpl() {
		classes = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of(OWL.THING, OWL.NOTHING));

		objectProperties = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of(OWL.BOTTOM_OBJECT_PROPERTY, OWL.TOP_OBJECT_PROPERTY));

		dataProperties = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of(OWL.BOTTOM_DATA_PROPERTY, OWL.TOP_DATA_PROPERTY));

		annotationProperties = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of());
	}

	@Override
	public MutableOntologyVocabularyCategory classes() {
		return classes;
	}

	@Override
	public MutableOntologyVocabularyCategory objectProperties() {
		return objectProperties;
	}

	@Override
	public MutableOntologyVocabularyCategory dataProperties() {
		return dataProperties;
	}

	@Override
	public MutableOntologyVocabularyCategory annotationProperties() { return annotationProperties; }
}
