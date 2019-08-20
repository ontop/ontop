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
import it.unibz.inf.ontop.model.type.impl.SQLServerDBTypeFactory;

import java.util.UUID;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.term.functionsymbol.db.impl.MySQLDBFunctionSymbolFactory.UUID_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.NTEXT_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TEXT_STR;

public class SQLServerDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String LEN_STR = "LEN";
    private static final String CEILING_STR = "CEILING";

    private static final String UNSUPPORTED_MSG = "Not supported by SQL server";

    // Created in init()
    private DBFunctionSymbol substr2FunctionSymbol;

    @Inject
    private SQLServerDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createSQLServerRegularFunctionTable(typeFactory), typeFactory);
    }

    @Override
    protected void init() {
        // Always call it first
        super.init();

        // Non-regular
        substr2FunctionSymbol = new DBFunctionSymbolWithSerializerImpl(SUBSTR_STR + "2",
                ImmutableList.of(abstractRootDBType, abstractRootDBType), dbStringType, false, this::serializeSubString2);
    }

    /**
     * Treats NULLs as empty strings
     */
    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullToleratingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    /**
     * Uses the operator +
     *
     * Assumes that the DB parameter CONCAT_NULL_YIELDS_NULL is ON
     */
    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol("CONCAT+", arity, dbStringType, abstractRootDBType,
                Serializers.getOperatorSerializer(ADD_STR));
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        builder.putAll(super.createNormalizationTable());

        // Other DB datetimes
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();

        DBTermType datetime = dbTypeFactory.getDBTermType(SQLServerDBTypeFactory.DATETIME_STR);
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(datetime);
        builder.put(datetime, xsdDatetime, datetimeNormFunctionSymbol);
        builder.put(datetime, xsdDatetimeStamp, datetimeNormFunctionSymbol);

        DBTermType datetimeOffset = dbTypeFactory.getDBTermType(SQLServerDBTypeFactory.DATETIMEOFFSET_STR);
        DBTypeConversionFunctionSymbol datetimeOffsetNormFunctionSymbol = createDateTimeNormFunctionSymbol(datetimeOffset);
        builder.put(datetimeOffset, xsdDatetime, datetimeOffsetNormFunctionSymbol);
        builder.put(datetimeOffset, xsdDatetimeStamp, datetimeOffsetNormFunctionSymbol);

        return builder.build();
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createSQLServerRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol strlenFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(LEN_STR, 1, dbIntType,
                false, abstractRootDBType);
        table.remove(CHAR_LENGTH_STR, 1);
        table.put(LEN_STR, 1, strlenFunctionSymbol);

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        // Removals not replaced by regular function symbols
        table.remove(SUBSTRING_STR, 2);
        table.remove(SUBSTR_STR, 2);

        return ImmutableTable.copyOf(table);
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(LEN_STR, 1);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(UUID_STR, uuid, dbStringType,
                (terms, termConverter, termFactory) ->
                        "LOWER(CONVERT(NVARCHAR(100),NEWID()))");
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Do not call getUUIDNameInDialect for SQL Server");
    }

    /**
     * TODO: update
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return super.getDBRegexpMatches2();
    }

    /**
     * TODO: update
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return super.getDBRegexpMatches3();
    }

    @Override
    public DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return getNullRejectingDBConcat(arity);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter,
                                       TermFactory termFactory) {
        return String.format("(CHARINDEX(%s,%s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("LEFT(%s,SIGN(CHARINDEX(%s,%s))* (CHARINDEX(%s,%s)-1))", str, before, str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        //FIXME when no match found should return empty string
        return String.format("SUBSTRING(%s,CHARINDEX(%s,%s)+LEN(%s),SIGN(CHARINDEX(%s,%s))*LEN(%s))",
                str, after, str, after, after, str, str);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(CONVERT(VARCHAR(40), HASHBYTES('MD5',%s),2))",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(CONVERT(VARCHAR(40), HASHBYTES('SHA1',%s),2))", termConverter.apply(terms.get(0)));

    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256',%s),2))",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("LOWER(CONVERT(VARCHAR(128), HASHBYTES('SHA2_512',%s),2))",
                termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: use a different implementation of the FunctionSymbol for simplifying in the presence of DATETIME (has no TZ)
     */
    @Override
    protected DBFunctionSymbol createTzFunctionSymbol() {
        return super.createTzFunctionSymbol();
    }

    /**
     * TODO: change strategy as it returns "00:00" when no timezone is specified instead of ""
     * If done on the string, then we could make the CAST between DB timestamps implicit
     * (DATEPART(TZ...) is not supported for DATETIME)
     */
    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                 Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CONVERT(nvarchar(5), DATEADD(minute, DATEPART(TZ, %s), 0), 114)",
                termConverter.apply(terms.get(0)));
    }

    /**
     * Asks the timezone to be included
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter,
                                           TermFactory termFactory) {
        return String.format("CONVERT(nvarchar(50),%s,127)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new SQLServerDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDBBooleanIfElseNull() {
        return new SQLServerBooleanDBIfElseNullFunctionSymbolImpl(dbBooleanType);
    }

    /**
     * TODO: try to support the fragment that reduces to REPLACE
     */
    @Override
    public DBFunctionSymbol getDBRegexpReplace3() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace4() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Cast made explicit when the input type is ntext or text as
     * «The data types text and varchar are incompatible in the equal to operator»
     */
    @Override
    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType,
                                                                                    DBTermType targetType) {
        switch (inputType.getName()) {
            case NTEXT_STR:
            case TEXT_STR:
                return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType,
                        Serializers.getCastSerializer(targetType)) {
                    // Trick: force it to be non-injective (to prevent it to be lifted above DISTINCT)
                    @Override
                    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
                        return false;
                    }
                };
            default:
                // Implicit cast
                return super.createStringToStringCastFunctionSymbol(inputType, targetType);
        }
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return new SQLServerTimestampISODenormFunctionSymbol(timestampType, dbStringType);
    }

    @Override
    protected String serializeYear(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("YEAR(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMonth(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("MONTH(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDay(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DAY(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeHours(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DATEPART(HOUR, %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMinutes(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DATEPART(MINUTE, %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSeconds(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DATEPART(SECOND, %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBFunctionSymbol createRoundFunctionSymbol(DBTermType dbTermType) {
        return new SQLServerRoundFunctionSymbol(dbTermType);
    }

    @Override
    protected DBFunctionSymbol createCeilFunctionSymbol(DBTermType dbTermType) {
        return new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(CEILING_STR, 1, dbTermType, false);
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        return substr2FunctionSymbol;
    }

    protected String serializeSubString2(ImmutableList<? extends ImmutableTerm> terms,
                                         Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String start = termConverter.apply(terms.get(1));
        return String.format("SUBSTRING(%s,%s,LEN(%s))", str, start, str);
    }
}
