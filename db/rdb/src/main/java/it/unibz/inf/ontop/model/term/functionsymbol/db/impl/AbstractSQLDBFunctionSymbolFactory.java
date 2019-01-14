package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableTable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;

public abstract class AbstractSQLDBFunctionSymbolFactory extends AbstractDBFunctionSymbolFactory {

    protected static final String UPPER_STR = "UPPER";
    protected static final String UCASE_STR = "UCASE";
    protected static final String LOWER_STR = "LOWER";
    protected static final String LCASE_STR = "LCASE";
    protected static final String CONCAT_STR = "CONCAT";
    protected static final String REPLACE_STR = "REPLACE";
    protected static final String REGEXP_REPLACE_STR = "REGEXP_REPLACE";
    protected static final String AND_STR = "AND";
    protected static final String SUBSTR_STR = "SUBSTR";
    protected static final String SUBSTRING_STR = "SUBSTRING";
    protected static final String CHAR_LENGTH_STR = "CHAR_LENGTH";
    protected static final String LENGTH_STR = "LENGTH";
    protected static final String RIGHT_STR = "RIGHT";

    private final DBTypeFactory dbTypeFactory;
    private final DBTermType dbStringType;
    private final DBTermType dbBooleanType;
    private final DBTermType abstractRootDBType;
    private final TermType abstractRootType;
    private final DBFunctionSymbol ifThenElse;
    private final DBBooleanFunctionSymbol isStringEmpty;

    protected AbstractSQLDBFunctionSymbolFactory(ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable,
                                                 ImmutableTable<String, Integer, DBFunctionSymbol> regularFunctionTable,
                                                 TypeFactory typeFactory) {
        super(normalizationTable, regularFunctionTable, typeFactory);
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
        this.dbStringType = dbTypeFactory.getDBStringType();
        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        this.ifThenElse = createDBIfThenElse(dbBooleanType, abstractRootDBType);
        this.isStringEmpty = createIsStringEmpty(dbBooleanType, abstractRootDBType);
        this.abstractRootType = typeFactory.getAbstractAtomicTermType();
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
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        ImmutableTable.Builder<String, Integer, DBFunctionSymbol> builder = ImmutableTable.builder();

        // TODO: provide the base input types
        DBFunctionSymbol upperFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(UPPER_STR, 1, dbStringType,
                false, abstractRootDBType);
        builder.put(UPPER_STR, 1, upperFunctionSymbol);
        builder.put(UCASE_STR, 1, upperFunctionSymbol);

        DBFunctionSymbol lowerFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(LOWER_STR, 1, dbStringType,
                false, abstractRootDBType);
        builder.put(LOWER_STR, 1, lowerFunctionSymbol);
        builder.put(LCASE_STR, 1, lowerFunctionSymbol);


        DBFunctionSymbol replace3FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(REPLACE_STR, 3, dbStringType,
                false, abstractRootDBType);
        builder.put(REPLACE_STR, 3, replace3FunctionSymbol);

        DBFunctionSymbol regexpReplace3FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(REGEXP_REPLACE_STR, 3, dbStringType,
                false, abstractRootDBType);
        builder.put(REGEXP_REPLACE_STR, 3, regexpReplace3FunctionSymbol);

        DBFunctionSymbol regexpReplace4FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(REGEXP_REPLACE_STR, 4, dbStringType,
                false, abstractRootDBType);
        builder.put(REGEXP_REPLACE_STR, 4, regexpReplace4FunctionSymbol);

        DBFunctionSymbol subString2FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(SUBSTRING_STR, 2, dbStringType,
                false, abstractRootDBType);
        builder.put(SUBSTRING_STR, 2, subString2FunctionSymbol);
        builder.put(SUBSTR_STR, 2, subString2FunctionSymbol);

        DBFunctionSymbol subString3FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(SUBSTRING_STR, 3, dbStringType,
                false, abstractRootDBType);
        builder.put(SUBSTRING_STR, 3, subString3FunctionSymbol);
        builder.put(SUBSTR_STR, 3, subString3FunctionSymbol);

        DBFunctionSymbol rightFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(RIGHT_STR, 2, dbStringType,
                false, abstractRootDBType);
        builder.put(RIGHT_STR, 2, rightFunctionSymbol);

        // TODO: check precise output type
        DBFunctionSymbol strlenFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(CHAR_LENGTH_STR, 1, dbIntType,
                false, abstractRootDBType);
        builder.put(CHAR_LENGTH_STR, 1, strlenFunctionSymbol);
        //TODO: move away this synonym as it is non-standard
        builder.put(LENGTH_STR, 1, strlenFunctionSymbol);


        return builder.build();
    }

    @Override
    protected DBFunctionSymbol createRegularUntypedFunctionSymbol(String nameInDialect, int arity) {
        // TODO: avoid if-then-else
        if (isAnd(nameInDialect))
            return createDBAnd(arity);
        else if (isConcat(nameInDialect))
            return createDBConcat(arity);
        return new DefaultSQLUntypedDBFunctionSymbol(nameInDialect, arity, dbTypeFactory.getAbstractRootDBType());
    }

    @Override
    protected DBBooleanFunctionSymbol createRegularBooleanFunctionSymbol(String nameInDialect, int arity) {
        return new DefaultSQLSimpleDBBooleanFunctionSymbol(nameInDialect, arity, dbBooleanType, abstractRootDBType);
    }

    protected boolean isConcat(String nameInDialect) {
        return nameInDialect.equals(CONCAT_STR);
    }

    protected boolean isAnd(String nameInDialect) {
        return nameInDialect.equals(AND_STR);
    }

    protected DBConcatFunctionSymbol createDBConcat(int arity) {
        return new DefaultDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType);
    }

    protected DBBooleanFunctionSymbol createDBAnd(int arity) {
        return new DefaultDBAndFunctionSymbol(AND_STR, arity, dbBooleanType);
    }

    protected DBFunctionSymbol createDBIfThenElse(DBTermType dbBooleanType, DBTermType abstractRootDBType) {
        return new DefaultSQLIfThenElseFunctionSymbol(dbBooleanType, abstractRootDBType);
    }

    protected DBBooleanFunctionSymbol createIsStringEmpty(DBTermType dbBooleanType, DBTermType abstractRootDBType) {
        return new DefaultSQLIsStringEmptyFunctionSymbol(dbBooleanType, abstractRootDBType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        return new DefaultSQLSimpleDBCastFunctionSymbol(dbTypeFactory.getAbstractRootDBType(), targetType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return targetType.equals(dbBooleanType)
                ? new DefaultSQLSimpleDBBooleanCastFunctionSymbol(inputType, targetType)
                : new DefaultSQLSimpleDBCastFunctionSymbol(inputType, targetType);
    }

    @Override
    protected DBFunctionSymbol createDBCase(int arity) {
        return new DefaultSQLCaseFunctionSymbol(arity, dbBooleanType, abstractRootDBType);
    }

    @Override
    protected DBStrictEqFunctionSymbol createDBStrictEquality(int arity) {
        return new DefaultDBStrictEqFunctionSymbol(arity, abstractRootType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDBStrictNEquality(int arity) {
        return new DefaultDBStrictNEqFunctionSymbol(arity, abstractRootType, dbBooleanType);
    }

    @Override
    protected DBFunctionSymbol createR2RMLIRISafeEncode() {
        return new DefaultSQLR2RMLSafeIRIEncodeFunctionSymbol(dbStringType);
    }

    @Override
    public DBFunctionSymbol getDBIfThenElse() {
        return ifThenElse;
    }

    @Override
    public DBFunctionSymbol getDBUpper() {
        return getRegularDBFunctionSymbol(UPPER_STR, 1);
    }

    @Override
    public DBFunctionSymbol getDBLower() {
        return getRegularDBFunctionSymbol(LOWER_STR, 1);
    }

    @Override
    public DBFunctionSymbol getDBReplace3() {
        return getRegularDBFunctionSymbol(REPLACE_STR, 3);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace4() {
        return getRegularDBFunctionSymbol(REGEXP_REPLACE_STR, 4);
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        return getRegularDBFunctionSymbol(SUBSTRING_STR, 2);
    }

    @Override
    public DBFunctionSymbol getDBSubString3() {
        return getRegularDBFunctionSymbol(SUBSTRING_STR, 3);
    }

    @Override
    public DBFunctionSymbol getDBRight() {
        return getRegularDBFunctionSymbol(RIGHT_STR, 2);
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(CHAR_LENGTH_STR, 1);
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
    public DBBooleanFunctionSymbol getDBIsStringEmpty() {
        return isStringEmpty;
    }

}
