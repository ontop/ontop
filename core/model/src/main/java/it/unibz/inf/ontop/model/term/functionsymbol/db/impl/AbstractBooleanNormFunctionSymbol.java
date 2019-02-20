package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractDBTypeConversionFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractBooleanNormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType booleanType;

    protected AbstractBooleanNormFunctionSymbol(DBTermType booleanType, DBTermType stringType) {
        super("booleanLexicalNorm", booleanType, stringType);
        this.booleanType = booleanType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(booleanType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    /**
     * Here we assume that the DB has only one way to represent the boolean value as a string
     */
    @Override
    protected boolean isAlwaysInjective() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) {
        return termFactory.getDBConstant(normalizeValue(constant.getValue()), getTargetType());
    }

    protected String normalizeValue(String value) {
        return value.toLowerCase();
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableTerm term = terms.get(0);
        ImmutableFunctionalTerm newFunctionalTerm = termFactory.getDBCase(Stream.of(
                Maps.immutableEntry(termFactory.getStrictEquality(term, termFactory.getDBBooleanConstant(true)),
                        termFactory.getDBStringConstant("true")),
                Maps.immutableEntry(termFactory.getStrictEquality(term, termFactory.getDBBooleanConstant(false)),
                        termFactory.getDBStringConstant("false"))),
                termFactory.getNullConstant());

        return termConverter.apply(newFunctionalTerm);
    }

}
