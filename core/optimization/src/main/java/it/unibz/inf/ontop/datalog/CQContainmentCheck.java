package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.substitution.Substitution;

public interface CQContainmentCheck {

	/**
	 * Returns true if the first query (cq1) is contained in the second query (cq2)
	 *    (in other words, the first query is more specific, it has fewer answers)
	 * 
	 * @param cq1
	 * @param cq2
	 * @return true if the first query is contained in the second query
	*/
	
	boolean isContainedIn(CQIE cq1, CQIE cq2);

	Substitution computeHomomorphsim(CQIE q1, CQIE q2);
	
}
