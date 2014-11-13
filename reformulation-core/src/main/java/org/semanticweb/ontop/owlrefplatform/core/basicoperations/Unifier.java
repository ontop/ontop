package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import fj.P;
import fj.P2;
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
    private static OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

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
        Multimap<Predicate, Integer> emptyMulti= ArrayListMultimap.create();
        Unifier unifier = getMGU(first, second, false, emptyMulti);
        return unifier;
    }

    /**
     * TODO: explain
     * @param first
     * @param second
     * @param multiTypedPredicateIndex
     * @return
     */
    public static Unifier getTypePropagatingMGU(Function first, Function second,
                                              Multimap<Predicate, Integer> multiTypedPredicateIndex) {
        Unifier unifier = getMGU(first, second, true, multiTypedPredicateIndex);
        return unifier;
    }

    private static Unifier getMGU(Function first, Function second, boolean typePropagation,
                                 Multimap<Predicate,Integer> multiTypedPredicateIndex) {

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
				
				if (!mgu.compose(term1, term2, typePropagation))
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

                    /**
                     * Normal composition
                     */
                    if(!typePropagation) {
                        if (!mgu.compose(fterm1.getTerm(innertermidx), fterm2.getTerm(innertermidx)))
                            return null;
                    }
                    /**
                     * Specific case: composition with type propagation.
                     */
                    else {
                        if (!mgu.composeAndPropagateType(fterm1, fterm2.getTerm(innertermidx), innertermidx,
                                multiTypedPredicateIndex))
                            return null;
                    }
					
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
        return compose(term1, term2, false);
	}

    private boolean compose(Term term1, Term term2, boolean typePropagation) {
        Substitution s = getSubstitution(term1, term2, typePropagation);

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
    private boolean putSubstitution(Substitution s) {
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


    /**
     * SIDE-EFFECT method. Not just a simple test.
     *
     * Use case: handle aggregates.
     * TODO: explain further.
     *
     */
    private boolean composeAndPropagateType(Function atom1, Term term2, int termIndex, Multimap<Predicate, Integer> multiTypedPredicateIndex) {
        Predicate functionSymbol1 = atom1.getFunctionSymbol();
        Term term1 = atom1.getTerm(termIndex);

        Substitution s;
        if (multiTypedPredicateIndex.containsKey(functionSymbol1) ){ // it is a problematic predicate regarding templates
            if (multiTypedPredicateIndex.get(functionSymbol1).contains(termIndex)){ //the term is the problematic one
                s = new NeutralSubstitution();
            } else{
                s = getSubstitution(term1, term2, true);
            }
        }
        else{
            s = getSubstitution(term1, term2, true);
        }

        boolean acceptSubstitution = putSubstitution(s);
        return acceptSubstitution;
    }

    /**
     * Normal case.
     */
    private static Substitution getSubstitution(Term term1, Term term2) {
        return getSubstitution(term1, term2, false);
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
	private static Substitution getSubstitution(Term term1, Term term2, boolean propagateType) {

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
            /**
             * Type propagation special case.
             */
        } else if (propagateType && (term1 instanceof Function)) {
            P2<VariableImpl, Term> proposedTerms = getTypePropagatingSubstitution((Function) term1, term2);
            t1 = proposedTerms._1();
            t2 = proposedTerms._2();
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
             * Only in the normal mode
             *
             * Prevents unifications like p(x) -> x.
             */
            if (fTerm.containsTerm(t1) && (!propagateType))
				return null;
			else
				return new Substitution(t1, t2);
		}
		// this should never happen 
		throw new RuntimeException("Unsupported unification case: " + term1 + " " + term2);
	}

    /**
     * TODO: explain
     */
    private static P2<VariableImpl, Term> getTypePropagatingSubstitution(Function functionalTerm1, Term term2) {
        Predicate functionSymbol1 = functionalTerm1.getFunctionSymbol();

        /**
         * Term1 is an aggregate --> looks inside its first sub-term.
         */
        if (functionSymbol1.isAggregationPredicate()) {
            Term subTerm = functionalTerm1.getTerm(0);

            /**
             * If its first sub-term is functional, it may be a data type.
             */
            if (subTerm instanceof Function) {
                Predicate subTermSymbol = ((Function) subTerm).getFunctionSymbol();

                /**
                 * If is a type, applies this type to the second term.
                 */
                if (subTermSymbol.isDataTypePredicate()) {
                    Term typedTerm2 = dataFactory.getFunction(subTermSymbol, term2);
                    return P.p((VariableImpl)term2, typedTerm2);
                }
            }

            /**
             * Term 1 is a data type.
             *
             * Then, we have to look if there is an aggregate inside.
             */
        } else if (functionSymbol1.isDataTypePredicate()) {
            Predicate type = functionSymbol1;
            Term subTerm = functionalTerm1.getTerm(0);

            //case where the aggregate is inside type, Count for instance
            /**
             * The sub-term is functional ...
             */
            if (subTerm instanceof Function) {
                functionSymbol1 = ((Function) subTerm).getFunctionSymbol();

                /**
                 *  ... and is an aggregate
                 */
                if (functionSymbol1.isAggregationPredicate()) {
                    Term subSubTerm = ((Function) subTerm).getTerm(0);

                    if (subSubTerm instanceof Function) {
                        Term typedTerm2 = dataFactory.getFunction(type, term2);
                        return P.p((VariableImpl)term2, typedTerm2);
                    }

                }
            }
        }
        /**
         * If term1 is a variable, at least term2 is.
         */
        return P.p((VariableImpl)term2, (Term)functionalTerm1);
    }

	@Override
	public String toString() {
		return "Unifier: " + map.toString();
	}	
}
