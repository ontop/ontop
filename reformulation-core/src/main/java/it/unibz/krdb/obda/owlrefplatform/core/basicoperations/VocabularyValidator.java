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
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class VocabularyValidator {

	private final TBoxReasoner reasoner;
	
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	public VocabularyValidator(TBoxReasoner reasoner) {
		this.reasoner = reasoner;
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

	public <T extends Term> void replaceEquivalences(List<T> body) {
		// Get the predicates in the target query.
		for (int i = 0; i < body.size(); i++) {
			Term t = body.get(i);
			if (t instanceof Function) {
				Function atom = (Function)t;

				/*
				 * Calling recursively for nested expressions
				 */
				if (atom.isAlgebraFunction()) {
					replaceEquivalences(atom.getTerms());
					continue;
				}
				
				if (atom.isBooleanFunction())
					continue;

				T newAtom = (T)getNormal(atom);

				body.set(i, newAtom);
			}
		}
	}
	
	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	public Function getNormal(Function atom) {
		Predicate p = atom.getFunctionSymbol();
		
		if (p.getArity() == 1) {
			OClass c = ofac.createClass(p.getName());
			OClass equivalent = reasoner.getClassRepresentative(c);
			if (equivalent != null)
				return dfac.getFunction(equivalent.getPredicate(), atom.getTerms());
		} 
		else {
			ObjectPropertyExpression op = ofac.createObjectProperty(p.getName());
			ObjectPropertyExpression equivalent = reasoner.getObjectPropertyRepresentative(op);
			if (equivalent != null) {
				if (!equivalent.isInverse()) 
					return dfac.getFunction(equivalent.getPredicate(), atom.getTerms());
				else 
					return dfac.getFunction(equivalent.getPredicate(), atom.getTerm(1), atom.getTerm(0));
			}
			else {
				DataPropertyExpression dp = ofac.createDataProperty(p.getName());
				DataPropertyExpression equiv2 = reasoner.getDataPropertyRepresentative(dp);
				if (equiv2 != null) {
					return dfac.getFunction(equiv2.getPredicate(), atom.getTerms());
				}				
			}
		}
		return atom;
	}

	/***
	 * Given a collection of mappings and an equivalence map for classes and
	 * properties, it returns a new collection in which all references to
	 * class/properties with equivalents has been removed and replaced by the
	 * equivalents.
	 * 
	 * For example, given the map hasFather -> inverse(hasChild)
	 * 
	 * If there is a mapping:
	 * 
	 * q(x,y):- hasFather(x,y) <- SELECT x, y FROM t
	 * 
	 * This will be replaced by the mapping
	 * 
	 * q(x,y):- hasChild(y,x) <- SELECT x, y FROM t
	 * 
	 * The same is done for classes.
	 * 
	 * @param originalMappings
	 * @return
	 */
	public Collection<OBDAMappingAxiom> replaceEquivalences(Collection<OBDAMappingAxiom> originalMappings) {
		
		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
		for (OBDAMappingAxiom mapping : originalMappings) {
			
			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			
			CQIE newTargetQuery = replaceEquivalences(targetQuery, false);
			result.add(dfac.getRDBMSMappingAxiom(mapping.getId(), mapping.getSourceQuery().toString(), newTargetQuery));

		}
		return result;
	}
	
}
