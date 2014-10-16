package org.semanticweb.ontop.owlapi3;

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

import java.util.Vector;

import org.semanticweb.ontop.io.TargetQueryVocabularyValidator;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

public class TargetQueryValidator implements TargetQueryVocabularyValidator {
	
	/** The OBDA model for validating the target query */
	private OBDAModel obdaModel;

	/** Data factory **/
	private OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

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
			Predicate p = atom.getPredicate();

			boolean isClass = isClass(p);
			boolean isObjectProp = isObjectProperty(p);
			boolean isDataProp = isDataProperty(p);
			boolean isTriple = isTriple(p);

			// Check if the predicate contains in the ontology vocabulary as one
			// of these components (i.e., class, object property, data property).
			boolean isPredicateValid = isClass || isObjectProp || isDataProp || isTriple;

			String debugMsg = "The predicate: [" + p.getName().toString() + "]";
			if (isPredicateValid) {
				COL_TYPE colType[] = null;
				if (isClass) {
					colType = new COL_TYPE[] { COL_TYPE.OBJECT };
					debugMsg += " is a Class.";
				} else if (isObjectProp) {
					colType = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT };
					debugMsg += " is an Object property.";
				} else if (isDataProp) {
					colType = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL };
					debugMsg += " is a Data property.";
				}
				Predicate predicate = dataFactory.getPredicate(p.getName(), atom.getArity(), colType);
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
		return obdaModel.isDeclaredClass(predicate);
	}
	
	@Override
	public boolean isObjectProperty(Predicate predicate) {
		return obdaModel.isDeclaredObjectProperty(predicate);
	}

	@Override
	public boolean isDataProperty(Predicate predicate) {
		return obdaModel.isDeclaredDataProperty(predicate);
	}
	
	@Override
	public boolean isTriple(Predicate predicate){
		return predicate.equals(OBDAVocabulary.QUEST_TRIPLE_PRED);
	}
}
