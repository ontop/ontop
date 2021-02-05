package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;

public abstract class AbstractNonFunctionalTerm implements NonFunctionalTerm {

    @Override
    public ImmutableTerm simplify(VariableNullability variableNullability) {
        return this;
    }

    @Override
    public ImmutableTerm simplify() {
        return this;
    }
}
