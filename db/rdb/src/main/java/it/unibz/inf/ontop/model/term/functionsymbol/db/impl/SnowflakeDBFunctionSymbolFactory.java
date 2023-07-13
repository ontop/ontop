package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.SnowflakeDBTypeFactory.*;

public class SnowflakeDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UUID_STRING_STR = "UUID_STRING";
    private static final String RANDOM_STR = "RANDOM";

    @Inject
    protected SnowflakeDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createSnowflakeRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createSnowflakeRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));


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
        return String.format("CONTAINS(%s,%s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("NVL(SUBSTR(%s,0,POSITION(%s IN %s)-1),'')", str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("SUBSTRING(%s,POSITION(%s IN %s) + LENGTH(%s), CAST( SIGN(POSITION(%s IN %s)) * LENGTH(%s) AS INTEGER))",
                str, after, str , after , after, str, str);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("MD5(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA1(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s, 256)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s, 384)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s, 512)", termConverter.apply(terms.get(0)));
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
        return createNullRejectingDBConcat(arity);
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TO_CHAR(%s, 'YYYY-MM-DDTHH24:MI:SS.FF3TZHTZM')", termConverter.apply(terms.get(0)));
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
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return "ROW_NUMBER() OVER (ORDER BY 1)";
    }

    @Override
    protected DBFunctionSymbol createEncodeURLorIRI(boolean preserveInternationalChars) {
        return new MySQLEncodeURLorIRIFunctionSymbolImpl(dbStringType, preserveInternationalChars);
    }

    /**
     * XSD CAST functions
     */
    // NOTE: Not possible to specify NaN or 0/0
    @Override
    protected String serializeCheckAndConvertBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(CASE WHEN %s IS NULL THEN NULL " +
                        "WHEN CAST(%s AS " + NUMBER_38_10_STR + ") = 0 THEN '0' " +
                        "WHEN %s = '' THEN '0' " +
                        "ELSE '1' " +
                        "END)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertBooleanFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CASE WHEN %s='1' THEN 1 " +
                        "WHEN UPPER(%s) LIKE 'TRUE' THEN 1 " +
                        "WHEN %s='0' THEN 0 " +
                        "WHEN UPPER(%s) LIKE 'FALSE' THEN 0 " +
                        "ELSE NULL " +
                        "END",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRY_CAST(%s AS DOUBLE PRECISION)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRY_CAST(%s AS FLOAT)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertFloatFromNonFPNumeric(ImmutableList<? extends ImmutableTerm> terms,
                                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeCheckAndConvertFloat(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRY_CAST(%s AS " + NUMBER_38_10_STR + ")", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertFloatFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s='1' THEN 1.0 " +
                        "WHEN UPPER(%1$s) LIKE 'TRUE' THEN 1.0 " +
                        "WHEN %1$s='0' THEN 0.0 " +
                        "WHEN UPPER(%1$s) LIKE 'FALSE' THEN 0.0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDecimalFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s='1' THEN 1.0 " +
                        "WHEN UPPER(%1$s) LIKE 'TRUE' THEN 1.0 " +
                        "WHEN %1$s='0' THEN 0.0 " +
                        "WHEN UPPER(%1$s) LIKE 'FALSE' THEN 0.0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CAST(FLOOR(ABS(TRY_CAST(%1$s AS " + NUMBER_38_10_STR + "))) * SIGN(TRY_CAST(%1$s AS DECIMAL)) AS INTEGER)",
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
    protected String serializeCheckAndConvertStringFromDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN %1$s %% 1 = 0 THEN FLOOR(ABS(%1$s)) * SIGN(TRY_CAST (%1$s AS DECIMAL)) " +
                        "ELSE %1$s " +
                        "END)",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateTimeFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRY_CAST(%s AS DATETIME)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDateFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRY_CAST(%s AS DATE)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return this.serializeCheckAndConvertDateFromDateTime(terms, termConverter, termFactory);
    }

    @Override
    protected DBFunctionSymbol createDBSample(DBTermType termType) {
        return new DBSampleFunctionSymbolImpl(termType, "ANY_VALUE");
    }


    @Override
    protected DBTermType inferOutputTypeMathOperator(String dbMathOperatorName, DBTermType arg1Type, DBTermType arg2Type) {
        if (dbMathOperatorName.equals(SPARQL.NUMERIC_DIVIDE))
            return dbDecimalType;

        return super.inferOutputTypeMathOperator(dbMathOperatorName, arg1Type, arg2Type);
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
        return String.format("EXTRACT(SECOND FROM %s) * 1000", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMicroseconds(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(SECOND FROM %s) * 1000000", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    public DBFunctionSymbol getDBDateTrunc(String datePart) {
        if(ImmutableSet.of("microseconds", "milliseconds", "decade", "century", "millennium").contains(datePart.toLowerCase())) {
            throw new IllegalArgumentException(String.format("Snowflake does not support DATE_TRUNC on %s.", datePart));
        }
        return super.getDBDateTrunc(datePart);
    }
}
