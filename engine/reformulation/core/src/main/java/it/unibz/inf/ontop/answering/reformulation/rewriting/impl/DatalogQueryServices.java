package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

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

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.datalog.impl.CQCUtilities;
import it.unibz.inf.ontop.datalog.EQNormalizer;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatalogQueryServices {
	
	private static final Logger log = LoggerFactory.getLogger(DatalogQueryServices.class);
	private final TermFactory termFactory;
	private final UnifierUtilities unifierUtilities;
	private final SubstitutionUtilities substitutionUtilities;
	private final EQNormalizer eqNormalizer;
	private final CQCUtilities cqcUtilities;

	@Inject
	private DatalogQueryServices(TermFactory termFactory, UnifierUtilities unifierUtilities, SubstitutionUtilities substitutionUtilities,
								 EQNormalizer eqNormalizer, CQCUtilities cqcUtilities) {
		this.termFactory = termFactory;
		this.unifierUtilities = unifierUtilities;
		this.substitutionUtilities = substitutionUtilities;
		this.eqNormalizer = eqNormalizer;
		this.cqcUtilities = cqcUtilities;
	}

	// to be taken from it.unibz.inf.ontop.owlrefplatform.core.unfolding.DatalogUnfolder
	
	private Function getFreshAtom(Function a, String suffix) {
		List<Term> termscopy = new ArrayList<>(a.getArity());
		
		for (Term t : a.getTerms()) {
			if (t instanceof Variable) {
				Variable v = (Variable)t;
				termscopy.add(termFactory.getVariable(v.getName() + suffix));
			}
			else
				termscopy.add(t.clone());
		}
		return termFactory.getFunction(a.getFunctionSymbol(), termscopy);
		
	}
	
	public List<CQIE> plugInDefinitions(List<CQIE> rules, DatalogProgram defs) {
		
		PriorityQueue<CQIE> queue = new PriorityQueue<CQIE>(rules.size(), new Comparator<CQIE> () {
			@Override
			public int compare(CQIE arg0, CQIE arg1) {
				return arg0.getBody().size() - arg1.getBody().size();
			} 
			});

		queue.addAll(rules);
				
		List<CQIE> output = new LinkedList<CQIE>();
				
		while (!queue.isEmpty()) {
			CQIE query = queue.poll();
			//log.debug("QUEUE SIZE: " + queue.size() + " QUERY " + query);
				
			List<Function> body = query.getBody();
			int chosenAtomIdx = 0;
			List<CQIE> chosenDefinitions = null;
			ListIterator<Function> bodyIterator = body.listIterator();
			while (bodyIterator.hasNext()) {
				Function currentAtom = bodyIterator.next(); // body.get(i);	

				List<CQIE> definitions = defs.getRules(currentAtom.getFunctionSymbol());
				if ((definitions != null) && (definitions.size() != 0)) {
					if ((chosenDefinitions == null) || (chosenDefinitions.size() < definitions.size())) {
						chosenDefinitions = definitions;
						chosenAtomIdx = bodyIterator.previousIndex();
					}
				}
			}

			boolean replaced = false;
			if (chosenDefinitions != null) {
				int maxlen = 0;
				for (Variable v : query.getReferencedVariables())
					maxlen = Math.max(maxlen, v.getName().length());
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < maxlen; i++)
					sb.append('t');
				String suffix = sb.toString();
				
				for (CQIE rule : chosenDefinitions) {				
					//CQIE newquery = ResolutionEngine.resolve(rule, query, chosenAtomIdx);					
					Substitution mgu = unifierUtilities.getMGU(getFreshAtom(rule.getHead(), suffix),
                            query.getBody().get(chosenAtomIdx));
					if (mgu != null) {
						CQIE newquery = query.clone();
						List<Function> newbody = newquery.getBody();
						newbody.remove(chosenAtomIdx);
						for (Function a : rule.getBody())   
							newbody.add(getFreshAtom(a, suffix));
												
						// newquery contains only cloned atoms, so it is safe to unify "in-place"
						substitutionUtilities.applySubstitution(newquery, mgu, false);
						
						// REDUCE
						eqNormalizer.enforceEqualities(newquery);
						//makeSingleOccurrencesAnonymous(q.getBody(), q.getHead().getTerms());
						// newquery = QueryAnonymizer.anonymize(newquery); // TODO: make it in place
						cqcUtilities.removeRundantAtoms(newquery);
						
						queue.add(newquery);
						replaced = true;
					}

				}						
			}
			if (!replaced) {
				boolean found = false;
				ListIterator<CQIE> i = output.listIterator();
				while (i.hasNext()) {
					CQIE q2 = i.next();
					if (CQCUtilities.SYNTACTIC_CHECK.isContainedIn(query, q2)) {
						found = true;
						break;
					}
					else if (CQCUtilities.SYNTACTIC_CHECK.isContainedIn(q2, query)) {
						i.remove();				
						log.debug("   PRUNED {} BY {}", q2, query);
					}
				}
				
				if (!found) {
					log.debug("ADDING TO THE RESULT {}", query);
					
					output.add(query.clone());			
					Collections.sort(output, new Comparator<CQIE> () {
						@Override
						public int compare(CQIE arg1, CQIE arg0) {
							return arg1.getBody().size() - arg0.getBody().size();
						} 
						});
				}
			}
		}
		
		return output;
	}
}
