package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.SMALLINT_STR;

public class DB2DBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String CURRENT_TIMESTAMP_SPACE_STR = "CURRENT TIMESTAMP";
    private static final String VARCHAR_STR = "VARCHAR";
    private final DBFunctionSymbolSerializer numberToStringSerializer;

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for DB2";


    @Inject
    protected DB2DBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDB2RegularFunctionTable(typeFactory), typeFactory);
        this.numberToStringSerializer = (terms, termConverter, termFactory) ->
                String.format("REPLACE(VARCHAR(%s),' ', '')", termConverter.apply(terms.get(0)));
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDB2RegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_SPACE_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_SPACE_STR, 0, nowFunctionSymbol);

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> createNormalizationMap() {
        ImmutableMap.Builder<DBTermType, DBTypeConversionFunctionSymbol> builder = ImmutableMap.builder();
        builder.putAll(super.createNormalizationMap());

        return builder.build();
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        Table<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> table = HashBasedTable.create();
        table.putAll(super.createNormalizationTable());

        // SMALLINT boolean normalization
        DBTermType smallIntType = dbTypeFactory.getDBTermType(SMALLINT_STR);
        table.put(smallIntType, typeFactory.getXsdBooleanDatatype(),
                new DefaultNumberNormAsBooleanFunctionSymbol(smallIntType, dbStringType));
        return ImmutableTable.copyOf(table);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return createDBConcatOperator(arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        if (arity != 2)
            throw new UnsupportedOperationException("CONCAT is strictly binary in DB2. Please use the operator instead.");
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new OneDigitDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }


    /**
     * TODO: handle the timezone!
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("REPLACE(VARCHAR_FORMAT(%s,'YYYY-MM-DD HH24:MI:SS.FF'),' ','T')",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return new OneDigitBooleanNormFunctionSymbolImpl(booleanType, dbStringType);
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        return getRegularDBFunctionSymbol(SUBSTR_STR, 2);
    }

    @Override
    public DBFunctionSymbol getDBSubString3() {
        return getRegularDBFunctionSymbol(SUBSTR_STR, 3);
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(LENGTH_STR, 1);
    }

    @Override
    public DBFunctionSymbol getDBNow() {
        return getRegularDBFunctionSymbol(CURRENT_TIMESTAMP_SPACE_STR, 0);
    }

    /**
     * TODO: try to support it
     */
    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("UUID: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(LOCATE(%2$s , %1$s) > 0)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format("LEFT(%s,SIGN(LOCATE(%s,%s)) * (LOCATE(%s,%s)-1))", str, before, str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("RTRIM(SUBSTR(%s,LOCATE(%s,%s)+LENGTH(%s), SIGN(LOCATE(%s,%s))*LENGTH(%s)))",
                str, after, str , after, after, str, str);
    }

    /**
     * TODO: try to support it
     */
    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("MD5: " + NOT_YET_SUPPORTED_MSG);
    }

    /**
     * TODO: try to support it
     */
    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA1: " + NOT_YET_SUPPORTED_MSG);
    }

    /**
     * TODO: try to support it
     */
    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA256: " + NOT_YET_SUPPORTED_MSG);
    }

    /**
     * TODO: try to support it
     */
    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA384: " + NOT_YET_SUPPORTED_MSG);
    }

    /**
     * TODO: try to support it
     */
    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA512: " + NOT_YET_SUPPORTED_MSG);
    }

    /**
     * TODO: try to support it
     */
    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                 Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("Serialization of the time zone: " + NOT_YET_SUPPORTED_MSG);
    }

    /**
     * Removes the padding added by DB2
     */
    @Override
    protected DBTypeConversionFunctionSymbol createIntegerToStringCastFunctionSymbol(DBTermType inputType) {
        return new DefaultCastIntegerToStringFunctionSymbol(inputType, dbStringType, numberToStringSerializer);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDecimalToStringCastFunctionSymbol(DBTermType inputType) {
        return createNonIntegerNumberToStringCastFunctionSymbol(inputType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createFloatDoubleToStringCastFunctionSymbol(DBTermType inputType) {
        return createNonIntegerNumberToStringCastFunctionSymbol(inputType);
    }

    /**
     * Removes the padding added by DB2
     */
    protected DBTypeConversionFunctionSymbol createNonIntegerNumberToStringCastFunctionSymbol(DBTermType inputType) {
        return new DefaultSimpleDBCastFunctionSymbol(inputType, dbStringType, numberToStringSerializer);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDefaultCastToStringFunctionSymbol(DBTermType inputType) {
        return new DefaultSimpleDBCastFunctionSymbol(inputType, dbStringType,
                Serializers.getRegularSerializer(VARCHAR_STR));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        if (targetType.equals(dbTypeFactory.getDBStringType())) {
            return new DefaultSimpleDBCastFunctionSymbol(dbTypeFactory.getAbstractRootDBType(), dbStringType,
                    Serializers.getRegularSerializer(VARCHAR_STR));
        }
        return super.createSimpleCastFunctionSymbol(targetType);
    }

    @Override
    protected DBFunctionSymbol createDBAvg(DBTermType inputType, boolean isDistinct) {
        // To make sure the AVG does not return an integer but a decimal
        if (inputType.equals(dbIntegerType))
            return new ForcingFloatingDBAvgFunctionSymbolImpl(inputType, dbDecimalType, isDistinct);

        return super.createDBAvg(inputType, isDistinct);
    }

    /**
     * Time extension - duration arithmetic
     */

    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(TRUNC(WEEKS_BETWEEN(%s, %s)) AS INTEGER)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(TRUNC(DAYS_BETWEEN(%s, %s)) AS INTEGER)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(TRUNC(HOURS_BETWEEN(%s, %s)) AS INTEGER)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(TRUNC(MINUTES_BETWEEN(%s, %s)) AS INTEGER)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(TRUNC(SECONDS_BETWEEN(%s, %s)) AS INTEGER)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(TRUNC(SECONDS_BETWEEN(%s, %s),3) AS INTEGER)*1000",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    /**
     * CAST functions
     */
    @Override
    protected String serializeCheckAndConvertFloatFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CASE WHEN %s THEN 1 ELSE 0 END",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN REGEXP_LIKE(%1$s, '^-?([0-9]+[.]?[0-9]*|[.][0-9]+)$') THEN " +
                        "CAST(%1$s AS DECIMAL(20, 6)) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN REGEXP_LIKE(%1$s, '^-?([0-9]+[.]?[0-9]*|[.][0-9]+)$') THEN " +
                        "CAST(FLOOR(ABS(CAST(%1$s AS DECIMAL(20,6)))) * SIGN(CAST(%1$s AS DECIMAL)) AS INTEGER) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN CAST(%1$s AS DECIMAL(20, 6)) = 0 THEN 'false' " +
                        "WHEN %1$s = '' THEN 'false' " +
                        "ELSE 'true' " +
                        "END)",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DATE(%s)", termConverter.apply(terms.get(0)));
    }

    //TODO: Solution for DB2 is imperfect. Not possible to include all scenarios!
    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN TRANSLATE(%1$s, ' ', '0123456789,. ' ) != '' " +
                        " THEN NULL ELSE CAST(%1$s AS DOUBLE) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN TRANSLATE(%1$s, ' ', '0123456789,. ' ) != '' " + " THEN NULL " +
                        "WHEN (CAST(%1$s AS FLOAT) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS FLOAT) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS FLOAT) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS FLOAT) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (TRANSLATE(%1$s, ' ', '0123456789- ' ) != '' AND " +
                        "TRANSLATE(%1$s, ' ', '0123456789/ ' ) != '' ) " +
                        " THEN NULL ELSE CAST(%1$s AS DATE) END",
                term);
    }

    @Override
    protected String serializeDecade(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR(EXTRACT(YEAR FROM %s) / 10.00000)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCentury(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CEIL(EXTRACT(YEAR FROM %s) / 100.00000)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMillennium(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CEIL(EXTRACT(YEAR FROM %s) / 1000.00000)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMilliseconds(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(EXTRACT(SECOND FROM %s) * 1000)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMicroseconds(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR(EXTRACT(SECOND FROM %s) * 1000000)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeWeek(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        var term = termConverter.apply(terms.get(0));
        return String.format("WEEK_ISO(%s)", term);
    }

    @Override
    protected String serializeQuarter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("QUARTER(%s)", termConverter.apply(terms.get(0)));
    }
}
