package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transformer.DefinitionPushDownTransformer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

/**
 * Helper for the ontop-optimization module
 *
 * See CoreSingletons for the motivation
 *
 */
public interface OptimizationSingletons {

    CoreSingletons getCoreSingletons();

    OntopOptimizationSettings getSettings();

    // used by downstream applications outside Ontop
    GeneralStructuralAndSemanticIQOptimizer getGeneralStructuralAndSemanticIQOptimizer();

    // used by downstream applications outside Ontop
    QueryPlanner getQueryPlanner();
}
