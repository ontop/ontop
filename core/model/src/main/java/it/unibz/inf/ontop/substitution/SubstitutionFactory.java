package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface SubstitutionFactory {

    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableMap<Variable, T> newSubstitutionMap);
    <T extends ImmutableTerm, U> ImmutableSubstitution<T> getSubstitutionWithIdentityEntries(Collection<U> entries, Function<U, Variable> variableProvider, Function<U,T> termProvider);

    <T extends ImmutableTerm, U> ImmutableSubstitution<T> getSubstitution(Collection<U> entries, Function<U, Variable> variableProvider, Function<U,T> termProvider);

    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2,
                                                                       Variable k3, T v3);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution();

    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableList<Variable> variables, ImmutableList<? extends T> values);

   ImmutableSubstitution<ImmutableTerm> getNullSubstitution(Stream<Variable> variables);

    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(ImmutableMap<Variable, Variable> substitutionMap);

    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Stream<Variable> stream, Function<Variable, Variable> transformer);

    InjectiveVar2VarSubstitution generateNotConflictingRenaming(VariableGenerator variableGenerator,
                                                                ImmutableSet<Variable> variables);

    <T extends ImmutableTerm> ImmutableSubstitution<T> replace(ImmutableSubstitution<T> substitution, Variable variable, T newValue);

    /**
     *
     * @param substitution1
     * @param substitution2
     * @return
     * @param <T>
     * @throws IllegalArgumentException if the substitutions do not agree on one of the variables
     */
    <T extends ImmutableTerm> ImmutableSubstitution<T> union(ImmutableSubstitution<? extends T> substitution1, ImmutableSubstitution<? extends T> substitution2);

    /**
     *  Viewing a substitution as a function (takes a term, returns a term).
     *  this method yield the substitution "(g o f)", where (g o f)(x) = g(f(x))
     *  Note that we assume f(x) = x if x is not explicitly in the domain of substitution f
     * @param g
     * @param f
     * @return
     * @param <T>
     */
    <T extends ImmutableTerm> ImmutableSubstitution<T> compose(ImmutableSubstitution<? extends T> g, ImmutableSubstitution<? extends T> f);

    /**
     * { (x,y) | (x,y) \in (g o f), x not excluded }
     * Returns Optional.empty() when the resulting substitution is not injective.
     * Variables to exclude from the domain are typically fresh temporary variables that can be ignored.
     * Ignoring them is sufficient in many cases to guarantee that the substitution is injective.
     */
    InjectiveVar2VarSubstitution compose(InjectiveVar2VarSubstitution g, InjectiveVar2VarSubstitution f,
                                                                             Set<Variable> variablesToExcludeFromTheDomain);

}
