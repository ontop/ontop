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

import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;

// TODO: move to a more appropriate package

public class TargetQueryValidator  {
	
	public static List<String> validate(List<? extends Function> targetQuery, MutableOntologyVocabulary vocabulary) {

	    return targetQuery.stream()
                .filter(atom -> !isValid(atom.getFunctionSymbol(), vocabulary))
                .map(atom -> atom.getFunctionSymbol().getName())
                .collect(ImmutableCollectors.toList());
	}

	public static boolean isValid(Predicate p, MutableOntologyVocabulary vocabulary) {

        return vocabulary.classes().contains(p.getName())
                || vocabulary.objectProperties().contains(p.getName())
                || vocabulary.dataProperties().contains(p.getName())
                || vocabulary.annotationProperties().contains(p.getName())
                || p.isTriplePredicate()
                || p.getName().equals(IriConstants.SAME_AS)
                || p.getName().equals(IriConstants.CANONICAL_IRI);
    }
}
