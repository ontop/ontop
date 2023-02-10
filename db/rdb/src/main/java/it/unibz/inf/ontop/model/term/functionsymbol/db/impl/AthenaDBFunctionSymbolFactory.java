package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsTrueFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class AthenaDBFunctionSymbolFactory extends PrestoDBFunctionSymbolFactory {

    @Inject
    protected AthenaDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(typeFactory);
    }

    /**
     * Athena uses 'True/False' for SQL queries, but '1/0' for results, so we need a way to parse these results.
     */
    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new OneDigitDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    /**
     * Athena uses 'True/False' for SQL queries, but '1/0' for results, so we need a way to parse these results.
     */
    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return new OneDigitBooleanNormFunctionSymbolImpl(booleanType, dbStringType);
    }
}
