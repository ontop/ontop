package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DBBooleanCaseFunctionSymbolImpl extends DefaultDBCaseFunctionSymbol implements DBBooleanFunctionSymbol {
    protected DBBooleanCaseFunctionSymbolImpl(int arity, DBTermType dbBooleanType, DBTermType rootDBTermType,
                                              boolean doOrderingMatter) {
        super("BOOL_CASE" + arity + (doOrderingMatter ? "" : "_UNORDERED"), arity, dbBooleanType, rootDBTermType,
                doOrderingMatter);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ImmutableExpression simplifyValue(ImmutableTerm immutableTerm, VariableNullability variableNullability,
                                                TermFactory termFactory) {
        ImmutableTerm simplifiedTerm = immutableTerm.simplify(variableNullability);
        return (simplifiedTerm instanceof ImmutableExpression)
                ? (ImmutableExpression) simplifiedTerm
                : termFactory.getIsTrue((NonFunctionalTerm) simplifiedTerm);
    }

    @Override
    protected ImmutableFunctionalTerm buildCase(Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> newWhenPairs,
                                                ImmutableTerm defaultValue, TermFactory termFactory) {
        return termFactory.getDBBooleanCase(
                (Stream<Map.Entry<ImmutableExpression, ImmutableExpression>>)(Stream<?>) newWhenPairs,
                (ImmutableExpression) defaultValue, doOrderingMatter);
    }

    @Override
    public ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                     VariableNullability variableNullability) {
        if (!terms.stream().allMatch(t -> t instanceof ImmutableExpression))
            throw new MinorOntopInternalBugException("Was expecting all its arguments to be ImmutableExpression-s");

        ImmutableList<ImmutableExpression> twoVLExpressions = terms.stream()
                .map(t -> (ImmutableExpression) t)
                .map(t -> t.simplify2VL(variableNullability))
                .map(t -> (t instanceof ImmutableExpression)
                        ? (ImmutableExpression) t
                        : termFactory.getIsTrue((NonFunctionalTerm) t))
                .collect(ImmutableCollectors.toList());

        /*
         * Tries to simplify the CASE into a disjunction
         */
        Optional<ImmutableExpression> optionalSimplification = tryToReduceToDisjunctionOrConjunction(
                twoVLExpressions, variableNullability, termFactory);
        if (optionalSimplification.isPresent())
            return optionalSimplification.get()
                    .simplify2VL(variableNullability);

        ImmutableTerm newTerm = simplify(twoVLExpressions, termFactory, variableNullability);
        // Makes sure that the returned expression has been inform that "2VL simplifications" can be applied
        // Prevents an infinite loop
        if (newTerm instanceof ImmutableExpression) {
            ImmutableExpression newExpression = (ImmutableExpression) newTerm;
            if ((!this.equals(newExpression.getFunctionSymbol())) || (!terms.equals(newExpression.getTerms()))) {
                return newExpression.simplify2VL(variableNullability);
            }
        }
        if (newTerm.isNull())
            return termFactory.getDBBooleanConstant(false);
        return newTerm;
    }

    /**
     * Transforms, for instance,
     *
     *    - CASE_5(c1, IS_TRUE(TRUE), c2, IS_TRUE(TRUE), IS_TRUE(FALSE)) into OR_2(c1,c2)
     *    - CASE_5(c1, IS_TRUE(FALSE), c2, IS_TRUE(FALSE), IS_TRUE(TRUE)) into AND_2(NOT(c1),NOT(c2))
     *         if c1 and c2 are not nullable
     *
     */
    private Optional<ImmutableExpression> tryToReduceToDisjunctionOrConjunction(ImmutableList<ImmutableExpression> twoVLExpressions,
                                                                                VariableNullability variableNullability,
                                                                                TermFactory termFactory) {
        if (!doOrderingMatter) {
            ImmutableTerm defaultValue = extractDefaultValue(twoVLExpressions, termFactory);
            ImmutableExpression falseExpression = termFactory.getIsTrue(termFactory.getDBBooleanConstant(false));
            ImmutableExpression trueExpression = termFactory.getIsTrue(termFactory.getDBBooleanConstant(true));

            if (defaultValue.equals(falseExpression) || defaultValue.equals(trueExpression)) {
                boolean isFalseByDefault = defaultValue.equals(falseExpression);
                ImmutableExpression oppositeExpression = isFalseByDefault ? trueExpression : falseExpression;

                if (IntStream.range(0, twoVLExpressions.size() - 1)
                        .filter(i -> i % 2 == 1)
                        .mapToObj(twoVLExpressions::get)
                        .allMatch(e -> e.equals(oppositeExpression))) {

                    ImmutableSet<ImmutableExpression> conditions = IntStream.range(0, twoVLExpressions.size() -1)
                            .filter(i -> i % 2 == 0)
                            .mapToObj(twoVLExpressions::get)
                            .collect(ImmutableCollectors.toSet());

                    /*
                     * When the default value is TRUE, we have to make sure none of conditions is nullable
                     * because of intented use of a negation
                     *
                     * Recall that NOT(NULL) -> NULL while NOT(FALSE) -> TRUE
                     */
                    if (isFalseByDefault || conditions.stream()
                            .noneMatch(c -> c.isNullable(variableNullability.getNullableVariables()))) {

                        Stream<ImmutableExpression> newConditionStream = conditions.stream()
                                // Negated if the default value is TRUE
                                .map(c -> isFalseByDefault ? c : termFactory.getDBNot(c));

                        return isFalseByDefault
                                ? termFactory.getDisjunction(newConditionStream)
                                : termFactory.getConjunction(newConditionStream);
                    }
                }
            }
        }
        return Optional.empty();
    }

}
