package it.unibz.inf.ontop.model.term.functionsymbol;

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
}
