package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractDBAggregationFunctionSymbol extends FunctionSymbolImpl implements DBFunctionSymbol {

    private final DBTermType targetType;
    private final DBFunctionSymbolSerializer serializer;

    protected AbstractDBAggregationFunctionSymbol(@Nonnull String name,
                                                  @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                                  @Nonnull DBTermType targetType,
                                                  @Nonnull DBFunctionSymbolSerializer serializer) {
        super(name, expectedBaseTypes);
        this.targetType = targetType;
        this.serializer = serializer;
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
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    public boolean isAggregation() {
        return true;
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }
}
