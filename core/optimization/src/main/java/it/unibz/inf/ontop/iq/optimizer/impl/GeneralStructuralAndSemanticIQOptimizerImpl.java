package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.view.OntopViewUnfolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GeneralStructuralAndSemanticIQOptimizerImpl implements GeneralStructuralAndSemanticIQOptimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralStructuralAndSemanticIQOptimizerImpl.class);
    private final UnionAndBindingLiftOptimizer bindingLiftOptimizer;
    private final JoinLikeOptimizer joinLikeOptimizer;
    private final OrderBySimplifier orderBySimplifier;
    private final AggregationSimplifier aggregationSimplifier;
    private final OntopViewUnfolder viewUnfolder;

    @Inject
    private GeneralStructuralAndSemanticIQOptimizerImpl(UnionAndBindingLiftOptimizer bindingLiftOptimizer,
                                                        JoinLikeOptimizer joinLikeOptimizer,
                                                        OrderBySimplifier orderBySimplifier,
                                                        AggregationSimplifier aggregationSimplifier,
                                                        OntopViewUnfolder viewUnfolder) {
        this.bindingLiftOptimizer = bindingLiftOptimizer;
        this.joinLikeOptimizer = joinLikeOptimizer;
        this.orderBySimplifier = orderBySimplifier;
        this.aggregationSimplifier = aggregationSimplifier;
        this.viewUnfolder = viewUnfolder;
    }

    @Override
    public IQ optimize(IQ query) {
        //lift bindings and union when it is possible
        IQ liftedQuery = bindingLiftOptimizer.optimize(query);

        boolean isLogDebugEnabled = LOGGER.isDebugEnabled();
        if (isLogDebugEnabled)
            LOGGER.debug("New lifted query: \n" + liftedQuery.toString());

        IQ queryAfterJoinLikeAndViewUnfolding = liftedQuery;
        do {
            long beginningJoinLike = System.currentTimeMillis();
            queryAfterJoinLikeAndViewUnfolding = joinLikeOptimizer.optimize(queryAfterJoinLikeAndViewUnfolding);

            if (isLogDebugEnabled)
                LOGGER.debug(String.format("New query after fixed point join optimization (%d ms): \n%s",
                        System.currentTimeMillis() - beginningJoinLike,
                        queryAfterJoinLikeAndViewUnfolding.toString()));

            IQ queryBeforeUnfolding = queryAfterJoinLikeAndViewUnfolding;
            // Unfolds Ontop views one level at a time (hence the loop)
            queryAfterJoinLikeAndViewUnfolding = viewUnfolder.optimize(queryBeforeUnfolding);

            if (queryBeforeUnfolding.equals(queryAfterJoinLikeAndViewUnfolding))
                break;

        } while (true);

        IQ queryAfterAggregationSimplification = aggregationSimplifier.optimize(queryAfterJoinLikeAndViewUnfolding);
        if (isLogDebugEnabled)
            LOGGER.debug("New query after simplifying the aggregation node: \n" + queryAfterAggregationSimplification);
        IQ optimizedQuery = orderBySimplifier.optimize(queryAfterAggregationSimplification);
        if (isLogDebugEnabled)
            LOGGER.debug("New query after simplifying the order by node: \n" + optimizedQuery);

        return optimizedQuery;
    }
}
