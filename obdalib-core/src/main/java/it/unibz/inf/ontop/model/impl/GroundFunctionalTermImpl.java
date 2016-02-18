package it.unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;

import java.util.List;

import static it.unibz.inf.ontop.model.impl.GroundTermTools.castIntoGroundTerms;


public class GroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements GroundFunctionalTerm {

    protected GroundFunctionalTermImpl(ImmutableList<? extends GroundTerm> terms, Predicate functor) {
        super(functor, terms);
    }

    protected GroundFunctionalTermImpl(Predicate functor, List<? extends ImmutableTerm> terms)
            throws GroundTermTools.NonGroundTermException {
        this(castIntoGroundTerms(terms), functor);
    }

    public GroundFunctionalTermImpl(Function functionalTermToClone) throws GroundTermTools.NonGroundTermException {
        this(functionalTermToClone.getFunctionSymbol(), castIntoGroundTerms(functionalTermToClone.getTerms()));
    }


    @Override
    public ImmutableList<? extends GroundTerm> getArguments() {
        return (ImmutableList<? extends GroundTerm>)super.getArguments();
    }

    @Override
    public boolean isGround() {
        return true;
    }
}
