package org.obda.owlrefplatform.core.basicoperations;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Function;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.ValueConstant;
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.FunctionalTermImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.VariableImpl;

/***
 * A class that allows you to perform different operations related to query
 * containment on conjunctive queries.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class CQCUtilities {

	private CQIE					canonicalQuery		= null;

	private static AtomUnifier		unifier				= new AtomUnifier();

	private static QueryAnonymizer	anonymizer			= new QueryAnonymizer();

	private static TermFactory		termFactory			= TermFactoryImpl.getInstance();

	List<Atom>						canonicalbody		= null;

	Atom							canonicalhead		= null;

	Set<Predicate>					canonicalpredicates	= new HashSet<Predicate>(50);

	public CQCUtilities(CQIE query) {
		this.canonicalQuery = getCanonicalQuery(query);
		canonicalbody = canonicalQuery.getBody();
		canonicalhead = canonicalQuery.getHead();
		for (Atom atom : canonicalbody) {
			canonicalpredicates.add(atom.getPredicate());
		}
	}

	/***
	 * Computes a query in which all variables have been replaced by
	 * ValueConstants that have the no type and have the same 'name' as the
	 * original variable.
	 * 
	 * This new query can be used for query containment checking.
	 * 
	 * @param q
	 */
	public static CQIE getCanonicalQuery(CQIE q) {
		CQIE canonicalquery = q.clone();

		int constantcounter = 1;

		Map<Variable, Term> substitution = new HashMap<Variable, Term>(50);
		Atom head = canonicalquery.getHead();
		constantcounter = getCanonicalAtom(head, constantcounter, substitution);

		for (Atom atom : canonicalquery.getBody()) {
			constantcounter = getCanonicalAtom(atom, constantcounter, substitution);
		}
		return canonicalquery;
	}

	/***
	 * Replaces each Variable inside the atom with a constant. It will return
	 * the counter updated + n, where n is the number of constants introduced by
	 * the method.
	 * 
	 * @param atom
	 * @param constantcounter
	 *            an initial counter for the naming of the new constants. This
	 *            is needed to provide a numbering to each of the new constants
	 * @return
	 */
	private static int getCanonicalAtom(Atom atom, int constantcounter, Map<Variable, Term> currentMap) {
		List<Term> headterms = atom.getTerms();
		for (int i = 0; i < headterms.size(); i++) {
			Term term = headterms.get(i);
			if (term instanceof Variable) {
				Term substitution = null;
				if (term instanceof VariableImpl) {
					substitution = currentMap.get(term);
					if (substitution == null) {
						ValueConstant newconstant = termFactory.createValueConstant("CAN" + term.getName() + constantcounter);
						constantcounter += 1;
						currentMap.put((Variable) term, newconstant);
						substitution = newconstant;
					}
				} else {
					ValueConstant newconstant = termFactory.createValueConstant("CAN" + term.getName() + constantcounter);
					constantcounter += 1;
					substitution = newconstant;
				}
				headterms.set(i, substitution);
			} else if (term instanceof Function) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				List<Term> functionterms = function.getTerms();
				for (int j = 0; j < functionterms.size(); j++) {
					Term fterm = functionterms.get(j);
					if (fterm instanceof Variable) {
						Term substitution = null;
						if (fterm instanceof VariableImpl) {
							substitution = currentMap.get(fterm);
							if (substitution == null) {
								ValueConstant newconstant = termFactory.createValueConstant("CAN" + fterm.getName() + constantcounter);
								constantcounter += 1;
								currentMap.put((Variable) fterm, newconstant);
								substitution = newconstant;

							}
						} else {
							ValueConstant newconstant = termFactory.createValueConstant("CAN" + fterm.getName() + constantcounter);
							constantcounter += 1;
							substitution = newconstant;
						}
						functionterms.set(j, substitution);
					}
				}
			}
		}
		return constantcounter;
	}

	/***
	 * True if the query used to construct this CQCUtilities object is is
	 * contained in 'query'.
	 * 
	 * @param query
	 * @return
	 */
	public boolean isContainedIn(CQIE query) {
		CQIE duplicate1 = query.clone();

		if (!query.getHead().getPredicate().equals(canonicalhead.getPredicate()))
			return false;

		for (Atom queryatom : query.getBody()) {
			if (!canonicalpredicates.contains(queryatom.getPredicate())) {
				return false;
			}
		}

		return hasAnswer(duplicate1);
	}

	/***
	 * Returns true if the query can be answered using the ground atoms in the
	 * body of our canonical query.
	 * 
	 * @param query
	 * @return
	 */
	private boolean hasAnswer(CQIE query) {

		List<Atom> body = query.getBody();
		for (int atomidx = 0; atomidx < body.size(); atomidx++) {
			Atom currentAtomTry = body.get(atomidx);

			for (int groundatomidx = 0; groundatomidx < canonicalbody.size(); groundatomidx++) {
				Atom currentGroundAtom = canonicalbody.get(groundatomidx);

				Map<Variable, Term> mgu = unifier.getMGU(currentAtomTry, currentGroundAtom);
				if (mgu != null) {
					CQIE satisfiedquery = unifier.applyUnifier(query.clone(), mgu);
					satisfiedquery.getBody().remove(atomidx);

					if (satisfiedquery.getBody().size() == 0)
						if (canonicalhead.toString().equals(satisfiedquery.getHead().toString()))
							return true;

					if (hasAnswer(satisfiedquery))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes all atoms that are equalt (syntactically) and then all the atoms
	 * that are redundant due to CQC.
	 * 
	 * @param queries
	 * @throws Exception
	 */
	public static HashSet<CQIE> removeDuplicateAtoms(Collection<CQIE> queries) throws Exception {
		HashSet<CQIE> newqueries = new HashSet<CQIE>(queries.size() * 2);
		for (CQIE cq : queries) {
			List<Atom> body = cq.getBody();
			for (int i = 0; i < body.size(); i++) {
				Atom currentAtom = body.get(i);
				for (int j = i + 1; j < body.size(); j++) {
					Atom comparisonAtom = body.get(j);
					if (currentAtom.getPredicate().equals(comparisonAtom.getPredicate())) {
						if (currentAtom.toString().equals(comparisonAtom.toString())) {
							body.remove(j);
						}
					}
				}
			}
			newqueries.add(anonymizer.anonymize(removeRundantAtoms(cq)));
		}
		return newqueries;
	}

	/***
	 * Removes all atoms that are redundant w.r.t to query containment.This is
	 * done by going through all unifyiable atoms, attempting to unify them. If
	 * they unify with a MGU that is empty, then one of the atoms is redundant.
	 * 
	 * 
	 * @param q
	 * @throws Exception
	 */
	public static CQIE removeRundantAtoms(CQIE q) throws Exception {
		CQIE result = q;
		for (int i = 0; i < result.getBody().size(); i++) {
			Atom currentAtom = result.getBody().get(i);
			for (int j = i + 1; j < result.getBody().size(); j++) {
				Atom nextAtom = result.getBody().get(j);
				Map<Variable, Term> map = unifier.getMGU(currentAtom, nextAtom);
				if (map != null && map.isEmpty()) {
					result = unifier.unify(result, i, j);
				}
			}

		}
		return result;
	}

}
