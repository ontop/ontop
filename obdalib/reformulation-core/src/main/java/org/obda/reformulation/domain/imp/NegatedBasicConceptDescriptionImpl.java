package org.obda.reformulation.domain.imp;

import org.obda.query.domain.Predicate;
import org.obda.reformulation.domain.BasicConceptDescription;
import org.obda.reformulation.domain.GeneralConceptDescription;

public class NegatedBasicConceptDescriptionImpl implements GeneralConceptDescription{

	private BasicConceptDescription basicConcept = null;

	protected NegatedBasicConceptDescriptionImpl(BasicConceptDescription basicConceptDescriptionImpl){
		this.basicConcept = basicConceptDescriptionImpl;
	}

	public BasicConceptDescription getBasicConceptDescriptionImpl(){
		return basicConcept;
	}

	public Predicate getPredicate() {
		return basicConcept.getPredicate();
	}

	public boolean isInverse() {

		return basicConcept.isInverse();
	}
}
