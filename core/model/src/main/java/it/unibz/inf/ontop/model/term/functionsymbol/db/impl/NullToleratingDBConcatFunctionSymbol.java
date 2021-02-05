package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

public class NullToleratingDBConcatFunctionSymbol extends AbstractDBConcatFunctionSymbol {

    protected NullToleratingDBConcatFunctionSymbol(String nameInDialect, int arity, DBTermType dbStringType,
                                                   DBTermType rootDBTermType, boolean isOperator) {
        super(nameInDialect, arity, dbStringType, rootDBTermType,
                isOperator
                        ? Serializers.getOperatorSerializer(nameInDialect)
                        : Serializers.getRegularSerializer(nameInDialect));
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    /**
     * Never returns NULL
     */
    public IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                                   VariableNullability variableNullability) {
        return IncrementalEvaluation.declareIsTrue();
    }

    @Override
    protected String extractString(Constant constant) {
        return constant.isNull()
                ? ""
                : constant.getValue();
    }
}
