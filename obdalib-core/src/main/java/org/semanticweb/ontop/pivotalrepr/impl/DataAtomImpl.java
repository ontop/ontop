package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.FunctionalTermImpl;
import org.semanticweb.ontop.model.impl.ImmutableFunctionalTermImpl;
import org.semanticweb.ontop.pivotalrepr.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.DataAtom;

import java.util.List;

public class DataAtomImpl extends ImmutableFunctionalTermImpl implements DataAtom {

    private final AtomPredicate predicate;

    protected DataAtomImpl(AtomPredicate predicate, List<Term> terms) {
        super(predicate, terms);
        this.predicate = predicate;
    }

    protected DataAtomImpl(AtomPredicate predicate, Term... terms) {
        super(predicate, terms);
        this.predicate = predicate;
    }

    @Override
    public AtomPredicate getPredicate() {
        return predicate;
    }

    @Override
    public int getEffectiveArity() {
        return getTerms().size();
    }

    @Override
    public boolean shareReferenceToTheSameAbstraction(DataAtom otherAtom) {
        if (!predicate.equals(getPredicate()))
            return false;

        if (getEffectiveArity() != otherAtom.getEffectiveArity())
            return false;

        return true;
    }

}
