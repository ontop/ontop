package it.unibz.inf.ontop.datalog;

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

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.OClass;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;

import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

//TODO: rename, and possibly split
public class VocabularyValidator {

	private final TBoxReasoner reasoner;
	private final ImmutableOntologyVocabulary voc;

	public VocabularyValidator(TBoxReasoner reasoner, ImmutableOntologyVocabulary voc) {
		this.reasoner = reasoner;
		this.voc = voc;
	}


	public CQIE replaceEquivalences(CQIE query) {
		return DATALOG_FACTORY.getCQIE(query.getHead(), replaceEquivalences(query.getBody()));
	}

	protected <T extends Term> List<T> replaceEquivalences(List<T> body) {
		List<T> result = new ArrayList<>(body.size());

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
					nt = TERM_FACTORY.getFunction(atom.getFunctionSymbol(), replaceEquivalences(atom.getTerms()));
				}
				else {
					nt = getNormal(atom);
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
				return TERM_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerms());
		} 
		else if (/*p.isObjectProperty()*/ (p.getArity() == 2) && voc.containsObjectProperty(p.getName())) {
			ObjectPropertyExpression ope = voc.getObjectProperty(p.getName());
			ObjectPropertyExpression equivalent = reasoner.getObjectPropertyDAG().getCanonicalForm(ope);
			if (equivalent != null && !equivalent.equals(ope)) {
				if (!equivalent.isInverse()) 
					return TERM_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerms());
				else 
					return TERM_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerm(1), atom.getTerm(0));
			}
		}
		else if (/*p.isDataProperty()*/ (p.getArity() == 2)  && voc.containsDataProperty(p.getName())) {
			DataPropertyExpression dpe = voc.getDataProperty(p.getName());
			DataPropertyExpression equivalent = reasoner.getDataPropertyDAG().getCanonicalForm(dpe);
			if (equivalent != null && !equivalent.equals(dpe))
				return TERM_FACTORY.getFunction(equivalent.getPredicate(), atom.getTerms());
		}
		return atom;
	}
	
}
