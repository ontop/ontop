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
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// TODO This class needs to be restructured

public class QueryAnonymizer {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	/**
	 * Anonymizes the terms of an atom in a query, if they are anonymizable.
	 * Note that this will actually change the query terms by calling
	 * body.getTerms().set(i, new UndisintguishedVariable()) for each position i
	 * in the atom that can be anonymized.
	 * 
	 * Used only in DLRPerfectReformulator
	 * 
	 * @param q
	 * @param focusatomIndex
	 */
	public static void anonymize(CQIE q, int focusatomIndex) {

		List<Function> body = q.getBody();
		Function atom = (Function) body.get(focusatomIndex);
		int bodysize = body.size();
		int arity = atom.getPredicate().getArity();

		for (int i = 0; i < arity; i++) {
			Term term = atom.getTerms().get(i);
			if (term instanceof VariableImpl) {
				if (isVariableInHead(q, term))
					continue;
				/*
				 * Not in the head, it could be anonymizable, checking if the
				 * term appears in any other position in the query
				 */
				boolean isSharedTerm = false;
				for (int atomindex = 0; atomindex < bodysize; atomindex++) {
					Function currentAtom = (Function) body.get(atomindex);
					int currentarity = currentAtom.getArity();
					List<Term> currentTerms = currentAtom.getTerms();
					for (int termidx = 0; termidx < currentarity; termidx++) {
						Term comparisonTerm = currentTerms.get(termidx);
						/*
						 * If the terms is a variable that is not in the same
						 * atom or in the same position in the atom then we
						 * compare to check if they are equal, if they are equal
						 * then isShared will be set to true
						 */
						if ((comparisonTerm instanceof VariableImpl)
								&& ((atomindex != focusatomIndex) || (i != termidx))) {
							isSharedTerm = term.equals(comparisonTerm);

						} else if (comparisonTerm instanceof Function) {
							isSharedTerm = comparisonTerm
									.getReferencedVariables().contains(term);
						}
						if (isSharedTerm) {
							break;
						}
					}
					if (isSharedTerm)
						break;
				}
				/*
				 * If we never found the term in any other position, then we
				 * anonymize it
				 */
				if (!isSharedTerm) {
					atom.getTerms().set(i,
							fac.getVariableNondistinguished());
				}
			}
		}
	}

	public static CQIE anonymize(CQIE q) {

		HashMap<String, List<Function>> auxmap = new HashMap<String, List<Function>>();

		/*
		 * Collecting all variables and the places where they appear (Function and
		 * position)
		 */
		List<Function> body = q.getBody();
		for (Function atom : body) {
			List<Term> terms = atom.getTerms();
			for (Term t : terms) 
				collectAuxiliaries(t, atom, auxmap);
		}

		LinkedList<Function> newBody = new LinkedList<Function>();
		for (Function atom : body) {
			LinkedList<Term> vex = new LinkedList<Term>();
			for (Term t : atom.getTerms()) {
				List<Function> list = null;
				if (t instanceof VariableImpl) {
					list = auxmap.get(((VariableImpl) t).getName());
				}
				if (list != null && list.size() < 2 && !isVariableInHead(q, t)) {
					vex.add(fac.getVariableNondistinguished());
				} else {
					vex.add(t);
				}
			}
			Function newatom = fac.getFunction(atom.getPredicate().clone(), vex);
			newBody.add(newatom);
		}
		CQIE query = fac.getCQIE(q.getHead(), newBody);
		return query;
	}

	/**
	 * collects occurrences of variables
	 * 
	 * @param term
	 * @param atom
	 * @param auxmap: maps variable names to atoms they occur in
	 */
	
	private static void collectAuxiliaries(Term term, Function atom, HashMap<String, List<Function>> auxmap) {
		if (term instanceof Variable) {
			Variable var = (Variable) term;
			//Object[] obj = new Object[2];
			//obj[0] = atom;
			List<Function> list = auxmap.get(var.getName());
			if (list == null) {
				list = new LinkedList<Function>();
				auxmap.put(var.getName(), list);
			}
			list.add(atom);
		} 
		else if (term instanceof Function) {
			Function funct = (Function) term;
			for (Term t : funct.getTerms()) {
				collectAuxiliaries(t, atom, auxmap);
			}
		} 
		else {
			// NO-OP
			// Ignore constants
		}
	}

	private static boolean isVariableInHead(CQIE q, Term t) {
		if (t instanceof AnonymousVariable)
			return false;

		Function head = q.getHead();
		List<Term> headterms = head.getTerms();
		for (Term headterm : headterms) {
			if (headterm instanceof FunctionalTermImpl) {
				FunctionalTermImpl fterm = (FunctionalTermImpl) headterm;
				if (fterm.containsTerm(t))
					return true;
			} 
			else if (headterm.equals(t))
				return true;
		}
		return false;
	}

	/**
	 * method that enumerates all undistinguished variables in the given data
	 * log program. This will also remove any instances of
	 * UndisinguishedVariable and replace them by instance of Variable
	 * (enumerated as mentioned before). This step is needed to ensure that the
	 * algorithm treats each undistinguished variable as a unique variable.
	 * 
	 * Needed because the rewriter might generate query bodies like this B(x,_),
	 * R(x,_), underscores represent unique anonymous variables. However, the
	 * SQL generator needs them to be explicitly unique. replacing B(x,newvar1),
	 * R(x,newvar2)
	 * 
	 * @param dp
	 */

	public static CQIE deAnonymize(CQIE query) {
		
		query = query.clone();
		Function head = query.getHead();
		
		int counter = 1;
		LinkedList<Term> newTerms = new LinkedList<Term>();
		for (Term t : head.getTerms()) {
			if (t instanceof AnonymousVariable) {
				String newName = "_uv-" + counter;
				counter++;
				Term newT = fac.getVariable(newName);
				newTerms.add(newT);
			} else {
				newTerms.add(t);
			}
		}
		head.updateTerms(newTerms);

		for (Function a : query.getBody()) {
			LinkedList<Term> vec = new LinkedList<Term>();
			for (Term t : a.getTerms()) {
				if (t instanceof AnonymousVariable) {
					String newName = "_uv-" + counter;
					counter++;
					Term newT = fac.getVariable(newName);
					vec.add(newT);
				} else {
					vec.add(t);
				}
			}
			a.updateTerms(vec);
		}
		return query;
	}
}
