package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AlgebraOperatorPredicateImpl;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.URIConstantImpl;
import it.unibz.krdb.obda.model.impl.ValueConstantImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Unifier {

	private final Map<VariableImpl, Term> map = new HashMap<VariableImpl, Term>();
	

	public Term get(VariableImpl var) {
		return map.get(var);
	}

	public Term getRaw(Term var) {
		return map.get(var);
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public void put(VariableImpl var, Term term) {
		map.put(var, term);
	}
	
	public Set<VariableImpl> keySet() {
		return map.keySet();
	}
	
	/***
	 * This will compose the unifier with the substitution. Note that the unifier
	 * will be modified in this process.
	 * 
	 * The operation is as follows
	 * 
	 * {x/y, m/y} composed with y/z is equal to {x/z, m/z, y/z}
	 * 
	 * @param unifier The unifier that will be composed
	 * @param s The substitution to compose
	 */
	public void compose(Substitution s) {
		
		if (s instanceof NeutralSubstitution)
			return;
		
		List<VariableImpl> forRemoval = new LinkedList<VariableImpl>();
		for (Entry<VariableImpl,Term> entry : map.entrySet()) {
			VariableImpl v = entry.getKey();
			Term t = entry.getValue();
			if (s.getVariable().equals(t)) { // ROMAN: no need in isEqual(t, s.getVariable())
				if (v.equals(s.getTerm())) {  // ROMAN: no need in isEqual(v, s.getTerm())
					// The substitution for the current variable has become
					// trivial, e.g., x/x with the current composition. We
					// remove it to keep only a non-trivial unifier
					forRemoval.add(v);
				} else 
					map.put(v, s.getTerm());
				
			} 
			else if (t instanceof FunctionalTermImpl) {
				FunctionalTermImpl fclone = (FunctionalTermImpl)t.clone();
				List<Term> innerTerms = fclone.getTerms();
				boolean innerchanges = false;
				// TODO this ways of changing inner terms in functions is not
				// optimal, modify

				for (int i = 0; i < innerTerms.size(); i++) {
					Term innerTerm = innerTerms.get(i);

					if (s.getVariable().equals(innerTerm)) { // ROMAN: no need in isEqual(innerTerm, s.getVariable())
						fclone.getTerms().set(i, s.getTerm());
						innerchanges = true;
					}
				}
				if (innerchanges)
					map.put(v, fclone);
			}
		}
		map.keySet().removeAll(forRemoval);
		map.put(s.getVariable(), s.getTerm());
	}
	
	
	/***
	 * Computes the substitution that makes two terms equal. 
	 * 
	 * ROMAN: careful -- does not appear to work correctly with AnonymousVariables
	 * 
	 * @param term1
	 * @param term2
	 * @return
	 */
	public static Substitution getSubstitution(Term term1, Term term2) {

		if (!(term1 instanceof VariableImpl) && !(term2 instanceof VariableImpl)) {
			
			// neither is a variable, impossible to unify unless the two terms are
			// equal, in which case there the substitution is empty
			if ((term1 instanceof AnonymousVariable) || (term2 instanceof AnonymousVariable)) {
				// this is questionable -- consider R(_,_) and R(c,c)
				return new NeutralSubstitution();
			}
			
			if ((term1 instanceof VariableImpl) || (term1 instanceof FunctionalTermImpl) 
					|| (term1 instanceof ValueConstantImpl) || (term1 instanceof URIConstantImpl)) {
				if (term1.equals(term2))
					return new NeutralSubstitution();
				else
					return null;
			} 
			throw new RuntimeException("Exception comparing two terms, unknown term class. Terms: "
								+ term1 + ", " + term2 + " Classes: " + term1.getClass()
								+ ", " + term2.getClass());
		}

		// arranging the terms so that the first is always a variable 
		VariableImpl t1;
		Term t2;
		if (term1 instanceof VariableImpl) {
			t1 = (VariableImpl)term1;
			t2 = term2;
		} else {
			t1 = (VariableImpl)term2;
			t2 = term1;
		}

		// Undistinguished variables do not need a substitution, 
		// the unifier knows about this
		if (t2 instanceof AnonymousVariable) { // ROMAN: no need in (t1 instanceof AnonymousVariable)
			return new NeutralSubstitution();
		}
		else if (t2 instanceof VariableImpl) {
			if (t1.equals(t2))   // ROMAN: no need in isEqual(t1, t2) -- both are proper variables
				return new NeutralSubstitution();
			else 
				return new Substitution(t1, t2);
		} 
		else if ((t2 instanceof ValueConstantImpl) || (t2 instanceof URIConstantImpl)) {
			return new Substitution(t1, t2);
		} 
		else if (t2 instanceof FunctionalTermImpl) {
			FunctionalTermImpl fterm = (FunctionalTermImpl) t2;
			if (fterm.containsTerm(t1))
				return null;
			else
				return new Substitution(t1, t2);
		}
		// this should never happen 
		throw new RuntimeException("Unsupported unification case: " + term1 + " " + term2);
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
	 * ROMAN: I do not quite understand this method (in particular, lots of type casts)
	 *        all uses remove and commented out (some code adapted and moved to getSubstitution)
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
/*	
	private static boolean isEqual(Term t1, Term t2) {
		if (t1 == null || t2 == null)
			return false;
		// ROMAN: does this imply that the same substitution is applied to any number of AnonymousVariables?
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
			// ROMAN: how can we get here if the check above returns true?!
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
		} else if (t1 instanceof AlgebraOperatorPredicateImpl) { // ROMAN: this is not even a term!
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
*/	
	
}
