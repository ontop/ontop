package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Unifier {

	private final Map<VariableImpl, Term> map;

    /**
     * Default and normal constructor.
     */
    public Unifier() {
        this.map = new HashMap<>();
    }

    /**
     * When we want to define a custom unifier.
     *
     * Tip: make it immutable if you can.
     */
    public Unifier(Map<VariableImpl, Term> substitutions) {
        this.map = substitutions;
    }

    public ImmutableMap<VariableImpl, Term> toMap() {
        return ImmutableMap.copyOf(this.map);
    }


    public Term get(VariableImpl var) {
		return map.get(var);
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Deprecated
	public void put(VariableImpl var, Term term) {
		map.put(var, term);
	}
	
	@Deprecated
	public Set<VariableImpl> keySet() {
		return map.keySet();
	}

    /***
     * Computes the Most General Unifier (MGU) for two n-ary atoms.
     *
     * Supports atoms with terms: Variable, URIConstant, ValueLiteral, ObjectVariableImpl.
     * If a term is an ObjectVariableImpl it can't have nested ObjectVariableImpl terms.
     *
     * IMPORTANT: Function terms are supported as long as they are not nested.
     *
     * IMPORTANT: handling of AnonymousVariables is questionable --
     *         much is left to UnifierUtilities.apply (and only one version handles them)
     *
     * @param first
     * @param second
     * @return
     */
    public static Unifier getMGU(Function first, Function second) {

		// Basic case: if predicates are different or their arity is different,
		// then no unifier
		if ((first.getArity() != second.getArity() 
				|| !first.getFunctionSymbol().equals(second.getFunctionSymbol()))) {
			return null;
		}

		Function firstAtom = (Function) first.clone();
		Function secondAtom = (Function) second.clone();

		int arity = first.getArity();
		Unifier mgu = new Unifier();

		// Computing the disagreement set
		for (int termidx = 0; termidx < arity; termidx++) {
			
			// Checking if there are already substitutions calculated for the
			// current terms. If there are any, then we have to take the
			// substituted terms instead of the original ones.

			Term term1 = firstAtom.getTerm(termidx);
			Term term2 = secondAtom.getTerm(termidx);

			boolean changed = false;
			
			// We have two cases, unifying 'simple' terms, and unifying function terms. 
			if (!(term1 instanceof Function) || !(term2 instanceof Function)) {
				
				if (!mgu.compose(term1, term2))
					return null;
				
				changed = true;
			}
			else {
				
				// if both of them are function terms then we need to do some
				// check in the inner terms
								
				Function fterm1 = (Function) term1;
				Function fterm2 = (Function) term2;

                if ((fterm1.getTerms().size() != fterm2.getTerms().size()) || 
                			!fterm1.getFunctionSymbol().equals(fterm2.getFunctionSymbol())) {
                   return null;
                }

				int innerarity = fterm1.getTerms().size();
				for (int innertermidx = 0; innertermidx < innerarity; innertermidx++) {
                    if (!mgu.compose(fterm1.getTerm(innertermidx), fterm2.getTerm(innertermidx)))
                        return null;
					
					changed = true;
					
					// Applying the newly computed substitution to the 'replacement' of
					// the existing substitutions
					UnifierUtilities.applyUnifier(fterm1, mgu, innertermidx + 1);
					UnifierUtilities.applyUnifier(fterm2, mgu, innertermidx + 1);
				}
			} 
			if (changed) {
				
				// Applying the newly computed substitution to the 'replacement' of
				// the existing substitutions
				UnifierUtilities.applyUnifier(firstAtom, mgu, termidx + 1);
				UnifierUtilities.applyUnifier(secondAtom, mgu, termidx + 1);				
			}
		}
		return mgu;
	}

    /***
	 * This will compose the unifier with the substitution for term1 and term2. 
	 * 
	 * Note that the unifier will be modified in this process.
	 * 
	 * The operation is as follows
	 * 
	 * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
	 * 
	 * @param term1
	 * @param term2
     * @return true if the substitution exists (false if it does not)
	 */
	public boolean compose(Term term1, Term term2) {
        Substitution s = getSubstitution(term1, term2);

        boolean acceptSubstitution = putSubstitution(s);
        return acceptSubstitution;
    }

    /**
     * If there is a non-neutral substitution,
     * puts its entries into the current unifier and returns true;
     *
     * Otherwise, does nothing and returns false;
     *
     */
    protected boolean putSubstitution(Substitution s) {
        if (s == null)
            return false;

        if (s instanceof NeutralSubstitution)
            return true;

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
        return true;
    }


    /***
     * Computes the substitution that makes two terms equal.
     *
     * ROMAN: careful -- does not appear to work correctly with AnonymousVariables
     *
     * TODO: discuss about the order of the two terms (when it matters, when not). This seems
     * to be very important.
     *
     * @param term1
     * @param term2
     * @return
     */
	private static Substitution getSubstitution(Term term1, Term term2) {

		if (!(term1 instanceof VariableImpl) && !(term2 instanceof VariableImpl)) {
			
			// neither is a variable, impossible to unify unless the two terms are
			// equal, in which case there the substitution is empty
			if ((term1 instanceof AnonymousVariable) || (term2 instanceof AnonymousVariable)) {
				// this is questionable -- consider R(_,_) and R(c,c)
				return new NeutralSubstitution();
			}
			
			if ((term1 instanceof VariableImpl) || (term1 instanceof FunctionalTermImpl) 
					|| (term1 instanceof ValueConstantImpl) || (term1 instanceof URIConstantImpl)) {
				
				// ROMAN: why is BNodeConstantImpl not mentioned?
				
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
        }
        /**
         * TODO: explain why the two terms can be "reversed".
         */
        else {
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
            FunctionalTermImpl fTerm = (FunctionalTermImpl) t2;

            /**
             * Prevents unifications like p(x) -> x.
             */
            if (fTerm.containsTerm(t1))
				return null;
			else
				return new Substitution(t1, t2);
		}
		// this should never happen 
		throw new RuntimeException("Unsupported unification case: " + term1 + " " + term2);
	}

	@Override
	public String toString() {
		return "Unifier: " + map.toString();
	}	
}
