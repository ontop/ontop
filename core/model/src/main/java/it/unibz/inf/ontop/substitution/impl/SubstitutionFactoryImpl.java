package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SubstitutionFactoryImpl implements SubstitutionFactory {

    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private SubstitutionFactoryImpl(AtomFactory atomFactory, TermFactory termFactory, CoreUtilsFactory coreUtilsFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableMap<Variable, T> newSubstitutionMap) {
        return new ImmutableSubstitutionImpl<>(newSubstitutionMap, atomFactory, termFactory, this);
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1) {
        return getSubstitution(ImmutableMap.of(k1, v1));
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2) {
        return getSubstitution(ImmutableMap.of(k1, v1, k2, v2));
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2, Variable k3, T v3) {
        return getSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2,
                                                                              Variable k3, T v3, Variable k4, T v4) {
        return getSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution() {
        return new ImmutableSubstitutionImpl<>(ImmutableMap.of(), atomFactory, termFactory, this);
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getNullSubstitution(Stream<Variable> variables) {
        return new ImmutableSubstitutionImpl<>(
                variables.collect(ImmutableCollectors.toMap(v -> v, v -> termFactory.getNullConstant())),
                atomFactory, termFactory, this);
    }

    @Override
    public <T1 extends ImmutableTerm, T2 extends ImmutableTerm> ImmutableSubstitution<T2> transform(ImmutableSubstitution<T1> substitution, Function<T1, T2> function) {
        return new ImmutableSubstitutionImpl<>(substitution.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> function.apply(e.getValue()))),
                atomFactory, termFactory, this);
    }

    @Override
    public <T1 extends ImmutableTerm, T2 extends ImmutableTerm> ImmutableSubstitution<T2> transform(ImmutableSubstitution<T1> substitution, BiFunction<Variable, T1, T2> function) {
        return new ImmutableSubstitutionImpl<>(substitution.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> function.apply(e.getKey(), e.getValue()))),
                atomFactory, termFactory, this);
    }

    @Override
    public <T1 extends ImmutableTerm, T2 extends ImmutableTerm> ImmutableSubstitution<T2> filterAndTransform(ImmutableSubstitution<T1> substitution, BiPredicate<Variable, T1> filter, Function<T1, T2> function) {
        return new ImmutableSubstitutionImpl<>(substitution.getImmutableMap().entrySet().stream()
                .filter(e -> filter.test(e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> function.apply(e.getValue()))),
                atomFactory, termFactory, this);
    }

    @Override
    public Var2VarSubstitution getVar2VarSubstitution(ImmutableMap<Variable, Variable> substitutionMap) {
        return new Var2VarSubstitutionImpl(substitutionMap, atomFactory, termFactory, this);
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(ImmutableMap<Variable, Variable> substitutionMap) {
        return new InjectiveVar2VarSubstitutionImpl(substitutionMap, atomFactory, termFactory, this);
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Stream<Variable> stream, Function<Variable, Variable> transformer) {
        ImmutableMap<Variable, Variable> map = stream.collect(ImmutableCollectors.toMap(v -> v, transformer));
        return new InjectiveVar2VarSubstitutionImpl(map, atomFactory, termFactory, this);
    }

    /**
     * Non-conflicting variable:
     *   - initial variable of the variable set not known by the generator
     *   - or a fresh variable generated by the generator NOT PRESENT in the variable set
     */
    @Override
    public InjectiveVar2VarSubstitution generateNotConflictingRenaming(VariableGenerator variableGenerator,
                                                                       ImmutableSet<Variable> variables) {
        ImmutableMap<Variable, Variable> newMap = variables.stream()
                .map(v -> Maps.immutableEntry(v, generateNonConflictingVariable(v, variableGenerator, variables)))
                .filter(pair -> ! pair.getKey().equals(pair.getValue()))
                .collect(ImmutableCollectors.toMap());

        return getInjectiveVar2VarSubstitution(newMap);
    }

    private Variable generateNonConflictingVariable(Variable v, VariableGenerator variableGenerator,
                                                           ImmutableSet<Variable> variables) {

        Variable proposedVariable = variableGenerator.generateNewVariableIfConflicting(v);
        if (proposedVariable.equals(v)
                // Makes sure that a "fresh" variable does not exist in the variable set
                || (!variables.contains(proposedVariable)))
            return proposedVariable;

		/*
		 * Generates a "really fresh" variable
		 */
        ImmutableSet<Variable> knownVariables = Sets.union(
                variableGenerator.getKnownVariables(),
                variables)
                .immutableCopy();

        VariableGenerator newVariableGenerator = coreUtilsFactory.createVariableGenerator(knownVariables);
        Variable newVariable = newVariableGenerator.generateNewVariableFromVar(v);
        variableGenerator.registerAdditionalVariables(ImmutableSet.of(newVariable));
        return newVariable;
    }
}
