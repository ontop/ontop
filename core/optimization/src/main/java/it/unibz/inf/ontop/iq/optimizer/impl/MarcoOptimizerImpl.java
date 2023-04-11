package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.MarcoOptimizer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

import javax.inject.Inject;

public class MarcoOptimizerImpl implements MarcoOptimizer {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MarcoOptimizerImpl.class);
    private final IntermediateQueryFactory iqFactory;
    private final CoreSingletons coreSingletons;

    @Inject
    protected MarcoOptimizerImpl(CoreSingletons coreSingletons, IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(IQ query) {
        LOGGER.debug("MarcoOptimizerImpl.optimize() called");
        return query;
    }
}
