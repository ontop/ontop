package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.GroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.GroundTerm;


public class GroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements GroundFunctionalTerm {

    protected GroundFunctionalTermImpl(ImmutableList<? extends GroundTerm> terms, FunctionSymbol functor,
                                       TermFactory termFactory) {
        super(functor, terms, termFactory);
    }

    @Override
    public ImmutableList<? extends GroundTerm> getTerms() {
        return (ImmutableList<? extends GroundTerm>)super.getTerms();
    }

    @Override
    public boolean isGround() {
        return true;
    }

    @Override
    public boolean isDeterministic() {
        return getFunctionSymbol().isDeterministic()
                && getTerms().stream()
                .allMatch(GroundTerm::isDeterministic);
    }
}
