package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;

import java.util.Optional;
import java.util.function.Function;

public abstract class IQStateOptionalTransformer<T> extends IQStateTransformer<Optional<T>> {

    protected IQStateOptionalTransformer() {
        super(Optional::empty);
    }

    public static <T> T reachMonotoneFixedPoint(T initial, Function<T, ? extends Optional<T>> transformer) {
        T state = initial;
        while (true) {
            Optional<T> next = transformer.apply(state);
            if (next.isEmpty())
                return state;
            state = next.get();
        }
    }

    public static <T> T reachFixedPoint(T initial, Function<T, T> transformer, int maxIterations) {
        T state = initial;
        for(int i = 0; i < maxIterations; i++) {
            T next = transformer.apply(state);
            if (next.equals(state))
                return state;
            state = next;
        }
        throw new MinorOntopInternalBugException("No convergence after " + maxIterations + " iterations");
    }
}
