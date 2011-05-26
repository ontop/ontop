package org.obda.owlrefplatform.core.ontology.imp;

import inf.unibz.it.obda.model.Predicate;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.BasicConceptDescription;
import org.obda.owlrefplatform.core.ontology.ConceptDescription;
import org.obda.owlrefplatform.core.ontology.DescriptionFactory;
import org.obda.owlrefplatform.core.ontology.RoleDescription;

public class BasicDescriptionFactory implements DescriptionFactory{

	public ConceptDescription getConceptDescription(Predicate p,
			boolean negated, boolean inverse) {

		int arity = p.getArity();
		if(arity == 1){
			if(negated){
				return new NegatedBasicConceptDescriptionImpl(new AtomicConceptDescriptionImpl(p, inverse));

			}else{
				return new AtomicConceptDescriptionImpl(p, inverse);
			}
		}else if (arity == 2){
			if(negated){
				return new NegatedBasicConceptDescriptionImpl(new ExistentialConceptDescriptionImpl(p,inverse));
			}else{
				return new ExistentialConceptDescriptionImpl(p,inverse);

			}
		}else{
			throw new IllegalArgumentException("arity cannot be " +arity);
		}
	}

	public ConceptDescription getConceptDescription(Predicate p, boolean negated) {
		return getConceptDescription(p, negated, false);
	}

	public ConceptDescription getConceptDescription(Predicate p) {
		return getConceptDescription(p, false, false);
	}

	public ConceptDescription getConceptDescription(List<Predicate> p,
			boolean[] inverseMark, BasicConceptDescription tailConcept) {
		return new QualifiedExistentialConceptDescriptionImpl(p,inverseMark, tailConcept);
	}

	public ConceptDescription getConceptDescription(List<Predicate> p,
			boolean[] inverseMark) {
		return new QualifiedExistentialConceptDescriptionImpl(p,inverseMark, null);
	}

	public ConceptDescription getConceptDescription(List<Predicate> p) {

		boolean[] aux = new boolean[p.size()];
		for(int i=0;i<p.size();i++){
			aux[i] = true;
		}
		return new QualifiedExistentialConceptDescriptionImpl(p,aux, null);
	}

	public ConceptDescription getConceptDescription(
			List<ConceptDescription> descriptions, boolean isConjunction) {

		//TODO: throw not implemented exception
		return null;
	}

	public RoleDescription getRoleDescription(Predicate p, boolean inverse,
			boolean negated) {

//		returns a BasicRoleDescription if negated = false, else it returns a GeneralRoleDescription
//	     returns java.lang.IllegalArgumentException if p is not binary (n = 2)
		if(p.getArity() != 2){
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if(negated){
			return new NegatedRoleDescriptionImpl(p, inverse);
		}else{
			return new AtomicRoleDescriptionImpl(p, inverse);
		}
	}

	public RoleDescription getRoleDescription(Predicate p, boolean inverse) {
		return getRoleDescription(p,inverse, false);
	}

	public RoleDescription getRoleDescription(Predicate p) {
		return getRoleDescription(p,false, false);
	}

}
