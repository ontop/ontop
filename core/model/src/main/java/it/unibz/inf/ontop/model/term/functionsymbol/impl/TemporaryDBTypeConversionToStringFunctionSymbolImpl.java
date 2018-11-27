package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public class TemporaryDBTypeConversionToStringFunctionSymbolImpl extends AbstractDBTypeConversionFunctionSymbolImpl {

    protected TemporaryDBTypeConversionToStringFunctionSymbolImpl(DBTermType inputBaseType, DBTermType targetType) {
        super("TmpTo" + targetType.getName(), inputBaseType, targetType);
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.empty();
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return false;
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) {
        throw new MinorOntopInternalBugException("A TemporaryDBTypeConversionToStringFunctionSymbolImpl " +
                "should have been removed before asking it to simplified");
    }
}
