package it.unibz.inf.ontop.model;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Immutable
 */
public interface TermType {

    Predicate.COL_TYPE getColType();

    Optional<LanguageTag> getLanguageTagConstant();

    /**
     * Non-constant term
     */
    Optional<Term> getLanguageTagTerm();

    boolean isCompatibleWith(Predicate.COL_TYPE moreGeneralType);

    Optional<TermType> getCommonDenominator(TermType otherTermType);
}
