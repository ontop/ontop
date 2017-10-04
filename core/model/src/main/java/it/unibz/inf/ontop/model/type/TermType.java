package it.unibz.inf.ontop.model.type;

/**
 * TODO: explain
 *
 * Immutable
 */
public interface TermType {

    /**
     * Closest COL_TYPE (must be an ancestor)
     */
    @Deprecated
    COL_TYPE getColType();

    boolean isCompatibleWith(TermType otherTermType);

    TermType getCommonDenominator(TermType otherTermType);
}
