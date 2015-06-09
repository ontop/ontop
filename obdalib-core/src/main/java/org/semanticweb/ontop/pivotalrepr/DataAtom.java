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
     * TODO: find a better name
     */
    boolean shareReferenceToTheSameAbstraction(DataAtom headAtom2);
}
