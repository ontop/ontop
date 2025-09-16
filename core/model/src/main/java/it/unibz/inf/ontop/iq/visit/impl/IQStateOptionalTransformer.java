package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;

import java.util.Optional;
import java.util.function.Function;

public abstract class IQStateOptionalTransformer<T> extends IQStateDefaultTransformer<Optional<T>> {


    public static <T> T reachFinalState(T initial, Function<T, Optional<? extends T>> transformer, int maxIterations) {
        //Non-final
        T state = initial;
        for (int i = 0; i < maxIterations; i++) {
            Optional<? extends T> next = transformer.apply(state);
            if (next.isEmpty())
                return state;
            state = next.get();
        }
        throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", maxIterations));
    }

    protected final Optional<T> done() {
        return Optional.empty();
    }
}
