package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nonnull;

public interface QueryContextEvaluator {

    IQ optimize(IQ iq, @Nonnull QueryContext queryContext);
}
