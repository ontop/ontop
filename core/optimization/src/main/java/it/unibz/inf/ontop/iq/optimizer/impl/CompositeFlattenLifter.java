package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.optimizer.*;

public class CompositeFlattenLifter extends CompositeIQOptimizer implements FlattenLifter {

    @Inject
    private CompositeFlattenLifter(FilterLifter filterLifter,
                                   BasicFlattenLifter flattenLifter,
                                   BooleanExpressionPushDownOptimizer pushDownOptimizer) {

        super(ImmutableList.of(filterLifter, flattenLifter, pushDownOptimizer));
    }
}

