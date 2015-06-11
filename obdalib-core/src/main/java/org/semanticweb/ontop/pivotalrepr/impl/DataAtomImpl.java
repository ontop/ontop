package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.impl.ImmutableFunctionalTermImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.pivotalrepr.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.DataAtom;

public class DataAtomImpl extends ImmutableFunctionalTermImpl implements DataAtom {

    private final AtomPredicate predicate;

    protected DataAtomImpl(AtomPredicate predicate, ImmutableList<VariableImpl> variables) {
        super(predicate, (ImmutableList<ImmutableTerm>)(ImmutableList<?>)variables);
        this.predicate = predicate;
    }

    protected DataAtomImpl(AtomPredicate predicate, VariableImpl... variables) {
        super(predicate, variables);
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
    public boolean isEquivalent(DataAtom otherAtom) {
        if (!predicate.equals(getPredicate()))
            return false;

        if (getEffectiveArity() != otherAtom.getEffectiveArity())
            return false;

        return true;
    }

    @Override
    public ImmutableList<VariableImpl> getVariableTerms() {
        return (ImmutableList<VariableImpl>)(ImmutableList<?>)getImmutableTerms();
    }
}
