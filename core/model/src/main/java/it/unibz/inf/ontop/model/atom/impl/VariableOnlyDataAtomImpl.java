package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.atom.VariableOnlyDataAtom;

public class VariableOnlyDataAtomImpl extends AbstractDataAtomImpl implements VariableOnlyDataAtom {
    protected VariableOnlyDataAtomImpl(AtomPredicate predicate, ImmutableList<Variable> variables) {
        super(predicate, variables);
    }

    protected VariableOnlyDataAtomImpl(AtomPredicate predicate, Variable... variables) {
        super(predicate, variables);
    }

    @Override
    public ImmutableList<Variable> getArguments() {
        return (ImmutableList<Variable>)super.getArguments();
    }

    @Override
    public boolean isGround() {
        return getVariables().isEmpty();
    }
}
