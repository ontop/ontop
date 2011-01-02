package org.obda.reformulation.domain.imp;

import org.obda.query.domain.Predicate;
import org.obda.reformulation.domain.BasicRoleDescription;

public class AtomicRoleDescriptionImpl implements BasicRoleDescription{

	private boolean inverse = false;
	private Predicate predicate = null;


	protected AtomicRoleDescriptionImpl(Predicate p, boolean isInverse){
		this.predicate = p;
		this.inverse = isInverse;
	}

	public boolean isInverse() {
		return inverse;
	}
	 public Predicate getPredicate(){
		 return predicate;
	 }

}
