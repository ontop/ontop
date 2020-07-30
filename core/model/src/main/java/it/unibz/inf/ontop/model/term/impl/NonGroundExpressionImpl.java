package it.unibz.inf.ontop.model.term.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;

import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.checkNonGroundTermConstraint;

public class NonGroundExpressionImpl extends ImmutableExpressionImpl implements NonGroundFunctionalTerm {

    protected NonGroundExpressionImpl(TermFactory termFactory, BooleanFunctionSymbol functor, ImmutableTerm... terms) {
        super(termFactory, functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundExpressionImpl(BooleanFunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms,
                                      TermFactory termFactory) {
        super(functor, terms, termFactory);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }

    @Override
    public boolean isVar2VarEquality() {
        return getFunctionSymbol() instanceof DBStrictEqFunctionSymbol &&
                getTerms().size() == 2 &&
                getTerms().stream().allMatch(t -> t instanceof Variable);
    }
}
