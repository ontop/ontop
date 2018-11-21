package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class DefaultSQLDBFunctionSymbolFactory extends AbstractDBFunctionSymbolFactory {

    private final DBTypeFactory dbTypeFactory;

    @Inject
    private DefaultSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        this(createDefaultNormalizationTable(typeFactory), typeFactory);
    }

    protected DefaultSQLDBFunctionSymbolFactory(ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable,
                                                TypeFactory typeFactory) {
        super(normalizationTable, typeFactory);
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        return new DefaultSQLSimpleDBCastFunctionSymbol(dbTypeFactory.getAbstractRootDBType(), targetType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return new DefaultSQLSimpleDBCastFunctionSymbol(inputType, targetType);
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
}
