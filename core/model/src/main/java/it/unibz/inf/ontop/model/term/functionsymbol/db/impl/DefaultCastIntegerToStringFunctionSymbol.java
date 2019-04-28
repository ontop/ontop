package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * Can simplify itself in case of strict equalities with a constant
 */
public class DefaultCastIntegerToStringFunctionSymbol extends DefaultSimpleDBCastFunctionSymbol {

    @Nonnull
    private final DBTermType inputType;
    private final Pattern pattern;

    protected DefaultCastIntegerToStringFunctionSymbol(@Nonnull DBTermType inputType, DBTermType dbStringType,
                                                       DBFunctionSymbolSerializer serializer) {
        super(inputType, dbStringType, serializer);
        this.inputType = inputType;
        if (inputType.isAbstract())
            throw new IllegalArgumentException("Was expecting a concrete input type");
        this.pattern = Pattern.compile("^[0+]\\d+");
    }

    /**
     * Gets rid of the cast and simplifies the strict equality
     */
    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        String otherValue = otherTerm.getValue();
        // Positive non-null numbers normally does not start with + or by 0
        if (pattern.matcher(otherValue).matches())
            return IncrementalEvaluation.declareSameExpression();

        ImmutableExpression newEquality = termFactory.getStrictEquality(
                terms.get(0),
                termFactory.getDBConstant(otherTerm.getValue(), inputType));

        return newEquality.evaluate(variableNullability, true);
    }
}
