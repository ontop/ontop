package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;

public class SyntacticCQC {

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
				Unifier map = Unifier.getMGU(currentAtom, nextAtom);
				if (map != null && map.isEmpty()) {
					result = UnifierUtilities.unify(result, i, j);
				}
			}
		}
	}
	
}
