package it.unibz.inf.ontop.iq.visit.impl;

import java.util.Optional;

public abstract class IQStateOptionalTransformer<T extends IQStateOptionalTransformer<T>> extends IQStateTransformer<Optional<T>> {
    protected IQStateOptionalTransformer() {
        super(Optional::empty);
    }

    protected abstract Optional<T> next();

    public static <T extends IQStateOptionalTransformer<T>> T reachMonotoneFixedPoint(T initial) {
        T state = initial;
        while (true) {
            Optional<T> next = state.next();
            if (next.isEmpty())
                return state;
            state = next.get();
        }
    }
}
