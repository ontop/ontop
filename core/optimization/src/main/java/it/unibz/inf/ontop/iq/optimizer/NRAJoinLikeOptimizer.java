package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

public interface NRAJoinLikeOptimizer {

   IQ optimize(IQ iq, ExecutorRegistry executorRegistry) throws EmptyQueryException;
}
