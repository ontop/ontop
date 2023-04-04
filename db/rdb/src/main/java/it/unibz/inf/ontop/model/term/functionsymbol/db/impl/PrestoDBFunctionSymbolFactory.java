package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class PrestoDBFunctionSymbolFactory extends TrinoDBFunctionSymbolFactory {

    @Inject
    protected PrestoDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(typeFactory);
    }

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "ARRAY_JOIN(%sARRAY_AGG(%s)%s, %s)",
                        isDistinct ? "ARRAY_DISTINCT( " : "",
                        termConverter.apply(terms.get(0)),
                        isDistinct ? ") " : "",
                        termConverter.apply(terms.get(1))
                ));
    }

}
