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
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

public class H2SQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UUID_STR = "RANDOM_UUID";
    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";

    private static final String UNSUPPORTED_MSG = "Not supported by H2";

    @Inject
    private H2SQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createH2RegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createH2RegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter,
                                       TermFactory termFactory) {
        return String.format("(POSITION(%s,%s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("LEFT(%s,CHARINDEX(%s,%s)-1)", str, before, str);
    }

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

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASH('SHA256', STRINGTOUTF8(%s), 1)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                 Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return getNullRejectingDBConcat(arity);
    }

    /**
     * Treats NULLs as empty strings
     */
    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullToleratingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    /**
     * Asks the timezone to be included
     *
     * TODO: fix the timezone
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("REPLACE(FORMATDATETIME(%s,'yyyy-MM-dd HH:mm:ss.SSSZ'), ' ', 'T')", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBFunctionSymbol createDBAvg(DBTermType inputType, boolean isDistinct) {
        // To make sure the AVG does not return an integer but a decimal
        if (inputType.equals(dbIntegerType))
            return new ForcingFloatingDBAvgFunctionSymbolImpl(inputType, dbDecimalType, isDistinct);

        return super.createDBAvg(inputType, isDistinct);
    }

    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STR;
    }


}
