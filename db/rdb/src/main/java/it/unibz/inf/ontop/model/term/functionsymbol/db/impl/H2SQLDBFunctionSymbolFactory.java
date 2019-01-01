package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class H2SQLDBFunctionSymbolFactory extends DefaultSQLDBFunctionSymbolFactory {


    private final DBBooleanFunctionSymbol containsFunctionSymbol;

    @Inject
    private H2SQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDefaultNormalizationTable(typeFactory),
                createH2RegularFunctionTable(typeFactory), typeFactory);

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        containsFunctionSymbol = new H2ContainsFunctionSymbolImpl(dbTypeFactory.getAbstractRootDBType(),
                dbTypeFactory.getDBBooleanType());
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createH2RegularFunctionTable(
            TypeFactory typeFactory) {
        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));
        // ...
        return ImmutableTable.copyOf(table);
    }

    @Override
    public DBBooleanFunctionSymbol getDBContains() {
        return containsFunctionSymbol;
    }
}
