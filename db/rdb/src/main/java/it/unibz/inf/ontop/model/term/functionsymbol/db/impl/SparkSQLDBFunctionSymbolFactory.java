package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;


import java.util.function.Function;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.type.impl.SparkSQLDBTypeFactory.DECIMAL_38_10_STR;

/**
 *  SPARK-SQL 3.0.1 functions description available at : https://spark.apache.org/docs/3.0.1/api/sql/
 */
public class SparkSQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UNSUPPORTED_MSG = "Not supported by Spark or not yet implemented";

    @Inject
    protected SparkSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createSparkSQLRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createSparkSQLRegularFunctionTable(
            TypeFactory typeFactory) {

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return getNullRejectingDBConcat(arity);
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullToleratingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("REPLACE(DATE_FORMAT(%s,'yyyy-MM-dd HH:mm:ss.SSSxxx'),' ','T')", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return new SparkSQLTimestampDenormFunctionSymbol(timestampType, dbStringType);
    }

    @Override
    protected String getUUIDNameInDialect() {
        return String.format("uuid");
    }

    /**
     * ORDER BY is required in the OVER clause
     */
    @Override
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return "ROW_NUMBER() OVER (ORDER BY (SELECT NULL))";
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter,
                                       TermFactory termFactory) {
        return String.format("(INSTR(%s, %s) > 0)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("LEFT(%s,INSTR(%s,%s)-1)", str, str, before);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));

        // sign return 1 if positive number, 0 if 0, and -1 if negative number
        // it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,INSTR(%s,%s) + LENGTH(%s), SIGN(INSTR(%s,%s)) * LENGTH(%s))",
                str, str, after, after, str, after, str);
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
        return String.format("SHA2(%s,256)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s,384)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s,512)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException("Serialization of the time zone: " + UNSUPPORTED_MSG);
    }

    @Override
    protected DBFunctionSymbol createEncodeURLorIRI(boolean preserveInternationalChars) {
        return new SparkSQLEncodeURLorIRIFunctionSymbolImpl(dbStringType, preserveInternationalChars);
    }

    /**
     * Time extension - duration arithmetic
     */

    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR(DATEDIFF(%s, %s)/7)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DATEDIFF(%s, %s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR((BIGINT(%s) - BIGINT(%s))/3600)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR((BIGINT(%s) - BIGINT(%s))/60)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR((BIGINT(%s) - BIGINT(%s)))",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    /**
     * Supported since Spark 3.1
     */
    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR(UNIX_MILLIS(%s) - UNIX_MILLIS(%s))",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    /**
     * Experienced with Databricks
     */
    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new OneDigitDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    /**
     * Experienced with Databricks
     */
    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return new OneDigitBooleanNormFunctionSymbolImpl(booleanType, dbStringType);
    }

    /**
     The JSON_TYPEOF function is not supported by SPARK. For now, we use a work-around instead, where we check if the
     JSON_ARRAY_LENGTH function can be called on the element.
     This is not perfectly robust, though. E.g. the string "[1, 2, 3]" would be interpreted as an array.
     */
    @Override
    protected DBBooleanFunctionSymbol createIsArray(DBTermType dbType) {
        return new DBBooleanFunctionSymbolWithSerializerImpl(
                "JSON_IS_ARRAY",
                ImmutableList.of(dbType),
                dbBooleanType,
                false,
                (terms, termConverter, termFactory) -> String.format(
                        "(JSON_ARRAY_LENGTH(%s) IS NOT NULL)",
                        termConverter.apply(terms.get(0))
                ));
    }

    /**
     * XSD CAST functions
     */
    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s NOT RLIKE " + numericPattern +
                        " THEN NULL ELSE CAST(%1$s AS DOUBLE) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s NOT RLIKE " + numericPattern + " THEN NULL " +
                        "WHEN (CAST(%1$s AS FLOAT) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS FLOAT) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS FLOAT) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS FLOAT) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s NOT RLIKE " + numericNonFPPattern + " THEN NULL " +
                        "ELSE CAST(%1$s AS "+ DECIMAL_38_10_STR +") END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (%1$s NOT RLIKE " + datePattern1 + " AND " +
                        "%1$s NOT RLIKE " + datePattern2 +" AND " +
                        "%1$s NOT RLIKE " + datePattern3 +" AND " +
                        "%1$s NOT RLIKE " + datePattern4 +" ) " +
                        " THEN NULL ELSE CAST(%1$s AS DATE) END",
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
        return String.format("(CASE WHEN CAST(%1$s AS "+ DECIMAL_38_10_STR +") = 0 THEN 'false' " +
                        "WHEN %1$s = '' THEN 'false' " +
                        "WHEN %1$s = 'NaN' THEN 'false' " +
                        "ELSE 'true' " +
                        "END)",
                term);
    }

    @Override
    protected DBFunctionSymbol createDBSample(DBTermType termType) {
        return new DBSampleFunctionSymbolImpl(termType, "FIRST");
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
        return String.format("(EXTRACT(SECOND FROM %s) * 1000)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMicroseconds(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR(EXTRACT(SECOND FROM %s) * 1000000)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    public DBFunctionSymbol getDBDateTrunc(String datePart) {
        if(ImmutableSet.of("microseconds", "milliseconds", "decade", "century", "millennium").contains(datePart.toLowerCase())) {
            throw new IllegalArgumentException(String.format("SparkSQL does not support DATE_TRUNC on %s.", datePart));
        }
        return super.getDBDateTrunc(datePart);
    }
}


