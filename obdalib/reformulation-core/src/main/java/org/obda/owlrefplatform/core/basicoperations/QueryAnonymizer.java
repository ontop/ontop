package org.obda.owlrefplatform.core.basicoperations;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.DatalogProgram;
import inf.unibz.it.obda.model.OBDADataFactory;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.Variable;
import inf.unibz.it.obda.model.impl.AtomImpl;
import inf.unibz.it.obda.model.impl.CQIEImpl;
import inf.unibz.it.obda.model.impl.DatalogProgramImpl;
import inf.unibz.it.obda.model.impl.FunctionalTermImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;
import inf.unibz.it.obda.model.impl.UndistinguishedVariable;
import inf.unibz.it.obda.model.impl.VariableImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//TODO This class needs to be restructured

public class QueryAnonymizer {

	private final OBDADataFactory	termFactory	= OBDADataFactoryImpl.getInstance();

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

		List<Atom> body = q.getBody();
		Atom atom = body.get(focusatomIndex);
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
					Atom currentAtom = body.get(atomindex);
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
						if ((comparisonTerm instanceof VariableImpl) && ((atomindex != focusatomIndex) || (i != termidx))) {
							isSharedTerm = term.equals(comparisonTerm);
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
					atom.getTerms().set(i, termFactory.getNondistinguishedVariable());
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
		List<Atom> body = q.getBody();
		Iterator<Atom> it = body.iterator();
		while (it.hasNext()) {
			Atom atom = it.next();
			List<Term> terms = atom.getTerms();
			int pos = 0;
			Iterator<Term> term_it = terms.iterator();
			while (term_it.hasNext()) {
				Term t = term_it.next();
				if (t instanceof Variable) {
					Object[] obj = new Object[2];
					obj[0] = atom;
					obj[1] = pos;
					List<Object[]> list = auxmap.get(t.getName());
					if (list == null) {
						list = new LinkedList<Object[]>();
					}
					list.add(obj);
					auxmap.put(t.getName(), list);
				}
			}
		}

		Iterator<Atom> it2 = body.iterator();
		LinkedList<Atom> newBody = new LinkedList<Atom>();
		while (it2.hasNext()) {
			Atom atom = it2.next();
			List<Term> terms = atom.getTerms();
			Iterator<Term> term_it = terms.iterator();
			LinkedList<Term> vex = new LinkedList<Term>();
			while (term_it.hasNext()) {
				Term t = term_it.next();
				List<Object[]> list = auxmap.get(t.getName());
				if (list != null && list.size() < 2 && !isVariableInHead(q, t)) {
					vex.add(termFactory.getNondistinguishedVariable());
				} else {
					vex.add(t);
				}
			}
			Atom newatom = termFactory.getAtom(atom.getPredicate().copy(), vex);
			newBody.add(newatom);
		}
		CQIE query = termFactory.getCQIE(q.getHead(), newBody);
		return query;
	}

	private boolean isVariableInHead(CQIE q, Term t) {
		if (t instanceof UndistinguishedVariable)
			return false;

		Atom head = q.getHead();
		List<Term> headterms = head.getTerms();
		for (Term headterm : headterms) {
			if (headterm instanceof FunctionalTermImpl) {
				FunctionalTermImpl fterm = (FunctionalTermImpl)headterm;
				if (fterm.containsTerm(t))
					return true;
			} else if (headterm.equals(t))
				return true;
		}
		return false;
	}
}
