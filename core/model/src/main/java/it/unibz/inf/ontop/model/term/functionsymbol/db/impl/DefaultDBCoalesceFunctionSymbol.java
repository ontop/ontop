package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultDBCoalesceFunctionSymbol extends AbstractArgDependentTypedDBFunctionSymbol {

    private final DBFunctionSymbolSerializer serializer;

    protected DefaultDBCoalesceFunctionSymbol(String nameInDialect, int arity, DBTermType rootDBTermType,
                                              DBFunctionSymbolSerializer serializer) {
        super(nameInDialect + arity, IntStream.range(0, arity)
                .boxed()
                .map(i -> rootDBTermType)
                .collect(ImmutableCollectors.toList()));
        this.serializer = serializer;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected Stream<? extends ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> terms) {
        return terms.stream();
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> remainingTerms = newTerms.stream()
                .filter(t -> !t.isNull())
                .collect(ImmutableCollectors.toList());

        switch (remainingTerms.size()) {
            case 0:
                return termFactory.getNullConstant();
            case 1:
                return remainingTerms.get(0);
            default :
                ImmutableTerm firstRemainingTerm = remainingTerms.get(0);
                if (!firstRemainingTerm.isNullable(variableNullability.getNullableVariables()))
                    return firstRemainingTerm;
                return termFactory.getDBCoalesce(remainingTerms);
        }
    }
}
