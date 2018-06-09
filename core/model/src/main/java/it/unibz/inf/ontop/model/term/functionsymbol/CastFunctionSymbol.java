package it.unibz.inf.ontop.model.term.functionsymbol;

import it.unibz.inf.ontop.model.type.TermType;

public interface CastFunctionSymbol extends FunctionSymbol {

    TermType getTargetType();
}
