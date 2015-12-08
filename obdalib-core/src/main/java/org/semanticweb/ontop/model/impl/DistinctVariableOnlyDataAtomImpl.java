package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DistinctVariableOnlyDataAtom;
import org.semanticweb.ontop.model.Variable;

import static org.semanticweb.ontop.model.impl.DataAtomTools.areVariablesDistinct;

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
