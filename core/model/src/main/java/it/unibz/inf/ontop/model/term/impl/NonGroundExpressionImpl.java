package it.unibz.inf.ontop.model.term.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.EQ;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.checkNonGroundTermConstraint;

public class NonGroundExpressionImpl extends ImmutableExpressionImpl implements NonGroundFunctionalTerm {

    protected NonGroundExpressionImpl(BooleanFunctionSymbol functor, ImmutableTerm... terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundExpressionImpl(BooleanFunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }

    @Override
    public boolean isVar2VarEquality() {
        return getFunctionSymbol().equals(EQ) &&
                getTerms().size() == 2 &&
                getTerms().stream().allMatch(t -> t instanceof Variable);
    }
}
