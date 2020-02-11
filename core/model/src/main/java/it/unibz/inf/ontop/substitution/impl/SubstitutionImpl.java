package it.unibz.inf.ontop.substitution.impl;

import com.google.common.base.Joiner;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermImpl;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.*;


/**
 * Mutable reference implementation of a Substitution.
 *
 */
public class SubstitutionImpl implements Substitution {

    private final Map<Variable, Term> map;
    private final TermFactory termFactory;

    public SubstitutionImpl(TermFactory termFactory) {
        this.termFactory = termFactory;
        this.map = new HashMap<>();
    }

    public SubstitutionImpl(TermFactory termFactory, Variable v, Term t) {
        this.termFactory = termFactory;
        this.map = new HashMap<>();
        map.put(v, t);
    }

    @Override
    public Map<Variable, Term> getMap() {
        return map;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Deprecated
    public Set<Variable> keySet() {
        return map.keySet();
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }


    /***
     * Creates a unifier (singleton substitution) out of term1 and term2.
     *
     * Then, composes the current substitution with this unifier.
     * (remind that composition is not commutative).
     *
     *
     * Note that this Substitution object will be modified in this process.
     *
     * The operation is as follows
     *
     * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
     *
     * @param term1
     * @param term2
     * @return true if the substitution exists (false if it does not)
     */
    private boolean composeTerms(Term term1, Term term2) {

        /**
         * Special case: unification of two functional terms (possibly recursive)
         */
        if ((term1 instanceof Function) && (term2 instanceof Function)) {
            return composeFunctions((Function) term1, (Function) term2);
        }

        Substitution s = createUnifier(term1, term2, termFactory);

        // Rejected substitution (conflicts)
        if (s == null)
            return false;

        // Neutral substitution
        if (s.getMap().isEmpty())
            return true;

        // Not neutral, not null --> should be a singleton.
        Map.Entry<Variable, Term> substitution = s.getMap().entrySet().iterator().next();


        List<Variable> forRemoval = new ArrayList<>();
        for (Map.Entry<Variable,Term> entry : map.entrySet()) {
            Variable v = entry.getKey();
            Term t = entry.getValue();
            if (substitution.getKey().equals(t)) {
                if (v.equals(substitution.getValue())) {
                    // The substitution for the current variable has become
                    // trivial, e.g., x/x with the current composition. We
                    // remove it to keep only a non-trivial unifier
                    forRemoval.add(v);
                } else
                    map.put(v, substitution.getValue());

            }
            else if (t instanceof FunctionalTermImpl) {
                FunctionalTermImpl fclone = (FunctionalTermImpl)t.clone();
                boolean innerchanges = applySingletonSubstitution(fclone, substitution);
                if (innerchanges)
                    map.put(v, fclone);
            }
        }
        map.keySet().removeAll(forRemoval);
        map.put(substitution.getKey(), substitution.getValue());
        return true;
    }

    /**
     * May alter the functionalTerm (mutable style)
     *
     * Recursive
     */
    private static boolean applySingletonSubstitution(Function functionalTerm, Map.Entry<Variable, Term> substitution) {
        List<Term> innerTerms = functionalTerm.getTerms();
        boolean innerchanges = false;
        // TODO this ways of changing inner terms in functions is not
        // optimal, modify

        for (int i = 0; i < innerTerms.size(); i++) {
            Term innerTerm = innerTerms.get(i);

            if (innerTerm instanceof Function) {
                // Recursive call
                boolean newChange = applySingletonSubstitution((Function) innerTerm, substitution);
                innerchanges = innerchanges || newChange;
            }
            else if (substitution.getKey().equals(innerTerm)) {
                functionalTerm.getTerms().set(i, substitution.getValue());
                innerchanges = true;
            }
        }
        return innerchanges;
    }


    @Override
    public boolean composeFunctions(Function first, Function second) {
        // Basic case: if predicates are different or their arity is different,
        // then no unifier
        if ((first.getArity() != second.getArity()
                || !first.getFunctionSymbol().equals(second.getFunctionSymbol()))) {
            return false;
        }

        Function firstAtom = (Function) first.clone();
        Function secondAtom = (Function) second.clone();

        int arity = first.getArity();

        // Computing the disagreement set
        for (int termidx = 0; termidx < arity; termidx++) {

            // Checking if there are already substitutions calculated for the
            // current terms. If there are any, then we have to take the
            // substituted terms instead of the original ones.

            Term term1 = firstAtom.getTerm(termidx);
            Term term2 = secondAtom.getTerm(termidx);

            if (!composeTerms(term1, term2))
                return false;

            // Applying the newly computed substitution to the 'replacement' of
            // the existing substitutions
            applySubstitution(firstAtom, this, termidx + 1);
            applySubstitution(secondAtom, this, termidx + 1);
        }

        return true;
    }

    /***
     * Computes the unifier that makes two terms equal.
     *
     * ROMAN: careful -- does not appear to work correctly with AnonymousVariables
     *
     * @param term1
     * @param term2
     * @return
     */
    private static Substitution createUnifier(Term term1, Term term2, TermFactory termFactory) {

        if (!(term1 instanceof Variable) && !(term2 instanceof Variable)) {

            // neither is a variable, impossible to unify unless the two terms are
            // equal, in which case there the substitution is empty

            if (/*(term1 instanceof VariableImpl) ||*/ (term1 instanceof FunctionalTermImpl)
                    || (term1 instanceof Constant)) {

                // ROMAN: why is BNodeConstantImpl not mentioned?
                // BC: let's accept it, templates for Bnodes should be supported

                if (term1.equals(term2))
                    return new SubstitutionImpl(termFactory);
                else
                    return null;
            }
            throw new RuntimeException("Exception comparing two terms, unknown term class. Terms: "
                    + term1 + ", " + term2 + " Classes: " + term1.getClass()
                    + ", " + term2.getClass());
        }

        // arranging the terms so that the first is always a variable
        Variable t1;
        Term t2;
        if (term1 instanceof Variable) {
            t1 = (Variable)term1;
            t2 = term2;
        } else {
            t1 = (Variable)term2;
            t2 = term1;
        }

        // Undistinguished variables do not need a substitution,
        // the unifier knows about this
        if  (t2 instanceof Variable) {
            if (t1.equals(t2))   // ROMAN: no need in isEqual(t1, t2) -- both are proper variables
                return new SubstitutionImpl(termFactory);
            else
                return new SubstitutionImpl(termFactory, t1, t2);
        }
        else if (t2 instanceof Constant) {
            return new SubstitutionImpl(termFactory, t1, t2);
        }
        else if (t2 instanceof Function) {
            Function fterm = (Function) t2;
            if (fterm.containsTerm(t1))
                return null;
            else
                return new SubstitutionImpl(termFactory, t1, t2);
        }
        // this should never happen
        throw new RuntimeException("Unsupported unification case: " + term1 + " " + term2);
    }

    /**
     * Applies the substitution to all the terms in the list. Note that this
     * will not clone the list or the terms inside the list.
     *
     * @param atom
     * @param unifier
     */

    private static void applySubstitution(Function atom, Substitution unifier, int fromIndex) {

        List<Term> terms = atom.getTerms();

        for (int i = fromIndex; i < terms.size(); i++) {
            Term t = terms.get(i);

            // unifiers only apply to variables, simple or inside functional terms

            if (t instanceof Variable) {
                Term replacement = unifier.getMap().get((Variable) t);
                if (replacement != null)
                    terms.set(i, replacement);
            }
            else if (t instanceof Function) {
                applySubstitution((Function) t, unifier, 0);
            }
        }
    }

}
