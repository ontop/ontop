package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Abstract both for IF-THEN-ELSE or more general CASE
 *
 * Arguments are an alternation of (ImmutableExpression, ImmutableTerm) plus optionally an ImmutableTerm for the default case
 */
public abstract class AbstractDBIfThenFunctionSymbol extends AbstractArgDependentTypedDBFunctionSymbol
        implements DBIfThenFunctionSymbol {

    protected AbstractDBIfThenFunctionSymbol(@Nonnull String name, int arity, DBTermType dbBooleanType,
                                             DBTermType rootDBTermType) {
        super(name, computeBaseTypes(arity, dbBooleanType, rootDBTermType));
    }

    private static ImmutableList<TermType> computeBaseTypes(int arity, DBTermType dbBooleanType, DBTermType rootDBTermType) {
        Stream<DBTermType> regularConditions = IntStream.range(0, arity - (arity % 2))
                .boxed()
                .map(i -> (i % 2 == 0) ? dbBooleanType : rootDBTermType);

        Stream<DBTermType> typeStream = (arity % 2 == 0)
                ? regularConditions
                : Stream.concat(regularConditions, Stream.of(rootDBTermType));

        return typeStream.collect(ImmutableCollectors.toList());
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Stream<ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> terms) {
        return IntStream.range(1, terms.size())
                .filter(i -> i % 2 == 1)
                .boxed()
                .map(terms::get);
    }


    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                                  TermFactory termFactory, VariableNullability variableNullability) {
        int arity = getArity();

        List<Map.Entry<ImmutableExpression, ImmutableTerm>> newWhenPairs = new ArrayList<>();

        /*
         * When conditions
         */
        for (int i=0; i < arity - (arity % 2); i+=2) {
            ImmutableTerm term = terms.get(i);
            ImmutableExpression expression =  Optional.of(term)
                    .filter(t -> t instanceof ImmutableExpression)
                    .map(t -> (ImmutableExpression)t)
                    .orElseThrow(() -> new MinorOntopInternalBugException(term + " was expected to be " +
                            "an ImmutableExpression due to its position in " + this));

            ImmutableExpression.Evaluation evaluation = expression.evaluate2VL(variableNullability);
            if (evaluation.getValue().isPresent()) {
                switch (evaluation.getValue().get()) {
                    case TRUE:
                        ImmutableTerm possibleValue = terms.get(i+1).simplify(variableNullability);
                        if (newWhenPairs.isEmpty())
                            return possibleValue;
                        else
                            return termFactory.getDBCase(newWhenPairs.stream(), possibleValue);
                    default:
                        // Discard the case entry
                }
            }
            else {
                ImmutableExpression newExpression = evaluation.getExpression()
                        .orElseThrow(() -> new MinorOntopInternalBugException("The evaluation was expected " +
                                "to return an expression because no value was returned"));
                ImmutableTerm possibleValue = terms.get(i+1).simplify(variableNullability);
                newWhenPairs.add(Maps.immutableEntry(newExpression, possibleValue));
            }
        }

        ImmutableTerm defaultValue = extractDefaultValue(terms, termFactory)
                .simplify(variableNullability);

        if (newWhenPairs.isEmpty())
            return defaultValue;

        ImmutableFunctionalTerm newTerm = termFactory.getDBCase(newWhenPairs.stream(), defaultValue);

        // Make sure the size was reduced so as to avoid an infinite loop
        // For instance, new opportunities may appear when reduced to a IF_ELSE_NULL
        return (newWhenPairs.size() < terms.size() % 2)
                ? newTerm.simplify(variableNullability)
                : newTerm;
    }

    /**
     * Conservative: can only be post-processed when all sub-functional terms (at different levels of depth)
     * can be post-processed.
     *
     * TODO: consider perhaps a less conservative approach
     *
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return extractSubFunctionalTerms(arguments)
                .allMatch(ImmutableFunctionalTerm::canBePostProcessed);
    }

    /**
     * Recursive
     */
    protected Stream<ImmutableFunctionalTerm> extractSubFunctionalTerms(ImmutableList<? extends ImmutableTerm> subTerms) {
        return subTerms.stream()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm)t)
                .flatMap(f -> Stream.concat(Stream.of(f), extractSubFunctionalTerms(f.getTerms())));
    }

    protected abstract ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory);

    /**
     * Default, can be overridden
     */
    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }

    /**
     * Requires some of its arguments to be expressions
     */
    @Override
    protected ImmutableList<? extends ImmutableTerm> transformIntoRegularArguments(
            ImmutableList<? extends NonFunctionalTerm> arguments, TermFactory termFactory) {
        return IntStream.range(0, arguments.size())
                .boxed()
                .map(i -> i % 2 == 0
                        ? termFactory.getIsTrue(arguments.get(i))
                        : arguments.get(i))
                .collect(ImmutableCollectors.toList());
    }
}
