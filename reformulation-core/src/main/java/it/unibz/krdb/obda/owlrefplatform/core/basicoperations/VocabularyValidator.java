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
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.ImmutableOntologyVocabulary;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VocabularyValidator {

	private final TBoxReasoner reasoner;
	private final ImmutableOntologyVocabulary voc;
	
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	

	public VocabularyValidator(TBoxReasoner reasoner, ImmutableOntologyVocabulary voc) {
		this.reasoner = reasoner;
		this.voc = voc;
	}


	public CQIE replaceEquivalences(CQIE query) {
		return dfac.getCQIE(query.getHead(), replaceEquivalences(query.getBody()));
	}
		
	private <T extends Term> List<T> replaceEquivalences(List<T> body) {
		List<T> result = new ArrayList<T>(body.size());
		
		// Get the predicates in the target query.
		for (Term t : body) {
			Term nt;
			if (t instanceof Function) {
				Function atom = (Function)t;

				if (atom.isBooleanFunction()) {
					nt = t;
				}
				else if (atom.isAlgebraFunction()) {
					// Calling recursively for nested expressions
					nt = dfac.getFunction(atom.getFunctionSymbol(), replaceEquivalences(atom.getTerms()));
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
				return dfac.getFunction(equivalent.getPredicate(), atom.getTerms());
		} 
		else if (/*p.isObjectProperty()*/ (p.getArity() == 2) && voc.containsObjectProperty(p.getName())) {
			ObjectPropertyExpression ope = voc.getObjectProperty(p.getName());
			ObjectPropertyExpression equivalent = reasoner.getObjectPropertyDAG().getCanonicalForm(ope);
			if (equivalent != null && !equivalent.equals(ope)) { 
				if (!equivalent.isInverse()) 
					return dfac.getFunction(equivalent.getPredicate(), atom.getTerms());
				else 
					return dfac.getFunction(equivalent.getPredicate(), atom.getTerm(1), atom.getTerm(0));
			}
		}
		else if (/*p.isDataProperty()*/ (p.getArity() == 2)  && voc.containsDataProperty(p.getName())) {
			DataPropertyExpression dpe = voc.getDataProperty(p.getName());
			DataPropertyExpression equivalent = reasoner.getDataPropertyDAG().getCanonicalForm(dpe);
			if (equivalent != null && !equivalent.equals(dpe)) 
				return dfac.getFunction(equivalent.getPredicate(), atom.getTerms());
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
		
		Collection<OBDAMappingAxiom> result = new ArrayList<OBDAMappingAxiom>(originalMappings.size());
		for (OBDAMappingAxiom mapping : originalMappings) {			
			CQIE targetQuery = mapping.getTargetQuery();	
			CQIE newTargetQuery = dfac.getCQIE(targetQuery.getHead(), replaceEquivalences(targetQuery.getBody()));
			result.add(dfac.getRDBMSMappingAxiom(mapping.getId(), mapping.getSourceQuery(), newTargetQuery));
		}
		return result;
	}
	
}
