package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Operations that are available to all Substitution classes.
 *
 * @param <T> the type of elements in the range of the substitution
 */

public interface SubstitutionBasicOperations<T extends ImmutableTerm> {

    T applyToTerm(Substitution<? extends T> substitution, T t);

    ImmutableList<T> applyToTerms(Substitution<? extends T> substitution, ImmutableList<? extends T> terms);

    ImmutableMap<Integer, T> applyToTerms(Substitution<? extends T> substitution, ImmutableMap<Integer, ? extends T> argumentMap);

    /**
     *  Viewing a substitution as a function (takes a term, returns a term).
     *  this method yield the substitution "(g o f)", where (g o f)(x) = g(f(x))
     *  Note that we assume f(x) = x if x is not explicitly in the domain of substitution f
     * @param g
     * @param f
     * @return
     */

    Substitution<T> compose(Substitution<? extends T> g, Substitution<? extends T> f);



    T rename(Substitution<Variable> renaming, T t);

    Substitution<T> rename(InjectiveSubstitution<Variable> renaming, Substitution<? extends T> substitution);
}
