package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.*;

import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable implementation
 */
public abstract class ImmutableFunctionalTermImpl extends AbstractFunctionalTermImpl
        implements ImmutableFunctionalTerm {

    private final ImmutableList<? extends ImmutableTerm> terms;

    /**
     * Lazy cache for toString()
     */
    private String string;

    protected ImmutableFunctionalTermImpl(Predicate functor, ImmutableTerm... terms) {
        this(functor, ImmutableList.<ImmutableTerm>builder().add(terms).build());
    }

    protected ImmutableFunctionalTermImpl(Predicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor);
        // No problem since the list is immutable
        this.terms = terms;
        string = null;

        if (functor.getArity() != terms.size()) {
            throw new IllegalArgumentException("Arity violation: " + functor + " was expecting " + functor.getArity()
            + ", not " + terms.size());
        }
    }


    @Override
    public ImmutableList<Term> getTerms() {
        return (ImmutableList<Term>)(ImmutableList<?>) terms;
    }

    @Override
    public ImmutableTerm getTerm(int index) {
        return terms.get(index);
    }

    @Override
    public ImmutableList<? extends ImmutableTerm> getArguments() {
        return terms;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return ImmutableSet.copyOf(super.getVariables());
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return super.getVariables().stream();
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

    @Override
    public ImmutableFunctionalTerm clone() {
        return this;
    }

    /**
     * Cached toString()
     */
    @Override
    public String toString() {
        if (string == null) {
            string = super.toString();
        }
        return string;
    }

    /**
     * A bit hacky: only for the functional term
     * that derives from AbstractFunctionalTermImpl
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Function) {
            return toString().equals(other.toString());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
