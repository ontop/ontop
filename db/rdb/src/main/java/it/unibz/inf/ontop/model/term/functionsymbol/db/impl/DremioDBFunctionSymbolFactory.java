package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
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

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.INTEGER_STR;
import static it.unibz.inf.ontop.model.type.impl.PostgreSQLDBTypeFactory.SERIAL_STR;


public class DremioDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_YET_SUPPORTED_MSG = "Not supported by Dremio yet";
    private static final String POSITION_STR = "POSITION";

    @Inject
    protected DremioDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDremioRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDremioRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol positionFunctionSymbol = new SimpleTypedDBFunctionSymbolImpl(POSITION_STR, 2,
                dbTypeFactory.getDBLargeIntegerType(), false, abstractRootDBType,
                (terms, termConverter, termFactory) -> String.format("POSITION(%s IN %s)",
                        termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1))));
        table.put(POSITION_STR, 2, positionFunctionSymbol);
        return ImmutableTable.copyOf(table);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(POSITION(%s IN %s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
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

    /**
     * Dremio has a SPLIT_PART(string, pattern, index), but this function would fail for multiple occurrence of "pattern" in "string"
     */
    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format(
                "CASE "+
                        "WHEN LENGTH(%s) > 0 AND POSITION(%s IN %s) = 0 THEN '' "+
                        "ELSE SUBSTR(%s, POSITION(%s IN %s) + LENGTH(%s)) "+
                "END",
                before, before, str, str, before, str, before);
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
        throw new UnsupportedOperationException("Regex match parameters: "+NOT_YET_SUPPORTED_MSG);
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
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (a,b,c) -> String.format(
                        "GROUP_CONCAT or LIST_AGG "+ NOT_YET_SUPPORTED_MSG
                ));
    }

    /**
     * Requires sometimes to type NULLs
     */
    @Override
    protected DBFunctionSymbol createTypeNullFunctionSymbol(DBTermType termType) {
            return new NonSimplifiableTypedNullFunctionSymbol(termType);
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(NOT_YET_SUPPORTED_MSG);
    }
}
