package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.util.List;
import java.util.ArrayList;


/*
 * ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.setUriTemplateMatcher(questInstance.getUriTemplateMatcher());
		evaluator.evaluateExpressions(unfolding);
 */

class ExpressionUnsatisfiabilityCheck {
	List<Function> body;
	//CQIE query;
	
	//ExpressionUnsatisfiabilityCheck(){}
	
	void purgePredicates(CQIE q) {
		body = new ArrayList<Function>();
		
		for (Function atom: q.getBody()) {
			if (atom.getFunctionSymbol() instanceof BooleanOperationPredicate) {
				body.add(atom);
				// see evalBoolean
			}
			
		}
	}
	
	boolean check(CQIE q) {
		//return false;
		return Check_GT_LT(q);
	}
	
	boolean Check_GT_LT(CQIE q) {
		/*
		 * Check unsatisfiability of a query.
		 * Basic version, with only support for > (GT) and < (LT).
		 */
		purgePredicates(q);
		for (int atomidx = 0; atomidx < body.size(); atomidx++) {
			Function atom = body.get(atomidx);
			Predicate pred = atom.getFunctionSymbol();
			if (pred == OBDAVocabulary.AND) {
				body.set(atomidx, atom.getTerm(0));
				body.add(atom.getTerm(1));
				atomidx--;
			} else if (pred == OBDAVocabulary.GT
					|| pred == OBDAVocabulary.LT) {
				// store variables/constants
			} else {
				// remove from body (ignore)
				body.remove(atomidx);
				atomidx--;
			}
		}
		
		return false; // FIXME
	}
	
	
	
}