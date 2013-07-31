/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// TODO This class needs to be restructured

public class QueryAnonymizer {

	private static final OBDADataFactory termFactory = OBDADataFactoryImpl
			.getInstance();

	public DatalogProgram anonymize(DatalogProgram prog) {

		DatalogProgram newProg = termFactory.getDatalogProgram();
		List<CQIE> rules = prog.getRules();
		Iterator<CQIE> it = rules.iterator();
		while (it.hasNext()) {
			CQIE q = it.next();
			newProg.appendRule(anonymize(q));
		}

		return newProg;
	}

	/**
	 * Anonymizes the terms of an atom in a query, if they are anonymizable.
	 * Note that this will actually change the query terms by calling
	 * body.getTerms().set(i, new UndisintguishedVariable()) for each position i
	 * in the atom that can be anonymized.
	 * 
	 * @param q
	 * @param focusatomIndex
	 */
	public void anonymize(CQIE q, int focusatomIndex) {

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
							termFactory.getNondistinguishedVariable());
				}
			}
		}
	}

	public Collection<CQIE> anonymize(Collection<CQIE> cqs) {
		HashSet<CQIE> anonymous = new HashSet<CQIE>(1000);
		for (CQIE cq : cqs) {
			anonymous.add(anonymize(cq));
		}
		return anonymous;
	}

	public CQIE anonymize(CQIE q) {

		HashMap<String, List<Object[]>> auxmap = new HashMap<String, List<Object[]>>();

		/*
		 * Collecting all variables and the places where they appear (Function and
		 * position)
		 */
		List<Function> body = q.getBody();
		Iterator<Function> it = body.iterator();
		while (it.hasNext()) {
			Function atom = (Function) it.next();
			List<Term> terms = atom.getTerms();
			int pos = 0;
			for (Term t : terms) {
				collectAuxiliaries(t, atom, pos, auxmap);
			}
			pos++;
		}

		Iterator<Function> it2 = body.iterator();
		LinkedList<Function> newBody = new LinkedList<Function>();
		while (it2.hasNext()) {
			Function atom = (Function) it2.next();
			List<Term> terms = atom.getTerms();
			Iterator<Term> term_it = terms.iterator();
			LinkedList<Term> vex = new LinkedList<Term>();
			while (term_it.hasNext()) {
				Term t = term_it.next();
				List<Object[]> list = null;
				if (t instanceof VariableImpl) {
					list = auxmap.get(((VariableImpl) t).getName());
				}
				if (list != null && list.size() < 2 && !isVariableInHead(q, t)) {
					vex.add(termFactory.getNondistinguishedVariable());
				} else {
					vex.add(t);
				}
			}
			Function newatom = termFactory
					.getFunction(atom.getPredicate().clone(), vex);
			newBody.add(newatom);
		}
		CQIE query = termFactory.getCQIE(q.getHead(), newBody);
		return query;
	}

	private void collectAuxiliaries(Term term, Function atom, int pos,
			HashMap<String, List<Object[]>> auxmap) {
		if (term instanceof Variable) {
			Variable var = (Variable) term;
			Object[] obj = new Object[2];
			obj[0] = atom;
			obj[1] = pos;
			List<Object[]> list = auxmap.get(var.getName());
			if (list == null) {
				list = new LinkedList<Object[]>();
			}
			list.add(obj);
			auxmap.put(var.getName(), list);
		} else if (term instanceof Function) {
			Function funct = (Function) term;
			for (Term t : funct.getTerms()) {
				collectAuxiliaries(t, atom, pos, auxmap);
			}
		} else {
			// NO-OP
			// Ignore constants
		}
	}

	private boolean isVariableInHead(CQIE q, Term t) {
		if (t instanceof AnonymousVariable)
			return false;

		Function head = q.getHead();
		List<Term> headterms = head.getTerms();
		for (Term headterm : headterms) {
			if (headterm instanceof FunctionalTermImpl) {
				FunctionalTermImpl fterm = (FunctionalTermImpl) headterm;
				if (fterm.containsTerm(t))
					return true;
			} else if (headterm.equals(t))
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
	 * R(x,_), underscores reperesnt uniquie anonymous varaibles. However, the
	 * SQL generator needs them to be explicitly unique. replacing B(x,newvar1),
	 * R(x,newvar2)
	 * 
	 * @param dp
	 */
	public static DatalogProgram deAnonymize(DatalogProgram dp) {
		DatalogProgram result = termFactory.getDatalogProgram();
		Iterator<CQIE> it = dp.getRules().iterator();
		while (it.hasNext()) {
			CQIE query = it.next();
			result.appendRule(deAnonymize(query));
		}
		result.setQueryModifiers(dp.getQueryModifiers());
		return result;
	}

	public static CQIE deAnonymize(CQIE query) {
		// query = query.clone();
		Function head = query.getHead();
		Iterator<Term> hit = head.getTerms().iterator();
		OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
		int coutner = 1;
		int i = 0;
		LinkedList<Term> newTerms = new LinkedList<Term>();
		while (hit.hasNext()) {
			Term t = hit.next();
			if (t instanceof AnonymousVariable) {
				String newName = "_uv-" + coutner;
				coutner++;
				Term newT = factory.getVariable(newName);
				newTerms.add(newT);
			} else {
				newTerms.add(t);
			}
			i++;
		}
		head.updateTerms(newTerms);

		Iterator<Function> bit = query.getBody().iterator();
		while (bit.hasNext()) {
			Function a = (Function) bit.next();
			Iterator<Term> hit2 = a.getTerms().iterator();
			i = 0;
			LinkedList<Term> vec = new LinkedList<Term>();
			while (hit2.hasNext()) {
				Term t = hit2.next();
				if (t instanceof AnonymousVariable) {
					String newName = "_uv-" + coutner;
					coutner++;
					Term newT = factory.getVariable(newName);
					vec.add(newT);
				} else {
					vec.add(t);
				}
				i++;
			}
			a.updateTerms(vec);
		}
		return query;
	}
}
