package it.unibz.krdb.obda.owlrefplatform.core.translator;

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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/***
 * This is a hack class that helps fix and OBDA model in which the mappings
 * include predicates that have not been properly typed.
 * 
 * @author mariano
 * 
 */
public class MappingVocabularyRepair {

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(MappingVocabularyRepair.class);

	public static void fixOBDAModel(OBDAModel model, OntologyVocabulary vocabulary) {
		log.debug("Fixing OBDA Model");
		for (OBDADataSource source : model.getSources()) {
			Collection<OBDAMappingAxiom> mappings = new LinkedList<>(model.getMappings(source.getSourceID()));
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
	 * vocabulary.
	 * 
	 * @param originalMappings
	 * @param vocabulary
	 * @return
	 */
	private static Collection<OBDAMappingAxiom> fixMappingPredicates(Collection<OBDAMappingAxiom> originalMappings, OntologyVocabulary vocabulary) {
		//		log.debug("Reparing/validating {} mappings", originalMappings.size());

		Map<String, Predicate> urimap = new HashMap<>();
		for (OClass p : vocabulary.getClasses()) 
			urimap.put(p.getPredicate().getName(), p.getPredicate());

		for (ObjectPropertyExpression p : vocabulary.getObjectProperties()) 
			urimap.put(p.getPredicate().getName(), p.getPredicate());
		
		for (DataPropertyExpression p : vocabulary.getDataProperties()) 
			urimap.put(p.getPredicate().getName(), p.getPredicate());

		Collection<OBDAMappingAxiom> result = new LinkedList<>();
		for (OBDAMappingAxiom mapping : originalMappings) {
			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Function> newbody = new LinkedList<>();

			for (Function atom : targetQuery.getBody()) {
				Predicate p = atom.getFunctionSymbol();

				/* Fixing terms */
				List<Term> newTerms = new LinkedList<>();
				for (Term term : atom.getTerms()) {
					newTerms.add(fixTerm(term));
				}
				
				Function newatom = null;

				Predicate predicate = urimap.get(p.getName());
				if (predicate == null) {
					if (!p.isTriplePredicate()) {
						log.warn("WARNING: Mapping references an unknown class/property: " + p.getName());
						
						/*
						 * All this part is to handle the case where the predicate or the class is defined
						 * by the mapping but not present in the ontology.
						 */
						if (newTerms.size() == 1) {
							Predicate pred = dfac.getClassPredicate(p.getName());
							newatom = dfac.getFunction(pred, getNormalTerm(newTerms.get(0)));
						} 
						else if (newTerms.size() == 2) {
							Term t1 = newTerms.get(0);
							Term t2 = newTerms.get(1);

							if ((t1 instanceof Function) && (t2 instanceof Function)) {

								Function ft1 = (Function) t1;
								Function ft2 = (Function) t2;

								boolean t1uri = (ft1.getFunctionSymbol() instanceof URITemplatePredicate);  
								boolean t2uri = (ft2.getFunctionSymbol() instanceof URITemplatePredicate);

								if (t1uri && t2uri) {
									Predicate pred = dfac.getObjectPropertyPredicate(p.getName());
									newatom = dfac.getFunction(pred, getNormalTerm(t1), getNormalTerm(t2));

                                    if(p.getName().equals("http://www.w3.org/2002/07/owl#sameAs")){
                                    //need to add also the inverse
                                        Function inverseAtom = dfac.getFunction(pred, getNormalTerm(t2), getNormalTerm(t1));
                                        newbody.add(inverseAtom);
                                    }
								}
								else {
									Predicate pred = dfac.getDataPropertyPredicate(p.getName());
									newatom = dfac.getFunction(pred, getNormalTerm(t1), t2);
								}
							} 
							else  {
								System.err.println("INTERNAL ERROR: " + p.getName());
								throw new RuntimeException("INTERNAL ERROR: " + p.getName());
							}
						}
						else  {
							System.err.println("ERROR: Predicate has an incorrect arity: " + p.getName());
							throw new RuntimeException("ERROR: Predicate has an incorrect arity: " + p.getName());
						}
					}
					else {
						// TODO (ROMAN): WHY ONLY THE SUBJECT IS NORMALIZED?
						newatom = dfac.getTripleAtom(getNormalTerm(newTerms.get(0)), newTerms.get(1), newTerms.get(2));
					}
				}
				else {
					
					if (newTerms.size() == 1) {
						newatom = dfac.getFunction(predicate, getNormalTerm(newTerms.get(0)));
					}
					else if (newTerms.size() == 2) {
						if (predicate.isObjectProperty()) {
							newatom = dfac.getFunction(predicate, getNormalTerm(newTerms.get(0)), getNormalTerm(newTerms.get(1)));							
						}
						else {
							newatom = dfac.getFunction(predicate, getNormalTerm(newTerms.get(0)), newTerms.get(1));							
						}
					}
					else 
						throw new RuntimeException("ERROR: Predicate has an incorrect arity: " + p.getName());			
				}

				newbody.add(newatom);
			} //end for
			
			CQIE newTargetQuery = dfac.getCQIE(targetQuery.getHead(), newbody);
			result.add(dfac.getRDBMSMappingAxiom(mapping.getId(), ((OBDASQLQuery) mapping.getSourceQuery()).toString(), newTargetQuery));
		}
//		log.debug("Repair done. Returning {} mappings", result.size());
		return result;
	}
	
	/**
	 * Fixing wrapping each variable with a URI function if the
	 * position corresponds to an URI only position
	 */
	private static Term getNormalTerm(Term t) {
		if (!(t instanceof Function)) {
			return dfac.getUriTemplate(t);
		}
		else
			return t;
	}

	/***
	 * Fix functions that represent URI templates. Currently,the only fix
	 * necessary is replacing the old-style template function with the new one,
	 * that uses a string template and placeholders.
	 * 
	 * @param term
	 * @return
	 */
	private static Term fixTerm(Term term) {
		if (term instanceof Function) {
			Function fterm = (Function)term;
			Predicate predicate = fterm.getFunctionSymbol();
			if (predicate instanceof DatatypePredicate) {
				// no fix necessary
				return term;
			}
			if (predicate instanceof URITemplatePredicate) {
				// no fix necessary
				return term;
			}
			// We have a function that is not a built-in, hence its an old-style uri
			// template function(parm1,parm2,...)
			StringBuilder newTemplate = new StringBuilder();
			newTemplate.append(predicate.getName().toString());
			for (int i = 0; i < fterm.getArity(); i++) {
				newTemplate.append("-{}");
			}

			LinkedList<Term> newTerms = new LinkedList<>();
			newTerms.add(dfac.getConstantLiteral(newTemplate.toString()));
			newTerms.addAll(fterm.getTerms());

			return dfac.getUriTemplate(newTerms);
		}
		return term;
	}

}
