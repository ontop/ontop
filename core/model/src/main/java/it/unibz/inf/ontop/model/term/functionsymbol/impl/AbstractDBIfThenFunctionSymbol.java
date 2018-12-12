package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
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
public abstract class AbstractDBIfThenFunctionSymbol extends FunctionSymbolImpl implements DBFunctionSymbol {

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
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    /**
     * Is supposed to be strongly typed: does not compare the types of the possible values because
     * they are supposed to be the sames.
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {

        ImmutableList<TermTypeInference> typeInferences = extractPossibleValues(terms)
                .map(t -> (t instanceof Variable)
                        ? Optional.of(TermTypeInference.declareRedirectionToVariable((Variable) t))
                        : t.inferType())
                .flatMap(o -> o
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(ImmutableCollectors.toList());

        // Gives a preference to type inferences that determine the type (over the ones that redirect it to a variable)
        return typeInferences.stream()
                .filter(t -> t.getTermType().isPresent())
                .map(Optional::of)
                .findAny()
                .orElseGet(() -> typeInferences.stream()
                        .findAny());
    }

    protected Stream<ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> terms) {
        return IntStream.range(1, terms.size())
                .filter(i -> i % 2 == 1)
                .boxed()
                .map(terms::get);
    }


    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                                  boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory) {
        int arity = getArity();

        List<Map.Entry<ImmutableExpression, ImmutableTerm>> newWhenPairs = new ArrayList<>();

        /*
         * When conditions
         */
        for (int i=0; i < arity - (arity % 2); i+=2) {
            ImmutableExpression expression =  (ImmutableExpression) terms.get(i);

            ImmutableExpression.Evaluation evaluation = expression.evaluate(termFactory);
            if (evaluation.getValue().isPresent()) {
                switch (evaluation.getValue().get()) {
                    case TRUE:
                        ImmutableTerm possibleValue = terms.get(i+1).simplify(isInConstructionNodeInOptimizationPhase);
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
                ImmutableTerm possibleValue = terms.get(i+1).simplify(isInConstructionNodeInOptimizationPhase);
                newWhenPairs.add(Maps.immutableEntry(newExpression, possibleValue));
            }
        }

        ImmutableTerm defaultValue = extractDefaultValue(terms, termFactory)
                .simplify(isInConstructionNodeInOptimizationPhase);

        if (newWhenPairs.isEmpty())
            return defaultValue;

        ImmutableFunctionalTerm newTerm = termFactory.getDBCase(newWhenPairs.stream(), defaultValue);

        // Make sure the size was reduced so as to avoid an infinite loop
        // For instance, new opportunities may appear when reduced to a IF_ELSE_NULL
        return (newWhenPairs.size() < terms.size() % 2)
                ? newTerm.simplify(isInConstructionNodeInOptimizationPhase)
                : newTerm;
    }


    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms)
            throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }

    /**
     * Currently considered too dangerous to post-processed
     * as it may cause some functions to be called with invalid arguments.
     */
    @Override
    public boolean canBePostProcessed() {
        return false;
    }

    protected abstract ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory);
}
