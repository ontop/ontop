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

public class CDataDynamoDBDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for CData DynamoDB";

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
                    ImmutableTerm len = termFactory.getDBCharLength(terms.get(0));

                    return String.format("SUBSTRING(%s FROM %s FOR %s)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1)), termConverter.apply(len));
                });

        substring3 = new DBFunctionSymbolWithSerializerImpl(
                SUBSTR_STR + "3",
                ImmutableList.of(dbStringType, typeFactory.getDBTypeFactory().getDBLargeIntegerType(), typeFactory.getDBTypeFactory().getDBLargeIntegerType()),
                dbStringType,
                false,
                (terms, termConverter, termFactory) -> {
                    return String.format("SUBSTRING(%s FROM %s FOR %s)", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1)), termConverter.apply(terms.get(2)));
                });

    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createCDataDynamoDBRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        return ImmutableTable.copyOf(table);
    }



    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(CHARINDEX(%s, %s) >= 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("IF(CHARINDEX(%s, %s) >= 0, SUBSTRING(%s FROM 1 FOR CHARINDEX(%s, %s)), '')", before, str, str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("IF(CHARINDEX(%s, %s) >= 0, SUBSTRING(%s FROM CHARINDEX(%s, %s) + LEN(%s) + 1 FOR LEN(%s)), '')", after, str, str, after, str, after, str);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASHBYTES('MD5', %s)", termConverter.apply(terms.get(0)));
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASHBYTES('SHA1', %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASHBYTES('SHA2_256', %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASHBYTES('SHA3_384', %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASHBYTES('SHA2_512', %s)", termConverter.apply(terms.get(0)));
    }

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
        return String.format("STUFF(FORMAT(%s, 'YYYY-MM-dd\\'T\\'HH:mm:ssz'), LEN(FORMAT(%s, 'YYYY-MM-dd\\'T\\'HH:mm:ssz')) - 1, 0, ':')", termConverter.apply(terms.get(0)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("UUID: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        return substring2;
    }

    @Override
    public DBFunctionSymbol getDBSubString3() {
        return substring3;
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBGroupConcat(boolean isDistinct) {
        throw new UnsupportedOperationException("GROUP CONCAT: " + NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected DBFunctionSymbol createEncodeURLorIRI(boolean preserveInternationalChars) {
        return new CDataDynamoDBSQLEncodeURLorIRIFunctionSymbolImpl(dbStringType, preserveInternationalChars);
    }

    @Override
    protected DBFunctionSymbol createDBSum(DBTermType termType, boolean isDistinct) {
        return new NullIgnoringDBSumFunctionSymbol(termType, isDistinct) {
            @Override
            public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
                return String.format("SUM(%sCASE WHEN %s IS NULL THEN %s ELSE %s END)",
                        isDistinct() ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termType.isA(typeFactory.getDBTypeFactory().getDBLargeIntegerType()) ? "0" : "0.0",
                        termConverter.apply(terms.get(0)));
            }
        };
    }



}
