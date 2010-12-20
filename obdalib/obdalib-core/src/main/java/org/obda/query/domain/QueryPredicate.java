package org.obda.query.domain;

import java.net.URI;

public interface QueryPredicate extends Predicate {

	public Predicate getPredicate(URI name, int arity);
}
