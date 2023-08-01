package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.TrinoDBTypeFactory.DEFAULT_DECIMAL_STR;
import static it.unibz.inf.ontop.model.type.impl.SnowflakeDBTypeFactory.TIMESTAMP_LOCAL_TZ_STR;
import static it.unibz.inf.ontop.model.type.impl.SnowflakeDBTypeFactory.TIMESTAMP_NO_TZ_STR;

public class TrinoDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String RANDOM_STR = "RANDOM";
    private static final String UUID_STRING_STR = "UUID";
    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for Trino";

    private DBFunctionSymbol dbRight;


    @Inject
    protected TrinoDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createTrinoRegularFunctionTable(typeFactory), typeFactory);
        dbRight = new SimpleTypedDBFunctionSymbolImpl(RIGHT_STR, 2,
                dbTypeFactory.getDBStringType(), false, abstractRootDBType, this::serializeRight);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createTrinoRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> createNormalizationMap() {
        ImmutableMap.Builder<DBTermType, DBTypeConversionFunctionSymbol> builder = ImmutableMap.builder();
        builder.putAll(super.createNormalizationMap());


        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        // NB: TIMESTAMP_TZ_STR is the default, already done.
        for (String timestampTypeString : ImmutableList.of(TIMESTAMP_LOCAL_TZ_STR, TIMESTAMP_NO_TZ_STR)) {
            DBTermType timestampType = dbTypeFactory.getDBTermType(timestampTypeString);

            DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestampType);
            builder.put(timestampType, datetimeNormFunctionSymbol);
        }

        return builder.build();
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(POSITION(%s IN %s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("SUBSTRING(%s,1,POSITION(%s IN %s)-1)", str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("SUBSTRING(%s, IF(POSITION(%s IN %s) != 0, POSITION(%s IN %s) + LENGTH(%s), 0))", str, after, str, after, str, after);

    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeHashingFunction("MD5", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeHashingFunction("SHA1", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeHashingFunction("SHA256", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA384: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeHashingFunction("SHA512", terms, termConverter, termFactory);
    }

    private String serializeHashingFunction(String functionName, ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(TO_HEX(%s(CAST(%s as VARBINARY))))", functionName, termConverter.apply(terms.get(0)));

    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("(LPAD(EXTRACT(TIMEZONE_HOUR FROM %s)::text,2,'0') || ':' || LPAD(EXTRACT(TIMEZONE_MINUTE FROM %s)::text,2,'0'))", str, str);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return createDBConcatOperator(arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType,
                Serializers.getOperatorSerializer(CONCAT_OP_STR));
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullToleratingDBConcatFunctionSymbol("CONCAT", arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TO_ISO8601(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(LENGTH_STR, 1);
    }

    @Override
    protected String getRandNameInDialect() {
        return RANDOM_STR;
    }

    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STRING_STR;
    }


    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("week", terms, termConverter, termFactory);
    }


    @Override
    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("day", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("hour", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("minute", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("second", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("millisecond", terms, termConverter, termFactory);
    }

    private String serializeTimeBetween(String timeUnit, ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("date_diff('%s', %s, %s)",
                timeUnit,
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    protected String serializeRight(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SUBSTRING(%s, -%s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    public DBFunctionSymbol getDBRight() {
        return dbRight;
    }

    /**
     * XSD CAST functions
     */
    @Override
    protected String serializeCheckAndConvertFloatFromNonFPNumeric(ImmutableList<? extends ImmutableTerm> terms,
                                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (CAST(%1$s AS REAL) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS REAL) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS REAL) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS REAL) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN NOT REGEXP_LIKE(%1$s, " + numericPattern + ")" +
                        " THEN NULL ELSE CAST(%1$s AS DOUBLE) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN NOT REGEXP_LIKE(%1$s, " + numericPattern + ") THEN NULL " +
                        "WHEN (CAST(%1$s AS REAL) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS REAL) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS REAL) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS REAL) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN REGEXP_LIKE(%1$s, " + numericNonFPPattern + ") THEN " +
                        "CAST(%1$s AS " + DEFAULT_DECIMAL_STR + ") " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN REGEXP_LIKE(%1$s, "+ numericPattern +") THEN " +
                        "CAST(FLOOR(ABS(CAST(%1$s AS " + DEFAULT_DECIMAL_STR + "))) * SIGN(CAST(%1$s AS DECIMAL)) AS INTEGER) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertIntegerFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s='1' THEN 1 " +
                        "WHEN UPPER(%1$s) LIKE 'TRUE' THEN 1 " +
                        "WHEN %1$s='0' THEN 0 " +
                        "WHEN UPPER(%1$s) LIKE 'FALSE' THEN 0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN CAST(%1$s AS " + DEFAULT_DECIMAL_STR + ") = 0 THEN 'false' " +
                        "WHEN %1$s = '' THEN 'false' " +
                        "ELSE 'true' " +
                        "END)",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (REGEXP_LIKE(%1$s, " + datePattern1 + ") AND " +
                        "REGEXP_LIKE(%1$s, " + datePattern2 +") AND " +
                        "REGEXP_LIKE(%1$s, " + datePattern3 +") AND " +
                        "REGEXP_LIKE(%1$s, " + datePattern4 +") ) " +
                        " THEN NULL ELSE CAST(%1$s AS DATE) END",
                term);
    }

    @Override
    protected DBFunctionSymbol createDBSample(DBTermType termType) {
        return new DBSampleFunctionSymbolImpl(termType, "ARBITRARY");
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
        return String.format("(SECOND(%s) * 1000 + MILLISECOND(%s))", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMicroseconds(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(SECOND(%s) * 1000000 + MILLISECOND(%s) * 1000)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    public DBFunctionSymbol getDBDateTrunc(String datePart) {
        if(ImmutableSet.of("microseconds", "milliseconds", "microsecond", "millisecond", "decade", "century", "millennium").contains(datePart.toLowerCase())) {
            throw new IllegalArgumentException(String.format("This SQL dialect does not support DATE_TRUNC on %s.", datePart));
        }
        return super.getDBDateTrunc(datePart);
    }

}
