package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface SubstitutionFactory {

    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableMap<Variable, T> newSubstitutionMap);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2,
                                                                       Variable k3, T v3);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2,
                                                                       Variable k3, T v3, Variable k4, T v4);
    <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution();

   ImmutableSubstitution<ImmutableTerm> getNullSubstitution(Stream<Variable> variables);

    <T1 extends ImmutableTerm, T2 extends ImmutableTerm> ImmutableSubstitution<T2> transform(ImmutableSubstitution<T1> substitution, Function<T1, T2> function);

    <T1 extends ImmutableTerm, T2 extends ImmutableTerm> ImmutableSubstitution<T2> transform(ImmutableSubstitution<T1> substitution, BiFunction<Variable, T1, T2> function);

    <T1 extends ImmutableTerm, T2 extends ImmutableTerm> ImmutableSubstitution<T2> filterAndTransform(ImmutableSubstitution<T1> substitution, BiPredicate<Variable, T1> filter, Function<T1, T2> function);

    Var2VarSubstitution getVar2VarSubstitution(ImmutableMap<Variable, Variable> substitutionMap);
    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(ImmutableMap<Variable, Variable> substitutionMap);

    InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Stream<Variable> stream, Function<Variable, Variable> transformer);

    InjectiveVar2VarSubstitution generateNotConflictingRenaming(VariableGenerator variableGenerator,
                                                                ImmutableSet<Variable> variables);
}
