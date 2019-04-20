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
import static it.unibz.inf.ontop.model.type.impl.OracleDBTypeFactory.TIMESTAMP_LOCAL_TZ_STR;

public class OracleDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UNSUPPORTED_MSG = "Not supported by Oracle";
    private static final String RANDOM_STR = "DBMS_RANDOM.VALUE";

    @Inject
    protected OracleDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createOracleRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createOracleRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

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

        DBTermType dbDateType = dbTypeFactory.getDBTermType(DATE_STR);
        DBTypeConversionFunctionSymbol dateNormFunctionSymbol = new OracleDateNormFunctionSymbol(dbDateType, dbStringType);
        table.put(dbDateType, typeFactory.getDatatype(XSD.DATE), dateNormFunctionSymbol);

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
        return String.format("NVL(SUBSTR(%s,INSTR(%s,%s)+LENGTH(%s),SIGN(INSTR(%s,%s))*LENGTH(%s)),'')",
                str, str, after, after, str, after, str); //FIXME when no match found should return empty string
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

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
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

    /**
     * Made explicit, so as to enforce the use of the same character set
     */
    @Override
    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return new OracleCastToStringFunctionSymbol(inputType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createIntegerToStringCastFunctionSymbol(DBTermType inputType) {
        return new OracleCastIntegerToStringFunctionSymbol(inputType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDefaultCastToStringFunctionSymbol(DBTermType inputType) {
        return new OracleCastToStringFunctionSymbol(inputType, dbStringType);
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
        else
            return String.format(
                    "REPLACE(REPLACE(REGEXP_REPLACE(TO_CHAR(%s,'YYYY-MM-DD HH24:MI:SSxFF TZH:TZM'),' ','T',1,1),' ',''),',','.')",
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

    /**
     * TODO: fix it (dashes are not always inserted at the right places)
     */
    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(UUID_STR, uuid, dbStringType,
                (terms, termConverter, termFactory) ->
                        "REGEXP_REPLACE(RAWTOHEX(SYS_GUID()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\\1-\\2-\\3-\\4-\\5')"
                );
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Do not call getUUIDNameInDialect for Oracle");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        throw new RuntimeException("TODO: support");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        throw new RuntimeException("TODO: support");
    }
}
