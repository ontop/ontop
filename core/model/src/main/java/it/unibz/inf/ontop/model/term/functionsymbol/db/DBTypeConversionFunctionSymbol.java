package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

/**
 * Slightly more general abstraction than a regular DB cast,
 * as it MAY perform some normalization to a specific format.
 */
public interface DBTypeConversionFunctionSymbol extends DBFunctionSymbol {

    DBTermType getTargetType();

    Optional<DBTermType> getInputType();

    boolean isTemporary();

    /**
     * Returns true if does not transform the string representation of the value
     * (i.e. no normalization).
     *
     * Useful for simplifying nested casts ( A-to-B(B-to-A(x)) === x if both casts are simple)
     */
    boolean isSimple();
}
