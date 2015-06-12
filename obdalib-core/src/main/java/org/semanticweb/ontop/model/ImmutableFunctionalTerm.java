package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

/**
 * Functional term that is declared as immutable.
 *
 * --> Implementations must not support any mutable operations of Function.
 *
 */
public interface ImmutableFunctionalTerm extends Function, ImmutableTerm {

    /**
     * Please use getImmutableTerms() instead
     */
    @Deprecated
    @Override
    public ImmutableList<Term> getTerms();

    public ImmutableList<ImmutableTerm> getImmutableTerms();

    @Override
    ImmutableTerm getTerm(int index);

    /**
     * Not supported (mutable operation)
     */
    @Deprecated
    @Override
    void setTerm(int index, Term term);

    @Override
    ImmutableSet<Variable> getVariables();

    /**
     * Not supported (mutable operation)
     */
    @Deprecated
    @Override
    void updateTerms(List<Term> literals);

    /**
     * Not supported (mutable operation)
     */
    @Deprecated
    @Override
    void setPredicate(Predicate p);

}
