package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;

import static org.semanticweb.ontop.model.impl.DataAtomTools.areVariablesDistinct;


public class NonGroundDistinctVariableDataAtomImpl extends DataAtomImpl implements NonGroundDistinctVariableDataAtom {

    protected NonGroundDistinctVariableDataAtomImpl(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
        super(predicate, arguments);

        if (!areVariablesDistinct(arguments)) {
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
