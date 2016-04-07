package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.Predicate;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Immutable
 */
public interface TermType {

    Predicate.COL_TYPE getColType();

    Optional<LanguageTag> getLanguageTag();

    boolean isCompatibleWith(Predicate.COL_TYPE colType);

    Optional<TermType> getCommonDenominator(TermType otherTermType);

    boolean isNumeric();
}
