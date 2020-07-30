package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

public interface JoinLikeOptimizer {
    IQ optimize(IQ initialQuery, ExecutorRegistry executorRegistry);
}
