package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DistinctVariableDataAtom;
import org.semanticweb.ontop.model.Variable;


public class DistinctVariableDataAtomImpl extends VariableOnlyDataAtomImpl implements DistinctVariableDataAtom {

    protected DistinctVariableDataAtomImpl(AtomPredicate predicate, ImmutableSet<Variable> distinctVariables) {
        super(predicate, ImmutableList.copyOf(distinctVariables));
    }

    protected DistinctVariableDataAtomImpl(AtomPredicate predicate, ImmutableList<Variable> distinctVariables) {
        super(predicate, distinctVariables);

        if (ImmutableSet.copyOf(distinctVariables).size() < distinctVariables.size()) {
            throw new IllegalArgumentException("The given variables are not distinct!");
        }
    }

    protected DistinctVariableDataAtomImpl(AtomPredicate predicate, Variable... distinctVariables) {
        this(predicate, ImmutableList.copyOf(distinctVariables));
    }
}
