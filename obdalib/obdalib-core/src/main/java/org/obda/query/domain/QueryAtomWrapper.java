package org.obda.query.domain;

import inf.unibz.it.ucq.domain.ConstantTerm;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.VariableImpl;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.PredicateFactory;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;

public class QueryAtomWrapper implements Atom {

	private QueryAtom atom = null;
	private PredicateFactory predfactory = null;
	private TermFactory termfactory = null;

	public QueryAtomWrapper(QueryAtom at){
		this.atom = at;
		predfactory = BasicPredicateFactoryImpl.getInstance();
		termfactory = new TermFactoryImpl();
	}

	@Override
	public Atom copy() {
		Vector< Term> v = new Vector<Term>();
		Iterator<Term> it = getTerms().iterator();
		while(it.hasNext()){
			v.add(it.next().copy());
		}
		return new AtomImpl(getPredicate().copy(), v);
	}

	@Override
	public int getArity() {
		return atom.getTerms().size();
	}

	@Override
	public Predicate getPredicate() {

		return predfactory.getPredicate(atom.getNamedPredicate().getUri(), getArity());
	}

	@Override
	public List<Term> getTerms() {
		Vector<Term> terms = new Vector<Term>();
		List<QueryTerm> list = atom.getTerms();
		Iterator<QueryTerm> it = list.iterator();
		while(it.hasNext()){
			QueryTerm qt = it.next();
			if(qt instanceof ConstantTerm){
				try {
					throw new Exception("Mappings cannot contain constants");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(qt instanceof Variable){

				terms.add(termfactory.createVariable(((Variable) qt).getName()));

			}else if(qt instanceof FunctionTerm){

				FunctionSymbol fs = termfactory.getFunctionSymbol(((FunctionTerm) qt).getURI().toString());
				List<QueryTerm> para =((FunctionTerm) qt).getParameters();
				Iterator<QueryTerm> pit = para.iterator();
				Vector<Term> newPara = new Vector<Term>();
				while(pit.hasNext()){
					QueryTerm t = pit.next();
					if(qt instanceof Variable){
						newPara.add((VariableImpl)termfactory.createVariable(((Variable) qt).getName()));
					}else{
						try {
							throw new Exception("Mappings cannot contain constants");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				terms.add(termfactory.createObjectTerm(fs, newPara)); 
			}
		}
		return terms;
	}

	@Override
	public void updateTerms(List<Term> terms) {

	}

}
