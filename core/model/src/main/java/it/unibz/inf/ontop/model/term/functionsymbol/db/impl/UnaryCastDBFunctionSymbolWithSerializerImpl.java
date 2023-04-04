package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * DBFunctionSymbol for XSD Cast Functions. May return NULL without NULL arguments.
 */
public class UnaryCastDBFunctionSymbolWithSerializerImpl extends UnaryDBFunctionSymbolWithSerializerImpl {

    protected UnaryCastDBFunctionSymbolWithSerializerImpl(String name, DBTermType inputDBType, DBTermType targetType,
                                                          boolean isAlwaysInjective,
                                                          DBFunctionSymbolSerializer serializer) {
        super(name, inputDBType, targetType, isAlwaysInjective, serializer);
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }
}
