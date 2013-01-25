package it.unibz.krdb.obda.model;

import java.util.Collection;
import java.util.List;

public interface DatalogProgram extends OBDAQuery {

	public List<CQIE> getRules();

	public void appendRule(CQIE rule);

	public void appendRule(Collection<CQIE> rule);

	public void removeRule(CQIE rule);

	public void removeRules(Collection<CQIE> rule);
	
	public void removeAllRules();

	public boolean isUCQ();

	/***
	 * Returns all the rules that have the given predicate in their heads
	 * 
	 * @param headPredicate
	 * @return
	 */
	public List<CQIE> getRules(Predicate headPredicate);

	public DatalogProgram clone();

}
