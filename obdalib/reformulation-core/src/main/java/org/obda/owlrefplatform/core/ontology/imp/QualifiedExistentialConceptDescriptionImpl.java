package org.obda.owlrefplatform.core.ontology.imp;

import inf.unibz.it.obda.model.Predicate;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.BasicConceptDescription;
import org.obda.owlrefplatform.core.ontology.GeneralConceptDescription;

public class QualifiedExistentialConceptDescriptionImpl implements GeneralConceptDescription{

	private List<Predicate> predicates = null;
	BasicConceptDescription tailConceptDescriptionImpl = null;
	boolean[] inverseMark = null;

	protected QualifiedExistentialConceptDescriptionImpl(List<Predicate> predicates, boolean[] inverseMark, BasicConceptDescription tailConceptDescriptionImpl){

		if(predicates == null || inverseMark == null){
			throw new IllegalArgumentException("Parameters cannot be null");
		}
		if(predicates.size() != inverseMark.length){
			throw new IllegalArgumentException("Each predicate must have an inverse mark");
		}

		this.predicates = predicates;
		this.inverseMark = inverseMark;
		this.tailConceptDescriptionImpl = tailConceptDescriptionImpl;
	}

	 public boolean isInverseBinaryPredicate(int position){
		 return inverseMark[position];
	 }
//	  returns the value of inverseMark[position]

	public Predicate getPredicate() {
		return null;
	}

	@Override
	public boolean isInverse() {
		return tailConceptDescriptionImpl.isInverse();
	}
}
