package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.DistinctVariableDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;


public class NonGroundDistinctVariableDataAtomImpl extends AbstractDataAtomImpl implements DistinctVariableDataAtom {

    protected NonGroundDistinctVariableDataAtomImpl(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
        super(predicate, arguments);

        if (!DataAtomTools.areVariablesDistinct(arguments)) {
            throw new IllegalArgumentException("The given variables are not distinct!");
        }
    }

    protected NonGroundDistinctVariableDataAtomImpl(AtomPredicate predicate, Variable... distinctVariables) {
        this(predicate, ImmutableList.copyOf(distinctVariables));
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
