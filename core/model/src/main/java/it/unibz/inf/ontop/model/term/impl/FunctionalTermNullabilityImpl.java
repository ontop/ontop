package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;

import javax.annotation.Nullable;
import java.util.Optional;

public class FunctionalTermNullabilityImpl implements FunctionSymbol.FunctionalTermNullability {

    private final boolean isNullable;
    @Nullable
    private final Variable boundVariable;

    public FunctionalTermNullabilityImpl(boolean isNullable) {
        this.isNullable = isNullable;
        this.boundVariable = null;
    }

    public FunctionalTermNullabilityImpl(Variable boundVariable) {
        this.isNullable = true;
        this.boundVariable = boundVariable;
    }

    @Override
    public boolean isNullable() {
        return isNullable;
    }

    @Override
    public Optional<Variable> getBoundVariable() {
        return Optional.ofNullable(boundVariable);
    }
}
