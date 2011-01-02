package org.obda.reformulation.dllite;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.UndistinguishedVariable;
import org.obda.reformulation.domain.ConceptDescription;
import org.obda.reformulation.domain.PositiveInclusion;
import org.obda.reformulation.domain.RoleDescription;
import org.obda.reformulation.domain.imp.AtomicConceptDescriptionImpl;
import org.obda.reformulation.domain.imp.DLLiterConceptInclusionImpl;
import org.obda.reformulation.domain.imp.DLLiterRoleInclusionImpl;
import org.obda.reformulation.domain.imp.ExistentialConceptDescriptionImpl;

public class PositiveInclusionApplicator {

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
							Vector<Term> v = new Vector<Term>();
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
								
								Vector<Term> v = new Vector<Term>();
								v.add(0,anonym);
								v.add(1,t);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
								
							}else{
								Vector<Term> v = new Vector<Term>();
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
								Vector<Term> v = new Vector<Term>();
								v.add(0,t1);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(lefthandside.isInverse()){
								Vector<Term> v = new Vector<Term>();
								v.add(0,t2);
								v.add(1,t1);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(!lefthandside.isInverse()){
								Vector<Term> v = new Vector<Term>();
								v.add(0,t1);
								v.add(1,t2);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else{
								newBody.add(a.copy());
							}							
						}else if(t1 instanceof UndistinguishedVariable && righthandside.isInverse()){
							if(lefthandside instanceof AtomicConceptDescriptionImpl){
								Vector<Term> v = new Vector<Term>();
								v.add(0,t2);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(lefthandside.isInverse()){
								Vector<Term> v = new Vector<Term>();
								v.add(0,t1);
								v.add(1,t2);
								AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
								newBody.add(newatom);
							}else if(!lefthandside.isInverse()){
								Vector<Term> v = new Vector<Term>();
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
							Vector<Term> v = new Vector<Term>();
							v.add(0,t1);
							v.add(1,t2);
							AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
							newBody.add(newatom);
						}else{
							Vector<Term> v = new Vector<Term>();
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
