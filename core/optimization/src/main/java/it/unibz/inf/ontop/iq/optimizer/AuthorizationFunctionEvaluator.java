package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.IQ;

public interface AuthorizationFunctionEvaluator {

    IQ optimize(IQ iq, QueryContext queryContext);
}
