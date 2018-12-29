package it.unibz.inf.ontop.utils;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.impl.VariableNullabilityImpl;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;

public class DummyVariableNullability extends VariableNullabilityImpl {

    public DummyVariableNullability(ImmutableFunctionalTerm expression) {
        super(expression.getVariableStream()
                .map(ImmutableSet::of)
                .collect(ImmutableCollectors.toSet()));
    }
}
