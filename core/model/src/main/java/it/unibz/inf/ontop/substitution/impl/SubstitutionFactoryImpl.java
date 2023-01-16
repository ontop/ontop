package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SubstitutionFactoryImpl implements SubstitutionFactory {

    private final TermFactory termFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private SubstitutionFactoryImpl(TermFactory termFactory, CoreUtilsFactory coreUtilsFactory) {
        this.termFactory = termFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    private <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableMap<Variable, T> newSubstitutionMap) {
        return new ImmutableSubstitutionImpl<>(newSubstitutionMap, termFactory);
    }

    @Override
    public <T extends ImmutableTerm, U> ImmutableSubstitution<T> getSubstitutionRemoveIdentityEntries(Collection<U> entries, Function<U, Variable> variableProvider, Function<U, T> termProvider) {
        return new ImmutableSubstitutionImpl<>(entries.stream()
                .map(e -> Maps.immutableEntry(variableProvider.apply(e), termProvider.apply(e)))
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap()), termFactory);
    }

    @Override
    public <T extends ImmutableTerm, U> ImmutableSubstitution<T> getSubstitution(Collection<U> entries, Function<U, Variable> variableProvider, Function<U, T> termProvider) {
        return new ImmutableSubstitutionImpl<>(entries.stream()
                .collect(ImmutableCollectors.toMap(variableProvider, termProvider)), termFactory);
    }

    @Override
    public     <T extends ImmutableTerm, U, E extends Throwable> ImmutableSubstitution<T> getSubstitutionThrowsExceptions(Collection<U> entries, Function<U, Variable> variableProvider, FunctionThrowsExceptions<U,T,E> termProvider) throws E {
        ImmutableMap.Builder<Variable, T> substitutionMapBuilder = ImmutableMap.builder(); // exceptions - no stream
        for (U u : entries) {
            Variable v = variableProvider.apply(u);
            T t = termProvider.apply(u);
            substitutionMapBuilder.put(v, t);
        }
        return getSubstitution(substitutionMapBuilder.build());
    }

    @Override
    public <T extends ImmutableTerm, U> ImmutableSubstitution<T> getSubstitutionFromStream(Stream<U> stream, Function<U, Variable> variableProvider, Function<U, T> termProvider) {
        return new ImmutableSubstitutionImpl<>(stream
                .collect(ImmutableCollectors.toMap(variableProvider, termProvider)), termFactory);
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
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2, Variable k3, T v3, Variable k4, T v4) {
        return getSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution() {
        return getSubstitution(ImmutableMap.of());
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableList<Variable> variables, ImmutableList<? extends T> values) {
        if (variables.size() != values.size())
            throw new IllegalArgumentException("lists of different lengths");

        ImmutableMap<Variable, T> map = IntStream.range(0, variables.size())
                .filter(i -> !variables.get(i).equals(values.get(i)))
                .mapToObj(i -> Maps.<Variable, T>immutableEntry(variables.get(i), values.get(i)))
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap());

        return getSubstitution(map);
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getNullSubstitution(Set<Variable> variables) {
        return getSubstitution(
                variables.stream().collect(ImmutableCollectors.toMap(v -> v, v -> termFactory.getNullConstant())));
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution() {
        return getInjectiveVar2VarSubstitution(ImmutableMap.of());
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1) {
        return getInjectiveVar2VarSubstitution(ImmutableMap.of(v1, t1));
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1, Variable v2, Variable t2) {
        return getInjectiveVar2VarSubstitution(ImmutableMap.of(v1, t1, v2, t2));
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1, Variable v2, Variable t2, Variable v3, Variable t3) {
        return getInjectiveVar2VarSubstitution(ImmutableMap.of(v1, t1, v2, t2, v3, t3));
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Variable v1, Variable t1, Variable v2, Variable t2, Variable v3, Variable t3, Variable v4, Variable t4) {
        return getInjectiveVar2VarSubstitution(ImmutableMap.of(v1, t1, v2, t2, v3, t3, v4, t4));
    }

    private InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(ImmutableMap<Variable, Variable> substitutionMap) {
        return new InjectiveVar2VarSubstitutionImpl(substitutionMap, termFactory);
    }

    @Override
    public InjectiveVar2VarSubstitution injectiveVar2VarSubstitutionOf(ImmutableSubstitution<Variable> substitution) {
        return getInjectiveVar2VarSubstitution(((ImmutableSubstitutionImpl<Variable>)substitution).getImmutableMap());
    }

    @Override
    public InjectiveVar2VarSubstitution extractAnInjectiveVar2VarSubstitutionFromInverseOf(ImmutableSubstitution<Variable> substitution) {
        return getInjectiveVar2VarSubstitution(
                substitution.inverseMap().entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().iterator().next())));
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Stream<Variable> stream, Function<Variable, Variable> transformer) {
        ImmutableMap<Variable, Variable> map = stream.collect(ImmutableCollectors.toMap(v -> v, transformer));
        return getInjectiveVar2VarSubstitution(map);
    }

    @Override
    public InjectiveVar2VarSubstitution getInjectiveFreshVar2VarSubstitution(Stream<Variable> stream, VariableGenerator variableGenerator) {
        return getInjectiveVar2VarSubstitution(stream, variableGenerator::generateNewVariableFromVar);
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

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> union(ImmutableSubstitution<? extends T> substitution1, ImmutableSubstitution<? extends T> substitution2) {

        if (substitution1.isEmpty())
            return (ImmutableSubstitution)substitution2;

        if (substitution2.isEmpty())
            return (ImmutableSubstitution)substitution1;

        ImmutableMap<Variable, T> map = Stream.of(substitution1, substitution2)
                .map(ImmutableSubstitution::entrySet)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (val1, val2) -> {
                            if (!val1.equals(val2))
                                throw new IllegalArgumentException("Substitutions " + substitution1 + " and " + substitution2 + " do not agree on one of the variables");
                            return val1;
                        }));

        return getSubstitution(map);
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


    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> compose(ImmutableSubstitution<? extends T> g, ImmutableSubstitution<? extends T> f) {
        if (g.isEmpty())
            return (ImmutableSubstitution) f;

        if (f.isEmpty())
            return (ImmutableSubstitution) g;

        ImmutableMap<Variable, T> map = Stream.concat(
                        f.entrySet().stream()
                                .map(e -> Maps.immutableEntry(e.getKey(), (T)g.apply(e.getValue()))),
                        g.entrySet().stream())
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (fValue, gValue) -> fValue));

        return getSubstitution(map);
    }

    @Override
    public InjectiveVar2VarSubstitution compose(InjectiveVar2VarSubstitution g, InjectiveVar2VarSubstitution f, Set<Variable> variablesToExcludeFromTheDomain) {
        ImmutableSet<Variable> fDomain = f.getDomain();

        Stream<Map.Entry<Variable, Variable>> fEntryStream = f.entrySet().stream()
                .map(e -> Maps.immutableEntry(e.getKey(), g.applyToVariable(e.getValue())));

        Stream<Map.Entry<Variable, Variable>> gEntryStream = g.entrySet().stream()
                .filter(e -> !fDomain.contains(e.getKey()));

        ImmutableMap<Variable, Variable> newMap = Stream.concat(fEntryStream, gEntryStream)
                .filter(e -> !variablesToExcludeFromTheDomain.contains(e.getKey()))
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap());

        return getInjectiveVar2VarSubstitution(newMap);
    }

}
