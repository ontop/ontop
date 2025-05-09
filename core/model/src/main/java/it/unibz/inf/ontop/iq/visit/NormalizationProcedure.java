package it.unibz.inf.ontop.iq.visit;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.visit.impl.StateIQVisitor;

public interface NormalizationProcedure<T extends StateIQVisitor<T>> {
    int MAX_NORMALIZATION_ITERATIONS = 10000;

    default IQTree normalize() {
        StateIQVisitor<T> state = getInitialState();

        for (int i = 0; i < MAX_NORMALIZATION_ITERATIONS; i++) {
            StateIQVisitor<T> newState = state.reduce();

            if (newState.equals(state))
                return newState.toIQTree();

            state = newState;
        }

        throw new MinorOntopInternalBugException("Bug: NormalizationProcedure did not converge after "
                + MAX_NORMALIZATION_ITERATIONS + " iterations");
    }

    StateIQVisitor<T> getInitialState();
}

