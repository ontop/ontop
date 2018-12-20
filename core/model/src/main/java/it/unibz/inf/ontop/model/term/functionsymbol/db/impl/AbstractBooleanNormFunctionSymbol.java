package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractDBTypeConversionFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class AbstractBooleanNormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

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

    /**
     * Here we assume that the DB has only one way to represent the boolean value as a string
     */
    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
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
        throw new RuntimeException("TODO: implement getNativeDBString for " + getClass());
    }
}
