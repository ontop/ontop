package it.unibz.inf.ontop.model.term.impl;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;

public class ImmutabilityTools {

    private final TermFactory termFactory;

    /**
     * TODO: make it private
     */
    @Inject
    public ImmutabilityTools(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * In case the term is functional, creates an immutable copy of it.
     */
    public ImmutableTerm convertIntoImmutableTerm(Term term) {
        if (term instanceof Function) {
            if (term instanceof ImmutableFunctionalTerm) {
                return (ImmutableTerm) term;
            } else if (term instanceof Expression) {
                Expression expression = (Expression) term;
                return termFactory.getImmutableExpression(expression);
            } else {
                Function functionalTerm = (Function) term;
                return termFactory.getImmutableFunctionalTerm(functionalTerm);
            }
        }
        /**
         * Other terms (constant and variable) are immutable.
         */
        return (ImmutableTerm) term;
    }

    public static VariableOrGroundTerm convertIntoVariableOrGroundTerm(Term term) {
        if (term instanceof Variable) {
            return (Variable) term;
        } else if (GroundTermTools.isGroundTerm(term)) {
            return GroundTermTools.castIntoGroundTerm(term);
        } else {
            throw new IllegalArgumentException("Not a variable nor a ground term: " + term);
        }
    }

    /**
     * This method takes a immutable term and convert it into an old mutable function.
     */
    public Function convertToMutableFunction(ImmutableFunctionalTerm functionalTerm) {

        Predicate pred = functionalTerm.getFunctionSymbol();
        ImmutableList<Term> otherTerms = functionalTerm.getTerms();
        List<Term> mutableList = new ArrayList<>();
        Iterator<Term> iterator = otherTerms.iterator();
        while (iterator.hasNext()) {

            Term nextTerm = iterator.next();
            if (nextTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm term2Change = (ImmutableFunctionalTerm) nextTerm;
                Function newTerm = convertToMutableFunction(term2Change);
                mutableList.add(newTerm);
            } else {
                mutableList.add(nextTerm);
            }

        }
        Function mutFunc = termFactory.getFunction(pred, mutableList);
        return mutFunc;

    }

    /**
     * This method takes a immutable boolean term and convert it into an old mutable boolean function.
     */
    public Expression convertToMutableBooleanExpression(ImmutableExpression booleanExpression) {

        OperationPredicate pred = (OperationPredicate) booleanExpression.getFunctionSymbol();
        ImmutableList<Term> otherTerms = booleanExpression.getTerms();
        List<Term> mutableList = new ArrayList<>();

        Iterator<Term> iterator = otherTerms.iterator();
        while (iterator.hasNext()) {

            Term nextTerm = iterator.next();
            if (nextTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm term2Change = (ImmutableFunctionalTerm) nextTerm;
                Function newTerm = convertToMutableFunction(term2Change);
                mutableList.add(newTerm);
            } else {
                mutableList.add(nextTerm);
            }

        }
        Expression mutFunc = termFactory.getExpression(pred, mutableList);
        return mutFunc;

    }

    public Optional<ImmutableExpression> foldBooleanExpressions(
            ImmutableList<ImmutableExpression> conjunctionOfExpressions) {
        final int size = conjunctionOfExpressions.size();
        switch (size) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(conjunctionOfExpressions.get(0));
            case 2:
                return Optional.of(termFactory.getImmutableExpression(
                        AND,
                        conjunctionOfExpressions));
            default:
                // Non-final
                ImmutableExpression cumulativeExpression = termFactory.getImmutableExpression(
                        AND,
                        conjunctionOfExpressions.get(0),
                        conjunctionOfExpressions.get(1));
                for (int i = 2; i < size; i++) {
                    cumulativeExpression = termFactory.getImmutableExpression(
                            AND,
                            cumulativeExpression,
                            conjunctionOfExpressions.get(i));
                }
                return Optional.of(cumulativeExpression);
        }
    }

    public Optional<ImmutableExpression> foldBooleanExpressions(
            ImmutableExpression... conjunctionOfExpressions) {
        return foldBooleanExpressions(ImmutableList.copyOf(conjunctionOfExpressions));
    }

    public Optional<ImmutableExpression> foldBooleanExpressions(
            Stream<ImmutableExpression> conjunctionOfExpressions) {
        return foldBooleanExpressions(conjunctionOfExpressions
                .collect(ImmutableCollectors.toList()));
    }

    public ImmutableSet<ImmutableExpression> retainVar2VarEqualityConjuncts(ImmutableExpression expression) {
        return filterOuterMostConjuncts(e -> e.isVar2VarEquality(), expression);
    }

    public ImmutableSet<ImmutableExpression> discardVar2VarEqualityConjuncts(ImmutableExpression expression) {
        return filterOuterMostConjuncts(e -> !(e.isVar2VarEquality()), expression);
    }

    private ImmutableSet<ImmutableExpression> filterOuterMostConjuncts(java.util.function.Predicate<ImmutableExpression> filterMethod,
                                                                              ImmutableExpression expression) {

        ImmutableSet<ImmutableExpression> conjuncts = expression.flattenAND();
        if (conjuncts.size() > 1) {
            ImmutableList<ImmutableExpression> filteredConjuncts = conjuncts.stream()
                    .filter(filterMethod)
                    .collect(ImmutableCollectors.toList());
            switch (filteredConjuncts.size()) {
                case 0:
                    return ImmutableSet.of();
                case 1:
                    return ImmutableSet.of(filteredConjuncts.iterator().next());
                default:
                    return ImmutableSet.copyOf(filteredConjuncts);
            }
        }
        return filterMethod.test(expression) ?
                ImmutableSet.of(expression) :
                ImmutableSet.of();
    }
}
