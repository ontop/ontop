package it.unibz.krdb.obda.model;

public interface Atom extends Cloneable {
	
	public Atom clone();

	public Predicate getPredicate();
}
