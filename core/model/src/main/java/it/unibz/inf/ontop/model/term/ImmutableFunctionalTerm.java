package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.util.List;

/**
 * Functional term that is declared as immutable.
 *
 * --> Implementations must not support any mutable operations of Function.
 *
 */
public interface ImmutableFunctionalTerm extends Function, NonVariableTerm {

    /**
     * Please use getArguments() instead
     */
    @Deprecated
    @Override
    ImmutableList<Term> getTerms();

    /**
     * Immutable version of getTerms().
     */
    ImmutableList<? extends ImmutableTerm> getArguments();

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
