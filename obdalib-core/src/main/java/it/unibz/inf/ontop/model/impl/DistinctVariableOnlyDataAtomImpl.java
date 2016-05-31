package it.unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.Variable;

import static it.unibz.inf.ontop.model.impl.DataAtomTools.areVariablesDistinct;

public class DistinctVariableOnlyDataAtomImpl extends VariableOnlyDataAtomImpl implements DistinctVariableOnlyDataAtom {

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, ImmutableList<Variable> variables) {
        super(predicate, variables);

        if (!areVariablesDistinct(variables)) {
            throw new IllegalArgumentException("Variables must be distinct!");
        }
    }

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, Variable... variables) {
        super(predicate, variables);
    }
}
