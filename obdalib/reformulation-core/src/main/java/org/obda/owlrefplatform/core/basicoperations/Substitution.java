package org.obda.owlrefplatform.core.basicoperations;

import inf.unibz.it.obda.model.Term;


public class Substitution {

	//TODO make variable an instance of Variable
	private Term variable = null;
	private Term term = null;
	
	public Substitution(Term v, Term t){
		variable = v;
		term = t;
	}
	
	public Term getVariable(){
		return variable;
	}
	
	public Term getTerm(){
		return term;
	};
	
	public void setTerm(Term newTerm){
		term = newTerm;
	}
	
	public void setVariable(Term newVariable){
		term = newVariable;
	}
	
	public String toString() {
		return variable.toString() + "/" + term.toString();
	}
}
