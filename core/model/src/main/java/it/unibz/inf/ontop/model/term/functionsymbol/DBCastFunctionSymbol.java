package it.unibz.inf.ontop.model.term.functionsymbol;

import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public interface DBCastFunctionSymbol extends FunctionSymbol {

    DBTermType getTargetType();

    Optional<DBTermType> getInputType();

    boolean isTemporary();
}
