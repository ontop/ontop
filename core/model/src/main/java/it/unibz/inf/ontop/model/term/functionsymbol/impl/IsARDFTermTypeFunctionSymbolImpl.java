package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;

/**
 * TODO: find a better name!
 */
public class IsARDFTermTypeFunctionSymbolImpl extends BooleanFunctionSymbolImpl {

    protected IsARDFTermTypeFunctionSymbolImpl(MetaRDFTermType metaRDFTermType, DBTermType dbBooleanTermType) {
        super("isA", ImmutableList.of(metaRDFTermType), dbBooleanTermType);
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return true;
    }
}
