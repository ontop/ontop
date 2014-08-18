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
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/***
 * A Class that provides general utilities related to unification, of terms and
 * atoms.
 * 
 * @author mariano
 * 
 */
public class Unifier {

	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		
	public static Map<Variable, Term> getMGU(CQIE q, int position1,
			int position2) throws Exception {
		return getMGU(q.getBody().get(position1), q.getBody().get(position2));
	}

	/***
	 * Unifies two atoms in a conjunctive query returning a new conjunctive
	 * query. To to this we calculate the MGU for atoms, duplicate the query q
	 * into q', remove i and j from q', apply the mgu to q', and
	 * 
	 * @param q
	 * @param i
	 * @param j
	 * @return null if the two atoms are not unifiable, else a new conjunctive
	 *         query produced by the unification of j and i
	 * 
	 * @throws Exception
	 */
	public static CQIE unify(CQIE q, int i, int j) {

		Map<Variable, Term> mgu = getMGU(q.getBody().get(i), q.getBody()
				.get(j));
		if (mgu == null)
			return null;

		CQIE unifiedQ = applyUnifier(q, mgu);
		unifiedQ.getBody().remove(i);
		unifiedQ.getBody().remove(j - 1);

		Function atom1 = q.getBody().get(i);
		Function atom2 = q.getBody().get(j);
		Function newatom = unify((Function) atom1, (Function) atom2, mgu);
		unifiedQ.getBody().add(i, newatom);

		return unifiedQ;

	}

	/***
	 * Returns a new Function a, resulting the unification of atoms atom1 and atom2
	 * after with the given unifier. Note that this method takes into account
	 * that a unifier doesn't include substitutions for instances of
	 * UndistinguishedVariables.
	 * 
	 * That is, given 2 terms t1 in atom1, and t2 in atom2 in position i, such
	 * that t1 or t2 are instances of UndistinguishedVariable, then atom a will
	 * have in position i the term t1 or t2 that is NOT an undistinguished
	 * variable, compensating for the missing substitutions.
	 * 
	 * @param atom1
	 * @param atom2
	 * @param unifier
	 * @return
	 */
	private static Function unify(Function atom1, Function atom2,
			Map<Variable, Term> unifier) {
		Function newatom = (Function) atom1.clone();
		for (int i = 0; i < atom1.getTerms().size(); i++) {
			Term t1 = atom1.getTerms().get(i);
			Term t2 = atom2.getTerms().get(i);
			if (t1 instanceof AnonymousVariable) {
				newatom.getTerms().set(i, t2);
			}
		}
		applyUnifier(newatom, unifier);
		return newatom;
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
	public static CQIE applyUnifier(CQIE q, Map<Variable, Term> unifier,
			boolean clone) {

		CQIE newq = null;
		if (clone)
			newq = q.clone();
		else
			newq = q;

		/* applying the unifier to every term in the head */
		Function head = newq.getHead();
		applyUnifier(head, unifier);
		for (Function bodyatom : newq.getBody()) {
			applyUnifier(bodyatom, unifier);
		}
		return newq;
	}

	public static CQIE applyUnifier(CQIE q, Map<Variable, Term> unifier) {
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
	// public static void applyUnifier(Function atom, Map<Variable, NewLiteral>
	// unifier) {
	// applyUnifier(atom.getTerms(), unifier);
	// }

	public static void applyUnifier(Function term,
			Map<Variable, Term> unifier) {
		List<Term> terms = term.getTerms();
		applyUnifier(terms, unifier);
	}
	
	public static void applyUnifierToGetFact(Function term,
			Map<Variable, Term> unifier) {
		List<Term> terms = term.getTerms();
		applyUnifierToGetFact(terms, unifier);
	}

	// public static void applyUnifier(List<Function> terms,
	// Map<Variable, NewLiteral> unifier) {
	// for (Function f : terms) {
	// applyUnifier(f, unifier);
	// }
	// }

	
	public static void applyUnifier(List<Term> terms, 
			Map<Variable, Term> unifier) {
		applyUnifier(terms, unifier,0);
	}
	/***
	 * Applies the subsitution to all the terms in the list. Note that this
	 * will not clone the list or the terms insdie the list.
	 * 
	 * @param terms
	 * @param unifier
	 */
	public static void applyUnifier(List<Term> terms, 
			Map<Variable, Term> unifier, int fromIndex) {
		for (int i = fromIndex; i < terms.size(); i++) {
			Term t = terms.get(i);
			/*
			 * unifiers only apply to variables, simple or inside functional
			 * terms
			 */
			if (t instanceof VariableImpl) {
				Term replacement = unifier.get(t);
				if (replacement != null)
					terms.set(i, replacement);
			} else if (t instanceof Function) {
				Function t2 = (Function) t;
				applyUnifier(t2, unifier);

			}
		}
	}
	
	/**
	 * 
	 * @param terms
	 * @param unifier
	 */
	public static void applyUnifierToGetFact(List<Term> terms,
			Map<Variable, Term> unifier) {
		for (int i = 0; i < terms.size(); i++) {
			Term t = terms.get(i);
			/*
			 * unifiers only apply to variables, simple or inside functional
			 * terms
			 */
			if (t instanceof VariableImpl) {
				Term replacement = unifier.get(t);
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
	public static Map<Variable, Term> getMGU(Function first,
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

		Map<Variable, Term> mgu = new HashMap<Variable, Term>();

		for (int termidx = 0; termidx < arity; termidx++) {

			Term term1 = terms1.get(termidx);
			Term term2 = terms2.get(termidx);

			/*
			 * Checking if there are already substitutions calculated for the
			 * current terms. If there are any, then we have to take the
			 * substitutted terms instead of the original ones.
			 */
			Term currentTerm1 = mgu.get(term1);
			Term currentTerm2 = mgu.get(term2);

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

                Predicate functionSymbol1 = fterm1.getFunctionSymbol();
                Predicate functionSymbol2 = fterm2.getFunctionSymbol();

                if (fterm1.getTerms().size() != fterm2.getTerms().size()) {
                    return null;
                }

				if (!functionSymbol1.equals(functionSymbol2)) {

                    if(fterm1.isDataTypeFunction() && fterm2.isDataTypeFunction()) {

//                        //the 2 values are equal also in the case that they are both datatype and one of the two is a rdfs:literal
                        if (!(functionSymbol1.equals(OBDAVocabulary.RDFS_LITERAL)
                                || functionSymbol2.equals(OBDAVocabulary.RDFS_LITERAL)) ){

                            return null;
                        }

//                        if (functionSymbol1.equals(OBDAVocabulary.RDFS_LITERAL)) {
//                            fterm1.setPredicate(functionSymbol2);
//                        } else if (functionSymbol2.equals(OBDAVocabulary.RDFS_LITERAL)) {
//                            fterm2.setPredicate(functionSymbol1);
//                        } else {
//                            return null;
//                        }
                    } else {

                        return null;
                    }
				}

				int innerarity = fterm1.getTerms().size();
				List<Term> innerterms1 = fterm1.getTerms();
				List<Term> innerterms2 = fterm2.getTerms();
				for (int innertermidx = 0; innertermidx < innerarity; innertermidx++) {

					Term innerterm1 = innerterms1.get(innertermidx);
					Term innerterm2 = innerterms2.get(innertermidx);

					Term currentInnerTerm1 = mgu.get(innerterm1);
					Term currentInnerTerm2 = mgu.get(innerterm2);

					if (currentInnerTerm1 != null)
						innerterm1 = currentInnerTerm1;
					if (currentInnerTerm2 != null)
						innerterm2 = currentInnerTerm2;

					Substitution s = getSubstitution(innerterm1, innerterm2);
					if (s == null) {
						return null;
					}

					if (!(s instanceof NeutralSubstitution)) {
						composeUnifiers(mgu, s);
					}
				}

			} else {
				/*
				 * the normal case
				 */

				Substitution s = getSubstitution(term1, term2);

				if (s == null) {
					return null;
				}

				if (!(s instanceof NeutralSubstitution)) {
					composeUnifiers(mgu, s);
				}
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

	/***
	 * This will compose the unfier with the substitution. Note that the unifier
	 * will be modified in this process.
	 * 
	 * The operation is as follows
	 * 
	 * {x/y, m/y} composed with y/z is equal to {x/z, m/z, y/z}
	 * 
	 * @param unifier The unifier that will be composed
	 * @param s The substitution to compose
	 */
	public static void composeUnifiers(Map<Variable, Term> unifier,
			Substitution s) {
		List<Variable> forRemoval = new LinkedList<Variable>();
		for (Variable v : unifier.keySet()) {
			Term t = unifier.get(v);
			if (isEqual(t, s.getVariable())) {
				if (isEqual(v, s.getTerm())) {
					/*
					 * The substitution for the current variable has become
					 * trivial, e.g., x/x with the current composition. We
					 * remove it to keep only a non-trivial unifier
					 */
					forRemoval.add(v);
				} else {
					unifier.put(v, s.getTerm());
				}
			} else if (t instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) t;
				List<Term> innerTerms = function.getTerms();
				FunctionalTermImpl fclone = function.clone();
				boolean innerchanges = false;
				// TODO this ways of changing inner terms in functions is not
				// optimal, modify

				for (int i = 0; i < innerTerms.size(); i++) {
					Term innerTerm = innerTerms.get(i);

					if (isEqual(innerTerm, s.getVariable())) {
						fclone.getTerms().set(i, s.getTerm());
						innerchanges = true;
					}
				}
				if (innerchanges)
					unifier.put(v, fclone);
			}
		}
		unifier.keySet().removeAll(forRemoval);
		unifier.put((Variable) s.getVariable(), s.getTerm());
	}

	/***
	 * Computes the substitution that makes two terms equal. Note, two terms of
	 * class ObjectVariableImpl are not supported. This would require the
	 * analysis of the terms in each of these, this operation is not supported
	 * at the moment.
	 * 
	 * @param term1
	 * @param term2
	 * @return
	 */
	public static Substitution getSubstitution(Term term1,
			Term term2) {

		if (!(term1 instanceof VariableImpl)
				&& !(term2 instanceof VariableImpl)) {
			/*
			 * none is a variable, impossible to unify unless the two terms are
			 * equal, in which case there the substitution is empty
			 */
			if (isEqual(term1, term2))
				return new NeutralSubstitution();
			else
				return null;
		}

		/* Arranging the terms so that the first is always a variable */
		Term t1 = null;
		Term t2 = null;

		if (term1 instanceof VariableImpl) {
			t1 = term1;
			t2 = term2;
		} else {
			t1 = term2;
			t2 = term1;
		}

		/*
		 * Undistinguished variables do not need a substitution, the unifier
		 * knows about this
		 */
		if ((t1 instanceof AnonymousVariable || t2 instanceof AnonymousVariable)) {
			return new NeutralSubstitution();
		}

		if (t2 instanceof VariableImpl) {
			if (isEqual(t1, t2)) {
				return new NeutralSubstitution();
			} else {
				return new Substitution(t1, t2);
			}
		} else if (t2 instanceof ValueConstant) {
			return new Substitution(t1, t2);
		} else if (t2 instanceof URIConstantImpl) {
			return new Substitution(t1, t2);
		} else if (t2 instanceof FunctionalTermImpl) {
			FunctionalTermImpl fterm = (FunctionalTermImpl) t2;
			if (fterm.containsTerm(t1))
				return null;
			else
				return new Substitution(t1, t2);
		}
		/* This should never happen */
		throw new RuntimeException("Unsupported unification case: " + term1
				+ " " + term2);
	}

	/***
	 * A equality calculation based on the strings that identify the terms.
	 * Terms of different classes are always different. If any of the two terms
	 * is an instance of UndistinguishedVariable then the equality is true. This
	 * is to make sure that no substitutions are calculated for undistinguished
	 * variables (if the substitution is going to be used later for atom
	 * unification then the unifier must be aware of this special treatment of
	 * UndistinguishedVariable instances).
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	private static boolean isEqual(Term t1, Term t2) {
		if (t1 == null || t2 == null)
			return false;
		if ((t1 instanceof AnonymousVariable)
				|| (t2 instanceof AnonymousVariable))
			return true;
		if (t1.getClass() != t2.getClass())
			return false;
		if (t1 instanceof VariableImpl) {
			VariableImpl ct1 = (VariableImpl) t1;
			VariableImpl ct2 = (VariableImpl) t2;
			return ct1.equals(ct2);
		} else if (t1 instanceof AnonymousVariable) {
			return true;
		} else if (t1 instanceof FunctionalTermImpl) {
			FunctionalTermImpl ct1 = (FunctionalTermImpl) t1;
			FunctionalTermImpl ct2 = (FunctionalTermImpl) t2;
			return ct1.equals(ct2);
		} else if (t1 instanceof ValueConstantImpl) {
			ValueConstantImpl ct1 = (ValueConstantImpl) t1;
			ValueConstantImpl ct2 = (ValueConstantImpl) t2;
			return ct1.equals(ct2);
		} else if (t1 instanceof URIConstantImpl) {
			URIConstantImpl ct1 = (URIConstantImpl) t1;
			URIConstantImpl ct2 = (URIConstantImpl) t2;
			return ct1.equals(ct2);
		} else if (t1 instanceof AlgebraOperatorPredicateImpl) {
			AlgebraOperatorPredicateImpl ct1 = (AlgebraOperatorPredicateImpl) t1;
			AlgebraOperatorPredicateImpl ct2 = (AlgebraOperatorPredicateImpl) t2;
			return ct1.equals(ct2);
		} else {
			throw new RuntimeException(
					"Exception comparing two terms, unknown term class. Terms: "
							+ t1 + ", " + t2 + " Classes: " + t1.getClass()
							+ ", " + t2.getClass());
		}
	}
}
