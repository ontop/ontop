package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.transformer.DefinitionPushDownTransformer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

@Singleton
public class OptimizationSingletonsImpl implements OptimizationSingletons {

    private final CoreSingletons coreSingletons;
    private final OntopOptimizationSettings settings;
    private final GeneralStructuralAndSemanticIQOptimizer generalStructuralAndSemanticIQOptimizer;
    private final QueryPlanner queryPlanner;

    @Inject
    protected OptimizationSingletonsImpl(CoreSingletons coreSingletons,
                                         GeneralStructuralAndSemanticIQOptimizer generalStructuralAndSemanticIQOptimizer,
                                         QueryPlanner queryPlanner, OntopOptimizationSettings settings) {
        this.coreSingletons = coreSingletons;
        this.settings = settings;
        this.generalStructuralAndSemanticIQOptimizer = generalStructuralAndSemanticIQOptimizer;
        this.queryPlanner = queryPlanner;
    }

    @Override
    public CoreSingletons getCoreSingletons() {
        return coreSingletons;
    }

    @Override
    public OntopOptimizationSettings getSettings() {
        return settings;
    }

    @Override
    public GeneralStructuralAndSemanticIQOptimizer getGeneralStructuralAndSemanticIQOptimizer() {
        return generalStructuralAndSemanticIQOptimizer;
    }

    @Override
    public QueryPlanner getQueryPlanner() {
        return queryPlanner;
    }
}
