package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface DisjointPropertyAxiom extends DisjointDescriptionAxiom {
	
	public Predicate getFirst();
	
	public Predicate getSecond();

}
