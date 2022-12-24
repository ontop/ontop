package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

import java.util.HashSet;
import java.util.Set;

public class DistinctVariableOnlyDataAtomImpl extends AbstractDataAtomImpl<AtomPredicate>
        implements DistinctVariableOnlyDataAtom {

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, ImmutableList<Variable> variables) {
        super(predicate, variables);

        if (!areVariablesDistinct(variables)) {
            throw new IllegalArgumentException("Variables must be distinct!");
        }
    }

    protected DistinctVariableOnlyDataAtomImpl(AtomPredicate predicate, Variable... variables) {
        super(predicate, variables);
    }

    private static boolean areVariablesDistinct(ImmutableList<? extends VariableOrGroundTerm> arguments) {
        Set<Variable> encounteredVariables = new HashSet<>();

        for (VariableOrGroundTerm argument : arguments) {
            if (argument instanceof Variable) {
                if (!encounteredVariables.add((Variable)argument)) {
                    return false;
                }
            }
        }
        return true;
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
