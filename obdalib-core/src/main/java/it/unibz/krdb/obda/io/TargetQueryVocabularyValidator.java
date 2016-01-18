package it.unibz.krdb.obda.io;

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

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;

import java.util.List;

public interface TargetQueryVocabularyValidator {

	boolean validate(List<Function> targetQuery);

	List<String> getInvalidPredicates();

	/**
	 * Checks whether the predicate is a class assertion.
	 * 
	 * @param predicate
	 *            The target predicate.
	 * @return Returns true if the predicate is a class assertion from the input
	 *         ontology, or false otherwise.
	 */
    boolean isClass(Predicate predicate);

	/**
	 * Checks whether the predicate is a object property assertion.
	 * 
	 * @param predicate
	 *            The target predicate.
	 * @return Returns true if the predicate is a object property assertion from
	 *         the input ontology, or false otherwise.
	 */
    boolean isObjectProperty(Predicate predicate);

	/**
	 * Checks whether the predicate is a data property assertion.
	 * 
	 * @param predicate
	 *            The target predicate.
	 * @return Returns true if the predicate is a data property assertion from
	 *         the input ontology, or false otherwise.
	 */
    boolean isDataProperty(Predicate predicate);


	/**
	 * Checks whether the predicate is an annotation property assertion.
	 *
	 * @param predicate
	 *            The target predicate.
	 * @return Returns true if the predicate is an annotation property assertion from
	 *         the input ontology, or false otherwise.
	 */
    boolean isAnnotationProperty(Predicate predicate);


	/**
	 * Checks whether the predicate is a "triple", which is used for meta mapping
	 * 
	 * @param predicate a Predicate
     *
	 * @return
	 * 	True if the predicate is "triple", or false otherwise
	 */
	boolean isTriple(Predicate predicate);
}
