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

    Optional<LanguageTag> getOptionalLanguageTag();

    /**
     * TODO: find a better name
     */
    boolean isInstanceOf(Predicate.COL_TYPE expectedBaseType);

    Optional<TermType> getCommonDenominator(TermType otherTermType);

    boolean isNumeric();
}
