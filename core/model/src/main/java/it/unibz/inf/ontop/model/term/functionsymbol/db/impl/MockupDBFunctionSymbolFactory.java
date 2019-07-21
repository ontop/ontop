package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;

import java.util.UUID;
import java.util.function.Function;

/**
 * Mockup: for DB-independent tests only
 */
public class MockupDBFunctionSymbolFactory extends AbstractDBFunctionSymbolFactory {

    private static final String CONCAT_STR = "CONCAT";
    private static final String AND_STR = "AND";
    private static final String OR_STR = "OR";
    private static final String CHAR_LENGTH_STR = "CHARLENGTH";
    private static final String NOT_STR = "NOT";
    private static final String MULTIPLY_STR = "*";
    protected static final String DIVIDE_STR = "/";
    protected static final String ADD_STR = "+";
    protected static final String SUBSTRACT_STR = "-";
    private final TermType abstractRootType;
    private final DBTermType dbBooleanType;
    private final DBTermType abstractRootDBType;
    private final DBTermType dbStringType;
    private final DBTypeFactory dbTypeFactory;

    @Inject
    private MockupDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDefaultRegularFunctionTable(typeFactory), typeFactory);
        abstractRootType = typeFactory.getAbstractAtomicTermType();
        dbTypeFactory = typeFactory.getDBTypeFactory();
        dbBooleanType = dbTypeFactory.getDBBooleanType();
        abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        dbStringType = dbTypeFactory.getDBStringType();
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDefaultRegularFunctionTable(TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbStringType = dbTypeFactory.getDBStringType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        ImmutableTable.Builder<String, Integer, DBFunctionSymbol> builder = ImmutableTable.builder();
        return builder.build();
    }

    /**
     * This mockup does not provide any denormalization function symbol
     */
    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDenormalizationTable() {
        return ImmutableTable.of();
    }

    @Override
    protected DBFunctionSymbol createDBCount(boolean isUnary, boolean isDistinct) {
        DBTermType integerType = dbTypeFactory.getDBLargeIntegerType();
        return isUnary
                ? new DBCountFunctionSymbolImpl(abstractRootDBType, integerType, isDistinct)
                : new DBCountFunctionSymbolImpl(integerType, isDistinct);
    }

    @Override
    protected DBFunctionSymbol createDBSum(DBTermType termType, boolean isDistinct) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createDBAvg(DBTermType termType, boolean isDistinct) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createDBMin(DBTermType termType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createDBMax(DBTermType termType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createRegularUntypedFunctionSymbol(String nameInDialect, int arity) {
        switch (nameInDialect) {
            case AND_STR:
                return createDBAnd(arity);
            case OR_STR:
                return createDBOr(arity);
            case CONCAT_STR:
                return createDBConcat(arity);
            default:
                return new DefaultUntypedDBFunctionSymbol(nameInDialect, arity, abstractRootDBType);
        }
    }

    @Override
    protected DBBooleanFunctionSymbol createRegularBooleanFunctionSymbol(String nameInDialect, int arity) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    private DBFunctionSymbol createDBAnd(int arity) {
        return new DefaultDBAndFunctionSymbol(AND_STR, arity, dbBooleanType);
    }

    private DBFunctionSymbol createDBOr(int arity) {
        return new DefaultDBOrFunctionSymbol(OR_STR, arity, dbBooleanType);
    }

    @Override
    protected DBNotFunctionSymbol createDBNotFunctionSymbol(DBTermType dbBooleanType) {
        return new DefaultDBNotFunctionSymbol(NOT_STR, dbBooleanType);
    }

    private DBFunctionSymbol createDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return targetType.equals(dbBooleanType)
                ? new MockupSimpleDBBooleanCastFunctionSymbol(inputType, targetType)
                : new MockupSimpleDBCastFunctionSymbol(inputType, targetType);
    }

    @Override
    protected DBFunctionSymbol createDBCase(int arity) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBBooleanFunctionSymbol createDBBooleanCase(int arity) {
        return new DBBooleanCaseFunctionSymbolImpl(arity, dbBooleanType, abstractRootDBType);
    }

    @Override
    protected DBFunctionSymbol createCoalesceFunctionSymbol(int arity) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
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
        return new MockupR2RMLSafeIRIEncodeFunctionSymbol(dbStringType);
    }

    /**
     * Too simplistic!
     */
    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType) {
        return createSimpleCastFunctionSymbol(dbTypeFactory.getDBDateTimestampType(), dbStringType);
    }

    /**
     * Too simplistic!
     * @param booleanType
     */
    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return createSimpleCastFunctionSymbol(dbTypeFactory.getDBBooleanType(), dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanDenormFunctionSymbol() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
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
    protected DBFunctionSymbol createAbsFunctionSymbol(DBTermType dbTermType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createCeilFunctionSymbol(DBTermType dbTermType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createFloorFunctionSymbol(DBTermType dbTermType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected DBFunctionSymbol createRoundFunctionSymbol(DBTermType dbTermType) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> immutableTerms,
                                       Function<ImmutableTerm, String> immutableTermStringFunction,
                                       TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeYear(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeMonth(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeDay(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeHours(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeMinutes(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeSeconds(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBIfThenElse() {
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
        return getRegularDBFunctionSymbol("REPLACE", 3);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace3() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace4() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBSubString3() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBRight() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(CHAR_LENGTH_STR, 1);
    }

    @Override
    public DBConcatFunctionSymbol getNullRejectingDBConcat(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of CONCAT must be >= 2");
        return (DBConcatFunctionSymbol) getRegularDBFunctionSymbol(CONCAT_STR, arity);
    }

    @Override
    public DBConcatFunctionSymbol getDBConcatOperator(int arity) {
        return getNullRejectingDBConcat(arity);
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
        return new MockupDBIsNullOrNotFunctionSymbolImpl(true, dbBooleanType, abstractRootDBType);
    }

    @Override
    public DBIsNullOrNotFunctionSymbol getDBIsNotNull() {
        return new MockupDBIsNullOrNotFunctionSymbolImpl(false, dbBooleanType, abstractRootDBType);
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
    protected DBBooleanFunctionSymbol createNonStrictDateEquality() {
        return new DefaultDBNonStrictDateEqOperator(abstractRootDBType, dbBooleanType);
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
    protected DBBooleanFunctionSymbol createDateInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBDateInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDefaultInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBDefaultInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    public DBBooleanFunctionSymbol getDBIsStringEmpty() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBIsTrueFunctionSymbol getIsTrue() {
        return new DefaultDBIsTrueFunctionSymbol(dbBooleanType);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public DBFunctionSymbol getDBNow() {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBRand(UUID uuid) {
        throw new UnsupportedOperationException("Operation not supported by the MockupDBFunctionSymbolFactory");
    }

}
