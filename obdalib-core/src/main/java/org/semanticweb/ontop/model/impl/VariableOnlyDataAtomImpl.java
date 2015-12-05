package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.VariableOnlyDataAtom;

public class VariableOnlyDataAtomImpl extends NonGroundDataAtomImpl implements VariableOnlyDataAtom {
    protected VariableOnlyDataAtomImpl(AtomPredicate predicate, ImmutableList<Variable> variables) {
        super(predicate, variables);
    }

    protected VariableOnlyDataAtomImpl(AtomPredicate predicate, Variable... variables) {
        super(predicate, variables);
    }

    @Override
    public ImmutableList<Variable> getImmutableTerms() {
        return (ImmutableList<Variable>)super.getImmutableTerms();
    }
}
