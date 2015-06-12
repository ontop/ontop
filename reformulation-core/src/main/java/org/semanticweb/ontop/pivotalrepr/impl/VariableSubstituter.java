package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

/**
 * TODO: explain
 */
public class VariableSubstituter {

    /**
     * TODO: explain
     *
     * Returns a new IntermediateQuery (the original one is untouched).
     */
    public static IntermediateQuery cloneAndSubstituteVariables(final IntermediateQuery originalQuery,
                                                                final FunctionFreeDataAtom targetDataAtom,
                                                                final ImmutableSet<VariableImpl> reservedVariables) {
        // TODO: implement it
        return null;
    }
}
