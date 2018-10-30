package it.unibz.inf.ontop.model.term.functionsymbol;

import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

public interface CastFunctionSymbol extends FunctionSymbol {

    TermType getTargetType();

    Optional<TermType> getInputType();

    boolean isTemporary();
}
