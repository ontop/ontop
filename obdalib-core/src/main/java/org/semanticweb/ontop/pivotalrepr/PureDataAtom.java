package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Immutable data atom that only accepts variables as arguments.
 * These variables must NOT be duplicated.
 *
 */
public interface PureDataAtom extends FunctionFreeDataAtom {

    @Override
    VariableImpl getTerm(int index);

    /**
     * Gets the arguments (all of them are variables).
     */
    ImmutableList<VariableImpl> getVariableTerms();

    /**
     * This operation is not commutative.
     *
     * By A subsumes B, we mean that is exists a substitution S
     *   such that AS = B
     */
    boolean subsumes(FunctionFreeDataAtom atom);
}
