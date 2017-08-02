package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;

public class DistinctVariableOnlyDataAtomImpl extends VariableOnlyDataAtomImpl implements DistinctVariableOnlyDataAtom {

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, ImmutableList<Variable> variables) {
        super(predicate, variables);

        if (!DataAtomTools.areVariablesDistinct(variables)) {
            throw new IllegalArgumentException("Variables must be distinct!");
        }
    }

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, Variable... variables) {
        super(predicate, variables);
    }
}
