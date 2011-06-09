package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.QualifiedExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

public class BasicDescriptionFactory implements DescriptionFactory {

	public ExistentialConceptDescription getExistentialConceptDescription(Predicate p, boolean inverse) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		return new ExistentialConceptDescriptionImpl(p, inverse);
	}
	
	public QualifiedExistentialConceptDescription getExistentialConceptDescription(Predicate p, boolean inverse, AtomicConceptDescription filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null)
			throw new IllegalArgumentException("Must provide an atomic concept as a filler");
		return new QualifiedExistentialConceptDescriptionImpl(p, inverse, filler);
	}


	public AtomicConceptDescription getAtomicConceptDescription(Predicate p) {
		if (p.getArity() != 1) {
			throw new IllegalArgumentException("Concepts must have arity = 1");
		}
		return new AtomicConceptDescriptionImpl(p);

	}

	public RoleDescription getRoleDescription(Predicate p, boolean inverse) {
		return new AtomicRoleDescriptionImpl(p, inverse);
	}

	public RoleDescription getRoleDescription(Predicate p) {
		return getRoleDescription(p, false);
	}

}
