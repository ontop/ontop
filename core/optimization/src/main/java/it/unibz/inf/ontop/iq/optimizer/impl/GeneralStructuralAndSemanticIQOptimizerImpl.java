package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.lens.LensUnfolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
    private final PreventDistinctOptimizer preventDistinctOptimizer;
    private final DisjunctionOfEqualitiesMergingSimplifier disjunctionOfEqualitiesMergingSimplifier;
    private final AuthorizationFunctionEvaluator authorizationFunctionEvaluator;
    private final AllQueryContextFunctionSymbolEvaluator allQueryContextFunctionSymbolEvaluator;

    @Inject
    private GeneralStructuralAndSemanticIQOptimizerImpl(UnionAndBindingLiftOptimizer bindingLiftOptimizer,
                                                        JoinLikeOptimizer joinLikeOptimizer,
                                                        OrderBySimplifier orderBySimplifier,
                                                        AggregationSimplifier aggregationSimplifier,
                                                        LensUnfolder lensUnfolder,
                                                        AggregationSplitter aggregationSplitter,
                                                        FlattenLifter flattenLifter,
                                                        PreventDistinctOptimizer preventDistinctOptimizer,
                                                        DisjunctionOfEqualitiesMergingSimplifier disjunctionOfEqualitiesMergingSimplifier,
                                                        AuthorizationFunctionEvaluator authorizationFunctionEvaluator,
                                                        AllQueryContextFunctionSymbolEvaluator allQueryContextFunctionSymbolEvaluator) {
        this.bindingLiftOptimizer = bindingLiftOptimizer;
        this.joinLikeOptimizer = joinLikeOptimizer;
        this.orderBySimplifier = orderBySimplifier;
        this.aggregationSimplifier = aggregationSimplifier;
        this.lensUnfolder = lensUnfolder;
        this.aggregationSplitter = aggregationSplitter;
        this.flattenLifter = flattenLifter;
        this.preventDistinctOptimizer = preventDistinctOptimizer;
        this.disjunctionOfEqualitiesMergingSimplifier = disjunctionOfEqualitiesMergingSimplifier;
        this.authorizationFunctionEvaluator = authorizationFunctionEvaluator;
        this.allQueryContextFunctionSymbolEvaluator = allQueryContextFunctionSymbolEvaluator;
    }

    @Override
    public IQ optimize(IQ query, @Nullable QueryContext queryContext) {
        //lift bindings and union when it is possible
        IQ liftedQuery = bindingLiftOptimizer.optimize(query);

        LOGGER.debug("New lifted query:\n{}\n", liftedQuery);

        // Push expressions into a distinct below where possible if they have a data type that is not supported for distinct.
        IQ pushedIntoDistinct = preventDistinctOptimizer.optimize(liftedQuery);
        LOGGER.debug("Query tree after preventing DISTINCT for non-supported data types:\n{}\n", pushedIntoDistinct);

        IQ current = pushedIntoDistinct;
        do {
            long beginningAuthorizationEvaluation = System.currentTimeMillis();
            if (queryContext != null) {
                current = authorizationFunctionEvaluator.optimize(current, queryContext);
                LOGGER.debug("New query after evaluation authorization functions ({} ms):\n{}\n",
                        System.currentTimeMillis() - beginningAuthorizationEvaluation,
                        current);
            }

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

        IQ queryAfterContextualSimplification = queryContext == null
                ? current
                : allQueryContextFunctionSymbolEvaluator.optimize(current, queryContext);
        if (queryContext != null)
            LOGGER.debug("New query after simplifying using the context:\n{}\n", queryAfterContextualSimplification);

        IQ queryAfterAggregationSimplification = aggregationSimplifier.optimize(queryAfterContextualSimplification);
        LOGGER.debug("New query after simplifying the aggregation node:\n{}\n", queryAfterAggregationSimplification);

        IQ queryAfterAggregationSplitting = aggregationSplitter.optimize(queryAfterAggregationSimplification);
        LOGGER.debug("New query after trying to split the aggregation node:\n{}\n", queryAfterAggregationSplitting);

        IQ optimizedQuery = orderBySimplifier.optimize(queryAfterAggregationSplitting);
        LOGGER.debug("New query after simplifying the order by node:\n{}\n", optimizedQuery);

        IQ mergedDisjunctionsQuery = disjunctionOfEqualitiesMergingSimplifier.optimize(optimizedQuery);
        LOGGER.debug("New query after simplifying disjunctions of equalities:\n{}\n", mergedDisjunctionsQuery);

        // Called a second time in case the order of nodes was changed during previous optimization steps.
        IQ resultingQuery = preventDistinctOptimizer.optimize(mergedDisjunctionsQuery);
        LOGGER.debug("Query tree after preventing DISTINCT for non-supported data types, second pass:\n{}\n", resultingQuery);

        return resultingQuery;
    }
}
