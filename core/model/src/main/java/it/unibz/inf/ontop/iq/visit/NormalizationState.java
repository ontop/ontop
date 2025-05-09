package it.unibz.inf.ontop.iq.visit;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;

import java.util.Optional;

public interface NormalizationState<T extends NormalizationState<T>> {

    Optional<T> next();

    static <T extends NormalizationState<T>> T reachFixedPoint(T initial, int numberOfIterations) {
        //Non-final
        T state = initial;
        for (int i = 0; i < numberOfIterations; i++) {
            Optional<T> newState = state.next();
            if (newState.isEmpty() || newState.get().equals(state))
                return state;
            state = newState.get();
        }
        throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", numberOfIterations));
    }
}
