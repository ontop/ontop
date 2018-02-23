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
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import org.semanticweb.owlapi.model.IRI;
import org.semarglproject.vocab.OWL;

import java.util.*;
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

	private static final class MutableOntologyVocabularyCategoryImpl implements MutableOntologyVocabularyCategory {
		private final Map<String, Predicate> entities = new HashMap<>();
		private final ImmutableSet<String> builtins;
		private final Function<String, Predicate> ctor;

		MutableOntologyVocabularyCategoryImpl(ImmutableSet<String> builtins, Function<String, Predicate> ctor) {
            this.builtins = builtins;
            this.ctor = ctor;
		    builtins.forEach(c -> entities.put(c, ctor.apply(c)));
		}

		@Override
		public boolean contains(String iri) { return entities.containsKey(iri); }

		@Override
		public void declare(IRI iri) {
		    String s = iri.toString();
		    if (!entities.containsKey(s)) {
		        Predicate p = ctor.apply(s);
                entities.put(s, p);
            }
		}

		@Override
		public void remove(IRI iri) {
            String s = iri.toString();
		    if (!builtins.contains(s))
		        entities.remove(s);
		}

		@Override
		public Iterator<Predicate> iterator() { return entities.values().iterator(); }
	}

	private final MutableOntologyVocabularyCategoryImpl classes;

	private final MutableOntologyVocabularyCategoryImpl objectProperties;
	private final MutableOntologyVocabularyCategoryImpl dataProperties;
	private final MutableOntologyVocabularyCategoryImpl annotationProperties;

	// package only
	MutableOntologyVocabularyImpl(AtomFactory atomFactory) {
		classes = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of(OWL.THING, OWL.NOTHING),
				atomFactory::getClassPredicate);

		objectProperties = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of(OWL.BOTTOM_OBJECT_PROPERTY, OWL.TOP_OBJECT_PROPERTY),
				atomFactory::getObjectPropertyPredicate);

		dataProperties = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of(OWL.BOTTOM_DATA_PROPERTY, OWL.TOP_DATA_PROPERTY),
				atomFactory::getDataPropertyPredicate);

		annotationProperties = new MutableOntologyVocabularyCategoryImpl(
				ImmutableSet.of(),
				atomFactory::getAnnotationPropertyPredicate);
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
