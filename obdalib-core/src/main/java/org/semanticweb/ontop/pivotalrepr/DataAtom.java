package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableFunctionalTerm;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * TODO: explain
 *
 * Only accepts variables as arguments.
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
    boolean isEquivalent(DataAtom otherAtom);

    /**
     * Gets the arguments (all of them are variables).
     */
    ImmutableList<VariableImpl> getVariableTerms();
}
