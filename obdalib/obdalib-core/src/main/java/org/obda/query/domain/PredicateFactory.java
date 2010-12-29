package org.obda.query.domain;

import java.net.URI;

public interface PredicateFactory {

	public Predicate getPredicate(URI name, int arity);

}
