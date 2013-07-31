/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
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
