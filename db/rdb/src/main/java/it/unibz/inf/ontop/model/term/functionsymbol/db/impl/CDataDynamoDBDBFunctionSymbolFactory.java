package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.UUID;
import java.util.function.Function;

public class CDataDynamoDBDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private final DBFunctionSymbol substring2;
    private final DBFunctionSymbol substring3;

    @Inject
    protected CDataDynamoDBDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createCDataDynamoDBRegularFunctionTable(typeFactory), typeFactory);

        substring2 = new DBFunctionSymbolWithSerializerImpl(
                SUBSTR_STR + "2",
                ImmutableList.of(dbStringType, typeFactory.getDBTypeFactory().getDBLargeIntegerType()),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> {
                    // PostgreSQL does not tolerate bigint as argument (int8), just int4 (integer)
                    ImmutableTerm len = termFactory.getDBCharLength(terms.get(0));

                    return String.format("SUBSTRING(%s FROM %s FOR %s)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1)), termConverter.apply(len));
                });

        substring3 = new DBFunctionSymbolWithSerializerImpl(
                SUBSTR_STR + "3",
                ImmutableList.of(dbStringType, typeFactory.getDBTypeFactory().getDBLargeIntegerType(), typeFactory.getDBTypeFactory().getDBLargeIntegerType()),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> {
                    // PostgreSQL does not tolerate bigint as argument (int8), just int4 (integer)

                    return String.format("SUBSTRING(%s FROM %s FOR (%s - %s + 1))", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1)), termConverter.apply(terms.get(2)), termConverter.apply(terms.get(1)));
                });
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createCDataDynamoDBRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        /*  TODO-SCAFFOLD: Remove function symbols that are not supported, if any:
         *-------------------------------------------------------------------
         *      table.remove("UNSUPPORTED_FUNCTION", arity);
         */
        
        /*  TODO-SCAFFOLD: Change signature of basic functions, if necessary:
         *-------------------------------------------------------------------
         *      DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
         *              CURRENT_TIMESTAMP_STR,
         *              dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
         *      table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);
         */

        return ImmutableTable.copyOf(table);
    }



    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(POSITION(%s IN %s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("IF(POSITION(%s IN %s) != 0, SUBSTRING(%s FROM 1 FOR POSITION(%s IN %s)-1), '')", before, str, str, before, str);
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("IF(POSITION(%s IN %s) != 0, SUBSTRING(%s FROM POSITION(%s IN %s) + LEN(%s) FOR LEN(%s)), '')", after, str, str, after, str, after, str);
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("MD5(%s)", termConverter.apply(terms.get(0)));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA1(%s)", termConverter.apply(terms.get(0)));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA256(%s)", termConverter.apply(terms.get(0)));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA384(%s)", termConverter.apply(terms.get(0)));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA512(%s)", termConverter.apply(terms.get(0)));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("(LPAD(EXTRACT(TIMEZONE_HOUR FROM %s)::text,2,'0') || ':' || LPAD(EXTRACT(TIMEZONE_MINUTE FROM %s)::text,2,'0'))", str, str);
    }

    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("'week' datediff is not supported in CData's DynamoDB JDBC.");
    }


    @Override
    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("dd", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("hh", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("mi", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("ss", terms, termConverter, termFactory);
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeTimeBetween("ms", terms, termConverter, termFactory);
    }

    private String serializeTimeBetween(String timeUnit, ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("datediff('%s', %s, %s)",
                timeUnit,
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }



    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return createDBConcatOperator(arity);
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType,
                Serializers.getOperatorSerializer(CONCAT_OP_STR));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullToleratingDBConcatFunctionSymbol("CONCAT", arity, dbStringType, abstractRootDBType, false);
    }

    // TODO-SCAFFOLD: Implement DateTimeNorm serialization in ISO 8601 Format 'YYYY-MM-DDTHH:MM:SS+HH:MM'
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("This function was not yet implemented.");
    }

    // TODO-SCAFFOLD: Modify this default name, if necessary
    @Override
    protected String getUUIDNameInDialect() {
        return "UUID";
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        return substring2;
    }

    @Override
    public DBFunctionSymbol getDBSubString3() {
        return substring3;
    }
}
