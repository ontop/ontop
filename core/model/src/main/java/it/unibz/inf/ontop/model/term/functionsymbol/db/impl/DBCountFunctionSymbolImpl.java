package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;

import javax.annotation.Nonnull;

public class DBCountFunctionSymbolImpl extends AbstractDBAggregationFunctionSymbol {

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType inputType,
                                        @Nonnull DBTermType targetType,
                                        boolean isDistinct,
                                        @Nonnull DBFunctionSymbolSerializer serializer) {
        super(isDistinct ? "COUNT_DISTINCT_1" : "COUNT_1", ImmutableList.of(inputType), targetType, isDistinct, serializer);
    }

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType targetType,
                                        boolean isDistinct,
                                        @Nonnull DBFunctionSymbolSerializer serializer) {
        super(isDistinct ? "COUNT_DISTINCT_0" : "COUNT_0", ImmutableList.of(), targetType, isDistinct, serializer);
    }

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType inputType,
                                        @Nonnull DBTermType targetType, boolean isDistinct) {
        this(inputType, targetType, isDistinct,
                isDistinct
                        ? Serializers.getDistinctAggregationSerializer("COUNT")
                        : Serializers.getRegularSerializer("COUNT"));
    }

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType targetType, boolean isDistinct) {
        this(targetType, isDistinct, get0arySerializer(isDistinct));
    }

    protected static DBFunctionSymbolSerializer get0arySerializer(boolean isDistinct) {
        if (isDistinct)
            return (terms, termConverter, termFactory) -> "COUNT(DISTINCT(*))";
        else
            return (terms, termConverter, termFactory) -> "COUNT(*)";
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (getArity() == 0)
            return termFactory.getImmutableFunctionalTerm(this, newTerms);

        ImmutableTerm subTerm = newTerms.get(0);

        // If the argument is not nullable and the count is not distinct -> counts the numbers of rows
        if (!subTerm.isNullable(variableNullability.getNullableVariables()) && !isDistinct())
            return termFactory.getDBCount(false);

        if (subTerm instanceof ImmutableFunctionalTerm) {

            ImmutableFunctionalTerm functionalSubTerm = (ImmutableFunctionalTerm) subTerm;

            /*
             * If the argument is unary and injective, takes its sub-term
             */
            if (functionalSubTerm.getArity() == 1) {
                VariableGenerator uselessVariableGenerator = new VariableGeneratorImpl(ImmutableSet.of(), termFactory);

                boolean isInjective = functionalSubTerm.analyzeInjectivity(ImmutableSet.of(), variableNullability, uselessVariableGenerator)
                        .isPresent();
                if (isInjective)
                    // Recursive
                    return termFactory.getImmutableFunctionalTerm(this, functionalSubTerm.getTerm(0))
                            .simplify(variableNullability);
            }
        }

        // By default
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getDBIntegerConstant(0);
    }
}
