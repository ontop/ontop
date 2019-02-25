package it.unibz.inf.ontop.model.term.functionsymbol.db;

public interface DBIsNullOrNotFunctionSymbol extends DBBooleanFunctionSymbol {

    boolean isTrueWhenNull();
}
