package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

import java.util.Map;

public class ResolutionEngine {

	Unifier unifier = null;

	public ResolutionEngine() {
		unifier = new Unifier();
	}

	/**
	 * This method resolves a query atom (atomidx) against a rule's head,
	 * producing as output a new query q'. The original atom will be removed and
	 * replaced with the body of the resolved rule, which is appended at the end
	 * of original body.
	 * 
	 * 
	 * 
	 * All the restrictions specified in the class AtomUnifier about unification
	 * apply here to.
	 * 
	 * @param rule
	 * @param query
	 * @param atomidx
	 * @return
	 */
	public CQIE resolve(CQIE rule, CQIE query, int atomidx) {
		CQIE newquery = query.clone();
		Map<Variable, Term> mgu = unifier.getMGU(rule.getHead(), newquery.getBody().get(atomidx));
		if (mgu == null)
			return null;

		newquery.getBody().remove(atomidx);
		// newquery.getBody().addAll(atomidx, rule.getBody());
		newquery.getBody().addAll(rule.getBody());

		return unifier.applyUnifier(newquery, mgu);
	}
}
