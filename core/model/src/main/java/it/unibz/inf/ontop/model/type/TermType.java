package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

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
    Predicate.COL_TYPE getColType();

    boolean isCompatibleWith(TermType otherTermType);

    TermType getCommonDenominator(TermType otherTermType);
}
