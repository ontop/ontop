package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.TermTypeTermLifter;
import it.unibz.inf.ontop.iq.transformer.TermTypeTermLiftTransformer;

@Singleton
public class TermTypeTermLifterImpl implements TermTypeTermLifter {

    private final OptimizerFactory transformerFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private TermTypeTermLifterImpl(OptimizerFactory transformerFactory, IntermediateQueryFactory iqFactory) {
        this.transformerFactory = transformerFactory;
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        TermTypeTermLiftTransformer transformer = transformerFactory.createRDFTermTypeConstantTransformer(
                query.getVariableGenerator());
        IQTree newTree = transformer.transform(query.getTree());

        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

}
