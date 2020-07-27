package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class NullRejectingDBConcatFunctionSymbol extends AbstractDBConcatFunctionSymbol {


    protected NullRejectingDBConcatFunctionSymbol(String nameInDialect, int arity, DBTermType dbStringType,
                                                  DBTermType rootDBTermType, boolean isOperator) {
        super(nameInDialect, arity, dbStringType, rootDBTermType,
                isOperator
                        ? Serializers.getOperatorSerializer(nameInDialect)
                        : Serializers.getRegularSerializer(nameInDialect));
    }

    protected NullRejectingDBConcatFunctionSymbol(String nameInDialect, int arity, DBTermType dbStringType,
                                                  DBTermType rootDBTermType, DBFunctionSymbolSerializer serializer) {
        super(nameInDialect, arity, dbStringType, rootDBTermType, serializer);
    }

    @Override
    protected String extractString(Constant constant) {
        if (constant.isNull())
            throw new MinorOntopInternalBugException("Was expecting a non-null constant. Should be reached this point");
        return constant.getValue();
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }
}
