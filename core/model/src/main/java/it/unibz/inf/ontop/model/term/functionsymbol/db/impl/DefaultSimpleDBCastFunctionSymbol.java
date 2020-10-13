package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public class DefaultSimpleDBCastFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    @Nullable
    private final DBTermType inputType;
    private final DBFunctionSymbolSerializer serializer;

    protected DefaultSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType,
                                                DBTermType targetType, DBFunctionSymbolSerializer serializer) {
        super(inputBaseType.isAbstract()
                ? "to" + targetType
                : inputBaseType + "To" + targetType,
                inputBaseType, targetType);
        this.inputType = inputBaseType.isAbstract() ? null : inputBaseType;
        this.serializer = serializer;
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) {
        return termFactory.getDBConstant((constant).getValue(), getTargetType());
    }

    /**
     * Tries to simplify nested casts
     */
    @Override
    protected ImmutableTerm buildTermFromFunctionalTerm(ImmutableFunctionalTerm subTerm,
                                                        TermFactory termFactory, VariableNullability variableNullability) {
        if ((inputType != null) && inputType.equals(getTargetType()))
            return subTerm;

        if (subTerm.getFunctionSymbol() instanceof DBTypeConversionFunctionSymbol) {
            DBTypeConversionFunctionSymbol functionSymbol =
                    (DBTypeConversionFunctionSymbol) subTerm.getFunctionSymbol();

            ImmutableTerm subSubTerm = subTerm.getTerm(0);

            DBTermType targetType = getTargetType();

            if (functionSymbol.isSimple()) {
                return functionSymbol.getInputType()
                        .map(input -> input.equals(targetType)
                                ? subSubTerm
                                : termFactory.getDBCastFunctionalTerm(input, targetType, subSubTerm))
                        .orElseGet(() -> termFactory.getDBCastFunctionalTerm(targetType, subSubTerm));
            }
        }
        // Default
        return super.buildTermFromFunctionalTerm(subTerm, termFactory, variableNullability);
    }

    @Override
    protected ImmutableTerm buildFromVariable(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory, VariableNullability variableNullability) {
        if ((inputType != null) && inputType.equals(getTargetType()))
            return newTerms.get(0);
        else
            return super.buildFromVariable(newTerms, termFactory, variableNullability);
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.ofNullable(inputType);
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return getInputType().isPresent();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return getInputType().isPresent();
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    /**
     * Gets rid of the cast and simplifies the strict equality
     */
    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {

        // NB: incompatibility between the target DB types is assumed to be detected earlier. Not tested here.
        if ((inputType != null)
                && (otherTerm.getType() instanceof DBTermType)
                // TODO: remove this test once strict equalities will be enforced in SQL queries
                && inputType.areEqualitiesStrict()) {

            return perform2ndStepEvaluationStrictEqWithConstant(terms, otherTerm.getValue(), termFactory, variableNullability);
        }
        else
            return IncrementalEvaluation.declareSameExpression();
    }

    /**
     * After making the compatibility checks (2nd step)
     *
     * Low-level.
     */
    protected IncrementalEvaluation perform2ndStepEvaluationStrictEqWithConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                                 String otherLexicalValue,
                                                                                 TermFactory termFactory,
                                                                                 VariableNullability variableNullability) {
        assert inputType != null;
        Optional<Boolean> isValid = inputType.isValidLexicalValue(otherLexicalValue);
        if (!isValid.isPresent())
            return IncrementalEvaluation.declareSameExpression();

        if (isValid.get()) {
            ImmutableExpression newEquality = termFactory.getStrictEquality(
                    terms.get(0),
                    termFactory.getDBConstant(otherLexicalValue, inputType));

            return newEquality.evaluate(variableNullability, true);
        }
        else {
            ImmutableExpression newExpression = termFactory.getBooleanIfElseNull(
                    termFactory.getDBIsNotNull(terms.get(0)),
                    termFactory.getIsTrue(termFactory.getDBBooleanConstant(false)));

            return newExpression.evaluate(variableNullability, true);
        }
    }


}
