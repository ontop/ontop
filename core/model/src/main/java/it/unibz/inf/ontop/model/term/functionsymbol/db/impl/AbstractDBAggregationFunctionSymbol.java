package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
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

public abstract class AbstractDBAggregationFunctionSymbol extends AbstractAggregationFunctionSymbol implements DBFunctionSymbol {

    private final DBFunctionSymbolSerializer serializer;

    protected AbstractDBAggregationFunctionSymbol(@Nonnull String name,
                                                  @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                                  @Nonnull DBTermType targetType,
                                                  boolean isDistinct,
                                                  @Nonnull DBFunctionSymbolSerializer serializer) {
        super(name, expectedBaseTypes, targetType, isDistinct);
        this.serializer = serializer;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (getArity() >= 1) {
            ImmutableTerm newTerm = newTerms.get(0);
            return newTerm.isNull()
                    ? evaluateEmptyBag(termFactory)
                    : super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }
}
