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
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.UUID;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.term.functionsymbol.db.impl.MySQLDBFunctionSymbolFactory.UUID_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.DATE_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TIMESTAMP_STR;
import static it.unibz.inf.ontop.model.type.impl.OracleDBTypeFactory.NUMBER_STR;
import static it.unibz.inf.ontop.model.type.impl.OracleDBTypeFactory.TIMESTAMP_LOCAL_TZ_STR;

public class OracleDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UNSUPPORTED_MSG = "Not supported by Oracle";
    private static final String RANDOM_STR = "DBMS_RANDOM.VALUE";
    private static final String TO_CHAR_STR = "TO_CHAR";

    // Created in init()
    private DBFunctionSymbol dbRightFunctionSymbol;

    @Inject
    protected OracleDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createOracleRegularFunctionTable(typeFactory), typeFactory);
    }

    @Override
    protected void init() {
        super.init();
        dbRightFunctionSymbol = new SimpleTypedDBFunctionSymbolImpl(RIGHT_STR, 2, dbStringType, false,
                abstractRootDBType,
                ((terms, termConverter, termFactory) -> String.format("SUBSTR(%s,-1*%s)",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1)))));
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createOracleRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbStringType = dbTypeFactory.getDBStringType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));
        table.remove(RIGHT_STR, 2);

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        // Default TO_CHAR (unknown input type)
        DBFunctionSymbol toChar1FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(TO_CHAR_STR, 1, dbStringType,
                false, abstractRootDBType);
        table.put(TO_CHAR_STR, 1, toChar1FunctionSymbol);
        DBFunctionSymbol toChar2FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(TO_CHAR_STR, 2, dbStringType,
                false, abstractRootDBType);

        table.put(TO_CHAR_STR, 2, toChar2FunctionSymbol);

        return ImmutableTable.copyOf(table);
    }

    /**
     * TODO: shall we alert the user when TIMESTAMP is used with a XSD.DATETIMESTAMP?
     */
    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        Table<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> table = HashBasedTable.create();
        table.putAll(super.createNormalizationTable());

        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();
        DBTermType timestampLTzType = dbTypeFactory.getDBTermType(TIMESTAMP_LOCAL_TZ_STR);
        DBTypeConversionFunctionSymbol datetimeLTZNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestampLTzType);
        table.put(timestampLTzType, xsdDatetime, datetimeLTZNormFunctionSymbol);
        table.put(timestampLTzType, xsdDatetimeStamp, datetimeLTZNormFunctionSymbol);

        DBTermType timestampType = dbTypeFactory.getDBTermType(TIMESTAMP_STR);
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestampType);
        table.put(timestampType, xsdDatetime, datetimeNormFunctionSymbol);
        // No TZ for TIMESTAMP --> incompatible with XSD.DATETIMESTAMP

        // Date column in Oracle does include time information, too
        DBTermType dbDateType = dbTypeFactory.getDBTermType(DATE_STR);
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbolWoTz = createDateTimeNormFunctionSymbol(dbDateType);
        table.put(dbDateType, xsdDatetime, datetimeNormFunctionSymbolWoTz);
        DBTypeConversionFunctionSymbol dateNormFunctionSymbol = new OracleDateNormFunctionSymbol(dbDateType, dbStringType);
        table.put(dbDateType, typeFactory.getDatatype(XSD.DATE), dateNormFunctionSymbol);

        // NUMBER boolean normalization
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();
        DBTermType numberType = dbTypeFactory.getDBTermType(NUMBER_STR);
        table.put(numberType, xsdBoolean, new DefaultNumberNormAsBooleanFunctionSymbol(numberType, dbStringType));


        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDenormalizationTable() {
        Table<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> table = HashBasedTable.create();
        table.putAll(super.createDenormalizationTable());

        DBTermType dbDateType = dbTypeFactory.getDBTermType(DATE_STR);
        DBTypeConversionFunctionSymbol dateDenormFunctionSymbol = new OracleDateDenormFunctionSymbol(dbStringType, dbDateType);
        table.put(dbDateType, typeFactory.getDatatype(XSD.DATE), dateDenormFunctionSymbol);

        return ImmutableTable.copyOf(table);
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
    public DBFunctionSymbol getDBRight() {
        return dbRightFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(LENGTH_STR, 1);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBRand(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(RANDOM_STR, uuid, dbDoubleType,
                (terms, termConverter, termFactory) -> RANDOM_STR);
    }

    @Override
    protected String getRandNameInDialect() {
        throw new UnsupportedOperationException("getRandNameInDialect() must not be called for Oracle");
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(INSTR(%s,%s) > 0)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("NVL(SUBSTR(%s,0,INSTR(%s,%s)-1),'')", str, str, before);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        // NB: (%s IS NULL) here is for empty strings (NULL in Oracle...)
        return String.format("CASE WHEN %s IS NULL THEN %s ELSE NVL(SUBSTR(%s,INSTR(%s,%s)+LENGTH(%s),SIGN(INSTR(%s,%s))*LENGTH(%s)),'') END",
                after, str, str, str, after, after, str, after, str); //FIXME when no match found should return empty string
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(TO_CHAR(RAWTOHEX(SYS.DBMS_CRYPTO.HASH(UTL_I18N.STRING_TO_RAW(%s, 'AL32UTF8'), 2))))",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(TO_CHAR(RAWTOHEX(SYS.DBMS_CRYPTO.HASH(UTL_I18N.STRING_TO_RAW(%s, 'AL32UTF8'), SYS.DBMS_CRYPTO.HASH_SH1))))",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(TO_CHAR(RAWTOHEX(SYS.DBMS_CRYPTO.HASH(UTL_I18N.STRING_TO_RAW(%s, 'AL32UTF8'), SYS.DBMS_CRYPTO.HASH_SH256))))",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(TO_CHAR(RAWTOHEX(SYS.DBMS_CRYPTO.HASH(UTL_I18N.STRING_TO_RAW(%s, 'AL32UTF8'), SYS.DBMS_CRYPTO.HASH_SH512))))",
                termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: use a different implementation of the FunctionSymbol for simplifying in the presence of TIMESTAMP (has no TZ)
     * Currently: Oracle throws a fatal error
     */
    @Override
    protected DBFunctionSymbol createTzFunctionSymbol() {
        return super.createTzFunctionSymbol();
    }

    /**
     * TODO: reformat the number into 05:00 instead of 5:0
     */
    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN EXTRACT(TIMEZONE_HOUR FROM %s) IS NOT NULL \n" +
                "     THEN EXTRACT(TIMEZONE_HOUR FROM %s) || ':' || EXTRACT(TIMEZONE_MINUTE FROM %s)\n" +
                "     ELSE NULL\n" +
                "END", str, str, str);
    }

    @Override
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return "ROWNUM";
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return new OracleNullRejectingDBConcatFunctionSymbol(arity, dbStringType, abstractRootDBType);
    }

    /**
     * Treats NULLs as empty strings
     */
    @Override
    public DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullToleratingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);
    }

    /**
     * Treats NULLs as empty strings
     */
    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        if (arity != 2)
            throw new UnsupportedOperationException("CONCAT is a binary function in Oracle. Use || instead.");

        return new NullToleratingDBConcatFunctionSymbol(CONCAT_STR, 2, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        if (targetType.equals(dbStringType))
            return createDefaultCastToStringFunctionSymbol(abstractRootDBType);
        return super.createSimpleCastFunctionSymbol(targetType);
    }

    /**
     * Made explicit, so as to enforce the use of the same character set
     */
    @Override
    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return createDefaultCastToStringFunctionSymbol(inputType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createIntegerToStringCastFunctionSymbol(DBTermType inputType) {
        return new DefaultCastIntegerToStringFunctionSymbol(inputType, dbStringType,
                Serializers.getRegularSerializer(TO_CHAR_STR));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDefaultCastToStringFunctionSymbol(DBTermType inputType) {
        return new DefaultSimpleDBCastFunctionSymbol(inputType, dbStringType,
                Serializers.getRegularSerializer(TO_CHAR_STR));
    }

    @Override
    protected DBBooleanFunctionSymbol createDBBooleanCase(int arity, boolean doOrderingMatter) {
        return new WrappedDBBooleanCaseFunctionSymbolImpl(arity, dbBooleanType, abstractRootDBType, doOrderingMatter);
    }

    @Override
    protected DBIsNullOrNotFunctionSymbol createDBIsNull(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        return new ExpressionSensitiveSQLDBIsNullOrNotFunctionSymbolImpl(true, dbBooleanType, rootDBTermType);
    }

    @Override
    protected DBIsNullOrNotFunctionSymbol createDBIsNotNull(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        return new ExpressionSensitiveSQLDBIsNullOrNotFunctionSymbolImpl(false, dbBooleanType, rootDBTermType);
    }

    /**
     * Overrides
     */
    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType) {
        return new DefaultSQLTimestampISONormFunctionSymbol(
                dbDateTimestampType,
                dbStringType,
                (terms, converter, factory) -> serializeDateTimeNorm(dbDateTimestampType, terms, converter));
    }

    /**
     * NB: In some JDBC connection settings, returns a comma instead of a period.
     */
    protected String serializeDateTimeNorm(DBTermType dbDateTimestampType,
                                           ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter) {
        /*
         * TIMESTAMP by default does not support time zones
         */
    	if (dbDateTimestampType.equals(dbTypeFactory.getDBTermType(TIMESTAMP_STR))) {
            return String.format("REPLACE(REPLACE(TO_CHAR(%s,'YYYY-MM-DD HH24:MI:SSxFF'),' ','T'),',','.')", termConverter.apply(terms.get(0)));
        }
        // Timezone support
        else if (dbDateTimestampType.equals(dbTypeFactory.getDBTermType(DATE_STR))) {
            return String.format("REPLACE(REGEXP_REPLACE(TO_CHAR(CAST (%s AS TIMESTAMP WITH TIME ZONE),'YYYY-MM-DD HH24:MI:SSxFFTZH:TZM'),' ','T',1,1),',','.')", termConverter.apply(terms.get(0)));
        }
    	else
            return String.format(
                    "REPLACE(REPLACE(REGEXP_REPLACE(TO_CHAR(%s,'YYYY-MM-DD HH24:MI:SSxFFTZH:TZM'),' ','T',1,1),' ',''),',','.')",
                    termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("This method should not be called for Oracle");
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return new OracleTimestampISODenormFunctionSymbol(timestampType, dbStringType);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(UUID_STR, uuid, dbStringType,
                (terms, termConverter, termFactory) ->
                        "LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\\1-\\2-\\3-\\4-\\5'))"
                );
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Do not call getUUIDNameInDialect for Oracle");
    }

    /**
     * Time extension - duration arithmetic
     */

    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC(EXTRACT(DAY FROM %s - %s) / 7)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeWeeksBetweenFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((%s - %s)/7)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeDaysBetweenFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(DAY FROM %s - %s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeDaysBetweenFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("%s - %s",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(DAY FROM %s - %s) * 24 + " +
                        "EXTRACT(HOUR FROM %s - %s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(DAY FROM %s - %s) * 24 * 60 + " +
                        "EXTRACT(HOUR FROM %s - %s) * 60 + " +
                        "EXTRACT(MINUTE FROM %s - %s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(DAY FROM %s - %s) * 24 * 60 * 60 + " +
                        "EXTRACT(HOUR FROM %s - %s) * 60 * 60 + " +
                        "EXTRACT(MINUTE FROM %s - %s) * 60 + " +
                        "EXTRACT(SECOND FROM %s - %s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(EXTRACT(DAY FROM %s - %s) * 24 * 60 * 60 + " +
                        "EXTRACT(HOUR FROM %s - %s) * 60 * 60 + " +
                        "EXTRACT(MINUTE FROM %s - %s) * 60 + " +
                        "EXTRACT(SECOND FROM %s - %s))*1000",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }
}
