package org.obda.query.domain;

import java.net.URI;

public interface Predicate {
	
	public URI getName();
	public int getArity();
	public Predicate copy();

}
