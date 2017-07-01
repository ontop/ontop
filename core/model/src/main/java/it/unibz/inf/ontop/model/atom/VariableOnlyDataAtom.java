package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Data atom that only has variables as arguments
 */
public interface VariableOnlyDataAtom extends DataAtom {

    @Override
    ImmutableList<Variable> getArguments();
}