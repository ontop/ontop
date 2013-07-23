package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.model.CQIE;

/**
 * Classes that extend this abstract class must include a prefix manager to
 * support prefixed names. The <code>print(CQIE)</code> method is used to
 * format the conjunctive queries into a particular printed string.
 */
public abstract class CQFormatter {
	
	protected PrefixManager prefixManager;
	
	public CQFormatter(PrefixManager pm) {
		prefixManager = pm;
	}
	
	public abstract String print(CQIE query);
}
