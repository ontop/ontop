package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;

import java.util.List;

import static org.semanticweb.ontop.model.impl.GroundTermTools.castIntoGroundTerms;


public class GroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements GroundFunctionalTerm {

    protected GroundFunctionalTermImpl(ImmutableList<GroundTerm> terms, Predicate functor) {
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
    public ImmutableList<GroundTerm> getGroundTerms() {
        return (ImmutableList<GroundTerm>)(ImmutableList<?>)getTerms();
    }
}
