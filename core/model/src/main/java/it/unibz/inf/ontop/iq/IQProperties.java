package it.unibz.inf.ontop.iq;

/**
 * TODO: explain
 *
 * For optimization purposes
 *
 * Immutable
 *
 * TODO: enrich it
 */
public interface IQProperties {

    boolean isLifted();

    /**
     * Creates a NEW (immutable) object
     */
    IQProperties declareLifted();
}
