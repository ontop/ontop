package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

public class DenodoDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for Denodo";

    @Inject
    protected DenodoDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDenodoRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDenodoRegularFunctionTable(
            TypeFactory typeFactory) {

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol strlenFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol("LEN", 1, dbTypeFactory.getDBLargeIntegerType(),
                false, abstractRootDBType);
        table.put(CHAR_LENGTH_STR, 1, strlenFunctionSymbol);

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        return ImmutableTable.copyOf(table);
    }

    @Override
    public DBFunctionSymbol getDBRight() {
        return new SimpleTypedDBFunctionSymbolImpl(RIGHT_STR, 2, dbStringType, false,
                abstractRootDBType,
                (terms, converter, factory) -> serializeDBRight(terms, converter));
    }

    @Override
    public DBBooleanFunctionSymbol getDBStartsWith() {
        return new DenodoDBStrStartsWithFunctionSymbol(abstractRootDBType, dbBooleanType);
    }

    private String serializeDBRight(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter) {
        String str = termConverter.apply(terms.get(0));
        String length = termConverter.apply(terms.get(1));
        return String.format("SUBSTR(%s,LEN(%s)-%s+1)", str, str, length);
    }

    private String serializeDBStartsWith(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter) {
        String str = termConverter.apply(terms.get(0));
        String sbstr = termConverter.apply(terms.get(1));
        return String.format("SUBSTR(%s,1,LEN(%s)) = %s)", str, sbstr, sbstr);
    }

    /**
     * The documentation of Denodo does not correspond to the implementation.
     * In particular:
     * SUBSTR(<string> 1, 2)
     * returns the first character of <string>, whereas:
     * SUBSTR(<string> 0, 2)
     * returns the two first character of <string>.
     * Instead, the documentation says that the third parameter ("2" here) should be the length of the returned value.
     * It also says that indices start at 1 for SUBSTR and for POSITION.
     * So:
     * SUBSTR(<string> 1, POSITION(<substring> IN <string>))
     * should be the correct translation.
     * But it returns one more (trailing) character than it should.
     * And if instead we use:
     * SUBSTR(<string> 1, POSITION(<substring> IN <string>) - 1)
     * then Denodo throws the exception:
     * 'ERROR: negative substring length not allowed'
     * <p>
     * There is also another function:
     * SUBSTRING (<string>, <start>, <end>)
     * whose indices start at 0, and whose third parameter is the end index of the returned substring (excluded).
     * So according to the documentation, the following should do the trick:
     * SUBSTRING(<string>, 0, POSITION(<substring> IN <string>) - 1)
     * But again, Denodo throws the exception:
     * 'ERROR: negative substring length not allowed'
     * <p>
     * Instead, we get the desired output with:
     * SUBSTR(<string> 0, POSITION(<substring> IN <string>))
     * which does not make much sense.
     */
    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format(
                "SUBSTR(%s, 0, POSITION(%s IN %s))",
                str, before, str);
    }

    /**
     * See serializeStrBefore() for the inconsistencies of the SUBSTRING functions
     */
    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String sbstr = termConverter.apply(terms.get(1));
        return String.format("SUBSTR(" +
                        "%s," +
                        "POSITION(%s IN %s) + LEN(%s)," +
                        "LEN(%s)  * CAST(SIGN(POSITION(%s IN %s)) AS INTEGER))",
                str, sbstr, str, sbstr, str, sbstr, str);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASH(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA1: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA256: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA384: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("SHA512: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("TZ: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return createDBConcatOperator(arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol("CONCAT", arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("UUID: " + NOT_YET_SUPPORTED_MSG);
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
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
      return   String.format("REPLACE(FORMATDATE('yyyy-MM-dd HH:mm:ss.SSSSSSXXX', %s), ' ', 'T')", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(POSITION(%s IN %s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new LowerCaseDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDBBooleanCase(int arity, boolean doOrderingMatter) {
        return new WrappedDBBooleanCaseFunctionSymbolImpl(arity, dbBooleanType, abstractRootDBType, doOrderingMatter);
    }

    /**
     * Supported in the WHERE clause.
     * Fails in the SELECT clause (e.g. fails for unit test AbstractBindTestWithFunctions.testREGEX())
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return new DBBooleanFunctionSymbolWithSerializerImpl(
                REGEXP_LIKE_STR + "2",
                ImmutableList.of(dbStringType, dbStringType),
                dbBooleanType,
                false,
                (terms, converter, factory) ->
                        String.format(
                                "%s REGEXP_LIKE %s",
                                converter.apply(terms.get(0)),
                                converter.apply(terms.get(1))
                        ));
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        throw new UnsupportedOperationException(REGEXP_LIKE_STR + "3: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace3() {
        return new DefaultSQLSimpleTypedDBFunctionSymbol("REGEXP", 3, dbStringType,
                false, abstractRootDBType);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace4() {
        throw new UnsupportedOperationException(REGEXP_REPLACE_STR + "4: " + NOT_YET_SUPPORTED_MSG);
    }

    /**
     * Time extension - duration arithmetic
     */

    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)) - GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)))/604800000)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)) - GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)))/86400000)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)) - GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)))/3600000)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)) - GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)))/60000)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)) - GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)))/1000)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC(GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)) - GETTIMEINMILLIS(CAST(%s AS TIMESTAMP)))",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    //Denodo does not support 'distinct' GroupConcat
    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "GROUP_CONCAT(true, %s, '', %s)",
                        termConverter.apply(terms.get(1)),
                        termConverter.apply(terms.get(0))
                ));
    }

    @Override
    protected DBFunctionSymbol createDBRowNumber() {
        return new DBFunctionSymbolWithSerializerImpl("ROWNUM", ImmutableList.of(), dbIntegerType, true,
                (t, c, f) -> serializeDBRowNumber(c, f));
    }

    @Override
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return "ROWNUM()";
    }

    /**
     * XSD CAST functions
     */
    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN NOT %1$s REGEXP_ILIKE " + numericPattern + " THEN NULL " +
                        "WHEN (CAST(%1$s AS FLOAT) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS FLOAT) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS FLOAT) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS FLOAT) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN NOT %1$s REGEXP_ILIKE " + numericPattern +
                        " THEN NULL ELSE CAST(%1$s AS DOUBLE PRECISION) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s REGEXP_ILIKE " + numericNonFPPattern + " THEN " +
                        "CAST(%1$s AS DECIMAL) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s REGEXP_ILIKE "+ numericPattern +" THEN " +
                        "CAST(FLOOR(ABS(CAST(%1$s AS DECIMAL))) * SIGN(CAST(%1$s AS DECIMAL)) AS INTEGER) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (NOT %1$s REGEXP_ILIKE " + datePattern1 + " AND " +
                        "NOT %1$s REGEXP_ILIKE " + datePattern2 +" AND " +
                        "NOT %1$s REGEXP_ILIKE " + datePattern3 +" AND " +
                        "NOT %1$s REGEXP_ILIKE " + datePattern4 +" ) " +
                        " THEN NULL ELSE CAST(%1$s AS DATE) END",
                term);
    }

    @Override
    protected DBFunctionSymbol createDBSample(DBTermType termType) {
        return new DBSampleFunctionSymbolImpl(termType, "FIRST");
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
        return String.format("(EXTRACT(SECOND FROM %s) * 1000 + EXTRACT(MILLISECOND FROM %s))", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMicroseconds(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("FLOOR(EXTRACT(SECOND FROM %s) * 1000000 + EXTRACT(MILLISECOND FROM %s) * 1000)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDateTrunc(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String template = String.format(" WHEN %s LIKE '%%s' THEN TRUNC(%s, %%s)", termConverter.apply(terms.get(1)), termConverter.apply(terms.get(0)));
        ImmutableMap.Builder<String, String> possiblePartsBuilder = new ImmutableMap.Builder<>();
        possiblePartsBuilder.put("century", "'CC'");
        possiblePartsBuilder.put("year", "'YEAR'");
        possiblePartsBuilder.put("quarter", "'Q'");
        possiblePartsBuilder.put("month", "'MONTH'");
        possiblePartsBuilder.put("day", "'DAY'");
        possiblePartsBuilder.put("week", "'IW'");
        possiblePartsBuilder.put("hour", "'HH24'");
        possiblePartsBuilder.put("minute", "'MI'");
        ImmutableMap<String, String> possibleParts = possiblePartsBuilder.build();
        StringBuilder serializationBuilder = new StringBuilder("CASE");
        possibleParts.entrySet().stream()
                .forEach(entry -> serializationBuilder.append(String.format(template, entry.getKey(), entry.getValue())));
        serializationBuilder.append(" ELSE NULL END");
        return serializationBuilder.toString();
    }

    @Override
    public DBFunctionSymbol getDBDateTrunc(String datePart) {
        if(ImmutableSet.of("microseconds", "milliseconds", "microsecond", "millisecond", "decade", "second", "millennium").contains(datePart.toLowerCase())) {
            throw new IllegalArgumentException(String.format("Denodo does not support DATE_TRUNC on %s.", datePart));
        }
        return super.getDBDateTrunc(datePart);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBStdev(DBTermType dbType, boolean isPop, boolean isDistinct) {
        if(isDistinct) {
            throw new UnsupportedOperationException("This dialect does not allow the use of DISTINCT with the standard deviation function.");
        }
        return super.getNullIgnoringDBStdev(dbType, isPop, false);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBVariance(DBTermType dbType, boolean isPop, boolean isDistinct) {
        if(isDistinct) {
            throw new UnsupportedOperationException("This dialect does not allow the use of DISTINCT with the variance function.");
        }
        return super.getNullIgnoringDBVariance(dbType, isPop, false);
    }
}
