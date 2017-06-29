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
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.UnionFriendlyBindingExtractor;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MappingSameAsPredicateExtractor {

    private final Mapping mapping;
    private Set<ImmutableTerm> sameAsSet;

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


        for (AtomPredicate predicate : mapping.getPredicates()) {

            if (predicate.getName().equals(OBDAVocabulary.SAME_AS) ) { // we check for owl same as

                IntermediateQuery definition = mapping.getDefinition(predicate)
                        .orElseThrow(() -> new IllegalStateException("The mapping contains a predicate without a definition " +
                                "(-> inconsistent)"));


                SameAsIRIsExtractor extractor = new SameAsIRIsExtractor(definition);
                Optional<ImmutableSet<ImmutableTerm>> sameAsIRIs = extractor.getIRIs();
                if(sameAsIRIs.isPresent()){
                   sameAsSet = sameAsIRIs.get();

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



            if (!(predicate.getName().equals(OBDAVocabulary.SAME_AS) )) {

                    SameAsIRIsExtractor extractor = new SameAsIRIsExtractor(definition);
                    Optional<ImmutableSet<ImmutableTerm>> predicatesIRIs = extractor.getIRIs();

                    if(predicatesIRIs.isPresent()){
                        ImmutableSet<ImmutableTerm> predicatesIRIsSet = predicatesIRIs.get();

                        if (!Sets.intersection(sameAsSet, predicatesIRIsSet).isEmpty()){
                            if(extractor.isObjectProperty()){
                                objectPropertiesMapped.add(predicate);
                            }
                            else{
                                dataPropertiesAndClassesMapped.add(predicate);
                            }

                        }

                    } else
                        throw new IllegalArgumentException("property is not built properly");


            }


        }

    }


    public ImmutableSet<Predicate> getDataPropertiesAndClassesWithSameAs() {

        return ImmutableSet.copyOf(dataPropertiesAndClassesMapped);
    }

    public ImmutableSet<Predicate> getObjectPropertiesWithSameAs() {

        return ImmutableSet.copyOf(objectPropertiesMapped);
    }
}

