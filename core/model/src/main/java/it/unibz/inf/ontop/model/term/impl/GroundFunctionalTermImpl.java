package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.GroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.GroundTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;


import java.util.List;


public class GroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements GroundFunctionalTerm {

    protected GroundFunctionalTermImpl(ImmutableList<? extends GroundTerm> terms, FunctionSymbol functor) {
        super(functor, terms);
    }

    @Override
    public ImmutableList<? extends GroundTerm> getTerms() {
        return (ImmutableList<? extends GroundTerm>)super.getTerms();
    }

    @Override
    public boolean isGround() {
        return true;
    }
}
