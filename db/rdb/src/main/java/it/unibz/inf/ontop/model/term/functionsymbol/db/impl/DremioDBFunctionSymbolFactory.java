package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;


public class DremioDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for Dremio";

    @Inject
    protected DremioDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDremioRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDremioRegularFunctionTable(
            TypeFactory typeFactory) {

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        return createDefaultRegularFunctionTable(typeFactory);
    }

    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "GROUP_CONCAT(%s%s SEPARATOR %s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1))
                ));
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("INSTR(%s,%s) > 0",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format(
            "SUBSTR(%s, 0, POSITION(%s IN %s))",
            str, before, str);
        }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format(
                "SUBSTR(%s, " +
                        "POSITION(%s IN %s) + LENGTH(%s)," +
                        "LENGTH(%s) * POSITION(%s IN %s))",
                str, before, str, before, str, before, str);
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(NOT_YET_SUPPORTED_MSG);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return (DBConcatFunctionSymbol) getRegularDBFunctionSymbol(CONCAT_STR, arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType,
                Serializers.getOperatorSerializer(CONCAT_OP_STR));
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    /**
     * Made Implicit
     */
    protected DBTypeConversionFunctionSymbol createDatetimeToDatetimeCastFunctionSymbol(DBTermType inputType,
                                                                                        DBTermType targetType) {
        return new DefaultImplicitDBCastFunctionSymbol(inputType, targetType);
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("REPLACE(TO_CHAR(%s, 'YYYY-MM-DD\"T\"HH:MI:SSTZO'), 'UTCO', '+00:00')",
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Do not call getUUIDNameInDialect for Dremio");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return new DBBooleanFunctionSymbolWithSerializerImpl("REGEXP_MATCHES_3",
                ImmutableList.of(abstractRootDBType, abstractRootDBType, abstractRootDBType), dbBooleanType, false,
                this::serializeDBRegexpMatches3);
    }

    protected String serializeDBRegexpMatches3(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String string = termConverter.apply(terms.get(0));
        String pattern = termConverter.apply(terms.get(1));
        ImmutableTerm flagTerm = terms.get(2);
        if (flagTerm instanceof DBConstant) {
            String flags = ((DBConstant) flagTerm).getValue();
            switch (flags) {
                // Case sensitive
                case "":
                    return String.format("(%s REGEXP BINARY %s)", string, pattern);
                // Case insensitive
                case "i":
                    // TODO: is it robust to collation?
                    return String.format("(%s REGEXP %s)", string, pattern);
                default:
                    break;
            }
        }

        return getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 3)
                .getNativeDBString(terms, termConverter, termFactory);
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

}
