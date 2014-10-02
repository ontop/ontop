package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

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

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.EquivalenceMap;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryVocabularyValidator /*implements Serializable*/ {

	//private static final long serialVersionUID = -2901421485090507301L;

	private final Logger log = LoggerFactory.getLogger(QueryVocabularyValidator.class);

	/** The source ontology vocabulary for validating the target query */
	private final Set<Predicate> vocabulary;

	private final EquivalenceMap equivalences;


	public QueryVocabularyValidator(Set<Predicate> vocabulary, EquivalenceMap equivalences) {
		this.vocabulary = vocabulary;
		this.equivalences = equivalences;
	}
/*
	public boolean validatePredicates0(DatalogProgram input) {

		boolean isValid = true;
		
		for (CQIE query : input.getRules()) {
			for  (Function atom : query.getBody()) {
				if (!validate(atom))
					isValid = false;
			}
		}

		return isValid;
	}
*/
/*	
	private boolean validate(Function atom) {

		Predicate predicate = atom.getPredicate();

//		boolean isClass = vocabulary.contains(predicate)
//				|| equivalences.containsKey(predicate);
//		boolean isObjectProp =  vocabulary.contains(predicate)
//				|| equivalences.containsKey(predicate);
//		boolean isDataProp = vocabulary.contains(predicate)
//				|| equivalences.containsKey(predicate);
//		boolean isBooleanOpFunction = (predicate instanceof BooleanOperationPredicate);

		// Check if the predicate contains in the ontology vocabulary as one
		// of these components (i.e., class, object property, data property).
		// isClass || isObjectProp || isDataProp || isBooleanOpFunction;
		boolean isPredicateValid = vocabulary.contains(predicate)
				|| equivalences.containsKey(predicate) 
				|| (predicate instanceof BooleanOperationPredicate);

		if (!isPredicateValid) {
			String debugMsg = "The predicate: [" + predicate.toString() + "]";
			log.warn("WARNING: {} is missing in the ontology!", debugMsg);
			return false;
		}
		return true;
	}
*/
	/*
	 * Substitute atoms based on the equivalence map.
	 */
	public DatalogProgram replaceEquivalences(DatalogProgram queries) {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		DatalogProgram newprogram = fac.getDatalogProgram();
		newprogram.setQueryModifiers(queries.getQueryModifiers());
		for (CQIE query : queries.getRules()) {
			newprogram.appendRule(replaceEquivalences(query.clone(), true));
		}
		return newprogram;
	}

	public CQIE replaceEquivalences(CQIE query, boolean inplace) {
		if (!inplace) {
			query = query.clone();
		}
		replaceEquivalences(query.getBody());
		return query;
	}

	public void replaceEquivalences(List body) {
		// Get the predicates in the target query.
		for (int i = 0; i < body.size(); i++) {
			Function atom = (Function) body.get(i);

			/*
			 * Calling recursively for nested expressions
			 */
			if (atom.isAlgebraFunction()) {
				replaceEquivalences(atom.getTerms());
				continue;
			}
			
			if (atom.isBooleanFunction())
				continue;

			Function newatom = equivalences.getNormal(atom);

			body.set(i, newatom);
		}
	}
}
