package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;

import java.util.List;

import static org.semanticweb.ontop.model.impl.ImmutabilityTools.convertIntoImmutableTerm;

/**
 * Immutable implementation
 */
public class ImmutableFunctionalTermImpl extends AbstractFunctionalTermImpl
        implements ImmutableFunctionalTerm {

    private final ImmutableList<ImmutableTerm> terms;

    /**
     * Lazy cache for toString()
     */
    private String string;

    protected ImmutableFunctionalTermImpl(Predicate functor, ImmutableTerm... terms) {
        super(functor);
        this.terms = ImmutableList.<ImmutableTerm>builder().add(terms).build();
        string = null;
    }

    protected ImmutableFunctionalTermImpl(Predicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor);
        // No problem since the list is immutable
        this.terms = (ImmutableList<ImmutableTerm>)terms;
        string = null;
    }

    public ImmutableFunctionalTermImpl(Function functionalTermToClone) {
        this(functionalTermToClone.getFunctionSymbol(), convertTerms(functionalTermToClone));
    }

    private static ImmutableList<ImmutableTerm> convertTerms(Function functionalTermToClone) {
        ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
        for (Term term : functionalTermToClone.getTerms()) {
            builder.add(convertIntoImmutableTerm(term));
        }
        return builder.build();
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
    public ImmutableList<ImmutableTerm> getImmutableTerms() {
        return terms;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return ImmutableSet.copyOf(super.getVariables());
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
    public ImmutableSet<Variable> getReferencedVariables() {
        return ImmutableSet.copyOf(super.getReferencedVariables());
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
}
