package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nullable;

public interface AuthorizationFunctionEvaluator {

    IQ optimize(IQ iq, @Nullable QueryContext queryContext);
}
