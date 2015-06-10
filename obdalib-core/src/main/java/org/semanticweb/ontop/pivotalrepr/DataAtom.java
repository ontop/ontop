package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.ImmutableFunctionalTerm;

/**
 * TODO: explain
 *
 * Immutable.
 *
 * In the future, this class will be disassociated from the Function class.
 */
public interface DataAtom extends ImmutableFunctionalTerm {

    AtomPredicate getPredicate();

    /**
     * Effective arity (number of sub-terms).
     */
    int getEffectiveArity();

    /**
     * TODO: explain
     */
    boolean referSameAbstraction(DataAtom headAtom2);

    /**
     * Returns true if the atom contains
     * some functional terms.
     *
     * TODO: find a better name
     */
    boolean isTyped();

}
