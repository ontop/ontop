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

import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.*;
import static it.unibz.inf.ontop.model.type.impl.DenodoDBTypeFactory.*;

public class DenodoDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for Denodo";

    // Created in init()
    private DBFunctionSymbol dbRightFunctionSymbol;
    private DBFunctionSymbol dbLeftFunctionSymbol;

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

        DBFunctionSymbol regexLikeFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);


        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        builder.putAll(super.createNormalizationTable());

        // BOOL
        DBTermType boolType = dbTypeFactory.getDBTermType(BOOL_STR);
        builder.put(boolType, typeFactory.getXsdBooleanDatatype(), createBooleanNormFunctionSymbol(boolType));

        //TIMESTAMP
        DBTermType timeStamp = dbTypeFactory.getDBTermType(TIMESTAMP_STR);
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(timeStamp);
        builder.put(timeStamp, xsdDatetime, datetimeNormFunctionSymbol);
        builder.put(timeStamp, xsdDatetimeStamp, datetimeNormFunctionSymbol);

        //TIMETZ
        DBTermType timeTZType = dbTypeFactory.getDBTermType(TIMETZ_STR);
        // Takes care of putting
        DefaultTimeTzNormalizationFunctionSymbol timeTZNormFunctionSymbol = new DefaultTimeTzNormalizationFunctionSymbol(
                timeTZType, dbStringType,
                (terms, termConverter, termFactory) -> String.format(
                        "REGEXP_REPLACE(CAST(%s AS TEXT),'([-+]\\d\\d)$', '\\1:00')", termConverter.apply(terms.get(0))));
        builder.put(timeTZType, typeFactory.getDatatype(XSD.TIME), timeTZNormFunctionSymbol);

        return builder.build();
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
     * The documentation of Denodo does not correspond to the implementation
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

    /* Use "SUBSTR" rather than SUBSTRING to have indices start at 1 */
    @Override
    public DBFunctionSymbol getDBSubString2() {
        return getRegularDBFunctionSymbol(SUBSTR_STR, 2);
    }

    /* Use "SUBSTR" rather than SUBSTRING to have indices start at 1 */
    @Override
    public DBFunctionSymbol getDBSubString3() {
        return getRegularDBFunctionSymbol(SUBSTR_STR, 3);
    }

    /**
     * TODO: find a way to use the stored TZ instead of the local one
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(%s AS TIMESTAMPTZ)", termConverter.apply(terms.get(0)));
    }


//
//    @Override
//    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
//        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType,
//                Serializers.getOperatorSerializer(CONCAT_OP_STR));
//    }

//    @Override
//    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
//        return new NullToleratingDBConcatFunctionSymbol("CONCAT", arity, dbStringType, abstractRootDBType, false);
//    }
//
//    @Override
//    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
//        return new OneLetterDBIsTrueFunctionSymbolImpl(dbBooleanType);
//    }

//    @Override
//    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
//           return termConverter.apply(terms.get(0));
//    }
//
//    @Override
//    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
//        return new OneLetterBooleanNormFunctionSymbolImpl(booleanType, dbStringType);
//    }
//
//    @Override
//    public DBFunctionSymbol getDBSubString2() {
//        return getRegularDBFunctionSymbol(SUBSTR_STR, 2);
//    }
//
//    @Override
//    public DBFunctionSymbol getDBSubString3() {
//        return getRegularDBFunctionSymbol(SUBSTR_STR, 3);
//    }

    //    @Override
//    protected String getRandNameInDialect() {
//        return RANDOM_STR;
//    }
//
//    @Override
//    protected String getUUIDNameInDialect() {
//        throw new UnsupportedOperationException("Should not be used for PostgreSQL");
//    }
//
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

    /**
     * Supported in the WHERE clause.
     * Fails in the SELECT clause (e.g. fals for unit test AbstractBindTestWithFunctions.testREGEX())
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
        return new DBFunctionSymbolWithSerializerImpl(
                REGEXP_REPLACE_STR,
                ImmutableList.of(dbStringType, dbStringType, dbStringType),
                dbStringType,
                false,
                (terms, converter, factory) ->
                        String.format(
                                "REGEXP(%s, %s, %s)",
                                converter.apply(terms.get(0)),
                                converter.apply(terms.get(1)),
                                converter.apply(terms.get(2))
                        ));
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace4() {
        throw new UnsupportedOperationException(REGEXP_REPLACE_STR + "4: " + NOT_YET_SUPPORTED_MSG);
    }

}
