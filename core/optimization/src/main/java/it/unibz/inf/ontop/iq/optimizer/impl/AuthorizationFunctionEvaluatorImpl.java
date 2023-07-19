package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.AuthorizationFunctionEvaluator;

public class AuthorizationFunctionEvaluatorImpl implements AuthorizationFunctionEvaluator {

    @Inject
    protected AuthorizationFunctionEvaluatorImpl() {
    }

    @Override
    public IQ optimize(IQ iq, QueryContext queryContext) {
        // TODO: implement
        return iq;
    }
}
