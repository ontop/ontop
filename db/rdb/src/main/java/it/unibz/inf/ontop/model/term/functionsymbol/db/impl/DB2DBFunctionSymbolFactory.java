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

public class DB2DBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String CURRENT_TIMESTAMP_SPACE_STR = "CURRENT TIMESTAMP";

    @Inject
    protected DB2DBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDB2RegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDB2RegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_SPACE_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_SPACE_STR, 0, nowFunctionSymbol);
        table.remove(REGEXP_LIKE_STR, 2);
        table.remove(REGEXP_LIKE_STR, 3);

        return ImmutableTable.copyOf(table);
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
        if (arity != 2)
            throw new UnsupportedOperationException("CONCAT is strictly binary in DB2. Please use the operator instead.");
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    /**
     * TODO: handle the timezone!
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("REPLACE(VARCHAR_FORMAT(%s,'YYYY-MM-DD HH24:MI:SS.FF'),' ','T')",
                termConverter.apply(terms.get(0)));
    }

    @Override
    public DBFunctionSymbol getNow() {
        return getRegularDBFunctionSymbol(CURRENT_TIMESTAMP_SPACE_STR, 0);
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }
}
