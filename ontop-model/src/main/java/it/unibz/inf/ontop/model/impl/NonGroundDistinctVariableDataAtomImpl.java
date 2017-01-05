package it.unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.NonGroundDistinctVariableDataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;



public class NonGroundDistinctVariableDataAtomImpl extends DataAtomImpl implements NonGroundDistinctVariableDataAtom {

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
