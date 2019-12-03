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
import java.util.stream.Stream;

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
        if ((inputType != null)
                && (otherTerm.getType() instanceof DBTermType)
                // TODO: remove this test once strict equalities will be enforced in SQL queries
                && areCompatibleForStrictEq(inputType, (DBTermType) otherTerm.getType())) {
            ImmutableExpression newEquality = termFactory.getStrictEquality(
                    terms.get(0),
                    termFactory.getDBConstant(otherTerm.getValue(), inputType));

            return newEquality.evaluate(variableNullability, true);
        }
        else
            return IncrementalEvaluation.declareSameExpression();
    }

    /**
     * TEMPORARY: only returns true when we are sure that SQL equalities are strict for these types.
     *
     * TODO: remove it once strict equalities will be enforced in SQL queries
     */
    private boolean areCompatibleForStrictEq(DBTermType type1, DBTermType type2) {
        return Stream.of(type1.areEqualitiesStrict(type2), type2.areEqualitiesStrict(type1))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce((b1, b2) -> b1 && b2)
                .orElse(false);
    }


}
