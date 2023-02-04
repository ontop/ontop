package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SubstitutionFactoryImpl implements SubstitutionFactory {

    final TermFactory termFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final ImmutableTermsSubstitutionOperations immutableTermsSubstitutionOperations;

    @Inject
    private SubstitutionFactoryImpl(TermFactory termFactory, CoreUtilsFactory coreUtilsFactory) {
        this.termFactory = termFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.immutableTermsSubstitutionOperations = new ImmutableTermsSubstitutionOperations(termFactory);
    }

    private <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableMap<Variable, T> newSubstitutionMap) {
        return new ImmutableSubstitutionImpl<>(newSubstitutionMap, termFactory);
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
                .collect(ImmutableCollectors.toMap());

        return getSubstitution(map);
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
            return ImmutableSubstitutionImpl.invariantCast(substitution2);

        if (substitution2.isEmpty())
            return ImmutableSubstitutionImpl.invariantCast(substitution1);

        return Stream.of(substitution1, substitution2)
                .map(ImmutableSubstitution::entrySet)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toSubstitution());
    }


    @Override
    public <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, ImmutableSubstitution<T>> toSubstitution() {
        return toSubstitution(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public <T extends ImmutableTerm, U> Collector<U, ?, ImmutableSubstitution<T>> toSubstitution(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper) {
        return Collector.of(
                ImmutableMap::<Variable, T>builder,   // supplier
                (builder, e) -> builder.put(variableMapper.apply(e), termMapper.apply(e)), // accumulator
                (builder1, builder2) -> builder1.putAll(builder2.build()), // merger
                builder -> getSubstitution(builder.build()), // finisher
                Collector.Characteristics.UNORDERED);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, ImmutableSubstitution<T>> toSubstitutionSkippingIdentityEntries() {
        return toSubstitutionSkippingIdentityEntries(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public <T extends ImmutableTerm, U> Collector<U, ?, ImmutableSubstitution<T>> toSubstitutionSkippingIdentityEntries(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper) {
        return Collector.of(
                ImmutableMap::<Variable, T>builder,   // supplier
                (builder, e) -> putSkippingIdentityEntries(builder, variableMapper.apply(e), termMapper.apply(e)), // accumulator
                (builder1, builder2) -> builder1.putAll(builder2.build()), // merger
                builder -> getSubstitution(builder.build()), // finisher
                Collector.Characteristics.UNORDERED);
    }

    private static <T> void putSkippingIdentityEntries(ImmutableMap.Builder<Variable, T> builder, Variable v, T t) {
        if (!v.equals(t))
            builder.put(v, t);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Variable, ?, ImmutableSubstitution<T>> toSubstitution(Function<Variable, ? extends T> termMapper) {
        return toSubstitution(v -> v, termMapper);
    }

    @Override
    public Collector<Variable, ?, InjectiveVar2VarSubstitution> toInjectiveSubstitution(Function<Variable, Variable> termMapper) {
        return Collector.of(
                ImmutableMap::<Variable, Variable>builder,   // supplier
                (builder, v) -> builder.put(v, termMapper.apply(v)), // accumulator
                (builder1, builder2) -> builder1.putAll(builder2.build()), // merger
                builder -> getInjectiveVar2VarSubstitution(builder.build()), // finisher
                Collector.Characteristics.UNORDERED);
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
    public SubstitutionOperations<NonFunctionalTerm> onNonFunctionalTerms() {
        return new AbstractSubstitutionOperations<>(termFactory) {
            @Override
            public NonFunctionalTerm apply(ImmutableSubstitution<? extends NonFunctionalTerm> substitution, Variable variable) {
                return Optional.<NonFunctionalTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public NonFunctionalTerm applyToTerm(ImmutableSubstitution<? extends NonFunctionalTerm> substitution, NonFunctionalTerm t) {
                return (t instanceof Variable)  ? apply(substitution, (Variable) t) : t;
            }
            @Override
            public ImmutableUnificationTools.UnifierBuilder<NonFunctionalTerm, ?> unifierBuilder(ImmutableSubstitution<NonFunctionalTerm> substitution) {
                return new ImmutableUnificationTools.VariableOrGroundTermUnifierBuilder<>(termFactory, this, substitution);
            }
        };
    }

    @Override
    public SubstitutionOperations<VariableOrGroundTerm> onVariableOrGroundTerms() {
        return new AbstractSubstitutionOperations<>(termFactory) {
            @Override
            public VariableOrGroundTerm apply(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, Variable variable) {
                return Optional.<VariableOrGroundTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }
            @Override
            public VariableOrGroundTerm applyToTerm(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, VariableOrGroundTerm t) {
                return (t instanceof Variable) ? apply(substitution, (Variable) t) : t;
            }
            @Override
            public ImmutableUnificationTools.UnifierBuilder<VariableOrGroundTerm, ?> unifierBuilder(ImmutableSubstitution<VariableOrGroundTerm> substitution) {
                return new ImmutableUnificationTools.VariableOrGroundTermUnifierBuilder<>(termFactory, this, substitution);
            }
        };
    }

    @Override
    public SubstitutionOperations<Variable> onVariables() {
        return new AbstractSubstitutionOperations<>(termFactory) {
            @Override
            public Variable apply(ImmutableSubstitution<? extends Variable> substitution, Variable variable) {
                return Optional.<Variable>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public Variable applyToTerm(ImmutableSubstitution<? extends Variable> substitution, Variable t) {
                return apply(substitution, t);
            }
            @Override
            public ImmutableUnificationTools.UnifierBuilder<Variable, ?> unifierBuilder(ImmutableSubstitution<Variable> substitution) {
                return new ImmutableUnificationTools.VariableOrGroundTermUnifierBuilder<>(termFactory, this, substitution);
            }
        };
    }

    @Override
    public SubstitutionOperations<ImmutableTerm> onImmutableTerms() {
        return immutableTermsSubstitutionOperations;
    }

    @Override
    public SubstitutionComposition<NonVariableTerm> onNonVariableTerms() {
        return new AbstractSubstitutionComposition<>(termFactory) {
            @Override
            public NonVariableTerm applyToTerm(ImmutableSubstitution<? extends NonVariableTerm> substitution, NonVariableTerm t) {
                if (t instanceof Constant) {
                    return t;
                }
                if (t instanceof ImmutableFunctionalTerm) {
                    return immutableTermsSubstitutionOperations.apply(substitution, (ImmutableFunctionalTerm) t);
                }
                throw new IllegalArgumentException("Unexpected kind of term: " + t.getClass());
            }
        };
    }
}
