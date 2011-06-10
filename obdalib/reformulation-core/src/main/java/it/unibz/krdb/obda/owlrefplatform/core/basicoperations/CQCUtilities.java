package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

	private static OBDADataFactory		termFactory			= OBDADataFactoryImpl.getInstance();

	List<Atom>						canonicalbody		= null;

	Atom							canonicalhead		= null;

	Set<Predicate>					canonicalpredicates	= new HashSet<Predicate>(50);

	static Logger					log					= LoggerFactory.getLogger(CQCUtilities.class);

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
						ValueConstant newconstant = termFactory.getValueConstant("CAN" + term.getName() + constantcounter);
						constantcounter += 1;
						currentMap.put((Variable) term, newconstant);
						substitution = newconstant;
					}
				} else {
					ValueConstant newconstant = termFactory.getValueConstant("CAN" + term.getName() + constantcounter);
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
								ValueConstant newconstant = termFactory.getValueConstant("CAN" + fterm.getName() + constantcounter);
								constantcounter += 1;
								currentMap.put((Variable) fterm, newconstant);
								substitution = newconstant;

							}
						} else {
							ValueConstant newconstant = termFactory.getValueConstant("CAN" + fterm.getName() + constantcounter);
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
				if (mgu == null)
					continue;

				CQIE satisfiedquery = unifier.applyUnifier(query.clone(), mgu);
				satisfiedquery.getBody().remove(atomidx);

				if (satisfiedquery.getBody().size() == 0)
					if (canonicalhead.equals(satisfiedquery.getHead()))
						return true;
				/*
				 * Stopping early if we have chosen an MGU that has no
				 * possibility of being successful because of the head.
				 */
				if (unifier.getMGU(canonicalhead, satisfiedquery.getHead()) == null) {
					continue;
				}
				if (hasAnswer(satisfiedquery))
					return true;

			}
		}
		return false;
	}

	private boolean hasAnswer2(CQIE query2) {

		LinkedList<CQIE> querystack = new LinkedList<CQIE>();
		querystack.addLast(query2);
		LinkedList<Integer> lastAttemptIndexStack = new LinkedList<Integer>();
		lastAttemptIndexStack.addLast(0);

		while (!querystack.isEmpty()) {

			CQIE currentquery = querystack.pop();
			List<Atom> body = currentquery.getBody();
			int atomidx = lastAttemptIndexStack.pop();
			Atom currentAtomTry = body.get(atomidx);

			for (int groundatomidx = 0; groundatomidx < canonicalbody.size(); groundatomidx++) {
				Atom currentGroundAtom = canonicalbody.get(groundatomidx);

				Map<Variable, Term> mgu = unifier.getMGU(currentAtomTry, currentGroundAtom);
				if (mgu != null) {
					CQIE satisfiedquery = unifier.applyUnifier(currentquery.clone(), mgu);
					satisfiedquery.getBody().remove(atomidx);

					if (satisfiedquery.getBody().size() == 0) {
						if (canonicalhead.equals(satisfiedquery.getHead())) {
							return true;
						}
					}

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
						if (currentAtom.equals(comparisonAtom)) {
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

	/***
	 * Removes queries that are contained syntactically, using the method
	 * isContainedInSyntactic(CQIE q1, CQIE 2). To make the process more
	 * efficient, we first sort the list of queries as to have longer queries
	 * first and shorter queries last.
	 * 
	 * Removal of queries is done in two main double scans. The first scan goes
	 * top-down/down-top, the second scan goes down-top/top-down
	 * 
	 * @param queries
	 */
	public static void removeContainedQueriesSyntacticSorter(List<CQIE> queries, boolean twopasses) {
		int initialsize = queries.size();
		log.debug("Removing trivially redundant queries. Initial set size: {}:", initialsize);
		long startime = System.currentTimeMillis();
		
		Comparator<CQIE> lenghtComparator = new Comparator<CQIE>() {

			@Override
			public int compare(CQIE o1, CQIE o2) {
				return o2.getBody().size() - o1.getBody().size();
			}
		};

		Collections.sort(queries, lenghtComparator);

		for (int i = 0; i < queries.size(); i++) {
			for (int j = queries.size() - 1; j > i; j--) {
				if (isContainedInSyntactic(queries.get(i), queries.get(j))) {
					queries.remove(i);
					i = -1;
					break;
				}
			}
		}

		if (twopasses) {
			for (int i = queries.size() - 1; i > 0; i--) {
				for (int j = 0; j < i; j++) {
					if (isContainedInSyntactic(queries.get(i), queries.get(j))) {
						queries.remove(i);
						i = +1;
						break;
					}
				}
			}
		}

		
		int newsize = queries.size();
		int queriesremoved = initialsize - newsize;
		long endtime = System.currentTimeMillis();
		long time = (endtime - startime)/1000;
		log.debug("Done. Time elapse: {}s", time);
		log.debug("Resulting size: {}   Queries removed: {}", newsize, queriesremoved);

	}

	/***
	 * Check if query cq1 is contained in cq2, sintactically. That is, if the
	 * head of cq1 and cq2 are equal according to toString().equals and each
	 * atom in cq2 is also in the body of cq1 (also by means of
	 * toString().equals().
	 * 
	 * @param cq1
	 * @param cq2
	 * @return
	 */
	public static boolean isContainedInSyntactic(CQIE cq1, CQIE cq2) {
		if (!cq2.getHead().equals(cq1.getHead())) {
			return false;
		}

		for (Atom atom : cq2.getBody()) {
			if (!cq1.getBody().contains(atom))
				return false;
		}
		return true;
	}

	/***
	 * Removes queries that are contained syntactically, using the method
	 * isContainedInSyntactic(CQIE q1, CQIE 2). To make the process more
	 * efficient, we first sort the list of queries as to have longer queries
	 * first and shorter queries last.
	 * 
	 * Removal of queries is done in two main double scans. The first scan goes
	 * top-down/down-top, the second scan goes down-top/top-down
	 * 
	 * @param queries
	 */
	public static void removeContainedQueriesSorted(List<CQIE> queries, boolean twopasses) {

		
		int initialsize = queries.size();
		log.debug("Removing CQC redundant queries. Initial set size: {}:", initialsize);
		
		long startime = System.currentTimeMillis();
		
		
		Comparator<CQIE> lenghtComparator = new Comparator<CQIE>() {

			@Override
			public int compare(CQIE o1, CQIE o2) {
				return o2.getBody().size() - o1.getBody().size();
			}
		};

		Collections.sort(queries, lenghtComparator);

		for (int i = 0; i < queries.size(); i++) {
			CQCUtilities cqc = new CQCUtilities(queries.get(i));
				for (int j = queries.size() - 1; j > i; j--) {
					if (cqc.isContainedIn(queries.get(j))) {
						queries.remove(i);
						i -= 1;
						break;
					}
				}
		}

		if (twopasses) {
			for (int i = (queries.size() - 1); i >= 0; i--) {
				CQCUtilities cqc = new CQCUtilities(queries.get(i));
				for (int j = 0; j < i; j++) {
					if (cqc.isContainedIn(queries.get(j))) {
						queries.remove(i);
						break;
					}
				}
			}
		}

		int newsize = queries.size();
		int queriesremoved = initialsize - newsize;
		
		
		long endtime = System.currentTimeMillis();
		long time = (endtime - startime)/1000;
		log.debug("Done. Time elapse: {}s", time);
		log.debug("Resulting size: {}   Queries removed: {}", newsize, queriesremoved);

	}

	//
	// private HashSet<CQIE> removeContainedQueries(Collection<CQIE> queries) {
	// HashSet<CQIE> result = new HashSet<CQIE>(queries.size());
	//
	// LinkedList<CQIE> workingcopy = new LinkedList<CQIE>();
	// workingcopy.addAll(queries);
	//
	// for (int i = 0; i < workingcopy.size(); i++) {
	// CQCUtilities cqcutil = new CQCUtilities(workingcopy.get(i));
	// for (int j = i + 1; j < workingcopy.size(); j++) {
	// if (cqcutil.isContainedIn(workingcopy.get(j))) {
	// workingcopy.remove(i);
	// i = -1;
	// break;
	// }
	//
	// CQCUtilities cqcutil2 = new CQCUtilities(workingcopy.get(j));
	// if (cqcutil2.isContainedIn(workingcopy.get(i)))
	// workingcopy.remove(j);
	//
	// }
	// }
	// result.addAll(workingcopy);
	// return result;
	// }

}
