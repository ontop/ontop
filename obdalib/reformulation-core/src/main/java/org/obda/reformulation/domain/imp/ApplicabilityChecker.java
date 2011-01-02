package org.obda.reformulation.domain.imp;

import org.obda.query.domain.Atom;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.UndistinguishedVariable;
import org.obda.reformulation.domain.ConceptDescription;
import org.obda.reformulation.domain.PositiveInclusion;
import org.obda.reformulation.domain.RoleDescription;

public class ApplicabilityChecker {

	/**
	 * Check whether the given positive inclusion is applicable to the given atom
	 *
	 * @param pi the positive inclusion
	 * @param atom the atom
	 * @return true if the positive inclusion is applicable to the atom, false otherwise
	 */
	public boolean isPIApplicable(PositiveInclusion pi, Atom atom){

		//checks: (3) I is a role inclusion assertion and its right-hand side is either P or P-
		if(pi instanceof DLLiterRoleInclusionImpl){
			RoleDescription including = ((DLLiterRoleInclusionImpl)pi).getIncluding();
			if(including instanceof AtomicRoleDescriptionImpl){
				AtomicRoleDescriptionImpl role = (AtomicRoleDescriptionImpl) including;
				if(role.getPredicate().getName().equals(atom.getPredicate().getName())){
					return true;
				}else{
					return false;
				}

			}else if(including instanceof NegatedRoleDescriptionImpl){
				NegatedRoleDescriptionImpl neg = (NegatedRoleDescriptionImpl) including;
				if(neg.getPredicate().getName().equals(atom.getPredicate().getName())){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}

		}else{
			//I is applicable to an atom A(x) if it has A in its right-hand side
			Predicate pred =atom.getPredicate();
			ConceptDescription inc = ((DLLiterConceptInclusionImpl)pi).getIncluding();
			Predicate inc_predicate = inc.getPredicate();
			String predName = pred.getName().toString().trim();
			String incpredName = inc_predicate.getName().toString();
			if(pred.getArity() ==2){
				Term t2 =atom.getTerms().get(1);
				Term t1 =atom.getTerms().get(0);
				ConceptDescription including = ((DLLiterConceptInclusionImpl)pi).getIncluding();
				if(including instanceof ExistentialConceptDescriptionImpl){
					ExistentialConceptDescriptionImpl imp = (ExistentialConceptDescriptionImpl) including;
					if(t2 instanceof UndistinguishedVariable && !imp.isInverse()){//I is applicable to an atom P(x1, x2) if (1) x2 = _ and the right-hand side of I is exist P
						if(including.getPredicate().getName().toString().equals(atom.getPredicate().getName().toString())){
							return !including.isInverse();
						}else{
							return false;
						}
					}else if(t1 instanceof UndistinguishedVariable && imp.isInverse()){//I is applicable to an atom P(x1, x2) if (1) x1 = _ and the right-hand side of I is exist P-
						if(including.getPredicate().getName().toString().equals(atom.getPredicate().getName().toString())){
							return including.isInverse();
						}else{
							return false;
						}
					}else{
						return false;
					}
				}else{
					return false;
				}
			}else{
				if(predName.equals(incpredName) && pred.getArity() ==1 &&  inc_predicate.getArity()==1){
					return true;
				}else{
					return false;
				}
			}
		}
	}
}
