package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * TODO: find a better name
 *
 * Data atom only composed of variables; all these variables are distinct.
 *
 */
public interface DistinctVariableOnlyDataAtom extends DataAtom<AtomPredicate> {

    @Override
    ImmutableList<Variable> getArguments();

    @Override
    Variable getTerm(int index);
}
