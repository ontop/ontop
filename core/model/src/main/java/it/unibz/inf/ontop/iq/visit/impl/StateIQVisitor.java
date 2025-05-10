package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQTree;

public abstract class StateIQVisitor<T extends StateIQVisitor<T>> extends IQStateTransformer<T> {

    public StateIQVisitor() {
        super(null);
    }

    @Override
    protected final T done() {
        return (T)this;
    }

    protected final T continueTo(IQTree next) {
        return next.acceptVisitor(this);
    }

    public static <T extends StateIQVisitor<T>> T reachFixedPoint(T initialState, int maxNumberOfIterations) {
        //Non-final
        T state = initialState;
        for (int i = 0; i < maxNumberOfIterations; i++) {
            T newState = state.reduce();
            if (newState.equals(state))
                return newState;
            state = newState;
        }
        throw new MinorOntopInternalBugException("Bug: NormalizationProcedure did not converge after "
                + maxNumberOfIterations + " iterations");
    }


    public abstract T reduce();

    public abstract IQTree toIQTree();

    @Override
    public abstract boolean equals(Object o);
}
