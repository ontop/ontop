package org.obda.query.domain.imp;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.FunctionSymbol;
import org.obda.query.domain.Term;
import org.obda.query.domain.Variable;

public class ObjectVariableImpl implements Variable{

	private FunctionSymbol functor= null;
	private List<Term> terms = null;

	
	protected ObjectVariableImpl(FunctionSymbol fs, List<Term> vars){ 
		this.functor = fs;
		this.terms = vars;
	}

	 public boolean equals(Object obj){
		 if(obj == null || !(obj instanceof Variable)){
			 return false;
		 }
		 
		 return this.hash() == ((ObjectVariableImpl)obj).hash();
	 }

	 public long hash(){
		 return functor.hash();
	 }

	
	public String getName() {
		return functor.getName();
	}

	public Variable copy() {
		Vector<Term> vex = new Vector<Term>();
		Iterator<Term> it = terms.iterator();
		while(it.hasNext()){
			vex.add(it.next().copy());
		}
		return new ObjectVariableImpl(functor.copy(), vex);
	}
	
	public void setTerms(List<Term> terms){
		this.terms = terms;
	}
	
	public List<Term> getTerms(){
		return terms;
	}

}
