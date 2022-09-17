package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class DefaultHexBinaryDenormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType binaryType;
    private final DBFunctionSymbolSerializer serializer;
    private final DBTermType stringType;

    protected DefaultHexBinaryDenormFunctionSymbol(DBTermType binaryType, DBTermType stringType,
                                                   DBFunctionSymbolSerializer serializer) {
        super("hexBinaryLexicalNorm" + binaryType, stringType, binaryType);
        this.stringType = stringType;
        this.binaryType = binaryType;
        this.serializer = serializer;
    }

    /**
     * TODO: try to implement it
     */
    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        return termFactory.getImmutableFunctionalTerm(this, constant);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(stringType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }
}
