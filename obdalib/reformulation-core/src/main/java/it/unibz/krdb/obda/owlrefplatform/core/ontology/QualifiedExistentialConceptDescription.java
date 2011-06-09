package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface QualifiedExistentialConceptDescription extends ConceptDescription {
	public boolean isInverse();

	public Predicate getPredicate();

	public AtomicConceptDescription getFiller();
}
