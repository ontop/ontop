package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

/*
 * Note: The the unifier does not distinguish between undistinguished variables
 * i.e.when we have an atom A(#,#) which should be unified with B(b,c) the
 * unifier willreturn two thetas #/b, #/c. So far so good but this will lead to
 * problems whenapplying the thetas because there is no distinction between
 * #-variables, sothe first theta is applied to all #-variables and the rest is
 * ignored.In order to avoid problems one can enumerate the undistinguished
 * variables ie. A(#1,#2)
 */

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.*;

import java.util.Collection;
import java.util.List;

/***
 * A Class that provides general utilities related to unification, of terms and
 * atoms.
 * 
 * @author mariano
 * 
 */
public class UnifierUtilities {

	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		
	/***
	 * Unifies two atoms in a conjunctive query returning a new conjunctive
	 * query. To to this we calculate the MGU for atoms, duplicate the query q
	 * into q', remove i and j from q', apply the mgu to q', and
	 * 
	 * @param q
	 * @param i
	 * @param j (j > i)
	 * @return null if the two atoms are not unifiable, else a new conjunctive
	 *         query produced by the unification of j and i
	 * 
	 * @throws Exception
	 */
	public static CQIE unify(CQIE q, int i, int j) {

		Unifier mgu = Unifier.getMGU(q.getBody().get(i), q.getBody().get(j));
		if (mgu == null)
			return null;

		CQIE unifiedQ = applyUnifier(q, mgu);
		unifiedQ.getBody().remove(i);
		unifiedQ.getBody().remove(j - 1);

		Function atom1 = q.getBody().get(i);
		Function atom2 = q.getBody().get(j);
		//Function newatom = unify((Function) atom1, (Function) atom2, mgu);
		
		// take care of anonymous variables
		Function newatom = (Function) atom1.clone();
		for (int ii = 0; ii < atom1.getTerms().size(); ii++) {
			Term t1 = atom1.getTerms().get(ii);
			if (t1 instanceof AnonymousVariable) 
				newatom.getTerms().set(ii, atom2.getTerms().get(ii));
		}
		applyUnifier(newatom, mgu);
		
		unifiedQ.getBody().add(i, newatom);

		return unifiedQ;
	}


	/***
	 * This method will return a new query, resulting from the application of
	 * the unifier to the original query q. To do this, we will call the clone()
	 * method of the original query and then will call applyUnifier to each atom
	 * of the cloned query.
	 * 
	 * @param q
	 * @param unifier
	 * @return
	 */
	public static CQIE applyUnifier(CQIE q, Unifier unifier, boolean clone) {

		CQIE newq;
		if (clone)
			newq = q.clone();
		else
			newq = q;

		Function head = newq.getHead();
		applyUnifier(head, unifier);
		for (Function bodyatom : newq.getBody()) 
			applyUnifier(bodyatom, unifier);
		
		return newq;
	}

	public static CQIE applyUnifier(CQIE q, Unifier unifier) {
		return applyUnifier(q, unifier, true);
	}



	/***
	 * Applies the substitution to all the terms in the list. Note that this
	 * will not clone the list or the terms inside the list.
	 * 
	 * @param atom
	 * @param unifier
	 */
	
	public static void applyUnifier(Function atom, Unifier unifier) {
		applyUnifier(atom, unifier,0);
	}
	
	public static void applyUnifier(Function atom, Unifier unifier, int fromIndex) {
		
		List<Term> terms = atom.getTerms();
		
		for (int i = fromIndex; i < terms.size(); i++) {
			Term t = terms.get(i);
			
			// unifiers only apply to variables, simple or inside functional terms
			
			if (t instanceof VariableImpl) {
				Term replacement = unifier.get((VariableImpl)t);
				if (replacement != null)
					terms.set(i, replacement);
			} 
			else if (t instanceof Function) {
				Function t2 = (Function) t;
				applyUnifier(t2, unifier);
			}
		}
	}
	
	/**
	 * 
	 * @param atom
	 * @param unifier
	 */
	public static void applyUnifierToGetFact(Function atom, Unifier unifier) {
		
		List<Term> terms = atom.getTerms();
		for (int i = 0; i < terms.size(); i++) {
			Term t = terms.get(i);
			/*
			 * unifiers only apply to variables, simple or inside functional
			 * terms
			 */
			if (t instanceof VariableImpl) {
				Term replacement = unifier.get((VariableImpl)t);
				if (replacement != null) {
					terms.set(i, replacement);
				} else {
					terms.set(i, ofac.getConstantFreshLiteral());
				}
			} else if (t instanceof Function) {
				Function t2 = (Function) t;
				applyUnifier(t2, unifier);
			}
		}
	}

	
	public static Unifier getNullifier(Collection<Variable> vars) {
		Unifier unifier = new Unifier();

		for (Variable var : vars) {
			unifier.put((VariableImpl)var, OBDAVocabulary.NULL);
		}
		return unifier;
	}
}
