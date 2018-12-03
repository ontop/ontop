package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class DefaultSQLDBFunctionSymbolFactory extends AbstractDBFunctionSymbolFactory {

    protected static final String UPPER_STR = "UPPER";
    protected static final String UCASE_STR = "UCASE";
    protected static final String CONCAT_STR = "CONCAT";

    private final DBTypeFactory dbTypeFactory;

    @Inject
    private DefaultSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        this(createDefaultNormalizationTable(typeFactory), createDefaultRegularFunctionTable(typeFactory), typeFactory);
    }

    protected DefaultSQLDBFunctionSymbolFactory(ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable,
                                                ImmutableTable<String, Integer, DBFunctionSymbol> regularFunctionTable,
                                                TypeFactory typeFactory) {
        super(normalizationTable, regularFunctionTable, typeFactory);
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
    }

    @Override
    protected DBFunctionSymbol createRegularFunctionSymbol(String nameInDialect, int arity) {
        return new DefaultSQLUntypedDBFunctionSymbol(nameInDialect, arity, dbTypeFactory.getAbstractRootDBType());
    }

    protected static ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDefaultNormalizationTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        DBTermType stringType = dbTypeFactory.getDBStringType();
        DBTermType timestampType = dbTypeFactory.getDBDateTimestampType();
        DBTermType booleanType = dbTypeFactory.getDBBooleanType();

        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();

        // Date time
        builder.put(timestampType, typeFactory.getXsdDatetimeDatatype(),
                new DefaultSQLTimestampISONormFunctionSymbol(timestampType, stringType));
        // Boolean
        builder.put(booleanType, typeFactory.getXsdBooleanDatatype(),
                new DefaultSQLBooleanNormFunctionSymbol(booleanType, stringType));

        return builder.build();
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDefaultRegularFunctionTable(TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbStringType = dbTypeFactory.getDBStringType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        ImmutableTable.Builder<String, Integer, DBFunctionSymbol> builder = ImmutableTable.builder();

        // TODO: provide the base input types
        DBFunctionSymbol upperFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(UPPER_STR, 1, dbStringType,
                false, abstractRootDBType);
        builder.put(UPPER_STR, 1, upperFunctionSymbol);
        builder.put(UCASE_STR, 1, upperFunctionSymbol);

        DBFunctionSymbol concatFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(CONCAT_STR, 2, dbStringType,
                false, abstractRootDBType);
        builder.put(CONCAT_STR, 2, concatFunctionSymbol);
        return builder.build();
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        return new DefaultSQLSimpleDBCastFunctionSymbol(dbTypeFactory.getAbstractRootDBType(), targetType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return new DefaultSQLSimpleDBCastFunctionSymbol(inputType, targetType);
    }

    @Override
    public DBFunctionSymbol getDBUCase() {
        return getRegularDBFunctionSymbol(UCASE_STR, 1);
    }

    @Override
    public DBFunctionSymbol getDBConcat() {
        return getRegularDBFunctionSymbol(CONCAT_STR, 2);
    }
}
