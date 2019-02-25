package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsNullOrNotFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;

public class DefaultDBIfElseNullFunctionSymbol extends AbstractDBIfThenFunctionSymbol implements DBIfElseNullFunctionSymbol {

    protected DefaultDBIfElseNullFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        this("IF_ELSE_NULL", dbBooleanType, rootDBTermType);
    }

    protected DefaultDBIfElseNullFunctionSymbol(String name, DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super(name, 2, dbBooleanType, rootDBTermType);
    }

    @Override
    protected ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory) {
        return termFactory.getNullConstant();
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newPossibleValue = terms.get(1).simplify(variableNullability);

        if (newPossibleValue.isNull())
            return newPossibleValue;

        // TODO: evaluate in 2-value logic
        ImmutableExpression.Evaluation conditionEvaluation = ((ImmutableExpression) terms.get(0)).evaluate(variableNullability);

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
            if (functionalTerm.getFunctionSymbol() instanceof RDFTermFunctionSymbol) {

                return termFactory.getRDFFunctionalTerm(
                        termFactory.getIfElseNull(newCondition, functionalTerm.getTerm(0)),
                        termFactory.getIfElseNull(newCondition, functionalTerm.getTerm(1)))
                        .simplify(variableNullability);
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
     */
    protected boolean canBeReplacedByValue(ImmutableExpression conditionExpression, ImmutableTerm possibleValue,
                                           TermFactory termFactory) {
        ImmutableList<ImmutableExpression> conjuncts = conditionExpression.flattenAND()
                .collect(ImmutableCollectors.toList());

        if (conjuncts.stream().allMatch(c ->
                (c.getFunctionSymbol() instanceof DBIsNullOrNotFunctionSymbol)
                && !((DBIsNullOrNotFunctionSymbol) c.getFunctionSymbol()).isTrueWhenNull())) {
            ImmutableSet<ImmutableTerm> notNullTerms = conjuncts.stream()
                    .map(c -> c.getTerm(0))
                    .collect(ImmutableCollectors.toSet());

            return nullify(possibleValue, notNullTerms, termFactory)
                    .simplify()
                    .isNull();
        }
        return false;
    }

    /**
     * Recursive
     */
    private ImmutableTerm nullify(ImmutableTerm term,
                                  ImmutableSet<ImmutableTerm> termsToReplaceByNulls,
                                  TermFactory termFactory) {
        if (termsToReplaceByNulls.contains(term))
            return termFactory.getNullConstant();
        else if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            return termFactory.getImmutableFunctionalTerm(
                    functionalTerm.getFunctionSymbol(),
                    functionalTerm.getTerms().stream()
                            // Recursive
                        .map(t -> nullify(t, termsToReplaceByNulls, termFactory))
                        .collect(ImmutableCollectors.toList()));
        }
        else
            return term;
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
}
