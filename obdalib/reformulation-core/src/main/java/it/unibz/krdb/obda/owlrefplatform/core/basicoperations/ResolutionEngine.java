package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

import java.util.List;
import java.util.Map;

public class ResolutionEngine {

	/**
	 * This method resolves a query atom (atomidx) against a rule's head,
	 * producing as output a new query q'. The original atom will be removed and
	 * replaced with the body of the resolved rule, which is appended at the end
	 * of original body.
	 * 
	 * All the restrictions specified in the class AtomUnifier about unification
	 * apply here to.
	 * 
	 * @param rule
	 * @param query
	 * @param atomidx
	 * @return
	 */
	public static CQIE resolve(CQIE rule, CQIE query, int atomidx) {
		Map<Variable, Term> mgu = Unifier.getMGU(rule.getHead(), query.getBody().get(atomidx));
		if (mgu == null)
			return null;

		CQIE newquery = query.clone();
		List<Atom> body = newquery.getBody();
		body.remove(atomidx);
		body.addAll(rule.clone().getBody()); 

		// newquery contains only clones atoms, so it is safe to unify "in-place"
		Unifier.applyUnifierInPlace(newquery, mgu);
		return newquery;
	}
}
