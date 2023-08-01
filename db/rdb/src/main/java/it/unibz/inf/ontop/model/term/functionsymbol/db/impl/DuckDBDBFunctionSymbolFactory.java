package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.UUID;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.SnowflakeDBTypeFactory.TIMESTAMP_LOCAL_TZ_STR;
import static it.unibz.inf.ontop.model.type.impl.SnowflakeDBTypeFactory.TIMESTAMP_NO_TZ_STR;
import static it.unibz.inf.ontop.model.type.impl.DuckDBDBTypeFactory.DEFAULT_DECIMAL_STR;

public class DuckDBDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UUID_STRING_STR = "UUID";

    private DBFunctionSymbol charLengthFunctionSymbol;
    private DBFunctionSymbol regexpLikeFunctionSymbol;


    @Inject
    protected DuckDBDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDuckDBRegularFunctionTable(typeFactory), typeFactory);

        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();

        this.charLengthFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol("LENGTH", 1, dbIntType,
                false, abstractRootDBType);
        this.regexpLikeFunctionSymbol = new DefaultSQLSimpleDBBooleanFunctionSymbol("REGEXP_MATCHES", 2, dbBooleanType,
                abstractRootDBType);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDuckDBRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        table.remove(CHAR_LENGTH_STR, 1);
        table.remove(REGEXP_LIKE_STR, 2);
        table.remove(REGEXP_LIKE_STR, 3);

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
        return String.format("IF(POSITION(%s IN %s) != 0, SUBSTRING(%s, POSITION(%s IN %s) + LENGTH(%s)), '')", after, str, str, after, str, after);

    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("MD5(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("DuckDB only supports the md5 hashing function.");
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("DuckDB only supports the md5 hashing function.");
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("DuckDB only supports the md5 hashing function.");
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("DuckDB only supports the md5 hashing function.");
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
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return (DBBooleanFunctionSymbol) this.regexpLikeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return this.charLengthFunctionSymbol;
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        /* DuckDB STRFTIME formats a timestamp:
            %x: ISO date representation
            %X: ISO time representation
            %z: UTC offset in the form +HHMM or -HHMM

           We can combine this as %xT%X%z to get a string in the form 'YYYY-MM-DDTHH:MM:SS+HHMM.
           However, we want the string to end with +HH:MM instead. So we split it before the last
           two characters and add a ':' in-between.
         */
        return String.format("STRFTIME(CAST(%s as TIMESTAMP WITH TIME ZONE), '%%xT%%X%%z')",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(0)));
    }


    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STRING_STR;
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(getUUIDNameInDialect(), uuid, dbStringType) {

            @Override
            public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory) {
                return "CAST(UUID() as TEXT)";
            }

        };
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

    @Override
    protected String getRandNameInDialect() {
        return "RANDOM";
    }

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "GROUP_CONCAT(%s%s, %s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1))
                ));
    }

    /**
     * XSD CAST functions
     */
    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN REGEXP_MATCHES(%1$s, "+ numericPattern +") THEN " +
                        "CAST(FLOOR(ABS(CAST(%1$s AS " + DEFAULT_DECIMAL_STR + "))) * SIGN(CAST(%1$s AS DECIMAL)) AS INTEGER) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN REGEXP_MATCHES(%1$s, " + numericNonFPPattern + ") THEN " +
                        "CAST(%1$s AS " + DEFAULT_DECIMAL_STR + ") " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected DBFunctionSymbol createDBSample(DBTermType termType) {
        return new DBSampleFunctionSymbolImpl(termType, "FIRST");
    }
}
