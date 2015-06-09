package org.semanticweb.ontop.model.impl;

import org.semanticweb.ontop.model.ImmutableFunctionalTerm;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;

import java.util.List;

/**
 * Immutable implementation
 */
public class ImmutableFunctionalTermImpl extends FunctionalTermImpl
        implements ImmutableFunctionalTerm {
    protected ImmutableFunctionalTermImpl(Predicate functor, Term... terms) {
        super(functor, terms);
    }

    protected ImmutableFunctionalTermImpl(Predicate functor, List<Term> terms) {
        super(functor, terms);
    }

    @Override
    public void setPredicate(Predicate predicate) {
        throw new UnsupportedOperationException("A ImmutableFunctionalTermImpl is immutable.");
    }

    @Override
    public void setTerm(int index, Term newTerm) {
        throw new UnsupportedOperationException("A ImmutableFunctionalTermImpl is immutable.");
    }

    @Override
    public void updateTerms(List<Term> newterms) {
        throw new UnsupportedOperationException("A ImmutableFunctionalTermImpl is immutable.");
    }

}
