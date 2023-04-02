package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FederationOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

public class FederationOptimizerImpl implements FederationOptimizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected FederationOptimizerImpl(CoreSingletons coreSingletons, IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        this.coreSingletons = coreSingletons;
    }


    @Override
    public IQ optimize(IQ query) {
        return query;


    }

}
