package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface Property extends Description {

	public boolean isInverse();

	public Predicate getPredicate();
}
