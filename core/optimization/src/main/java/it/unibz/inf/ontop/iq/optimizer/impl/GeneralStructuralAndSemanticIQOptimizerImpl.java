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
    public IQ optimize(IQ query, ExecutorRegistry executorRegistry) {
        //lift bindings and union when it is possible
        IQ liftedQuery = bindingLiftOptimizer.optimize(query);

        LOGGER.debug("New lifted query: \n{}", liftedQuery);

        IQ queryAfterJoinLikeAndViewUnfolding = liftedQuery;
        do {
            long beginningJoinLike = System.currentTimeMillis();
            queryAfterJoinLikeAndViewUnfolding = joinLikeOptimizer.optimize(queryAfterJoinLikeAndViewUnfolding, executorRegistry);

            LOGGER.debug("New query after fixed point join optimization ({} ms):\n{}\n",
                        System.currentTimeMillis() - beginningJoinLike,
                        queryAfterJoinLikeAndViewUnfolding);

            IQ queryBeforeUnfolding = queryAfterJoinLikeAndViewUnfolding;
            // Unfolds Ontop views one level at a time (hence the loop)
            queryAfterJoinLikeAndViewUnfolding = viewUnfolder.optimize(queryBeforeUnfolding);

            if (queryBeforeUnfolding.equals(queryAfterJoinLikeAndViewUnfolding))
                break;

        } while (true);

        IQ queryAfterAggregationSimplification = aggregationSimplifier.optimize(queryAfterJoinLikeAndViewUnfolding);
        LOGGER.debug("New query after simplifying the aggregation node:\n{}\n", queryAfterAggregationSimplification);

        IQ optimizedQuery = orderBySimplifier.optimize(queryAfterAggregationSimplification);
        LOGGER.debug("New query after simplifying the order by node:\n{}\n", optimizedQuery);

        return optimizedQuery;
    }
}
