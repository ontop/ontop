package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public abstract class AbstractTypedDBFunctionSymbol extends FunctionSymbolImpl implements DBFunctionSymbol {

    private final DBTermType targetType;

    protected AbstractTypedDBFunctionSymbol(String name, ImmutableList<TermType> expectedBaseTypes, DBTermType targetType) {
        super(name, expectedBaseTypes);
        this.targetType = targetType;
    }

    public DBTermType getTargetType() {
        return targetType;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    /**
     * By default, to be overridden when necessary
     */
    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }
}
