package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.optimizer.*;

public class CompositeFlattenLifter extends CompositeIQOptimizer implements FlattenLifter {

    @Inject
    private CompositeFlattenLifter(FilterLifter filterLifter,
                                   BasicFlattenLifterImpl flattenLifter,
                                   BooleanExpressionPushDownOptimizer pushDownOptimizer) {
        super(filterLifter, flattenLifter, pushDownOptimizer);
    }
}

