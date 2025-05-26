package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

public class TDEngineDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_SUPPORTED_ERRROR_MESSAGE = "%s is not supported in TDEngine";
    private static final String NOW_STR = "NOW";

    @Inject
    protected TDEngineDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createTDengineRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createTDengineRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        table.remove(FORMAT, 3);
        table.remove(UCASE_STR, 1);
        table.remove(LCASE_STR, 1);
        table.remove(REGEXP_REPLACE_STR, 3);
        table.remove(REGEXP_REPLACE_STR, 4);
        table.remove(NULLIF_STR, 2);

       DBFunctionSymbol nowFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(
                NOW_STR, 0, dbTypeFactory.getDBDateTimestampType(), false, abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        return ImmutableTable.copyOf(table);
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

        return String.format("SUBSTRING(%s,1,CAST(POSITION(%s IN %s)-1 as INTEGER))", str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));

        return String.format("CASE WHEN POSITION(%s IN %s) != 0 THEN SUBSTRING(%s, CAST(POSITION(%s IN %s) + LENGTH(%s) AS INTEGER)) ELSE '' END", after, str, str, after, str, after);
    }

    @Override
    public DBFunctionSymbol getDBRight() {
        return new SimpleTypedDBFunctionSymbolImpl(RIGHT_STR, 2, dbStringType, false,
                abstractRootDBType,
                (terms, converter, factory) -> serializeDBRight(terms, converter));
    }

    private String serializeDBRight(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter) {
        String str = termConverter.apply(terms.get(0));
        String length = termConverter.apply(terms.get(1));
        return String.format("SUBSTRING(%s,CAST(LENGTH(%s)-%s+1 AS INTEGER))", str, str, length);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("MD5(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(String.format(NOT_SUPPORTED_ERRROR_MESSAGE, "SHA1()"));
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(String.format(NOT_SUPPORTED_ERRROR_MESSAGE, "SHA256()"));
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(String.format(NOT_SUPPORTED_ERRROR_MESSAGE, "SHA384()"));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(String.format(NOT_SUPPORTED_ERRROR_MESSAGE, "SHA512()"));
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return createRegularDBConcat(arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        throw new UnsupportedOperationException(String.format(NOT_SUPPORTED_ERRROR_MESSAGE, "Concatenation operator"));
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol("CONCAT", arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected String serializeDateTimeNormWithTZ(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("TO_CHAR(%s, 'yyyy-mm-ddThh24:mi:ss.mstz')", str);
    }

    @Override
    protected String serializeDateTimeNormNoTZ(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("TO_CHAR(%s, 'yyyy-mm-ddThh24:mi:ss.ms')", str);
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(String.format(NOT_SUPPORTED_ERRROR_MESSAGE, "TZ(timestamp)"));
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException(String.format(NOT_SUPPORTED_ERRROR_MESSAGE, "UUID()"));
    }

}
