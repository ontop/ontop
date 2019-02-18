package it.unibz.inf.ontop.iq.optimizer;

/**
 * Lifts certain functional terms that are blocked by a UNION:
 *   - non DBFunctionSymbols (e.g. IRI dictionary function symbols)
 *   - DBFunctionSymbols that declare to prefer to be post-processed (e.g. IRI string template function symbols)
 *
 * TODO: find a better name
 */
public interface PostProcessableFunctionLifter extends IQOptimizer {
}
