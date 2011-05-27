package org.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;

import org.obda.owlrefplatform.core.ontology.GeneralRoleDescription;

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
