package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.AggregationFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractAggregationFunctionSymbol extends FunctionSymbolImpl implements AggregationFunctionSymbol {

    private final DBTermType targetType;
    private final boolean isDistinct;

    protected AbstractAggregationFunctionSymbol(@Nonnull String name,
                                                @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                                @Nonnull DBTermType targetType,
                                                boolean isDistinct) {
        super(name, expectedBaseTypes);
        this.targetType = targetType;
        this.isDistinct = isDistinct;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isAggregation() {
        return true;
    }

    public boolean isDistinct() {
        return isDistinct;
    }
}
