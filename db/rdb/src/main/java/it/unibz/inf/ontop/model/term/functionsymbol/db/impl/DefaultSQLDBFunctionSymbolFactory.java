package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;

public class DefaultSQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    @Inject
    private DefaultSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDefaultNormalizationTable(typeFactory), createDefaultRegularFunctionTable(typeFactory), typeFactory);
    }

    @Override
    public DBBooleanFunctionSymbol getDBContains() {
        throw new UnsupportedOperationException(
                "Not supported in the Default SQL factory since no-one uses " +
                        "the old official standard function.\n" +
                        "Please specific it in your dialect factory");
    }

}
