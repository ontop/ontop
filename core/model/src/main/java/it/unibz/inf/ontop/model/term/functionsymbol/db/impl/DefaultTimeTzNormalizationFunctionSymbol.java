package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class DefaultTimeTzNormalizationFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {
    private final DBTermType timeTzType;
    private final DBFunctionSymbolSerializer serializer;

    protected DefaultTimeTzNormalizationFunctionSymbol(DBTermType timeTzType, DBTermType targetType,
                                                       DBFunctionSymbolSerializer serializer) {
        super(timeTzType + "Norm", timeTzType, targetType);
        this.timeTzType = timeTzType;
        this.serializer = serializer;
    }

    /**
     * No optimization
     */
    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) throws DBTypeConversionException {
        return termFactory.getImmutableFunctionalTerm(this, constant);
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(timeTzType);
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
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
}
