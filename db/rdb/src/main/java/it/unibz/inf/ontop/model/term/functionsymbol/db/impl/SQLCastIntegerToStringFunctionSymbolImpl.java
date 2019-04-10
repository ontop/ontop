package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

/**
 * Can simplify itself in case of strict equalities with a constant
 */
public class SQLCastIntegerToStringFunctionSymbolImpl extends DefaultSQLSimpleDBCastFunctionSymbol {

    @Nonnull
    private final DBTermType inputType;

    protected SQLCastIntegerToStringFunctionSymbolImpl(@Nonnull DBTermType inputType, DBTermType dbStringType) {
        super(inputType, dbStringType);
        this.inputType = inputType;
        if (inputType.isAbstract())
            throw new IllegalArgumentException("Was expecting a concrete input type");
    }

    /**
     * Gets rid of the cast and simplifies the strict equality
     */
    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        String otherValue = otherTerm.getValue();
        // Positive numbers normally does not start with +
        if (otherValue.startsWith("+"))
            return IncrementalEvaluation.declareSameExpression();

        ImmutableExpression newEquality = termFactory.getStrictEquality(
                terms.get(0),
                termFactory.getDBConstant(otherTerm.getValue(), inputType));

        return newEquality.evaluate(variableNullability, true);
    }
}
