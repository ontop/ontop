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

public class TDengineDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    @Inject
    protected TDengineDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createTDengineRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createTDengineRegularFunctionTable(
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

        return String.format("SUBSTRING(%s,1,POSITION(%s IN %s)-1)", str, before, str);
    }

    // TODO-SCAFFOLD: Modify this default implementation, if necessary
    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("IF(POSITION(%s IN %s) != 0, SUBSTRING(%s, POSITION(%s IN %s) + LENGTH(%s)), '')", after, str, str, after, str, after);
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

    @Override
    protected String serializeDateTimeNormWithTZ(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("This function (serializeDateTimeNormWithTZ) was not yet implemented.");
    }

    // TODO-SCAFFOLD: Implement DateTimeNorm serialization in ISO 8601 Format 'YYYY-MM-DDTHH:MM:SS+HH:MM'
    //@Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("This function (serializeDateTimeNorm) was not yet implemented.");
    }

    // TODO-SCAFFOLD: Modify this default name, if necessary
    @Override
    protected String getUUIDNameInDialect() {
        return "UUID";
    }

}
