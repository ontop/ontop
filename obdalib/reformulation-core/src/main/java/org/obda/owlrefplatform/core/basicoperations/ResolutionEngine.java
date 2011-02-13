package org.obda.owlrefplatform.core.basicoperations;

import java.util.Map;

import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;
import org.obda.query.domain.Variable;

public class ResolutionEngine {

	AtomUnifier	unifier	= null;
	
	public ResolutionEngine() {
		unifier = new AtomUnifier();
	}
	
	

	/**
	 * This method resolves a query atom (atomidx) against a rule's head,
	 * producing as output a new query q'.
	 * 
	 * Note, queries used as a rule can only have 1 atom in the body. Moreover,
	 * all the restrictions specified in the class AtomUnifier about unification
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
		newquery.getBody().addAll(atomidx, rule.getBody());
		newquery.getBody().set(atomidx, rule.getBody().get(0));
		return unifier.applyUnifier(newquery, mgu);
	}
}
