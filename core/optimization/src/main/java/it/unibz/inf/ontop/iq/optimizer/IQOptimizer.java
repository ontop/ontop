package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.IQ;

@FunctionalInterface
public interface IQOptimizer {

    IQ optimize(IQ query);
}
