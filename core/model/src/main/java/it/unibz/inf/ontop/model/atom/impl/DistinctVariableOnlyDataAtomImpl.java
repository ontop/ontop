package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.type.TermType;

public class DistinctVariableOnlyDataAtomImpl extends AbstractDataAtomImpl<AtomPredicate>
        implements DistinctVariableOnlyDataAtom {

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, ImmutableList<Variable> variables) {
        super(predicate, variables);

        if (!DataAtomTools.areVariablesDistinct(variables)) {
            throw new IllegalArgumentException("Variables must be distinct!");
        }
    }

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, Variable... variables) {
        super(predicate, variables);
    }

    @Override
    public Variable getTerm(int index) {
        return (Variable) super.getTerm(index);
    }

    @Override
    public ImmutableList<Variable> getArguments() {
        return (ImmutableList<Variable>)super.getArguments();
    }

}
