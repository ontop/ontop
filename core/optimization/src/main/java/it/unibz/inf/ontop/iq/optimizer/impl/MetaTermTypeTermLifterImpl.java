package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.MetaTermTypeTermLifter;
import it.unibz.inf.ontop.iq.transformer.MetaTermTypeTermLiftTransformer;

@Singleton
public class MetaTermTypeTermLifterImpl implements MetaTermTypeTermLifter {

    private final OptimizerFactory transformerFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private MetaTermTypeTermLifterImpl(OptimizerFactory transformerFactory, IntermediateQueryFactory iqFactory) {
        this.transformerFactory = transformerFactory;
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        MetaTermTypeTermLiftTransformer transformer = transformerFactory.createRDFTermTypeConstantTransformer(
                query.getVariableGenerator());
        IQTree newTree = transformer.transform(query.getTree());

        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

}
