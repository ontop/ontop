package org.obda.reformulation.dllite;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.UndistinguishedVariable;
import org.obda.reformulation.domain.ConceptDescription;
import org.obda.reformulation.domain.PositiveInclusion;
import org.obda.reformulation.domain.RoleDescription;
import org.obda.reformulation.domain.imp.AtomicConceptDescriptionImpl;
import org.obda.reformulation.domain.imp.AtomicRoleDescriptionImpl;
import org.obda.reformulation.domain.imp.DLLiterConceptInclusionImpl;
import org.obda.reformulation.domain.imp.DLLiterRoleInclusionImpl;
import org.obda.reformulation.domain.imp.ExistentialConceptDescriptionImpl;
import org.obda.reformulation.domain.imp.NegatedRoleDescriptionImpl;

public class PositiveInclusionApplicator {

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
	
	public CQIE applyPI(CQIE q, PositiveInclusion inclusion){
		
		List<Atom> body = q.getBody();
		Iterator<Atom> it = body.iterator();
		LinkedList<Atom> newBody = new LinkedList<Atom>();
		
		
		while(it.hasNext()){
			Atom a = it.next();
			if(a.getArity() == 1){
				if(inclusion instanceof DLLiterConceptInclusionImpl){
					DLLiterConceptInclusionImpl inc = (DLLiterConceptInclusionImpl) inclusion; 
					ConceptDescription lefthandside = inc.getIncluded();
					ConceptDescription righthandside = inc.getIncluding();
					if(righthandside.getPredicate().getName().toString().equals( a.getPredicate().getName().toString()) && a.getArity() == righthandside.getPredicate().getArity()){ 
					
						if(lefthandside instanceof AtomicConceptDescriptionImpl){
							
							List<Term> terms = a.getTerms();
							LinkedList<Term> v = new LinkedList<Term>();
							Iterator<Term> tit = terms.iterator();
							while(tit.hasNext()){
								v.add(tit.next().copy());
							}
							AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
							newBody.add(newatom);
						}else if(lefthandside instanceof ExistentialConceptDescriptionImpl){
							
							Term t = a.getTerms().get(0); 
							Term anonym = new UndistinguishedVariable();
							
							if(((ExistentialConceptDescriptionImpl) lefthandside).isInverse()){
								
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,anonym);
								v.add(1,t);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
								
							}else{
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,t);
								v.add(1,anonym);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}
						}else{
							newBody.add(a.copy());
						}
					}else{
						newBody.add(a.copy());
					}
				}else{
					newBody.add(a.copy());
				}
				
			}else{
				if(inclusion instanceof DLLiterConceptInclusionImpl){
					DLLiterConceptInclusionImpl inc = (DLLiterConceptInclusionImpl) inclusion;
					ConceptDescription lefthandside = inc.getIncluded();
					ConceptDescription righthandside = inc.getIncluding();
					if(righthandside.getPredicate().getName().toString().equals( a.getPredicate().getName().toString()) && a.getArity() == righthandside.getPredicate().getArity()){
						
						Term t1 = a.getTerms().get(0);
						Term t2 = a.getTerms().get(1);
						
						if(t2 instanceof UndistinguishedVariable && !righthandside.isInverse()){
							
							if(lefthandside instanceof AtomicConceptDescriptionImpl){
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,t1);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(lefthandside.isInverse()){
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,t2);
								v.add(1,t1);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(!lefthandside.isInverse()){
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,t1);
								v.add(1,t2);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else{
								newBody.add(a.copy());
							}							
						}else if(t1 instanceof UndistinguishedVariable && righthandside.isInverse()){
							if(lefthandside instanceof AtomicConceptDescriptionImpl){
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,t2);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(lefthandside.isInverse()){
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,t1);
								v.add(1,t2);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(!lefthandside.isInverse()){
								LinkedList<Term> v = new LinkedList<Term>();
								v.add(0,t2);
								v.add(1,t1);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else{
								newBody.add(a.copy());
							}
						}else{							
							newBody.add(a.copy());
						}
					}else{
						newBody.add(a.copy());
					}
				}else if (inclusion instanceof DLLiterRoleInclusionImpl){
				
					DLLiterRoleInclusionImpl inc = (DLLiterRoleInclusionImpl) inclusion;
					RoleDescription lefthandside = inc.getIncluded();
					RoleDescription righthandside = inc.getIncluding();
					if(righthandside.getPredicate().getName().toString().equals(a.getPredicate().getName().toString()) && a.getArity() == righthandside.getPredicate().getArity()){
						
						Term t1 = a.getTerms().get(0);
						Term t2 = a.getTerms().get(1);
						if((righthandside.isInverse() && lefthandside.isInverse()) || (!righthandside.isInverse() && !lefthandside.isInverse())){
							LinkedList<Term> v = new LinkedList<Term>();
							v.add(0,t1);
							v.add(1,t2);
							AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
							newBody.add(newatom);
						}else{
							LinkedList<Term> v = new LinkedList<Term>();
							v.add(0,t2);
							v.add(1,t1);
							AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
							newBody.add(newatom);
						}
						
					}else{
						newBody.add(a.copy());
					}
				}else{
					newBody.add(a.copy());
				}
			}
		}
		if(q.getBody().size() != newBody.size()){
			try {
				throw new Exception("ERROR: Size of body not the same!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		CQIEImpl query = new CQIEImpl(q.getHead(), newBody, q.isBoolean());
		return query;
	}
}
