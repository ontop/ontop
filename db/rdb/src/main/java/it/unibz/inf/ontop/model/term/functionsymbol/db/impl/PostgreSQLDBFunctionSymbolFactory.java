package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


import static it.unibz.inf.ontop.model.term.functionsymbol.db.impl.MySQLDBFunctionSymbolFactory.UUID_STR;
import static it.unibz.inf.ontop.model.type.impl.PostgreSQLDBTypeFactory.*;

public class PostgreSQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String RANDOM_STR = "RANDOM";
    private static final String POSITION_STR = "POSITION";

    private final DBTermType dbJsonType;
    private final DBTermType dbJsonBType;

    @Inject
    protected PostgreSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createPostgreSQLRegularFunctionTable(typeFactory), typeFactory);
        this.dbJsonType = dbTypeFactory.getDBTermType(JSON_STR);
        this.dbJsonBType = dbTypeFactory.getDBTermType(JSONB_STR);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createPostgreSQLRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        DBTermType dbStringType = dbTypeFactory.getDBStringType();
        DBTermType dbInt4 = dbTypeFactory.getDBTermType(INTEGER_STR);

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol positionFunctionSymbol = new SimpleTypedDBFunctionSymbolImpl(POSITION_STR, 2,
                dbTypeFactory.getDBLargeIntegerType(), false, abstractRootDBType,
                (terms, termConverter, termFactory) -> String.format("position(%s in %s)",
                        termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1))));
        table.put(POSITION_STR, 2, positionFunctionSymbol);

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        DBFunctionSymbol substr2FunctionSymbol = new DBFunctionSymbolWithSerializerImpl(
                SUBSTR_STR + "2",
                ImmutableList.of(dbStringType, dbInt4),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> {
                    // PostgreSQL does not tolerate bigint as argument (int8), just int4 (integer)
                    ImmutableTerm newTerm1 = termFactory.getDBCastFunctionalTerm(dbInt4, terms.get(1)).simplify();

                    return String.format("substr(%s,%s)", termConverter.apply(terms.get(0)), termConverter.apply(newTerm1));
                });
        table.put(SUBSTR_STR, 2, substr2FunctionSymbol);

        DBFunctionSymbol substr3FunctionSymbol = new DBFunctionSymbolWithSerializerImpl(
                SUBSTR_STR + "3",
                ImmutableList.of(dbStringType, dbInt4, dbInt4),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> {
                    // PostgreSQL does not tolerate bigint as argument (int8), just int4 (integer)
                    ImmutableTerm newTerm1 = termFactory.getDBCastFunctionalTerm(dbInt4, terms.get(1)).simplify();
                    ImmutableTerm newTerm2 = termFactory.getDBCastFunctionalTerm(dbInt4, terms.get(2)).simplify();

                    return String.format("substr(%s,%s,%s)",
                        termConverter.apply(terms.get(0)), termConverter.apply(newTerm1), termConverter.apply(newTerm2));
                });
        table.put(SUBSTR_STR, 3, substr3FunctionSymbol);

        table.remove(REGEXP_LIKE_STR, 2);
        table.remove(REGEXP_LIKE_STR, 3);

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> createNormalizationMap() {
        ImmutableMap.Builder<DBTermType, DBTypeConversionFunctionSymbol> builder = ImmutableMap.builder();
        builder.putAll(super.createNormalizationMap());

        //TIMESTAMP
        DBTermType timeStamp = dbTypeFactory.getDBTermType(TIMESTAMP_STR);
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(timeStamp);
        builder.put(timeStamp, datetimeNormFunctionSymbol);

        //TIMETZ
        DBTermType timeTZType = dbTypeFactory.getDBTermType(TIMETZ_STR);
        // Takes care of putting
        DefaultTimeTzNormalizationFunctionSymbol timeTZNormFunctionSymbol = new DefaultTimeTzNormalizationFunctionSymbol(
                timeTZType, dbStringType,
                (terms, termConverter, termFactory) -> String.format(
                        "REGEXP_REPLACE(CAST(%s AS TEXT),'([-+]\\d\\d)$', '\\1:00')", termConverter.apply(terms.get(0))));
        builder.put(timeTZType, timeTZNormFunctionSymbol);

        return builder.build();
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        builder.putAll(super.createNormalizationTable());

        //GEOMETRY
        DBTermType defaultDBGeometryType = dbTypeFactory.getDBGeometryType();
        DBTypeConversionFunctionSymbol geometryNormFunctionSymbol = createGeometryNormFunctionSymbol(defaultDBGeometryType);
        builder.put(defaultDBGeometryType,typeFactory.getWktLiteralDatatype(), geometryNormFunctionSymbol);

        //GEOGRAPHY - Data type exclusive to PostGIS
        DBTermType defaultDBGeographyType = dbTypeFactory.getDBGeographyType();
        DBTypeConversionFunctionSymbol geographyNormFunctionSymbol = createGeometryNormFunctionSymbol(defaultDBGeographyType);
        builder.put(defaultDBGeographyType,typeFactory.getWktLiteralDatatype(), geographyNormFunctionSymbol);

        return builder.build();
    }

    @Override
    public DBFunctionSymbol getDBJsonEltAsText(ImmutableList<String> path) {
        return new DBFunctionSymbolWithSerializerImpl(
                "JSON_GET_ELT_AS_TEXT:"+printPath(path),
                ImmutableList.of(
                        dbJsonType
                ),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> String.format(
                        "%s#>>%s",
                        termConverter.apply(terms.get(0)),
                        serializePath(path)
                ));
    }

    @Override
    public DBFunctionSymbol getDBJsonElt(ImmutableList<String> path) {
        return new DBFunctionSymbolWithSerializerImpl(
                "JSON_GET_ELT:"+printPath(path),
                ImmutableList.of(
                        dbJsonType
                ),
                dbJsonType,
                false,
                (terms, termConverter, termFactory) -> String.format(
                        "%s#>%s",
                        termConverter.apply(terms.get(0)),
                        serializePath(path)
                ));
    }

    private String serializePath(ImmutableList<String> path) {
        return "'{" + String.join(",", path) + "}'";
    }

    private String printPath(ImmutableList<String> path) {
        return String.join(".", path);
    }

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "string_agg(%s%s,%s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(
                                termFactory.getDBCastFunctionalTerm(
                                        dbTypeFactory.getDBStringType(),
                                        terms.get(0)
                                )),
                        termConverter.apply(terms.get(1))
                ));
    }

    /**
     * Requires sometimes to type NULLs
     */
    @Override
    protected DBFunctionSymbol createTypeNullFunctionSymbol(DBTermType termType) {
        // Cannot CAST to SERIAL --> CAST to INTEGER instead
        if (termType.getCastName().equals(SERIAL_STR))
            return new NonSimplifiableTypedNullFunctionSymbol(termType, dbTypeFactory.getDBTermType(INTEGER_STR));
        else
            return new NonSimplifiableTypedNullFunctionSymbol(termType);
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
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new OneLetterDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createIsArray(DBTermType dbTermType) {
        if (dbTermType.equals(dbJsonType)) {
            return new DBBooleanFunctionSymbolWithSerializerImpl(
                    "JSON_IS_ARRAY",
                    ImmutableList.of(dbJsonType),
                    dbBooleanType,
                    false,
                    (terms, termConverter, termFactory) -> String.format(
                            "json_typeof(%s) = 'array'",
                            termConverter.apply(terms.get(0))
                    ));
        }

        if (dbTermType.equals(dbJsonBType)) {
            return new DBBooleanFunctionSymbolWithSerializerImpl(
                    "JSONB_IS_ARRAY",
                    ImmutableList.of(dbJsonBType),
                    dbBooleanType,
                    false,
                    (terms, termConverter, termFactory) -> String.format(
                            "jsonb_typeof(%s) = 'array'",
                            termConverter.apply(terms.get(0))
                    ));
        }
        throw new UnsupportedOperationException("Unsupported nested datatype: " + dbTermType.getName());
    }

    @Override
    protected DBBooleanFunctionSymbol createJsonIsBoolean(DBTermType dbType) {
        return createJsonHasType("JSON_IS_BOOLEAN", dbType, ImmutableList.of("boolean"));
    }

    @Override
    protected DBBooleanFunctionSymbol createJsonIsScalar(DBTermType dbType) {
        return createJsonHasType("JSON_IS_SCALAR", dbType, ImmutableList.of("boolean", "string", "number"));
    }

    @Override
    protected DBBooleanFunctionSymbol createJsonIsNumber(DBTermType dbType) {
        return createJsonHasType("JSON_IS_NUMBER", dbType, ImmutableList.of("number"));
    }

    private DBBooleanFunctionSymbol createJsonHasType(String functionName, DBTermType jsonLikeType, ImmutableList<String> types) {
        String typeOfFunctionString;
        if (jsonLikeType.equals(dbJsonType)) {
            typeOfFunctionString = "json_typeof";
        }
        else if (jsonLikeType.equals(dbJsonBType)) {
            typeOfFunctionString = "jsonb_typeof";
        }
        else
            throw new UnsupportedOperationException("Unsupported JSON-like type: " + jsonLikeType.getName());

        return new DBBooleanFunctionSymbolWithSerializerImpl(
                functionName,
                ImmutableList.of(dbJsonType),
                dbBooleanType,
                false,
                (terms, termConverter, termFactory) ->
                        String.format(
                                "%s(%s) IN (%s) ",
                                typeOfFunctionString,
                                termConverter.apply(terms.get(0)),
                                types.stream()
                                        .map(t -> "'" + t+ "'")
                                        .collect(Collectors.joining(","))
                        ));
    }

    /**
     * TODO: find a way to use the stored TZ instead of the local one
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // Enforces ISO 8601: https://stackoverflow.com/questions/38834022/turn-postgres-date-representation-into-iso-8601-string
        // However, use the local TZ instead of the stored TZ
        return String.format("TO_JSON(%s)#>>'{}'", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return new OneLetterBooleanNormFunctionSymbolImpl(booleanType, dbStringType);
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
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(UUID_STR, uuid, dbStringType,
                (terms, termConverter, termFactory) ->
                        "MD5(RANDOM()::text || CLOCK_TIMESTAMP()::text)::uuid");
    }

    @Override
    protected String getRandNameInDialect() {
        return RANDOM_STR;
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Should not be used for PostgreSQL");
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
        return String.format("LEFT(%s,CAST (SIGN(POSITION(%s IN %s))*(POSITION(%s IN %s)-1) AS INTEGER))", str, before, str, before, str);
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

    /**
     * Requires pgcrypto to be enabled (CREATE EXTENSION pgcrypto)
     */
    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("encode(digest(%s, 'sha1'), 'hex')", termConverter.apply(terms.get(0)));
    }

    /**
     * Requires pgcrypto to be enabled (CREATE EXTENSION pgcrypto)
     */
    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("encode(digest(%s, 'sha256'), 'hex')", termConverter.apply(terms.get(0)));
    }

    /**
     * Requires pgcrypto to be enabled (CREATE EXTENSION pgcrypto)
     */
    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("encode(digest(%s, 'sha384'), 'hex')", termConverter.apply(terms.get(0)));
    }

    /**
     * Requires pgcrypto to be enabled (CREATE EXTENSION pgcrypto)
     */
    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("encode(digest(%s, 'sha512'), 'hex')", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("(LPAD(EXTRACT(TIMEZONE_HOUR FROM %s)::text,2,'0') || ':' || LPAD(EXTRACT(TIMEZONE_MINUTE FROM %s)::text,2,'0'))", str, str);
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return new DBBooleanFunctionSymbolWithSerializerImpl(REGEXP_LIKE_STR + "2",
                ImmutableList.of(abstractRootDBType, abstractRootDBType), dbBooleanType, false,
                Serializers.getOperatorSerializer("~"));
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return new DBBooleanFunctionSymbolWithSerializerImpl(REGEXP_LIKE_STR + "3",
                ImmutableList.of(abstractRootDBType, abstractRootDBType, abstractRootType), dbBooleanType, false,
                /*
                 * TODO: is it safe to assume the flags are not empty?
                 */
                ((terms, termConverter, termFactory) -> {
                    /*
                     * Normalizes the flag
                     *   - DOT_ALL: s -> n
                     */
                    ImmutableTerm flagTerm = termFactory.getDBReplace(terms.get(2),
                            termFactory.getDBStringConstant("s"),
                            termFactory.getDBStringConstant("n"));

                    ImmutableTerm extendedPatternTerm = termFactory.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
                            termFactory.getDBStringConstant("(?"),
                            flagTerm,
                            termFactory.getDBStringConstant(")"),
                            terms.get(1)))
                            .simplify();


                    return String.format("%s ~ %s",
                            termConverter.apply(terms.get(0)),
                            termConverter.apply(extendedPatternTerm));
                }));
    }

    /**
     * Cast made explicit when the input type is char
     */
    @Override
    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType,
                                                                                    DBTermType targetType) {
        switch (inputType.getName()) {
            case CHAR_STR:
                return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType,
                        Serializers.getCastSerializer(targetType));
            default:
                // Implicit cast
                return super.createStringToStringCastFunctionSymbol(inputType, targetType);
        }
    }

    /**
     * Time extension - duration arithmetic
     */

    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/604800)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/86400)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/3600)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/60)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s)))",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CEIL((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))*1000)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeHexBinaryNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("upper(encode(%s, 'hex'))", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeHexBinaryDenorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("decode(%s, 'hex')", termConverter.apply(terms.get(0)));
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace3() {
        return new DBFunctionSymbolWithSerializerImpl("DB_REGEXP_REPLACE_3",
                ImmutableList.of(abstractRootDBType, abstractRootDBType, abstractRootDBType), dbStringType, false,
                ((terms, termConverter, termFactory) -> String.format("regexp_replace(%s,%s,%s,'g')",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1)),
                        termConverter.apply(terms.get(2)))));
    }

    /**
     * CAST functions
     */
    // NOTE: Special handling for NaN
    @Override
    protected String serializeCheckAndConvertBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN CAST(%1$s AS DECIMAL) = 0 THEN 'false' " +
                        "WHEN %1$s = '' THEN 'false' " +
                        "WHEN CAST(%1$s AS DECIMAL) = 'NaN'::NUMERIC THEN 'false' " +
                        "ELSE 'true' " +
                        "END)",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s !~ " + numericPattern +
                        " THEN NULL ELSE CAST(%1$s AS DOUBLE PRECISION) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s !~ " + numericPattern + " THEN NULL " +
                        "WHEN (CAST(%1$s AS FLOAT) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS FLOAT) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS FLOAT) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS FLOAT) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloatFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CASE WHEN %s THEN 1 ELSE 0 END",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDecimalFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s='1' THEN 1.0 " +
                        "WHEN %1$s THEN 1.0 " +
                        "WHEN %1$s='0' THEN 0.0 " +
                        "WHEN NOT %1$s THEN 0.0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s ~ '^-?([0-9]+[.]?[0-9]*|[.][0-9]+)$' THEN " +
                        "CAST(FLOOR(ABS(CAST(%1$s AS DECIMAL))) * SIGN(CAST(%1$s AS DECIMAL)) AS INTEGER) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DATE(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (%1$s !~ " + datePattern1 + " AND " +
                        "%1$s !~ " + datePattern2 +" AND " +
                        "%1$s !~ " + datePattern3 +" AND " +
                        "%1$s !~ " + datePattern4 +" ) " +
                        " THEN NULL ELSE DATE(%1$s) END",
                term);
    }

    @Override
    public DBFunctionSymbol getDBDateTrunc(String datePart) {
        if(ImmutableSet.of("microsecond", "millisecond").contains(datePart.toLowerCase())) {
            throw new IllegalArgumentException("PostgreSQL does not support DATE_TRUNC on 'millisecond' or 'microsend'. Use 'milliseconds' or 'microseconds' instead.");
        }
        return super.getDBDateTrunc(datePart);
    }

}
