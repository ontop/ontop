package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsTrueFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

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

    @Override
    protected String serializeDecade(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(FLOOR(EXTRACT(YEAR FROM %s) / 10.00000) AS INTEGER)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCentury(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(CEIL(EXTRACT(YEAR FROM %s) / 100.00000) AS INTEGER)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMillennium(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(CEIL(EXTRACT(YEAR FROM %s) / 1000.00000) AS INTEGER)", termConverter.apply(terms.get(0)));
    }
}
