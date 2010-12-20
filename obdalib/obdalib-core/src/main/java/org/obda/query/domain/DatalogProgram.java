package org.obda.query.domain;

import java.util.List;

public interface DatalogProgram extends Query{

	public List<CQIE> getRules();
	public void appendRule(CQIE rule);
	public void appendRule(List<CQIE> rule);
	public void removeRule(CQIE rule);
	public void removeRules(List<CQIE> rule);
	public boolean isUCQ();
}
