package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GeneralStructuralAndSemanticIQOptimizerImpl implements GeneralStructuralAndSemanticIQOptimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralStructuralAndSemanticIQOptimizerImpl.class);
    private final UnionAndBindingLiftOptimizer bindingLiftOptimizer;
    private final JoinLikeOptimizer joinLikeOptimizer;
    private final IQConverter iqConverter;
    private final OrderBySimplifier orderBySimplifier;
    private final AggregationSimplifier aggregationSimplifier;
    private final IntermediateQueryFactory iqFactory;
    private final ProjectionShrinkingOptimizer projectionShrinker;

    @Inject
    private GeneralStructuralAndSemanticIQOptimizerImpl(UnionAndBindingLiftOptimizer bindingLiftOptimizer,
                                                        JoinLikeOptimizer joinLikeOptimizer,
                                                        IQConverter iqConverter, OrderBySimplifier orderBySimplifier,
                                                        AggregationSimplifier aggregationSimplifier, IntermediateQueryFactory iqFactory,
                                                        ProjectionShrinkingOptimizer projectionShrinker) {
        this.bindingLiftOptimizer = bindingLiftOptimizer;
        this.joinLikeOptimizer = joinLikeOptimizer;
        this.iqConverter = iqConverter;
        this.orderBySimplifier = orderBySimplifier;
        this.aggregationSimplifier = aggregationSimplifier;
        this.iqFactory = iqFactory;
        this.projectionShrinker = projectionShrinker;
    }

    @Override
    public IQ optimize(IQ query, ExecutorRegistry executorRegistry) {
        //lift bindings and union when it is possible
        IQ liftedQuery = bindingLiftOptimizer.optimize(query);
        LOGGER.debug("New lifted query: \n" + liftedQuery.toString());

        try {
            // Non-final
            IntermediateQuery intermediateQuery = iqConverter.convert(liftedQuery, executorRegistry);

            intermediateQuery = projectionShrinker.optimize(intermediateQuery);

            LOGGER.debug("After projection shrinking: \n" + intermediateQuery.toString());

            long beginningJoinLike = System.currentTimeMillis();
            intermediateQuery = joinLikeOptimizer.optimize(intermediateQuery);
            LOGGER.debug(String.format(
                    "New query after fixed point join optimization (%d ms): \n%s",
                    System.currentTimeMillis() - beginningJoinLike,
                    intermediateQuery.toString()));

            IQ queryAfterAggregationSimplification = aggregationSimplifier.optimize(iqConverter.convert(intermediateQuery));
            LOGGER.debug("New query after simplifying the aggregation node: \n" + queryAfterAggregationSimplification);
            IQ optimizedQuery = orderBySimplifier.optimize(queryAfterAggregationSimplification);
            LOGGER.debug("New query after simplifying the order by node: \n" + optimizedQuery);

            return optimizedQuery;
        } catch (EmptyQueryException e ) {
            DistinctVariableOnlyDataAtom projectionAtom = query.getProjectionAtom();
            return iqFactory.createIQ(projectionAtom,
                    iqFactory.createEmptyNode(projectionAtom.getVariables()));
        }
    }
}
