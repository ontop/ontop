package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
