package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

@Singleton
public class OptimizationSingletonsImpl implements OptimizationSingletons {

    private final OptimizerFactory optimizerFactory;
    private final CoreSingletons coreSingletons;
    private final UnionBasedQueryMerger unionBasedQueryMerger;
    private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;
    private final OntopOptimizationSettings settings;
    private final GeneralStructuralAndSemanticIQOptimizer generalStructuralAndSemanticIQOptimizer;
    private final JoinLikeOptimizer joinLikeOptimizer;
    private final QueryPlanner queryPlanner;

    @Inject
    protected OptimizationSingletonsImpl(OptimizerFactory optimizerFactory, CoreSingletons coreSingletons,
                                         UnionBasedQueryMerger unionBasedQueryMerger,
                                         RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor,
                                         GeneralStructuralAndSemanticIQOptimizer generalStructuralAndSemanticIQOptimizer,
                                         JoinLikeOptimizer joinLikeOptimizer, QueryPlanner queryPlanner,
                                         OntopOptimizationSettings settings) {
        this.optimizerFactory = optimizerFactory;
        this.coreSingletons = coreSingletons;
        this.unionBasedQueryMerger = unionBasedQueryMerger;
        this.requiredExtensionalDataNodeExtractor = requiredExtensionalDataNodeExtractor;
        this.settings = settings;
        this.generalStructuralAndSemanticIQOptimizer = generalStructuralAndSemanticIQOptimizer;
        this.joinLikeOptimizer = joinLikeOptimizer;
        this.queryPlanner = queryPlanner;
    }

    @Override
    public CoreSingletons getCoreSingletons() {
        return coreSingletons;
    }

    @Override
    public OptimizerFactory getOptimizerFactory() {
        return optimizerFactory;
    }

    @Override
    public UnionBasedQueryMerger getUnionBasedQueryMerger() {
        return unionBasedQueryMerger;
    }

    @Override
    public OntopOptimizationSettings getSettings() {
        return settings;
    }

    @Override
    public RequiredExtensionalDataNodeExtractor getRequiredExtensionalDataNodeExtractor() {
        return requiredExtensionalDataNodeExtractor;
    }

    @Override
    public GeneralStructuralAndSemanticIQOptimizer getGeneralStructuralAndSemanticIQOptimizer() {
        return generalStructuralAndSemanticIQOptimizer;
    }

    @Override
    public JoinLikeOptimizer getJoinLikeOptimizer() {
        return joinLikeOptimizer;
    }

    @Override
    public QueryPlanner getQueryPlanner() {
        return queryPlanner;
    }
}
