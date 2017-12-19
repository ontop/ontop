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
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.ontology.impl.ClassImpl;
import it.unibz.inf.ontop.spec.ontology.impl.DataPropertyExpressionImpl;
import it.unibz.inf.ontop.spec.ontology.impl.ObjectPropertyExpressionImpl;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

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

	private final MutableOntologyVocabularyCategoryImpl classes = new MutableOntologyVocabularyCategoryImpl(
			ImmutableSet.of(ClassImpl.owlThingIRI, ClassImpl.owlNothingIRI),
            c -> TERM_FACTORY.getClassPredicate(c));

	private final MutableOntologyVocabularyCategoryImpl objectProperties = new MutableOntologyVocabularyCategoryImpl(
			ImmutableSet.of(ObjectPropertyExpressionImpl.owlBottomObjectPropertyIRI,
					ObjectPropertyExpressionImpl.owlTopObjectPropertyIRI),
            c -> TERM_FACTORY.getObjectPropertyPredicate(c));

	private final MutableOntologyVocabularyCategoryImpl dataProperties = new MutableOntologyVocabularyCategoryImpl(
			ImmutableSet.of(DataPropertyExpressionImpl.owlBottomDataPropertyIRI,
					DataPropertyExpressionImpl.owlTopDataPropertyIRI),
            c -> TERM_FACTORY.getDataPropertyPredicate(c));

	private final MutableOntologyVocabularyCategoryImpl annotationProperties = new MutableOntologyVocabularyCategoryImpl(
			ImmutableSet.of(),
            c -> TERM_FACTORY.getAnnotationPropertyPredicate(c));

	// package only
	MutableOntologyVocabularyImpl() { }

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
