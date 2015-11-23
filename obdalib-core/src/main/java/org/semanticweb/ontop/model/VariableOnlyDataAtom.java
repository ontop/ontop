package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableList;

/**
 * Data atom that only has variables as arguments
 */
public interface VariableOnlyDataAtom extends NonGroundDataAtom {

    /**
     * Gets the arguments (variables and ground terms).
     */
    @Override
    ImmutableList<Variable> getVariablesOrGroundTerms();
}
