package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsNullOrNotFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Function;

public class DefaultDBIfElseNullFunctionSymbol extends AbstractDBIfThenFunctionSymbol implements DBIfElseNullFunctionSymbol {

    protected DefaultDBIfElseNullFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        this("IF_ELSE_NULL", dbBooleanType, rootDBTermType);
    }

    protected DefaultDBIfElseNullFunctionSymbol(String name, DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super(name, 2, dbBooleanType, rootDBTermType, false);
    }

    @Override
    protected ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory) {
        return termFactory.getNullConstant();
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm firstTerm = terms.get(0);
        ImmutableTerm newPossibleValue = terms.get(1).simplify(variableNullability);

        if (firstTerm.isNull() || newPossibleValue.isNull())
            return termFactory.getNullConstant();

        ImmutableExpression condition = Optional.of(firstTerm)
                .filter(t -> t instanceof ImmutableExpression)
                .map(t -> (ImmutableExpression) t)
                .orElseThrow(() -> new MinorOntopInternalBugException("The first term of an IF_ELSE_NULL " +
                        "was expected to be an expression"));

        ImmutableExpression.Evaluation conditionEvaluation = condition.evaluate2VL(variableNullability);

        Optional<ImmutableTerm> optionalSimplifiedTerm = conditionEvaluation.getValue()
                .map(v -> {
                    switch (v) {
                        case TRUE:
                            return newPossibleValue;
                        case FALSE:
                        case NULL:
                        default:
                            return termFactory.getNullConstant();
                    }
                });
        if (optionalSimplifiedTerm.isPresent())
            return optionalSimplifiedTerm.get();

        ImmutableExpression newCondition = conditionEvaluation.getExpression()
                .orElseThrow(() -> new MinorOntopInternalBugException("Inconsistent evaluation"));
        return simplify(newCondition, newPossibleValue, termFactory, variableNullability);
    }

    protected ImmutableTerm simplify(ImmutableExpression newCondition, ImmutableTerm newThenValue, TermFactory termFactory,
                                     VariableNullability variableNullability) {
        if (canBeReplacedByValue(newCondition, newThenValue, termFactory)) {
            return newThenValue;
        }

        if (newThenValue instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) newThenValue;
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
            if (functionSymbol instanceof RDFTermFunctionSymbol) {

                return termFactory.getRDFFunctionalTerm(
                        termFactory.getIfElseNull(newCondition, functionalTerm.getTerm(0)),
                        termFactory.getIfElseNull(newCondition, functionalTerm.getTerm(1)))
                        .simplify(variableNullability);
            }
            /*
             * Lifts the RDFTermTypeFunctionSymbol above
             */
            else if (functionSymbol instanceof RDFTermTypeFunctionSymbol) {
                return termFactory.getImmutableFunctionalTerm(
                        functionSymbol,
                        termFactory.getImmutableFunctionalTerm(this, newCondition, functionalTerm.getTerm(0)));
            }
        }
        else if (newThenValue instanceof RDFConstant) {
            RDFConstant constant = (RDFConstant) newThenValue;

            return termFactory.getRDFFunctionalTerm(
                    termFactory.getIfElseNull(newCondition, termFactory.getDBStringConstant(constant.getValue())),
                    termFactory.getIfElseNull(newCondition, termFactory.getRDFTermTypeConstant(constant.getType())))
                    .simplify(variableNullability);
        }
        return termFactory.getImmutableFunctionalTerm(this, newCondition, newThenValue);
    }

    /**
     * When the condition is only composed of IS_NOT_NULL expressions, see if the IF_ELSE_NULL(...) can be replaced
     * by its "then" value
     *
     * TODO: group together terms that are known to be nullable together to detect more cases
     */
    protected boolean canBeReplacedByValue(ImmutableExpression conditionExpression, ImmutableTerm possibleValue,
                                           TermFactory termFactory) {
        return conditionExpression.flattenAND()
                .allMatch(c -> isEnforced(c, possibleValue, termFactory));
    }

    private boolean isEnforced(ImmutableExpression subConditionExpression, ImmutableTerm possibleValue,
                               TermFactory termFactory) {
        return Optional.of(subConditionExpression)
                .filter(e -> (e.getFunctionSymbol() instanceof DBIsNullOrNotFunctionSymbol)
                        && !((DBIsNullOrNotFunctionSymbol) e.getFunctionSymbol()).isTrueWhenNull())
                .filter(e -> Nullifiers.nullify(possibleValue, e.getTerm(0), termFactory)
                        .simplify()
                        .isNull())
                .isPresent();
    }

    /**
     * Only looks if the second argument is guaranteed to be post-processed or not
     * since the first argument (the condition expression) will always be evaluated.
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return extractSubFunctionalTerms(arguments.subList(1, 2))
                .allMatch(ImmutableFunctionalTerm::canBePostProcessed);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getIfThenElse(
                        (ImmutableExpression) terms.get(0),
                        terms.get(1),
                        termFactory.getNullConstant()));
    }

    @Override
    public ImmutableExpression liftUnaryBooleanFunctionSymbol(ImmutableList<? extends ImmutableTerm> ifElseNullTerms,
                                                              BooleanFunctionSymbol unaryBooleanFunctionSymbol,
                                                              TermFactory termFactory) {
        if (unaryBooleanFunctionSymbol.getArity() != 1)
            throw new IllegalArgumentException("unaryBooleanFunctionSymbol was expected to be unary");

        if (ifElseNullTerms.size() != 2)
            throw new IllegalArgumentException("ifElseNullTerms was expected to have 2 values");

        ImmutableExpression condition = Optional.of(ifElseNullTerms.get(0))
                .filter(c -> c instanceof ImmutableExpression)
                .map(c -> (ImmutableExpression) c)
                .orElseThrow(() -> new IllegalArgumentException("The first term in ifElseNullTerms was expected " +
                        "to be an ImmutableExpression"));

        return termFactory.getBooleanIfElseNull(condition,
                termFactory.getImmutableExpression(unaryBooleanFunctionSymbol, ifElseNullTerms.get(1)));
    }

    @Override
    public IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                                   VariableNullability variableNullability) {
        ImmutableExpression condition = Optional.of(terms.get(0))
                .filter(t -> t instanceof ImmutableExpression)
                .map(t -> (ImmutableExpression) t)
                .orElseThrow(() -> new MinorOntopInternalBugException("Was expected an immutable expression as first term"));
        ImmutableTerm thenValue = terms.get(1);

        return termFactory.getConjunction(
                condition,
                termFactory.getDBIsNotNull(condition),
                termFactory.getDBIsNotNull(thenValue))
                .evaluate(variableNullability, true);
    }

    @Override
    public Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> analyzeInjectivity(
            ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonFreeVariables,
            VariableNullability variableNullability, VariableGenerator variableGenerator, TermFactory termFactory) {

        ImmutableTerm thenValue = arguments.get(1);
        /*
         * When the thenValue is non-null and a constant
         */
        if (thenValue instanceof NonNullConstant) {
            ImmutableExpression thenCondition = Optional.of(arguments.get(0))
                    .filter(c -> c instanceof ImmutableExpression)
                    .map(c -> (ImmutableExpression)c)
                    .orElseThrow(() -> new IllegalArgumentException("was expected an ImmutableExpression as first sub-term"));

            /*
             * If the thenCondition is non-nullable (like, e.g., IS_NOT_NULL)
             */
            if (termFactory.getDBIsNotNull(thenCondition)
                    .evaluate(variableNullability)
                    .getValue()
                    .filter(v -> v.equals(ImmutableExpression.Evaluation.BooleanValue.TRUE))
                    .isPresent()) {
                Variable newVariable = variableGenerator.generateNewVariable();
                ImmutableFunctionalTerm newFunctionalTerm = termFactory.getIfElseNull(
                        termFactory.getIsTrue(newVariable),
                        thenValue);
                ImmutableMap<Variable, ImmutableFunctionalTerm> subTermSubstitutionMap = ImmutableMap.of(newVariable, thenCondition);

                return Optional.of(termFactory.getFunctionalTermDecomposition(newFunctionalTerm, subTermSubstitutionMap));
            }

        }
        return super.analyzeInjectivity(arguments, nonFreeVariables, variableNullability, variableGenerator, termFactory);
    }

    @Override
    public ImmutableExpression pushDownExpression(ImmutableExpression expression, int indexOfDBIfThenFunctionSymbol,
                                                  TermFactory termFactory) {
        BooleanFunctionSymbol booleanFunctionSymbol = expression.getFunctionSymbol();

        if ((booleanFunctionSymbol.getArity() == 1) &&
                termFactory.getImmutableExpression(booleanFunctionSymbol, termFactory.getNullConstant())
                .simplify()
                .isNull()) {
            ImmutableList<? extends ImmutableTerm> ifThenArguments = Optional.of(expression.getTerm(indexOfDBIfThenFunctionSymbol))
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> ((ImmutableFunctionalTerm) t).getTerms())
                    .orElseThrow(() -> new IllegalArgumentException("Wrong index"));

            return liftUnaryBooleanFunctionSymbol(ifThenArguments, booleanFunctionSymbol, termFactory);
        }
        else
            return super.pushDownExpression(expression, indexOfDBIfThenFunctionSymbol, termFactory);
    }
}
