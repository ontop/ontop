package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;


import java.util.function.Function;
import java.util.stream.Collectors;

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
        return String.format("(POSITION(%s IN %s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("LEFT(%s,POSITION(%s,%s)-1)", str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));

        // sign return 1 if positive number, 0 if 0, and -1 if negative number
        // it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,POSITION(%s,%s) + LENGTH(%s), SIGN(POSITION(%s,%s)) * LENGTH(%s))",
                str, after, str, after, after, str, str);
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
        Create a JSON path supported by SPARK (of the form '$.x.y.z').
     */
    private String serializePath(ImmutableList<String> path) {
        return "\'$."+
                path.stream()
                        .collect(Collectors.joining("."))
                +"\'";
    }

    /**
     Create a JSON path in human-readable format.
     */
    private String printPath(ImmutableList<String> path) {
        return path.stream()
                .collect(Collectors.joining("."));
    }

    /**
     Access JSON object through a given path using the GET_JSON_OBJECT function and then cast it to STRING.
     If no path is given, we just return the entire JSON object as a string.
     */
    @Override
    public DBFunctionSymbol getDBJsonEltAsText(ImmutableList<String> path) {
        if(path.size() == 0) {
            return new DBFunctionSymbolWithSerializerImpl(
                    "GET_JSON_OBJECT_TEXT",
                    ImmutableList.of(dbStringType),
                    dbStringType,
                    false,
                    (terms, termConverter, termFactory) -> String.format(
                            "CAST(%s AS STRING)",
                            termConverter.apply(terms.get(0))
                    ));
        }
        return new DBFunctionSymbolWithSerializerImpl(
                "GET_JSON_OBJECT_TEXT:" + printPath(path),
                ImmutableList.of(dbStringType),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> String.format(
                        "CAST(GET_JSON_OBJECT(%s, %s) AS STRING)",
                        termConverter.apply(terms.get(0)),
                        serializePath(path)
                ));
    }

    /**
     Access JSON object through a given path using the GET_JSON_OBJECT function and return it.
     If no path is given, we just return the entire JSON object as a string.
     */
    @Override
    public DBFunctionSymbol getDBJsonElt(ImmutableList<String> path) {
        if(path.size() == 0) {
            return new DBFunctionSymbolWithSerializerImpl(
                    "GET_JSON_OBJECT_TEXT",
                    ImmutableList.of(dbStringType),
                    dbStringType,
                    false,
                    (terms, termConverter, termFactory) -> String.format(
                            "CAST(%s AS STRING)",
                            termConverter.apply(terms.get(0))
                    ));
        }
        return new DBFunctionSymbolWithSerializerImpl(
                "GET_JSON_OBJECT:" + printPath(path),
                ImmutableList.of(dbStringType),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> String.format(
                        "GET_JSON_OBJECT(%s, %s)",
                        termConverter.apply(terms.get(0)),
                        serializePath(path)
                ));
    }

    /**
     The JSON_TYPEOF function is not supported by SPARK. For now, we use a work-around instead, where we just try
     to cast the element to the given name.
     This is not perfectly robust, though. E.g. the string "4" would be interpreted as an integer this way.
     */
    private DBBooleanFunctionSymbol createJsonHasType(String functionName, String typeName) {
        return new DBBooleanFunctionSymbolWithSerializerImpl(
                functionName,
                ImmutableList.of(dbStringType),
                dbBooleanType,
                false,
                (terms, termConverter, termFactory) ->
                        String.format(
                                "%s(%s) IS NOT NULL",
                                typeName,
                                termConverter.apply(terms.get(0))
                        ));
    }

    /**
     The JSON_TYPEOF function is not supported by SPARK. For now, we use a work-around instead, where we check if an
     element can be expressed as a JSON-array or JSON-object.
     This is not perfectly robust, though. E.g. the string "[1, 2, 3]" would be interpreted as a non-scalar.
     */
    @Override
    protected DBBooleanFunctionSymbol createJsonIsScalar(DBTermType dbType) {
        return new DBBooleanFunctionSymbolWithSerializerImpl(
                "JSON_IS_SCALAR",
                ImmutableList.of(dbType),
                dbBooleanType,
                false,
                (terms, termConverter, termFactory) -> String.format(
                        "(JSON_ARRAY_LENGTH(%s) IS NULL AND JSON_OBJECT_KEYS(%s) IS NULL)",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(0))
                ));
    }

    @Override
    protected DBBooleanFunctionSymbol createJsonIsBoolean(DBTermType dbType) {
        return createJsonHasType("JSON_IS_BOOLEAN", "BOOLEAN");
    }

    @Override
    protected DBBooleanFunctionSymbol createJsonIsNumber(DBTermType dbType) {
        return createJsonHasType("JSON_IS_NUMBER", "DOUBLE");
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
}


