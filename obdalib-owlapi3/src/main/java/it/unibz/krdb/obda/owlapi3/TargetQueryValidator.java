package it.unibz.krdb.obda.owlapi3;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import it.unibz.krdb.obda.io.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.Vector;

public class TargetQueryValidator implements TargetQueryVocabularyValidator {
	
	/** The OBDA model for validating the target query */
	private final OBDAModel obdaModel;

	/** Data factory **/
	private final OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

	/** List of invalid predicates */
	private Vector<String> invalidPredicates = new Vector<String>();

	public TargetQueryValidator(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
	}
	
	@Override
	public boolean validate(CQIE targetQuery) {
		// Reset the invalid list
		invalidPredicates.clear();

		// Get the predicates in the target query.
		for (Function atom : targetQuery.getBody()) {
			Predicate p = atom.getFunctionSymbol();

			boolean isClass = isClass(p);
			boolean isObjectProp = isObjectProperty(p);
			boolean isDataProp = isDataProperty(p);
			boolean isTriple = isTriple(p);

			// Check if the predicate contains in the ontology vocabulary as one
			// of these components (i.e., class, object property, data property).
			boolean isPredicateValid = isClass || isObjectProp || isDataProp || isTriple;

			String debugMsg = "The predicate: [" + p.getName().toString() + "]";
			if (isPredicateValid) {
				Predicate predicate;
				if (isClass) {
					predicate = dataFactory.getClassPredicate(p.getName());
					debugMsg += " is a Class.";
				} else if (isObjectProp) {
					predicate = dataFactory.getObjectPropertyPredicate(p.getName());
					debugMsg += " is an Object property.";
				} else if (isDataProp) {
					predicate = dataFactory.getDataPropertyPredicate(p.getName(), COL_TYPE.LITERAL);
					debugMsg += " is a Data property.";
				}
				else
					predicate = dataFactory.getPredicate(p.getName(), atom.getArity());
				atom.setPredicate(predicate); // TODO Fix the API!
			} else {
				invalidPredicates.add(p.getName().toString());
			}
		}
		boolean isValid = true;
		if (!invalidPredicates.isEmpty()) {
			isValid = false; // if the list is not empty means the string is invalid!
		}
		return isValid;
	}

	@Override
	public Vector<String> getInvalidPredicates() {
		return invalidPredicates;
	}

	@Override
	public boolean isClass(Predicate predicate) {
		return obdaModel.getOntologyVocabulary().containsClass(predicate.getName());
	}
	
	@Override
	public boolean isObjectProperty(Predicate predicate) {
		return obdaModel.getOntologyVocabulary().containsObjectProperty(predicate.getName());
	}

	@Override
	public boolean isDataProperty(Predicate predicate) {
		return obdaModel.getOntologyVocabulary().containsDataProperty(predicate.getName());
	}
	
	@Override
	public boolean isTriple(Predicate predicate){
		return predicate.isTriplePredicate();
	}
}
