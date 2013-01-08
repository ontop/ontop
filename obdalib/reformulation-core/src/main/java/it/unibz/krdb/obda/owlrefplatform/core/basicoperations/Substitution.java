package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.NewLiteral;


public class Substitution {

	//TODO make variable an instance of Variable
	private NewLiteral variable = null;
	private NewLiteral term = null;
	
	public Substitution(NewLiteral v, NewLiteral t){
		variable = v;
		term = t;
	}
	
	public NewLiteral getVariable(){
		return variable;
	}
	
	public NewLiteral getTerm(){
		return term;
	};
	
	public void setTerm(NewLiteral newTerm){
		term = newTerm;
	}
	
	public void setVariable(NewLiteral newVariable){
		term = newVariable;
	}
	
	public String toString() {
		return variable.toString() + "/" + term.toString();
	}
}
