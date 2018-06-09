package it.unibz.inf.ontop.model.term.functionsymbol;

import it.unibz.inf.ontop.model.type.TermType;

/**
 * Cast function symbol from one (input) type to another (target) type.
 */
public interface FullyDefinedCastFunctionSymbol extends CastFunctionSymbol {

    TermType getInputType();
}
