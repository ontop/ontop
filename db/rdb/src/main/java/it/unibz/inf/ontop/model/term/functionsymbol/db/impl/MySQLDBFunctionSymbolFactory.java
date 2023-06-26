package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TIMESTAMP_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.VARBINARY_STR;
import static it.unibz.inf.ontop.model.type.impl.MySQLDBTypeFactory.BIT_STR;
import static it.unibz.inf.ontop.model.type.impl.MySQLDBTypeFactory.YEAR_STR;

public class MySQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    protected static final String UUID_STR = "UUID";
    protected static final String  CURRENT_TZ_STR =
            "REPLACE(TIME_FORMAT(TIMEDIFF(NOW(),CONVERT_TZ(NOW(),@@session.time_zone,'+00:00')),'+%H:%i'),'+-','-')";
    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";

    private static final String UNSUPPORTED_MSG = "Not supported by MySQL";
    private final DatabaseInfoSupplier databaseInfoSupplier;

    @Inject
    protected MySQLDBFunctionSymbolFactory(TypeFactory typeFactory, DatabaseInfoSupplier databaseInfoSupplier) {
        super(createMySQLRegularFunctionTable(typeFactory), typeFactory);
        this.databaseInfoSupplier = databaseInfoSupplier;
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createMySQLRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        return ImmutableTable.copyOf(table);
    }

    /**
     * We know that the normalization function DATETIME -> xsd:datetimeStamp will always be invalid
     *   (it is not bound to any timezone).
     * TODO: how to inform the user? In a mapping it would be invalid, but what about a cast in a SPARQL query?
     */
    @Override
    protected ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> createNormalizationMap() {
        Map<DBTermType, DBTypeConversionFunctionSymbol> map = new HashMap<>();
        map.putAll(super.createNormalizationMap());

        // TIMESTAMP is not the default
        DBTermType timestamp = dbTypeFactory.getDBTermType(TIMESTAMP_STR);

        DBTypeConversionFunctionSymbol timestampNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestamp);
        map.put(timestamp, timestampNormFunctionSymbol);

        // BIT(1) boolean normalization
        DBTermType bitOne = dbTypeFactory.getDBTermType(BIT_STR, 1);
        map.put(bitOne, new DefaultNumberNormAsBooleanFunctionSymbol(bitOne, dbStringType));

        // Forbids the post-processing of YEAR_TO_TEXT as the JDBC driver converts strangely the YEAR
        DBTermType year = dbTypeFactory.getDBTermType(YEAR_STR);
        map.put(year, new NonPostProcessedSimpleDBCastFunctionSymbol(year, dbStringType,
                Serializers.getCastSerializer(dbStringType)));

        DBTermType varBinary = dbTypeFactory.getDBTermType(VARBINARY_STR);
        map.put(varBinary, createHexBinaryNormFunctionSymbol(varBinary));

        return ImmutableMap.copyOf(map);
    }

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "GROUP_CONCAT(%s%s SEPARATOR %s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1))
                ));
    }

    @Override
    protected String serializeHexBinaryNorm(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("UPPER(HEX(%s))", termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: provide a MySQL specific implementation
     */
    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return super.createDateTimeDenormFunctionSymbol(timestampType);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("INSTR(%s,%s) > 0",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format("LEFT(%s,INSTR(%s,%s)-1)", str,  str, before);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        // sign return 1 if positive number, 0 if 0 and -1 if negative number
        // it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,LOCATE(%s,%s) + LENGTH(%s), SIGN(LOCATE(%s,%s)) * LENGTH(%s))",
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

    /**
     * Tricky as this information may be lost while converting SPARQL constants into DB ones
     */
    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    /**
     * Only for >= 8.0.2
     *
     * TODO: provide an alternative implementation for the row unique str for previous versions
     *
     */
    @Override
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return super.serializeDBRowNumber(converter, termFactory);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType) {
        // TODO: check if it is safe to allow the decomposition
        return new DecomposeStrictEqualitySQLTimestampISONormFunctionSymbol(
                dbDateTimestampType,
                dbStringType,
                (terms, converter, factory) -> serializeDateTimeNorm(dbDateTimestampType, terms, converter));
    }

    /**
     * For DATETIME, never provides a time zone.
     * For TIMESTAMP, provides the session time zone used for generating the string
     *  (NB: TIMESTAMP is stored as a duration from a fixed date time)
     *
     */
    protected String serializeDateTimeNorm(DBTermType dbDateTimestampType,
                                           ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter) {

        String dateTimeStringWithoutTz = String.format("REPLACE(CAST(%s AS CHAR(30)),' ', 'T')",
                termConverter.apply(terms.get(0)));

        return dbDateTimestampType.getName().equals(TIMESTAMP_STR)
                ? String.format("CONCAT(%s,%s)", dateTimeStringWithoutTz, CURRENT_TZ_STR)
                : dateTimeStringWithoutTz;
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("This method should not be called for MySQL");
    }

    /**
     * MySQL only supports n-ary CONCAT but not operators like || and +
     */
    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return (DBConcatFunctionSymbol) getRegularDBFunctionSymbol(CONCAT_STR, arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    /**
     * Made Implicit
     */
    protected DBTypeConversionFunctionSymbol createDatetimeToDatetimeCastFunctionSymbol(DBTermType inputType,
                                                                                        DBTermType targetType) {
        return new DefaultImplicitDBCastFunctionSymbol(inputType, targetType);
    }

    @Override
    protected DBFunctionSymbol createEncodeURLorIRI(boolean preserveInternationalChars) {
        return new MySQLEncodeURLorIRIFunctionSymbolImpl(dbStringType, preserveInternationalChars);
    }

    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STR;
    }

    /**
     * NB: For MySQL >= 8, REGEXP_LIKE could be used
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return new DBBooleanFunctionSymbolWithSerializerImpl("REGEXP_MATCHES_2",
                ImmutableList.of(abstractRootDBType, abstractRootDBType), dbBooleanType, false,
                (terms, termConverter, termFactory) -> String.format(
                        // NB: BINARY is for making it case sensitive & CAST necessary since v8.0.22
                        "(CAST(%s AS BINARY) REGEXP BINARY %s)",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1))));
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return new DBBooleanFunctionSymbolWithSerializerImpl("REGEXP_MATCHES_3",
                ImmutableList.of(abstractRootDBType, abstractRootDBType, abstractRootDBType), dbBooleanType, false,
                this::serializeDBRegexpMatches3);
    }

    /**
     * TODO: throw an exception when the version is detected to be < 8 and reaching the "default" case?
     */
    protected String serializeDBRegexpMatches3(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String string = termConverter.apply(terms.get(0));
        String pattern = termConverter.apply(terms.get(1));
        ImmutableTerm flagTerm = terms.get(2);
        if (flagTerm instanceof DBConstant) {
            String flags = ((DBConstant) flagTerm).getValue();
            switch (flags) {
                // Case sensitive
                case "":
                    return String.format("(CAST(%s AS BINARY) REGEXP BINARY %s)", string, pattern);
                // Case insensitive
                case "i":
                    // TODO: is it robust to collation?
                    return String.format("(%s REGEXP %s)", string, pattern);
                default:
                    break;
            }
        }

        // REGEXP_LIKE is only supported by MySQL >= 8
        return getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 3)
                .getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("ROUND(TIMESTAMPDIFF(MICROSECOND, %s, %s)/1000)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    /**
     * XSD CAST functions
     */

    // Cast and regex differ in MySQL version 8 and above vs. previous versions
    private boolean isMySQLVersion8OrAbove() {
        return databaseInfoSupplier.getDatabaseVersion().isPresent() &&
                databaseInfoSupplier.getDatabaseVersion()
                        .map(s -> Integer.parseInt(s.substring(0, s.indexOf("."))))
                        .filter(s -> s > 7 ).isPresent();
    }

    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        if (isMySQLVersion8OrAbove()) {
            return String.format("CASE WHEN %1$s NOT REGEXP" + numericPattern +
                            " THEN NULL ELSE CAST(%1$s + 0.0 AS DOUBLE) END",
                    term); }
        else {
            return String.format("CASE WHEN %1$s NOT REGEXP BINARY" + numericPattern +
                            " THEN NULL ELSE %1$s + 0.0 END",
                    term); }
    }

    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        if (isMySQLVersion8OrAbove()) {
            return String.format("CASE WHEN %1$s NOT REGEXP " + numericPattern +
                            " THEN NULL ELSE CAST(%1$s + 0.0 AS FLOAT) END",
                    term);
        } else {
            return String.format("CASE WHEN %1$s NOT REGEXP BINARY " + numericPattern +
                            " THEN NULL ELSE %1$s + 0.0 END",
                    term);
        }
    }

    @Override
    protected String serializeCheckAndConvertFloatFromNonFPNumeric(ImmutableList<? extends ImmutableTerm> terms,
                                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (CAST(%1$s AS DECIMAL(60,30)) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS DECIMAL(60,30)) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS DECIMAL(60,30)) != 0) THEN NULL " +
                        "ELSE CAST(%1$s + 0.0 AS FLOAT) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        if (isMySQLVersion8OrAbove()) {
            return String.format("CASE WHEN %1$s NOT REGEXP " + numericNonFPPattern +
                            " THEN NULL ELSE CAST(%1$s AS DECIMAL(60,30)) END",
                    term);
        } else {
            return String.format("CASE WHEN %1$s NOT REGEXP BINARY " + numericNonFPPattern +
                            " THEN NULL ELSE CAST(%1$s AS DECIMAL(60,30)) END",
                    term);
        }
    }

    // SIGNED as a datatype cast truncates scale. This workaround addresses the issue.
    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        if (isMySQLVersion8OrAbove()) {
            return String.format("IF(%1$s REGEXP '[^0-9]+$', NULL , " +
                            "FLOOR(ABS(CAST(%1$s AS DECIMAL(60,30))))  * SIGN(CAST(%1$s AS DECIMAL(60,30)))) ",
                    term);
        } else {
            return String.format("IF(%1$s REGEXP '[^0-9]+$', %1$s RLIKE(if(1=1,')','a')) , " +
                            "FLOOR(ABS(CAST(%1$s AS DECIMAL(60,30)))) * SIGN(CAST(%1$s AS DECIMAL(60,30)))) ",
                    term);
        }
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
        return String.format("(CASE WHEN CAST(%1$s AS DECIMAL(60,30)) = 0 THEN 'false' " +
                        "WHEN %1$s = '' THEN 'false' " +
                        "WHEN %1$s = 'NaN' THEN 'false' " +
                        "ELSE 'true' " +
                        "END)",
                term);
    }

    // TRIM removes trailing 0-s
    @Override
    protected String serializeCheckAndConvertStringFromDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("TRIM(CASE WHEN MOD(%1$s,1) = 0 THEN CAST((%1$s) AS DECIMAL) " +
                        "ELSE %1$s " +
                        "END)+0",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateTimeFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(%s AS DATETIME)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        if (isMySQLVersion8OrAbove()) {
            return String.format("CASE WHEN (%1$s NOT REGEXP " + datePattern1 + " AND " +
                            "%1$s NOT REGEXP " + datePattern2 +" AND " +
                            "%1$s NOT REGEXP " + datePattern3 +" AND " +
                            "%1$s NOT REGEXP " + datePattern4 +" ) " +
                            " THEN NULL ELSE CAST(%1$s AS DATE) END",
                    term);
        } else {
            return String.format("CASE WHEN (%1$s NOT REGEXP BINARY " + datePattern1 + " AND " +
                            "%1$s NOT REGEXP BINARY " + datePattern2 +" AND " +
                            "%1$s NOT REGEXP BINARY " + datePattern3 +" AND " +
                            "%1$s NOT REGEXP BINARY " + datePattern4 +" ) " +
                            " THEN NULL ELSE CAST(%1$s AS DATE) END",
                    term);
        }
    }

    @Override
    public DBBooleanFunctionSymbol getDBIsArray(DBTermType dbType) {
        return new DBBooleanFunctionSymbolWithSerializerImpl(
                "JSON_IS_ARRAY",
                ImmutableList.of(typeFactory.getDBTypeFactory().getDBJsonType()),
                dbBooleanType,
                false,
                (terms, termConverter, termFactory) -> String.format(
                        "json_type(%s) = 'ARRAY'",
                        termConverter.apply(terms.get(0))
                ));
    }
}
