package org.obda.reformulation.domain.imp;

import org.obda.query.domain.Predicate;
import org.obda.reformulation.domain.GeneralRoleDescription;

public class NegatedRoleDescriptionImpl implements GeneralRoleDescription{

	 private boolean inverse = false;
	 private Predicate predicate = null;

	 protected NegatedRoleDescriptionImpl(Predicate p, boolean isInverse){
		 this.predicate = p;
		 this.inverse = isInverse;
	 }
	 public boolean isInverse(){
		 return inverse;
	 }
	 public Predicate getPredicate(){
		 return predicate;
	 }
}
