package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.util.Optional;

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

    Optional<ImmutableTerm> getLanguageTagTerm();

    /**
     * Returns false for a rdf:langString without a language tag.
     *
     * Note that such a partially defined term type is perfectly legitimate when defined as the range of a property.
     *
     * However, term types used inside a query must always be fully defined.
     */
    boolean isFullyDefined();

    boolean isCompatibleWith(TermType otherTermType);

    Optional<TermType> getCommonDenominator(TermType otherTermType);
}
