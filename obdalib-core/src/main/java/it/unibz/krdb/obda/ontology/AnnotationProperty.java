package it.unibz.krdb.obda.ontology;

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

import it.unibz.krdb.obda.model.Predicate;

/**
 * Represents AnnotationProperty from the OWL 2 Specification
 *
 * AnnotationProperty := IRI
 *
 * @author Sarah
 *
 */

public interface AnnotationProperty extends Description {

    /**
     * the name of the annotation property
     *
     * @return the predicate symbol that corresponds to the annotation property name
     */

    public Predicate getPredicate();


    public String getName();


    /**
     * the domain iri for the annotation property
     * <p>
     *
     *
     * @return iri  for the domain
     */

//    public AnnotationPropertyDomain getDomain();

    /**
     * the range iri for the annotation property
     * <p>
     * (
     * <p>
     *
     * @return iri for the range
     */

//    public AnnotationPropertyRange getRange();


}
