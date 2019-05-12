package it.unibz.inf.ontop.iq;

/**
 * TODO: explain
 *
 * For optimization purposes
 *
 * Immutable
 *
 * TODO: enrich it
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface IQProperties {

    boolean isNormalizedForOptimization();

    boolean areDistinctAlreadyRemoved();

    /**
     * Creates a NEW (immutable) object
     */
    IQProperties declareNormalizedForOptimization();

    IQProperties declareDistinctRemovalWithoutEffect();

    IQProperties declareDistinctRemovalWithEffect();
}
