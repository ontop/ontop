package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

/**
 * Mockup: for DB-independent tests only
 */
public class MockupDBFunctionSymbolFactory extends AbstractDBFunctionSymbolFactory {

    private static final String CONCAT_STR = "CONCAT";
    private static final String AND_STR = "AND";
    private final DBTermType dbBooleanType;
    private final DBTermType abstractRootDBType;
    private final DBTermType dbStringType;

    @Inject
    private MockupDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDefaultNormalizationTable(typeFactory), createDefaultRegularFunctionTable(typeFactory), typeFactory);
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        dbBooleanType = dbTypeFactory.getDBBooleanType();
        abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        dbStringType = dbTypeFactory.getDBStringType();
    }

    protected static ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDefaultNormalizationTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        return builder.build();
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDefaultRegularFunctionTable(TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbStringType = dbTypeFactory.getDBStringType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        ImmutableTable.Builder<String, Integer, DBFunctionSymbol> builder = ImmutableTable.builder();
        return builder.build();
    }

    @Override
    protected DBFunctionSymbol createRegularFunctionSymbol(String nameInDialect, int arity) {
        switch (nameInDialect) {
            case AND_STR:
                return createDBAnd(arity);
            case CONCAT_STR:
                return createDBConcat(arity);
            default:
                return new AbstractUntypedDBFunctionSymbol(nameInDialect,
                        IntStream.range(0, arity)
                                .boxed()
                                .map(i -> abstractRootDBType)
                                .collect(ImmutableCollectors.toList()));
        }
    }

    private DBFunctionSymbol createDBAnd(int arity) {
        return new DefaultDBAndFunctionSymbol(AND_STR, arity, dbBooleanType);
    }

    private DBFunctionSymbol createDBConcat(int arity) {
        return new DefaultDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createDBCase(int arity) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBBooleanFunctionSymbol createDBStrictEquality(int arity) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createR2RMLIRISafeEncode() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBIfElseNull() {
        return new MockupDBIfElseNullFunctionSymbol(dbBooleanType, abstractRootDBType);
    }

    @Override
    public DBFunctionSymbol getDBUpper() {
        return getRegularDBFunctionSymbol("UPPER", 1);
    }

    @Override
    public DBFunctionSymbol getDBLower() {
        return getRegularDBFunctionSymbol("LOWER", 1);
    }

    @Override
    public DBFunctionSymbol getDBReplace() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBSubString() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBRight() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBConcatFunctionSymbol getDBConcat(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of CONCAT must be >= 2");
        return (DBConcatFunctionSymbol) getRegularDBFunctionSymbol(CONCAT_STR, arity);
    }

    @Override
    public DBBooleanFunctionSymbol getDBAnd(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of AND must be >= 2");
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(AND_STR, arity);
    }

    @Override
    public DBBooleanFunctionSymbol getDBContains() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

}
