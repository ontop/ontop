package it.unibz.inf.ontop.iq.visit.impl;

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
}
