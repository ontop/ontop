package unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.model.AtomPredicate;
import unibz.inf.ontop.model.Variable;
import unibz.inf.ontop.model.VariableOnlyDataAtom;

public class VariableOnlyDataAtomImpl extends NonGroundDataAtomImpl implements VariableOnlyDataAtom {
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
}
