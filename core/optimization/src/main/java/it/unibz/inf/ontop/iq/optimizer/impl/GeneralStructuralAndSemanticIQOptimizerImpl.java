package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.lens.LensUnfolder;
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
    private final LensUnfolder lensUnfolder;
    private final AggregationSplitter aggregationSplitter;
    private final FlattenLifter flattenLifter;

    @Inject
    private GeneralStructuralAndSemanticIQOptimizerImpl(UnionAndBindingLiftOptimizer bindingLiftOptimizer,
                                                        JoinLikeOptimizer joinLikeOptimizer,
                                                        OrderBySimplifier orderBySimplifier,
                                                        AggregationSimplifier aggregationSimplifier,
                                                        LensUnfolder lensUnfolder,
                                                        AggregationSplitter aggregationSplitter,
                                                        FlattenLifter flattenLifter) {
        this.bindingLiftOptimizer = bindingLiftOptimizer;
        this.joinLikeOptimizer = joinLikeOptimizer;
        this.orderBySimplifier = orderBySimplifier;
        this.aggregationSimplifier = aggregationSimplifier;
        this.lensUnfolder = lensUnfolder;
        this.aggregationSplitter = aggregationSplitter;
        this.flattenLifter = flattenLifter;
    }

    @Override
    public IQ optimize(IQ query) {
        //lift bindings and union when it is possible
        IQ liftedQuery = bindingLiftOptimizer.optimize(query);

        LOGGER.debug("New lifted query:\n{}\n", liftedQuery);

        IQ current = liftedQuery;
        do {

            long beginningJoinLike = System.currentTimeMillis();
            current = joinLikeOptimizer.optimize(current);

            LOGGER.debug("New query after fixed point join optimization ({} ms):\n{}\n",
                        System.currentTimeMillis() - beginningJoinLike,
                        current);

            IQ queryBeforeUnfolding = current;
            // Unfolds Ontop views one level at a time (hence the loop)
            current = lensUnfolder.optimize(queryBeforeUnfolding);
            LOGGER.debug("New query after view unfolding:\n{}\n",
                    current
            );

            if (queryBeforeUnfolding.equals(current))
                break;

            current = flattenLifter.optimize(current);
            LOGGER.debug("New query after flatten lift:\n{}\n", current);

        } while (true);

        IQ queryAfterAggregationSimplification = aggregationSimplifier.optimize(current);
        LOGGER.debug("New query after simplifying the aggregation node:\n{}\n", queryAfterAggregationSimplification);

        IQ queryAfterAggregationSplitting = aggregationSplitter.optimize(queryAfterAggregationSimplification);
        LOGGER.debug("New query after trying to split the aggregation node:\n{}\n", queryAfterAggregationSplitting);

        IQ optimizedQuery = orderBySimplifier.optimize(queryAfterAggregationSplitting);
        LOGGER.debug("New query after simplifying the order by node:\n{}\n", optimizedQuery);

        return optimizedQuery;
    }
}
