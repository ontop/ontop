package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsTrueFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.SQLServerDBTypeFactory;

import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.NTEXT_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TEXT_STR;

public class SQLServerDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UUID_STR = "NEWID";
    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";
    private static final String LEN_STR = "LEN";

    private static final String UNSUPPORTED_MSG = "Not supported by SQL server";

    @Inject
    private SQLServerDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createSQLServerRegularFunctionTable(typeFactory), typeFactory);
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        builder.putAll(super.createNormalizationTable());

        // Other DB datetimes
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();

        // TODO: get rid of the typeCode (meaningless)
        DBTermType datetime = dbTypeFactory.getDBTermType(0, SQLServerDBTypeFactory.DATETIME_STR);
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(datetime);
        builder.put(datetime, xsdDatetime, datetimeNormFunctionSymbol);
        builder.put(datetime, xsdDatetimeStamp, datetimeNormFunctionSymbol);

        DBTermType datetimeOffset = dbTypeFactory.getDBTermType(0, SQLServerDBTypeFactory.DATETIMEOFFSET_STR);
        DBTypeConversionFunctionSymbol datetimeOffsetNormFunctionSymbol = createDateTimeNormFunctionSymbol(datetimeOffset);
        builder.put(datetimeOffset, xsdDatetime, datetimeOffsetNormFunctionSymbol);
        builder.put(datetimeOffset, xsdDatetimeStamp, datetimeOffsetNormFunctionSymbol);

        return builder.build();
    }

    /**
     * TODO: update
     */
    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createSQLServerRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBBooleanFunctionSymbol regexpLike2 = new DefaultSQLSimpleDBBooleanFunctionSymbol(REGEXP_LIKE_STR, 2, dbBooleanType,
                abstractRootDBType);
        table.put(REGEXP_LIKE_STR, 2, regexpLike2);

        DBBooleanFunctionSymbol regexpLike3 = new DefaultSQLSimpleDBBooleanFunctionSymbol(REGEXP_LIKE_STR, 3, dbBooleanType,
                abstractRootDBType);
        table.put(REGEXP_LIKE_STR, 3, regexpLike3);

        DBFunctionSymbol strlenFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(LEN_STR, 1, dbIntType,
                false, abstractRootDBType);
        table.remove(CHAR_LENGTH_STR, 1);
        table.put(LEN_STR, 1, strlenFunctionSymbol);

        DBFunctionSymbol nowFunctionSymbol = new SQLServerCurrentTimestampFunctionSymbol(
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        return ImmutableTable.copyOf(table);
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(LEN_STR, 1);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter,
                                       TermFactory termFactory) {
        return String.format("CHARINDEX(%s,%s) > 0",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("LEFT(%s,CHARINDEX(%s,%s)-1)", str, before, str);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));

        // sign return 1 if positive number, 0 if 0, and -1 if negative number
        // it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,CHARINDEX(%s,%s) + LENGTH(%s), SIGN(CHARINDEX(%s,%s)) * LENGTH(%s))",
                str, after, str, after, after, str, str);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASH('SHA256', STRINGTOUTF8(%s), 1)", termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
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
     * TODO: update
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 2);
    }

    /**
     * TODO: update
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 3);
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
    protected String getUUIDNameInDialect() {
        return UUID_STR;
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
                return new DefaultSQLSimpleDBCastFunctionSymbol(inputType, targetType);
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
}
