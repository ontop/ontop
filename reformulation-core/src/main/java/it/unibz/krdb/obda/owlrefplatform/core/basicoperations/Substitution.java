/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Term;


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
