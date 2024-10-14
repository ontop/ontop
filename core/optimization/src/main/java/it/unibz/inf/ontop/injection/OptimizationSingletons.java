package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

/**
 * Helper for the ontop-optimization module
 *
 * See CoreSingletons for the motivation
 *
 */
public interface OptimizationSingletons {

    CoreSingletons getCoreSingletons();

    /**
     * TODO: shall we keep it?
     */
    OptimizerFactory getOptimizerFactory();

    UnionBasedQueryMerger getUnionBasedQueryMerger();

    RequiredExtensionalDataNodeExtractor getRequiredExtensionalDataNodeExtractor();

    GeneralStructuralAndSemanticIQOptimizer getGeneralStructuralAndSemanticIQOptimizer();

    JoinLikeOptimizer getJoinLikeOptimizer();

    QueryPlanner getQueryPlanner();

    OntopOptimizationSettings getSettings();

    // TODO: complete
}
