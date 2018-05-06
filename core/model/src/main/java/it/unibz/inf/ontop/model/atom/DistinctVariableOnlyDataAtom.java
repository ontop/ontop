package it.unibz.inf.ontop.model.atom;

import it.unibz.inf.ontop.model.term.Variable;

/**
 * TODO: find a better name
 *
 * Data atom only composed of variables; all these variables are distinct.
 *
 */
public interface DistinctVariableOnlyDataAtom extends VariableOnlyDataAtom, DistinctVariableDataAtom {

    @Override
    Variable getTerm(int index);
}
