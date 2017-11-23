package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.tools.IQConverter;

@Singleton
public class ImmutableBindingLiftOptimizer implements BindingLiftOptimizer {

    private final IQConverter iqConverter;

    @Inject
    private ImmutableBindingLiftOptimizer(IQConverter iqConverter) {
        this.iqConverter = iqConverter;
    }

    /**
     * TODO: lift unions
     */
    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        IQ initialIQ = iqConverter.convert(query);
        IQ liftedIQ = initialIQ.liftBinding();

        return iqConverter.convert(liftedIQ, query.getDBMetadata(), query.getExecutorRegistry());
    }
}
