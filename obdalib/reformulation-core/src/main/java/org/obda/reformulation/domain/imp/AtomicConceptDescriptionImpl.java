package org.obda.reformulation.domain.imp;

import org.obda.query.domain.Predicate;
import org.obda.reformulation.domain.BasicConceptDescription;

public class AtomicConceptDescriptionImpl implements BasicConceptDescription{

	private Predicate predicate = null;
	private boolean isInverse = false;

	public AtomicConceptDescriptionImpl(Predicate p, boolean isInverse ){
		this.predicate =p;
		this.isInverse = isInverse;
	}

	public boolean isInverse(){
		return isInverse;
	}

	public Predicate getPredicate() {
		return predicate;
	}

}
