package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface SubstitutionFactory {

    <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, ImmutableSubstitution<T>> toSubstitution();
    <T extends ImmutableTerm> Collector<Variable, ?, ImmutableSubstitution<T>> toSubstitution(Function<Variable, ? extends T> termMapper);
    <T extends ImmutableTerm, U> Collector<U, ?, ImmutableSubstitution<T>> toSubstitution(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper);
    <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, ImmutableSubstitution<T>> toSubstitutionSkippingIdentityEntries();
    <T extends ImmutableTerm, U> Collector<U, ?, ImmutableSubstitution<T>> toSubstitutionSkippingIdentityEntries(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper);


    @FunctionalInterface
    interface FunctionThrowsExceptions<U, T, E extends Throwable> {
        T apply(U arg) throws E;
    }

    <T extends ImmutableTerm, U, E extends Throwable> ImmutableSubstitution<T> getSubstitutionThrowsExceptions(Collection<U> entries, Function<U, Variable> variableProvider, FunctionThrowsExceptions<U, T, E> termProvider) throws E;

    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution();
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable v1, T t1);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable v1, T t1, Variable v2, T t2);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable v1, T t1, Variable v2, T t2, Variable v3, T t3);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable v1, T t1, Variable v2, T t2, Variable v3, T t3, Variable v4, T t4);


    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableList<Variable> variables, ImmutableList<? extends T> values);


    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution();
    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1);
    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1, Variable v2, Variable t2);
    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1, Variable v2, Variable t2, Variable v3, Variable t3);
    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1, Variable v2, Variable t2, Variable v3, Variable t3, Variable v4, Variable t4);


    InjectiveVar2VarSubstitution injectiveVar2VarSubstitutionOf(ImmutableSubstitution<Variable> substitution);

    InjectiveVar2VarSubstitution extractAnInjectiveVar2VarSubstitutionFromInverseOf(ImmutableSubstitution<Variable> substitution);

    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Stream<Variable> stream, Function<Variable, Variable> transformer);

    InjectiveVar2VarSubstitution getInjectiveFreshVar2VarSubstitution(Stream<Variable> stream, VariableGenerator variableGenerator);

    InjectiveVar2VarSubstitution generateNotConflictingRenaming(VariableGenerator variableGenerator,
                                                                ImmutableSet<Variable> variables);

    /**
     *
     * @param substitution1
     * @param substitution2
     * @return
     * @param <T>
     * @throws IllegalArgumentException if the substitutions do not agree on one of the variables
     */
    <T extends ImmutableTerm> ImmutableSubstitution<T> union(ImmutableSubstitution<? extends T> substitution1, ImmutableSubstitution<? extends T> substitution2);



    SubstitutionOperations<NonFunctionalTerm> onNonFunctionalTerms();

    SubstitutionOperations<VariableOrGroundTerm> onVariableOrGroundTerms();

    SubstitutionOperations<Variable> onVariables();

    SubstitutionOperations<ImmutableTerm> onImmutableTerms();

    SubstitutionComposition<NonVariableTerm> onNonVariableTerms();
}
