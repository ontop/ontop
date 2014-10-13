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
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

		Unifier mgu = getMGU(q.getBody().get(i), q.getBody().get(j));
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

	// /**
	// * This method will apply the substitution in the unifier to all the terms
	// * of the atom. If nested terms occur, it will apply the unifier to ONLY
	// the
	// * first level of nesting.
	// *
	// * Note that this method will actually change the list of terms of the
	// atom,
	// * replacing variables in the domain of the unifier with their
	// substitution
	// * term.
	// *
	// * @param atom
	// * @param unifier
	// */

	public static void applyUnifier(Function atom, Unifier unifier) {
		applyUnifier(atom.getTerms(), unifier,0);
	}

	/***
	 * Applies the substitution to all the terms in the list. Note that this
	 * will not clone the list or the terms inside the list.
	 * 
	 * @param terms
	 * @param unifier
	 */
	public static void applyUnifier(List<Term> terms,  Unifier unifier, int fromIndex) {
		
		for (int i = fromIndex; i < terms.size(); i++) {
			Term t = terms.get(i);
			/*
			 * unifiers only apply to variables, simple or inside functional
			 * terms
			 */
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

	/***
	 * Computes the Most General Unifier (MGU) for two n-ary atoms. Supports
	 * atoms with terms: Variable, URIConstant, ValueLiteral, ObjectVariableImpl
	 * If a term is an ObjectVariableImpl it can't have nested
	 * ObjectVariableImpl terms.
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	public static Unifier getMGU(Function first,
			Function second) {

		/*
		 * Basic case, predicates are different or their arity is different,
		 * then no unifier
		 */
		Predicate predicate1 = first.getFunctionSymbol();
		Predicate predicate2 = second.getFunctionSymbol();
		if ((first.getArity() != second.getArity() || !predicate1
				.equals(predicate2))) {
			return null;

		}

		Function firstAtom = (Function) first.clone();
		Function secondAtom = (Function) second.clone();

		/* Computing the disagreement set */

		int arity = predicate1.getArity();
		List<Term> terms1 = firstAtom.getTerms();
		List<Term> terms2 = secondAtom.getTerms();

		Unifier mgu = new Unifier();

		for (int termidx = 0; termidx < arity; termidx++) {

			Term term1 = terms1.get(termidx);
			Term term2 = terms2.get(termidx);

			/*
			 * Checking if there are already substitutions calculated for the
			 * current terms. If there are any, then we have to take the
			 * substitutted terms instead of the original ones.
			 */
			Term currentTerm1 = mgu.getRaw(term1);
			Term currentTerm2 = mgu.getRaw(term2);

			if (currentTerm1 != null)
				term1 = currentTerm1;
			if (currentTerm2 != null)
				term2 = currentTerm2;

			/*
			 * We have two cases, unifying 'simple' terms, and unifying function
			 * terms. If Function terms are supported as long as they are not
			 * nested.
			 */

			if ((term1 instanceof Function) && (term2 instanceof Function)) {
				/*
				 * if both of them are a function term then we need to do some
				 * check in the inner terms, else we can give it to the MGU
				 * calculator directly
				 */
				Function fterm1 = (Function) term1;
				Function fterm2 = (Function) term2;

//                Predicate functionSymbol1 = fterm1.getFunctionSymbol();
 //               Predicate functionSymbol2 = fterm2.getFunctionSymbol();

                if (!fterm1.getFunctionSymbol().equals( fterm2.getFunctionSymbol())) {
                   return null;
                }
                if (fterm1.getTerms().size() != fterm2.getTerms().size()) {
                   return null;
                }

				int innerarity = fterm1.getTerms().size();
				List<Term> innerterms1 = fterm1.getTerms();
				List<Term> innerterms2 = fterm2.getTerms();
				for (int innertermidx = 0; innertermidx < innerarity; innertermidx++) {

					Term innerterm1 = innerterms1.get(innertermidx);
					Term innerterm2 = innerterms2.get(innertermidx);

					Term currentInnerTerm1 = mgu.getRaw(innerterm1);
					Term currentInnerTerm2 = mgu.getRaw(innerterm2);

					if (currentInnerTerm1 != null)
						innerterm1 = currentInnerTerm1;
					if (currentInnerTerm2 != null)
						innerterm2 = currentInnerTerm2;

					Substitution s = Unifier.getSubstitution(innerterm1, innerterm2);
					if (s == null) 
						return null;

					mgu.compose(s);
				}

			} else {
				/*
				 * the normal case
				 */

				Substitution s = Unifier.getSubstitution(term1, term2);
				if (s == null) 
					return null;

				mgu.compose(s);
			}

			/*
			 * Applying the newly computed substitution to the 'replacement' of
			 * the existing substitutions
			 */
			applyUnifier(terms1, mgu, termidx + 1);
			applyUnifier(terms2, mgu, termidx + 1);

		}
		return mgu;
	}
	
	public static Unifier getNullifier(Collection<Variable> vars) {
		Unifier unifier = new Unifier();

		for (Variable var : vars) {
			unifier.put((VariableImpl)var, OBDAVocabulary.NULL);
		}
		return unifier;
	}
}
