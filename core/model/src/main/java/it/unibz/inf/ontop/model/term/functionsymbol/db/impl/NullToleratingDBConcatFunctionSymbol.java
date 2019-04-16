package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
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
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }
}
