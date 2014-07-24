package org.semanticweb.ontop.owlrefplatform.core.translator;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DataTypePredicate;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.OBDASQLQuery;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * This is a hack class that helps fix and OBDA model in which the mappings
 * include predicates that have not been properly typed.
 * 
 * @author mariano
 * 
 */
public class MappingVocabularyRepair {

	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	Logger log = LoggerFactory.getLogger(MappingVocabularyRepair.class);

	public void fixOBDAModel(OBDAModel model, Set<Predicate> vocabulary) {
		log.debug("Fixing OBDA Model");
		for (OBDADataSource source : model.getSources()) {
			Collection<OBDAMappingAxiom> mappings = new LinkedList<OBDAMappingAxiom>(model.getMappings(source.getSourceID()));
			model.removeAllMappings(source.getSourceID());
			try {
				model.addMappings(source.getSourceID(), fixMappingPredicates(mappings, vocabulary));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/***
	 * Makes sure that the mappings given are correctly typed w.r.t. the given
	 * vocabualry.
	 * 
	 * @param originalMappings
	 * @param equivalencesMap
	 * @return
	 */
	public Collection<OBDAMappingAxiom> fixMappingPredicates(Collection<OBDAMappingAxiom> originalMappings, Set<Predicate> vocabulary) {
		//		log.debug("Reparing/validating {} mappings", originalMappings.size());
		HashMap<String, Predicate> urimap = new HashMap<String, Predicate>();
		for (Predicate p : vocabulary) {
			urimap.put(p.getName(), p);
		}

		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
		for (OBDAMappingAxiom mapping : originalMappings) {
			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Function> body = targetQuery.getBody();
			List<Function> newbody = new LinkedList<Function>();

			for (Function atom : body) {
				Predicate p = atom.getPredicate();

				Function newatom = null;
				Predicate predicate = urimap.get(p.getName());

				/* Fixing terms */
				LinkedList<Term> newTerms = new LinkedList<Term>();
				for (Term term : atom.getTerms()) {
					newTerms.add(fixTerm(term));
				}

				if (predicate == null) {
					/**
					 * ignore triple  
					 */
					//if (!p.equals(OBDAVocabulary.QUEST_TRIPLE_PRED)){
					if (!p.isTriplePredicate()){
						//throw new RuntimeException("ERROR: Mapping references an unknown class/property: " + p.getName());
						log.warn("WARNING: Mapping references an unknown class/property: " + p.getName());
						
						/**
						 * All this part is to handle the case where the predicate or the class is defined
						 * by the mapping but not present in the ontology.
						 */
						if (newTerms.size()==1){
							predicate=dfac.getClassPredicate(p.getName());
						} else if (newTerms.size()==2){


							Term t1= newTerms.get(0);
							Term t2= newTerms.get(1);

							if (( t1 instanceof Function) && ( t2 instanceof Function)){

								Function ft1 = (Function) t1;
								Function ft2 = (Function) t2;

								boolean t1uri = ft1.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI);
								boolean t2uri = ft2.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI);

								if (t1uri && t2uri){
									predicate= dfac.getObjectPropertyPredicate(p.getName());
								}else{
									predicate=dfac.getDataPropertyPredicate(p.getName());
								}
							} else {
								throw new RuntimeException("ERROR: Predicate has an incorrect arity: " + p.getName());
							}
						}
					}else{
						predicate = OBDAVocabulary.QUEST_TRIPLE_PRED;
					}

				}// predicate null

				/*
				 * Fixing wrapping each variable with a URI function if the
				 * position corresponds to an URI only position
				 */
				Term t0 = newTerms.get(0);
				if (!(t0 instanceof Function)){
					newTerms.set(0, dfac.getFunction(dfac.getUriTemplatePredicate(1), t0));
				}
				if (predicate.isObjectProperty() && !(newTerms.get(1) instanceof Function)) {
					newTerms.set(1, dfac.getFunction(dfac.getUriTemplatePredicate(1), newTerms.get(1)));
				}
				newatom = dfac.getFunction(predicate, newTerms);
				newbody.add(newatom);
			} //end for
			
			CQIE newTargetQuery = dfac.getCQIE(targetQuery.getHead(), newbody);
			result.add(dfac.getRDBMSMappingAxiom(mapping.getId(), ((OBDASQLQuery) mapping.getSourceQuery()).toString(), newTargetQuery));
		}
//		log.debug("Repair done. Returning {} mappings", result.size());
		return result;
	}

	/***
	 * Fixes any issues with the terms in mappings
	 * 
	 * @param term
	 * @return
	 */
	public Term fixTerm(Term term) {
		Term result = term;
		if (term instanceof Function) {
			result = fixTerm((Function) term);
		}
		return result;
	}

	/***
	 * Fix functions that represent URI templates. Currently,the only fix
	 * necessary is replacing the old-style template function with the new one,
	 * that uses a string tempalte and placeholders.
	 * 
	 * @param term
	 * @return
	 */
	public Function fixTerm(Function term) {
		Predicate predicate = term.getFunctionSymbol();
		if (predicate instanceof DataTypePredicate) {
			// no fix nexessary
			return term;
		}
		if (predicate.getName().toString().equals(OBDAVocabulary.QUEST_URI)) {
			// no fix necessary
			return term;
		}
		// We have a function that is not a built-in, hence its an old-style uri
		// template function(parm1,parm2,...)
		Predicate uriFunction = dfac.getUriTemplatePredicate(term.getArity() + 1);

		StringBuilder newTemplate = new StringBuilder();
		newTemplate.append(predicate.getName().toString());
		for (int i = 0; i < term.getArity(); i++) {
			newTemplate.append("-{}");
		}

		LinkedList<Term> newTerms = new LinkedList<Term>();
		newTerms.add(dfac.getConstantLiteral(newTemplate.toString()));
		newTerms.addAll(term.getTerms());

		return dfac.getFunction(uriFunction, newTerms);
	}
}
