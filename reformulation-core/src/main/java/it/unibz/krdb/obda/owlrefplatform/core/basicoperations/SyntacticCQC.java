package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SyntacticCQC {
	/**
	 * Removes all atoms that are equal (syntactically) and then all the atoms
	 * that are redundant due to CQC.
	 * 
	 * @param queries
	 */
	public static HashSet<CQIE> removeDuplicateAtoms(Collection<CQIE> queries) {
		HashSet<CQIE> newqueries = new HashSet<CQIE>(queries.size() * 2);
		for (CQIE cq : queries) {
			List<Function> body = cq.getBody();
			for (int i = 0; i < body.size(); i++) {
				Function currentAtom = body.get(i);
				for (int j = i + 1; j < body.size(); j++) {
					Function comparisonAtom = body.get(j);
					if (currentAtom.getPredicate().equals(comparisonAtom.getPredicate())) {
						if (currentAtom.equals(comparisonAtom)) {
							body.remove(j);
						}
					}
				}
			}
			removeRundantAtoms(cq);
			newqueries.add(QueryAnonymizer.anonymize(cq));
		}
		return newqueries;
	}

	/***
	 * Removes all atoms that are redundant w.r.t to query containment.This is
	 * done by going through all unifiable atoms, attempting to unify them. If
	 * they unify with a MGU that is empty, then one of the atoms is redundant.
	 * 
	 * 
	 * @param q
	 */
	public static void removeRundantAtoms(CQIE q) {
		CQIE result = q;
		for (int i = 0; i < result.getBody().size(); i++) {
			Function currentAtom = result.getBody().get(i);
			for (int j = i + 1; j < result.getBody().size(); j++) {
				Function nextAtom = result.getBody().get(j);
				Map<Variable, Term> map = Unifier.getMGU(currentAtom, nextAtom);
				if (map != null && map.isEmpty()) {
					result = Unifier.unify(result, i, j);
				}
			}
		}
	}
	
	/***
	 * Check if query cq1 is contained in cq2, syntactically. That is, if the
	 * head of cq1 and cq2 are equal according to toString().equals and each
	 * atom in cq2 is also in the body of cq1 (also by means of
	 * toString().equals().
	 * 
	 * @param cq1
	 * @param cq2
	 * @return
	 */
	public static boolean isContainedInSyntactic(CQIE cq1, CQIE cq2) {
		if (!cq2.getHead().equals(cq1.getHead())) 
			return false;

        List<Function> body = cq2.getBody();
        if (body.isEmpty())
            return false;

		for (Function atom : body) {
			// if (!body1.contains(atom))
			// return false;
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
	public static void removeContainedQueriesSyntactic(List<CQIE> queries) {

		for (int i = 0; i < queries.size(); i++) {
			for (int j = queries.size() - 1; j > i; j--) {
				if (isContainedInSyntactic(queries.get(i), queries.get(j))) {
//					log.debug("REMOVE: " + queries.get(i));
					queries.remove(i);
					i = -1;
					break;
				}
			}
		}

		{
			for (int i = queries.size() - 1; i > 0; i--) {
				for (int j = 0; j < i; j++) {
					if (isContainedInSyntactic(queries.get(i), queries.get(j))) {
//						log.debug("REMOVE: " + queries.get(i));
						queries.remove(i);
						i = +1;
						break;
					}
				}
			}
		}
	}
	
}
