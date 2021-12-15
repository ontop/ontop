package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.UnionAndBindingLiftOptimizer;
import it.unibz.inf.ontop.iq.tools.IQConverter;

@Singleton
public class LegacyBindingLiftOptimizer implements BindingLiftOptimizer {

    private final IQConverter iqConverter;
    private final UnionAndBindingLiftOptimizer optimizer;

    @Inject
    private LegacyBindingLiftOptimizer(IQConverter iqConverter, UnionAndBindingLiftOptimizer optimizer) {
        this.iqConverter = iqConverter;
        this.optimizer = optimizer;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        IQ initialIQ = iqConverter.convert(query);

        IQ liftedIQ = optimizer.optimize(initialIQ);

        return iqConverter.convert(liftedIQ);
    }
}
