package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.visitor.RequiredDataAtomExtractor;

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

    RequiredDataAtomExtractor getRequiredDataAtomExtractor();

    OntopOptimizationSettings getSettings();

    // TODO: complete
}
