package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

/*
 * #%L
 * ontop-reformulation-core
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
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.UnionFriendlyBindingExtractor;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MappingSameAsPredicateExtractor {

    private final Mapping mapping;
    private Map<ValueConstant, ValueConstant> sameAsMap;

    private Set<Predicate> dataPropertiesAndClassesMapped;

    private Set<Predicate> objectPropertiesMapped;

    private final UnionFriendlyBindingExtractor extractor = new UnionFriendlyBindingExtractor();

    /**
     * Constructs a mapping containing the URI of owl:sameAs
     */
    public MappingSameAsPredicateExtractor(Mapping mapping) throws IllegalArgumentException {


        this.mapping = mapping;

        dataPropertiesAndClassesMapped = new HashSet<Predicate>();
        objectPropertiesMapped = new HashSet<Predicate>();

        retrieveSameAsMappingsURIs();

        retrievePredicatesWithSameAs();
    }

    /**
     * method that gets the uris from the mappings of same as and store it in a map
     */
    private void retrieveSameAsMappingsURIs() {

        sameAsMap = new HashMap<>();

        for (AtomPredicate predicate : mapping.getPredicates()) {

            if (predicate.getName().equals(OBDAVocabulary.SAME_AS) ) { // we check for owl same as

                IntermediateQuery definition = mapping.getDefinition(predicate)
                        .orElseThrow(() -> new IllegalStateException("The mapping contains a predicate without a definition " +
                                "(-> inconsistent)"));


                DistinctVariableOnlyDataAtom projectionAtom = definition.getProjectionAtom();

                Term term1 = projectionAtom.getTerm(0);
                Term term2 = projectionAtom.getTerm(1);


                if (term1 instanceof Function && term2 instanceof Function) {

                    Function uri1 = (Function) term1;
                    ValueConstant prefix1 = (ValueConstant) uri1.getTerm(0);

                    Function uri2 = (Function) term2;
                    ValueConstant prefix2 = (ValueConstant) uri2.getTerm(0);
                    sameAsMap.put(prefix1, prefix2);


                } else
                    throw new IllegalArgumentException("owl:samesAs is not built properly");

            }

        }

    }

    /*
    Get class data and object predicate that could refer to a same as
     */
    private void retrievePredicatesWithSameAs() {


        for (AtomPredicate predicate : mapping.getPredicates()) {

            IntermediateQuery definition = mapping.getDefinition(predicate)
                    .orElseThrow(() -> new IllegalStateException("The mapping contains a predicate without a definition " +
                            "(-> inconsistent)"));

            DistinctVariableOnlyDataAtom projectionAtom = definition.getProjectionAtom();

            if (projectionAtom.getArity() == 2) {

                Function term1 = (Function) projectionAtom.getTerm(0);
                Function term2 = (Function) projectionAtom.getTerm(1);
                boolean t1uri = (term1.getFunctionSymbol() instanceof URITemplatePredicate);
                boolean t2uri = (term2.getFunctionSymbol() instanceof URITemplatePredicate);

                //predicate is object property
                if (t1uri && t2uri ) {

                    if(!predicate.isSameAsProperty()){

                        Term prefix1 = term1.getTerm(0);
                        Term prefix2 =  term2.getTerm(0);

                        if (sameAsMap.containsKey(prefix1) ||  (sameAsMap.containsKey(prefix2))) {

                            objectPropertiesMapped.add(predicate);

                        }

                    }

                }
                //predicate is data property or a class
                else {

                    Term prefix1 = term1.getTerm(0);


                    if (sameAsMap.containsKey(prefix1)) {
//                        Function dataProperty = DATA_FACTORY.getFunction(functionSymbol,prefix1, term2);
                        dataPropertiesAndClassesMapped.add(predicate);
                    }


                }

            } else if (projectionAtom.getArity() == 1) { //case of class

                Term term1 = projectionAtom.getTerm(0);
                if (term1 instanceof Function) {
                    Function uri1 = (Function) term1;
                    if (uri1.getFunctionSymbol() instanceof URITemplatePredicate) {

                        Term prefix1 = uri1.getTerm(0);

                        if (sameAsMap.containsKey(prefix1)) {
                            dataPropertiesAndClassesMapped.add(predicate);
                        }
                    }
                }

            } else
                throw new IllegalArgumentException("error finding owl:sameAs related to " + projectionAtom);

        }

    }


    public ImmutableSet<Predicate> getDataPropertiesAndClassesWithSameAs() {

        return ImmutableSet.copyOf(dataPropertiesAndClassesMapped);
    }

    public ImmutableSet<Predicate> getObjectPropertiesWithSameAs() {

        return ImmutableSet.copyOf(objectPropertiesMapped);
    }
}

