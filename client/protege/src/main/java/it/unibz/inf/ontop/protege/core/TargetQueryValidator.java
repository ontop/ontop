package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-obdalib-owlapi
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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

// TODO: move to a more appropriate package

public class TargetQueryValidator  {
	
	public static ImmutableList<IRI> validate(ImmutableList<TargetAtom> targetQuery, MutableOntologyVocabulary vocabulary) {
	    return targetQuery.stream()
				.map(TargetQueryValidator::extractIRI)
                .filter(iri -> !isValid(iri, vocabulary))
                .collect(ImmutableCollectors.toList());
	}

	private static IRI extractIRI(TargetAtom targetAtom) {
		return targetAtom.getPredicateIRI()
				.orElseThrow(() -> new NoPredicateIRIInTargetAtomException(targetAtom));
	}

	public static boolean isValid(IRI iri, MutableOntologyVocabulary vocabulary) {

        return vocabulary.classes().contains(iri)
                || vocabulary.objectProperties().contains(iri)
                || vocabulary.dataProperties().contains(iri)
                || vocabulary.annotationProperties().contains(iri)
                || iri.equals(OWL.SAME_AS)
                || iri.equals(Ontop.CANONICAL_IRI);
    }

    private static class NoPredicateIRIInTargetAtomException extends OntopInternalBugException {
		private NoPredicateIRIInTargetAtomException(TargetAtom targetAtom) {
			super("No IRI could be found the predicate in the target atom " + targetAtom
					+ "\nShould have detected before");
		}
	}
}
