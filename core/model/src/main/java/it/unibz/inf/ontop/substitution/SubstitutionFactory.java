package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface SubstitutionFactory {

    <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, Substitution<T>> toSubstitution();

    <T extends ImmutableTerm> Collector<Variable, ?, Substitution<T>> toSubstitution(Function<Variable, ? extends T> termMapper);

    Collector<Variable, ?, InjectiveSubstitution<Variable>> toFreshRenamingSubstitution(VariableGenerator variableGenerator);

    <T extends ImmutableTerm, U> Collector<U, ?, Substitution<T>> toSubstitution(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper);

    <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, Substitution<T>> toSubstitutionSkippingIdentityEntries();

    <T extends ImmutableTerm, U> Collector<U, ?, Substitution<T>> toSubstitutionSkippingIdentityEntries(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper);



    <T extends ImmutableTerm> Substitution<T> getSubstitution();
    <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable v1, T t1);
    <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable v1, T t1, Variable v2, T t2);
    <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable v1, T t1, Variable v2, T t2, Variable v3, T t3);
    <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable v1, T t1, Variable v2, T t2, Variable v3, T t3, Variable v4, T t4);


    <T extends ImmutableTerm> Substitution<T> getSubstitution(ImmutableList<Variable> variables, ImmutableList<? extends T> values);

    @FunctionalInterface
    interface FunctionThrowsExceptions<U, T, E extends Throwable> {
        T apply(U arg) throws E;
    }

    <T extends ImmutableTerm, U, E extends Throwable> Substitution<T> getSubstitutionThrowsExceptions(Collection<U> entries, Function<U, Variable> variableProvider, FunctionThrowsExceptions<U, T, E> termProvider) throws E;



    InjectiveSubstitution<Variable> extractAnInjectiveVar2VarSubstitutionFromInverseOf(Substitution<Variable> substitution);


    InjectiveSubstitution<Variable> generateNotConflictingRenaming(VariableGenerator variableGenerator, ImmutableSet<Variable> variables);


    /**
     * Applies the renaming on the keys and values of the given substitution.
     */
    default Substitution<ImmutableTerm> rename(InjectiveSubstitution<Variable> renaming, Substitution<?> substitution) { return onImmutableTerms().rename(renaming, substitution); }

    /**
     *
     * @param substitution1
     * @param substitution2
     * @return
     * @param <T>
     * @throws IllegalArgumentException if the substitutions do not agree on one of the variables
     */
    <T extends ImmutableTerm> Substitution<T> union(Substitution<? extends T> substitution1, Substitution<? extends T> substitution2);

    <T extends ImmutableTerm> Substitution<T> covariantCast(Substitution<? extends T> substitution);


    default Optional<Substitution<ImmutableTerm>> unify(ImmutableTerm t1, ImmutableTerm t2) { return onImmutableTerms().unify(t1, t2); }

    InjectiveSubstitution<Variable> getPrioritizingRenaming(Substitution<?> substitution, ImmutableSet<Variable> priorityVariables);



    SubstitutionOperations<NonFunctionalTerm> onNonFunctionalTerms();

    SubstitutionOperations<VariableOrGroundTerm> onVariableOrGroundTerms();

    SubstitutionOperations<Variable> onVariables();

    SubstitutionBasicOperations<NonGroundTerm> onNonGroundTerms();

    SubstitutionBasicOperations<NonConstantTerm> onNonConstantTerms();

    SubstitutionOperations<ImmutableTerm> onImmutableTerms();

    SubstitutionBasicOperations<NonVariableTerm> onNonVariableTerms();

    SubstitutionBasicOperations<ImmutableFunctionalTerm> onImmutableFunctionalTerms();

}
