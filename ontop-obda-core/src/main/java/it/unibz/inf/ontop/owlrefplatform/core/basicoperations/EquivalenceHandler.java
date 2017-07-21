package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;

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

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.OClass;
import it.unibz.inf.ontop.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class EquivalenceHandler {

	private final TBoxReasoner reasoner;
	private final ImmutableOntologyVocabulary voc;

	public EquivalenceHandler(TBoxReasoner reasoner, ImmutableOntologyVocabulary voc) {
		this.reasoner = reasoner;
		this.voc = voc;
	}


	public CQIE replaceEquivalences(CQIE query) {
		return DATA_FACTORY.getCQIE(query.getHead(), replaceEquivalences(query.getBody()));
	}

	protected <T extends Term> List<T> replaceEquivalences(List<T> body) {
		List<T> result = new ArrayList<T>(body.size());

		// Get the predicates in the target query.
		for (Term t : body) {
			Term nt;
			if (t instanceof Function) {
				Function atom = (Function)t;

				if (atom.isOperation()) {
					nt = t;
				}
				else if (atom.isAlgebraFunction()) {
					// Calling recursively for nested expressions
					nt = DATA_FACTORY.getFunction(atom.getFunctionSymbol(), replaceEquivalences(atom.getTerms()));
				}
				else {
					nt = (T)getNormal(atom);
				}
			}
			else
				nt = t;
			result.add((T)nt);
		}
		return result;
	}
	
	public Function getNormal(Function atom) {
		Predicate p = atom.getFunctionSymbol();
		
		// the contains tests are inefficient, but tests fails without them
		// p.isClass etc. do not work correctly -- throw exceptions because COL_TYPE is null
		if (/*p.isClass()*/ (p.getArity() == 1) && voc.containsClass(p.getName())) {
			OClass c = voc.getClass(p.getName());
			OClass equivalent = (OClass)reasoner.getClassDAG().getCanonicalForm(c);
			if (equivalent != null && !equivalent.equals(c))
				return DATA_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerms());
		} 
		else if (/*p.isObjectProperty()*/ (p.getArity() == 2) && voc.containsObjectProperty(p.getName())) {
			ObjectPropertyExpression ope = voc.getObjectProperty(p.getName());
			ObjectPropertyExpression equivalent = reasoner.getObjectPropertyDAG().getCanonicalForm(ope);
			if (equivalent != null && !equivalent.equals(ope)) {
				if (!equivalent.isInverse()) 
					return DATA_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerms());
				else 
					return DATA_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerm(1), atom.getTerm(0));
			}
		}
		else if (/*p.isDataProperty()*/ (p.getArity() == 2)  && voc.containsDataProperty(p.getName())) {
			DataPropertyExpression dpe = voc.getDataProperty(p.getName());
			DataPropertyExpression equivalent = reasoner.getDataPropertyDAG().getCanonicalForm(dpe);
			if (equivalent != null && !equivalent.equals(dpe))
				return DATA_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerms());
		}
		return atom;
	}
	
}
