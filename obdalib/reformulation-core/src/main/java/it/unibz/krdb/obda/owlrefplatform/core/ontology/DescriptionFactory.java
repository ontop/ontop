package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface DescriptionFactory {

	public ExistentialConceptDescription getExistentialConceptDescription(Predicate p, boolean inverse);
	
	public QualifiedExistentialConceptDescription getExistentialConceptDescription(Predicate p, boolean inverse, AtomicConceptDescription filler);

	public AtomicConceptDescription getAtomicConceptDescription(Predicate p);

	public RoleDescription getRoleDescription(Predicate p, boolean inverse);

	public RoleDescription getRoleDescription(Predicate p);

}
