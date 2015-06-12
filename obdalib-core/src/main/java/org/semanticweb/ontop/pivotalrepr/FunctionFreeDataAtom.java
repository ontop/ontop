package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableFunctionalTerm;
import org.semanticweb.ontop.model.NonFunctionalTerm;

/**
 * Immutable data atom that only accepts variables and constants as arguments.
 *
 * In the future, this class could be disassociated from the Function class.
 */
public interface FunctionFreeDataAtom extends ImmutableFunctionalTerm {

    AtomPredicate getPredicate();

    /**
     * Effective arity (number of sub-terms).
     */
    int getEffectiveArity();

    @Override
    NonFunctionalTerm getTerm(int index);

    /**
     * TODO: explain
     */
    boolean isEquivalent(FunctionFreeDataAtom otherAtom);

    /**
     * Gets the arguments (none of them are functional terms).
     */
    ImmutableList<NonFunctionalTerm> getNonFunctionalTerms();
}
