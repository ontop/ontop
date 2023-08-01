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

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class BigQueryDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for BigQuery";

    private static final String REGEXP_CONTAINS_STR = "REGEXP_CONTAINS";
    private static final String TO_JSON = "TO_JSON";
    private static final String JSON_VALUE = "JSON_VALUE";
    private static final String JSON_VALUE_ARRAY = "JSON_VALUE_ARRAY";
    private DBBooleanFunctionSymbol regexpContains;

    @Inject
    protected BigQueryDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createBigQueryRegularFunctionTable(typeFactory), typeFactory);

        regexpContains = new DefaultSQLSimpleDBBooleanFunctionSymbol(REGEXP_CONTAINS_STR, 2, dbBooleanType,
                abstractRootDBType);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createBigQueryRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        table.remove(REGEXP_LIKE_STR, 2);
        table.remove(REGEXP_LIKE_STR, 3);
        DBBooleanFunctionSymbol regexpContains = new DefaultSQLSimpleDBBooleanFunctionSymbol(REGEXP_CONTAINS_STR, 2, dbBooleanType,
                abstractRootDBType);
        DBFunctionSymbol toJson = new DefaultSQLSimpleTypedDBFunctionSymbol(TO_JSON, 1, typeFactory.getDBTypeFactory().getDBJsonType(), true, abstractRootDBType);
        DBFunctionSymbol jsonValue = new DefaultSQLSimpleTypedDBFunctionSymbol(JSON_VALUE, 2, typeFactory.getDBTypeFactory().getDBStringType(), false, abstractRootDBType) {
            @Override
            protected boolean mayReturnNullWithoutNullArguments() {
                return true;
            }
        };
        DBFunctionSymbol jsonValueArray = new DefaultSQLSimpleTypedDBFunctionSymbol(
                JSON_VALUE_ARRAY, 2,
                typeFactory.getDBTypeFactory().getDBArrayType(typeFactory.getDBTypeFactory().getDBStringType()),
                false, abstractRootDBType) {
            @Override
            protected boolean mayReturnNullWithoutNullArguments() {
                return true;
            }
        };
        table.put(REGEXP_CONTAINS_STR, 2, regexpContains);
        table.put(TO_JSON, 1, toJson);
        table.put(JSON_VALUE, 2, jsonValue);
        table.put(JSON_VALUE_ARRAY, 2, jsonValueArray);

        return ImmutableTable.copyOf(table);
    }


    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(STRPOS(%s, %s) > 0)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("CASE STRPOS(%s, %s) WHEN 0 THEN '' ELSE SUBSTRING(%s,1,STRPOS(%s, %s)-1) END", str, before, str, str, before);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("CASE STRPOS(%s, %s) WHEN 0 THEN '' ELSE SUBSTRING(%s, STRPOS(%s, %s) + LENGTH(%s)) END", str, after, str, str, after, after);
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
        return String.format("TO_HEX(%s(%s))", functionName, termConverter.apply(terms.get(0)));

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
        return String.format("FORMAT_TIMESTAMP(\"%%Y-%%m-%%dT%%X%%Ez\", %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String getUUIDNameInDialect() {
        return "GENERATE_UUID";
    }


    /**
     * BigQuery uses 'True/False' for SQL queries, but '1/0' for results, so we need a way to parse these results.
     */
    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new OneDigitDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    /**
     * BigQuery uses 'True/False' for SQL queries, but '1/0' for results, so we need a way to parse these results.
     */
    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return new OneDigitBooleanNormFunctionSymbolImpl(booleanType, dbStringType);
    }


    //BigQuery does not support week as a unit for timestamp_diff
    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("TIMESTAMP_DIFF(week): " + NOT_YET_SUPPORTED_MSG);
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
        return String.format("TIMESTAMP_DIFF(%s, %s, %s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                timeUnit);
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return this.regexpContains;
    }

    @Override
    protected Optional<DBFunctionSymbol> createRoundFunctionSymbol(DBTermType dbTermType) {
        // TODO: taken from H2SQLDBFunctionSymbolFactory, so same TODO applies
        return Optional.of(new UnaryDBFunctionSymbolWithSerializerImpl(ROUND_STR, dbTermType, dbTermType, false,
                (terms, termConverter, termFactory) -> String.format(
                        "CAST(ROUND(%s) AS %s)", termConverter.apply(terms.get(0)), dbTermType.getCastName())));
    }

    @Override
    protected Optional<DBFunctionSymbol> createFloorFunctionSymbol(DBTermType dbTermType) {
        // TODO: taken from H2SQLDBFunctionSymbolFactory, so same TODO applies
        return Optional.of(new UnaryDBFunctionSymbolWithSerializerImpl(FLOOR_STR, dbTermType, dbTermType, false,
                (terms, termConverter, termFactory) -> String.format(
                        "CAST(FLOOR(%s) AS %s)", termConverter.apply(terms.get(0)), dbTermType.getCastName())));
    }

    @Override
    protected Optional<DBFunctionSymbol> createCeilFunctionSymbol(DBTermType dbTermType) {
        // TODO: taken from H2SQLDBFunctionSymbolFactory, so same TODO applies
        return Optional.of(new UnaryDBFunctionSymbolWithSerializerImpl(CEIL_STR, dbTermType, dbTermType, false,
                (terms, termConverter, termFactory) -> String.format(
                        "CAST(CEIL(%s) AS %s)", termConverter.apply(terms.get(0)), dbTermType.getCastName())));
    }

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "STRING_AGG(%s%s,%s ORDER BY %s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1)),
                        termConverter.apply(terms.get(0))
                ));
    }

    @Override
    protected DBFunctionSymbol createEncodeURLorIRI(boolean preserveInternationalChars) {
        return new BigQuerySQLEncodeURLorIRIFunctionSymbolImpl(dbStringType, preserveInternationalChars);
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
}
