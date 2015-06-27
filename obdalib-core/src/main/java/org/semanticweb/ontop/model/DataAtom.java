package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableList;

/**
 * Immutable data atom that only accepts variables and ground terms as arguments.
 *
 * In the future, this class could be disassociated from the Function class.
 */
public interface DataAtom extends ImmutableFunctionalTerm {

    AtomPredicate getPredicate();

    /**
     * Effective arity (number of sub-terms).
     */
    int getEffectiveArity();

    @Override
    VariableOrGroundTerm getTerm(int index);

    /**
     * TODO: explain
     */
    boolean isEquivalent(DataAtom otherAtom);

    boolean hasSamePredicateAndArity(DataAtom otherAtom);

    /**
     * Gets the arguments (variables and ground terms).
     */
    ImmutableList<VariableOrGroundTerm> getVariablesOrGroundTerms();
}
