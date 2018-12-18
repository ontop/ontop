package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class AbstractTimestampISONormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType timestampType;

    protected AbstractTimestampISONormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType) {
        super("isoTimestamp", timestampType, dbStringType);
        this.timestampType = timestampType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(timestampType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement timestamp conversion");
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement getNativeDBString for " + getClass());
    }
}
