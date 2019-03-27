package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;

import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractSQLDBFunctionSymbolFactory extends AbstractDBFunctionSymbolFactory {

    protected static final String UPPER_STR = "UPPER";
    protected static final String UCASE_STR = "UCASE";
    protected static final String LOWER_STR = "LOWER";
    protected static final String LCASE_STR = "LCASE";
    protected static final String CONCAT_STR = "CONCAT";
    protected static final String REPLACE_STR = "REPLACE";
    protected static final String REGEXP_REPLACE_STR = "REGEXP_REPLACE";
    protected static final String AND_STR = "AND";
    protected static final String OR_STR = "OR";
    protected static final String NOT_STR = "NOT";
    protected static final String SUBSTR_STR = "SUBSTR";
    protected static final String SUBSTRING_STR = "SUBSTRING";
    protected static final String CHAR_LENGTH_STR = "CHAR_LENGTH";
    protected static final String LENGTH_STR = "LENGTH";
    protected static final String RIGHT_STR = "RIGHT";
    protected static final String MULTIPLY_STR = "*";
    protected static final String DIVIDE_STR = "/";
    protected static final String ADD_STR = "+";
    protected static final String SUBSTRACT_STR = "-";
    protected static final String ABS_STR = "ABS";
    protected static final String CEIL_STR = "CEIL";
    protected static final String ROUND_STR = "ROUND";
    protected static final String FLOOR_STR = "FLOOR";
    protected static final String RAND_STR = "RAND";
    protected static final String CURRENT_TIMESTAMP_STR = "CURRENT_TIMESTAMP";


    protected final DBTypeFactory dbTypeFactory;
    protected final TypeFactory typeFactory;
    protected final DBTermType dbStringType;
    protected final DBTermType dbBooleanType;
    private final DBTermType dbDoubleType;
    private final DBTermType abstractRootDBType;
    private final TermType abstractRootType;
    private final DBFunctionSymbol ifThenElse;
    private final DBBooleanFunctionSymbol isStringEmpty;
    private final DBIsNullOrNotFunctionSymbol isNull;
    private final DBIsNullOrNotFunctionSymbol isNotNull;
    private final DBIsTrueFunctionSymbol isTrue;

    protected AbstractSQLDBFunctionSymbolFactory(ImmutableTable<String, Integer, DBFunctionSymbol> regularFunctionTable,
                                                 TypeFactory typeFactory) {
        super(regularFunctionTable, typeFactory);
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
        this.typeFactory = typeFactory;
        this.dbStringType = dbTypeFactory.getDBStringType();
        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.dbDoubleType = dbTypeFactory.getDBDoubleType();
        this.abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        this.ifThenElse = createDBIfThenElse(dbBooleanType, abstractRootDBType);
        this.isStringEmpty = createIsStringEmpty(dbBooleanType, abstractRootDBType);
        this.abstractRootType = typeFactory.getAbstractAtomicTermType();
        this.isNull = createDBIsNull(dbBooleanType, abstractRootDBType);
        this.isNotNull = createDBIsNotNull(dbBooleanType, abstractRootDBType);
        this.isTrue = createDBIsTrue(dbBooleanType);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDefaultRegularFunctionTable(TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbStringType = dbTypeFactory.getDBStringType();
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType dbDateTimestamp = dbTypeFactory.getDBDateTimestampType();
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

        DBFunctionSymbol nowFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(CURRENT_TIMESTAMP_STR, 0,
                dbDateTimestamp, true, abstractRootDBType);
        builder.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);


        return builder.build();
    }

    @Override
    protected DBFunctionSymbol createRegularUntypedFunctionSymbol(String nameInDialect, int arity) {
        // TODO: avoid if-then-else
        if (isAnd(nameInDialect))
            return createDBAnd(arity);
        else if (isOr(nameInDialect))
            return createDBOr(arity);
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

    protected boolean isOr(String nameInDialect) {
        return nameInDialect.equals(OR_STR);
    }

    protected DBConcatFunctionSymbol createDBConcat(int arity) {
        return new DefaultDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType);
    }

    protected DBBooleanFunctionSymbol createDBAnd(int arity) {
        return new DefaultDBAndFunctionSymbol(AND_STR, arity, dbBooleanType);
    }

    protected DBBooleanFunctionSymbol createDBOr(int arity) {
        return new DefaultDBOrFunctionSymbol(OR_STR, arity, dbBooleanType);
    }

    @Override
    protected DBNotFunctionSymbol createDBNotFunctionSymbol(DBTermType dbBooleanType) {
        return new DefaultDBNotFunctionSymbol(NOT_STR, dbBooleanType);
    }

    protected DBFunctionSymbol createDBIfThenElse(DBTermType dbBooleanType, DBTermType abstractRootDBType) {
        return new DefaultSQLIfThenElseFunctionSymbol(dbBooleanType, abstractRootDBType);
    }

    protected DBBooleanFunctionSymbol createIsStringEmpty(DBTermType dbBooleanType, DBTermType abstractRootDBType) {
        return new DefaultSQLIsStringEmptyFunctionSymbol(dbBooleanType, abstractRootDBType);
    }

    protected DBIsNullOrNotFunctionSymbol createDBIsNull(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        return new DefaultSQLDBIsNullOrNotFunctionSymbol(true, dbBooleanType, rootDBTermType);
    }

    protected DBIsNullOrNotFunctionSymbol createDBIsNotNull(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        return new DefaultSQLDBIsNullOrNotFunctionSymbol(false, dbBooleanType, rootDBTermType);
    }

    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new DefaultDBIsTrueFunctionSymbol(dbBooleanType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        return new DefaultSQLSimpleDBCastFunctionSymbol(dbTypeFactory.getAbstractRootDBType(), targetType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        if (targetType.equals(dbBooleanType))
            return new DefaultSQLSimpleDBBooleanCastFunctionSymbol(inputType, targetType);

        DBTermType.Category inputCategory = inputType.getCategory();
        if (inputCategory.equals(targetType.getCategory())) {
            switch (inputCategory) {
                case STRING:
                    return createStringToStringCastFunctionSymbol(inputType, targetType);
                default:
                    return new DefaultSQLSimpleDBCastFunctionSymbol(inputType, targetType);
            }
        }
        return new DefaultSQLSimpleDBCastFunctionSymbol(inputType, targetType);
    }

    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType,
                                                                                    DBTermType targetType) {
        return new DefaultImplicitDBCastFunctionSymbol(inputType, targetType);
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
    protected DBFunctionSymbol createAbsFunctionSymbol(DBTermType dbTermType) {
        return new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(ABS_STR, 1, dbTermType, false);
    }

    @Override
    protected DBFunctionSymbol createCeilFunctionSymbol(DBTermType dbTermType) {
        return new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(CEIL_STR, 1, dbTermType, false);
    }

    @Override
    protected DBFunctionSymbol createFloorFunctionSymbol(DBTermType dbTermType) {
        return new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(FLOOR_STR, 1, dbTermType, false);
    }

    @Override
    protected DBFunctionSymbol createRoundFunctionSymbol(DBTermType dbTermType) {
        return new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(ROUND_STR, 1, dbTermType, false);
    }

    @Override
    protected String serializeYear(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(YEAR FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMonth(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(MONTH FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDay(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(DAY FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeHours(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(HOUR FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMinutes(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(MINUTE FROM %s)", termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: is it returning an integer or a decimal?
     */
    @Override
    protected String serializeSeconds(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(SECOND FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType) {
        return new DefaultSQLTimestampISONormFunctionSymbol(
                dbDateTimestampType,
                dbStringType,
                this::serializeDateTimeNorm);
    }


    protected abstract String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory);

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol() {
        return new DefaultBooleanNormFunctionSymbol(dbBooleanType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return new DefaultSQLTimestampISODenormFunctionSymbol(timestampType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanDenormFunctionSymbol() {
        return new DefaultBooleanDenormFunctionSymbol(dbBooleanType, dbStringType);
    }

    @Override
    protected DBMathBinaryOperator createMultiplyOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(MULTIPLY_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createDivideOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(DIVIDE_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createAddOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(ADD_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createSubstractOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(SUBSTRACT_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedMultiplyOperator() {
        return new DefaultUntypedDBMathBinaryOperator(MULTIPLY_STR, abstractRootDBType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedDivideOperator() {
        return new DefaultUntypedDBMathBinaryOperator(DIVIDE_STR, abstractRootDBType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedAddOperator() {
        return new DefaultUntypedDBMathBinaryOperator(ADD_STR, abstractRootDBType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedSubstractOperator() {
        return new DefaultUntypedDBMathBinaryOperator(SUBSTRACT_STR, abstractRootDBType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictNumericEquality() {
        return new DefaultDBNonStrictNumericEqOperator(abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictStringEquality() {
        return new DefaultDBNonStrictStringEqOperator(abstractRootDBType, dbBooleanType);

    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictDatetimeEquality() {
        return new DefaultDBNonStrictDatetimeEqOperator(abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictDefaultEquality() {
        return new DefaultDBNonStrictDefaultEqOperator(abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNumericInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBNumericInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createBooleanInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBBooleanInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createStringInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBStringInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDatetimeInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBDatetimeInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDefaultInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBDefaultInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
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
    public DBFunctionSymbol getDBReplace() {
        return getRegularDBFunctionSymbol(REPLACE_STR, 3);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace3() {
        return getRegularDBFunctionSymbol(REGEXP_REPLACE_STR, 3);
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
    public DBAndFunctionSymbol getDBAnd(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of AND must be >= 2");
        return (DBAndFunctionSymbol) getRegularDBFunctionSymbol(AND_STR, arity);
    }

    @Override
    public DBOrFunctionSymbol getDBOr(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of OR must be >= 2");
        return (DBOrFunctionSymbol) getRegularDBFunctionSymbol(OR_STR, arity);
    }

    @Override
    public DBIsNullOrNotFunctionSymbol getDBIsNull() {
        return isNull;
    }

    @Override
    public DBIsNullOrNotFunctionSymbol getDBIsNotNull() {
        return isNotNull;
    }

    @Override
    public DBBooleanFunctionSymbol getDBIsStringEmpty() {
        return isStringEmpty;
    }

    @Override
    public DBIsTrueFunctionSymbol getIsTrue() {
        return isTrue;
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBRand(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(getRandNameInDialect(), uuid, dbDoubleType);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(getUUIDNameInDialect(), uuid, dbStringType);
    }

    @Override
    public DBFunctionSymbol getNow() {
        return getRegularDBFunctionSymbol(CURRENT_TIMESTAMP_STR, 0);
    }

    /**
     * Can be overridden.
     *
     * Not an official SQL function
     */
    protected String getRandNameInDialect() {
        return RAND_STR;
    }

    protected abstract String getUUIDNameInDialect();

}
