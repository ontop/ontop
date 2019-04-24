package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.function.Function;

public class DefaultSQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UNSUPPORTED_MSG = "Not supported in the Default SQL factory since no-one uses " +
            "the old official standard function.\n" +
            "Please specific it in your dialect factory";

    @Inject
    private DefaultSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDefaultRegularFunctionTable(typeFactory), typeFactory);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> immutableTerms,
                                       Function<ImmutableTerm, String> immutableTermStringFunction, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                 Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);
    }

    /**
     * By default, we suppose that NULLs are tolerated
     */
    @Override
    public DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Minimal arity for string concatenation: 2");
        return new NullToleratingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);

    }

    /**
     * By default, we suppose that NULLs are tolerated
     */
    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullToleratingDBConcatFunctionSymbol(CONCAT_STR, 2, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
}
