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

    private <T extends ImmutableTerm> Substitution<T> getSubstitution(ImmutableMap<Variable, T> substitutionMap) {
        return new SubstitutionImpl<>(substitutionMap, termFactory);
    }

    @Override
    public <T extends ImmutableTerm, U, E extends Throwable> Substitution<T> getSubstitutionThrowsExceptions(Collection<U> entries, Function<U, Variable> variableProvider, FunctionThrowsExceptions<U,T,E> termProvider) throws E {
        ImmutableMap.Builder<Variable, T> substitutionMapBuilder = ImmutableMap.builder(); // exceptions - no stream
        for (U u : entries) {
            Variable v = variableProvider.apply(u);
            T t = termProvider.apply(u);
            substitutionMapBuilder.put(v, t);
        }
        return getSubstitution(substitutionMapBuilder.build());
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution() {
        return getSubstitution(ImmutableMap.of());
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1) {
        return getSubstitution(ImmutableMap.of(k1, v1));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2) {
        return getSubstitution(ImmutableMap.of(k1, v1, k2, v2));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2, Variable k3, T v3) {
        return getSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2, Variable k3, T v3, Variable k4, T v4) {
        return getSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(ImmutableList<Variable> variables, ImmutableList<? extends T> values) {
        if (variables.size() != values.size())
            throw new IllegalArgumentException("lists of different lengths");

        return IntStream.range(0, variables.size())
                .mapToObj(i -> Maps.immutableEntry(variables.get(i), values.get(i)))
                .collect(toSubstitutionSkippingIdentityEntries());
    }


    private <T extends ImmutableTerm> InjectiveSubstitution<T> getInjectiveVar2VarSubstitution(ImmutableMap<Variable, T> substitutionMap) {
        return new InjectiveSubstitutionImpl<>(substitutionMap, termFactory);
    }

    @Override
    public InjectiveSubstitution<Variable> injectiveOf(Substitution<Variable> substitution) {
        return getInjectiveVar2VarSubstitution(((SubstitutionImpl<Variable>)substitution).getImmutableMap());
    }

    @Override
    public InjectiveSubstitution<Variable> extractAnInjectiveVar2VarSubstitutionFromInverseOf(Substitution<Variable> substitution) {
        return getInjectiveVar2VarSubstitution(
                substitution.inverseMap().entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().iterator().next())));
    }


    @Override
    public <T extends ImmutableTerm> Substitution<T> union(Substitution<? extends T> substitution1, Substitution<? extends T> substitution2) {

        if (substitution1.isEmpty())
            return SubstitutionImpl.covariantCast(substitution2);

        if (substitution2.isEmpty())
            return SubstitutionImpl.covariantCast(substitution1);

        return Stream.of(substitution1, substitution2)
                .map(Substitution::entrySet)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toSubstitution());
    }


    @Override
    public <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, Substitution<T>> toSubstitution() {
        return toSubstitution(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public <T extends ImmutableTerm, U> Collector<U, ?, Substitution<T>> toSubstitution(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper) {
        return SubstitutionFactoryImpl.<T, U, Substitution<T>>getCollector(
                variableMapper, termMapper, ImmutableMap.Builder::put, this::getSubstitution);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, Substitution<T>> toSubstitutionSkippingIdentityEntries() {
        return toSubstitutionSkippingIdentityEntries(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public <T extends ImmutableTerm, U> Collector<U, ?, Substitution<T>> toSubstitutionSkippingIdentityEntries(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper) {
        return SubstitutionFactoryImpl.<T, U, Substitution<T>>getCollector(
                variableMapper, termMapper, SubstitutionFactoryImpl::putSkippingIdentityEntries, this::getSubstitution);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Variable, ?, Substitution<T>> toSubstitution(Function<Variable, ? extends T> termMapper) {
        return toSubstitution(v -> v, termMapper);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Variable, ?, InjectiveSubstitution<T>> toInjectiveSubstitution(Function<Variable, ? extends T> termMapper) {
        return SubstitutionFactoryImpl.<T, Variable, InjectiveSubstitution<T>>
                getCollector(v -> v, termMapper, ImmutableMap.Builder::put, this::getInjectiveVar2VarSubstitution);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Variable, ?, InjectiveSubstitution<T>> toInjectiveSubstitutionSkippingIdentityEntries(Function<Variable, ? extends T> termMapper) {
        return SubstitutionFactoryImpl.<T, Variable, InjectiveSubstitution<T>>
                getCollector(v -> v, termMapper, SubstitutionFactoryImpl::putSkippingIdentityEntries, this::getInjectiveVar2VarSubstitution);
    }

    @FunctionalInterface
    private interface TriConsumer<B, V, T> {
        void put(B b, V v, T t);
    }

    private static <T> void putSkippingIdentityEntries(ImmutableMap.Builder<Variable, T> builder, Variable v, T t) {
        if (!v.equals(t))
            builder.put(v, t);
    }

    private static <T extends ImmutableTerm, U, R> Collector<U, ImmutableMap.Builder<Variable, T>, R> getCollector(
            Function<U, Variable> variableMapper,
            Function<U, ? extends T> termMapper,
            TriConsumer<ImmutableMap.Builder<Variable, T>, Variable, T> builderPutMethod,
            Function<ImmutableMap<Variable, T>, R> constructor) {

        return Collector.of(
                ImmutableMap::builder,   // supplier
                (b, e) -> builderPutMethod.put(b, variableMapper.apply(e), termMapper.apply(e)), // accumulator
                (b1, b2) -> b1.putAll(b2.build()), // merger
                b -> constructor.apply(b.build()), // finisher
                Collector.Characteristics.UNORDERED);
    }


    /**
     * Non-conflicting variable:
     *   - initial variable of the variable set not known by the generator
     *   - or a fresh variable generated by the generator NOT PRESENT in the variable set
     */
    @Override
    public InjectiveSubstitution<Variable> generateNotConflictingRenaming(VariableGenerator variableGenerator, ImmutableSet<Variable> variables) {
        return variables.stream()
                .collect(toInjectiveSubstitutionSkippingIdentityEntries(v -> generateNonConflictingVariable(v, variableGenerator, variables)));
    }

    private Variable generateNonConflictingVariable(Variable v, VariableGenerator variableGenerator, ImmutableSet<Variable> variables) {

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
    public InjectiveSubstitution<Variable> getPrioritizingRenaming(Substitution<?> substitution, ImmutableSet<Variable> priorityVariables) {
        Substitution<Variable> renaming = substitution.builder()
                .restrictDomainTo(priorityVariables)
                .restrictRangeTo(Variable.class)
                .restrictRange(t -> !priorityVariables.contains(t))
                .build();

        return extractAnInjectiveVar2VarSubstitutionFromInverseOf(renaming);
    }

    @Override
    public SubstitutionOperations<NonFunctionalTerm> onNonFunctionalTerms() {
        return new AbstractSubstitutionOperations<>(termFactory) {
            @Override
            public NonFunctionalTerm apply(Substitution<? extends NonFunctionalTerm> substitution, Variable variable) {
                return Optional.<NonFunctionalTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public NonFunctionalTerm applyToTerm(Substitution<? extends NonFunctionalTerm> substitution, NonFunctionalTerm t) {
                return (t instanceof Variable) ? apply(substitution, (Variable) t) : t;
            }

            @Override
            public NonFunctionalTerm rename(Substitution<Variable> renaming, NonFunctionalTerm t) {
                return applyToTerm(renaming, t);
            }

            @Override
            public AbstractUnifierBuilder<NonFunctionalTerm> unifierBuilder(Substitution<NonFunctionalTerm> substitution) {
                return new AbstractUnifierBuilder<>(termFactory, this, substitution) {
                    @Override
                    protected UnifierBuilder<NonFunctionalTerm> unifyUnequalTerms(NonFunctionalTerm term1, NonFunctionalTerm term2) {
                        return attemptUnifying(term1, term2)
                                .or(() -> attemptUnifying(term2, term1))
                                .orElseGet(this::empty);
                    }
                };
            }

            @Override
            protected NonFunctionalTerm keyMapper(Map.Entry<Variable, NonFunctionalTerm> e) {
                return e.getKey();
            }
        };
    }

    @Override
    public SubstitutionOperations<VariableOrGroundTerm> onVariableOrGroundTerms() {
        return new AbstractSubstitutionOperations<>(termFactory) {
            @Override
            public VariableOrGroundTerm apply(Substitution<? extends VariableOrGroundTerm> substitution, Variable variable) {
                return Optional.<VariableOrGroundTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }
            @Override
            public VariableOrGroundTerm applyToTerm(Substitution<? extends VariableOrGroundTerm> substitution, VariableOrGroundTerm t) {
                return (t instanceof Variable) ? apply(substitution, (Variable) t) : t;
            }

            @Override
            public VariableOrGroundTerm rename(Substitution<Variable> renaming, VariableOrGroundTerm t) {
                return applyToTerm(renaming, t);
            }

            @Override
            public AbstractUnifierBuilder<VariableOrGroundTerm> unifierBuilder(Substitution<VariableOrGroundTerm> substitution) {
                return new AbstractUnifierBuilder<>(termFactory, this, substitution) {
                    @Override
                    protected UnifierBuilder<VariableOrGroundTerm> unifyUnequalTerms(VariableOrGroundTerm term1, VariableOrGroundTerm term2) {
                        return attemptUnifying(term1, term2)
                                .or(() -> attemptUnifying(term2, term1))
                                .orElseGet(this::empty);
                    }
                };
            }
            @Override
            protected VariableOrGroundTerm keyMapper(Map.Entry<Variable, VariableOrGroundTerm> e) {
                return e.getKey();
            }
        };
    }

    @Override
    public SubstitutionOperations<Variable> onVariables() {
        return new AbstractSubstitutionOperations<>(termFactory) {
            @Override
            public Variable apply(Substitution<? extends Variable> substitution, Variable variable) {
                return Optional.<Variable>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public Variable applyToTerm(Substitution<? extends Variable> substitution, Variable t) {
                return apply(substitution, t);
            }

            @Override
            public Variable rename(Substitution<Variable> renaming, Variable variable) {
                return apply(renaming, variable);
            }

            @Override
            public AbstractUnifierBuilder<Variable> unifierBuilder(Substitution<Variable> substitution) {
                return new AbstractUnifierBuilder<>(termFactory, this, substitution) {
                    @Override
                    protected UnifierBuilder<Variable> unifyUnequalTerms(Variable term1, Variable term2) {
                        //noinspection OptionalGetWithoutIsPresent
                        return attemptUnifying(term1, term2).get();
                    }
                };
            }
            @Override
            protected Variable keyMapper(Map.Entry<Variable, Variable> e) {
                return e.getKey();
            }
        };
    }

    @Override
    public SubstitutionBasicOperations<NonGroundTerm> onNonGroundTerms() {
        return new AbstractSubstitutionBasicOperations<>(termFactory) {

            @Override
            public NonGroundTerm applyToTerm(Substitution<? extends NonGroundTerm> substitution, NonGroundTerm t) {
                if (t instanceof Variable) {
                    Variable v = (Variable) t;
                    return Optional.<NonGroundTerm>ofNullable(substitution.get(v)).orElse(v);
                }

                if (t instanceof NonGroundFunctionalTerm)
                    return (NonGroundFunctionalTerm)immutableTermsSubstitutionOperations.apply(substitution, (NonGroundFunctionalTerm) t);

                throw new IllegalArgumentException("Unexpected kind of term: " + t.getClass());
            }

            @Override
            public NonGroundTerm rename(Substitution<Variable> renaming, NonGroundTerm t) {
                return applyToTerm(renaming, t);
            }
        };
    }

    @Override
    public SubstitutionBasicOperations<NonConstantTerm> onNonConstantTerms() {
        return new AbstractSubstitutionBasicOperations<>(termFactory) {

            @Override
            public NonConstantTerm applyToTerm(Substitution<? extends NonConstantTerm> substitution, NonConstantTerm t) {
                if (t instanceof Variable) {
                    Variable v = (Variable) t;
                    return Optional.<NonConstantTerm>ofNullable(substitution.get(v)).orElse(v);
                }

                if (t instanceof ImmutableFunctionalTerm)
                    return immutableTermsSubstitutionOperations.apply(substitution, (ImmutableFunctionalTerm) t);

                throw new IllegalArgumentException("Unexpected kind of term: " + t.getClass());
            }

            @Override
            public NonConstantTerm rename(Substitution<Variable> renaming, NonConstantTerm t) {
                return applyToTerm(renaming, t);
            }
        };
    }

    @Override
    public SubstitutionOperations<ImmutableTerm> onImmutableTerms() {
        return immutableTermsSubstitutionOperations;
    }

    @Override
    public SubstitutionBasicOperations<NonVariableTerm> onNonVariableTerms() {
        return new AbstractSubstitutionBasicOperations<>(termFactory) {
            @Override
            public NonVariableTerm applyToTerm(Substitution<? extends NonVariableTerm> substitution, NonVariableTerm t) {
                return internalApplyToTerm(substitution, t);
            }

            @Override
            public NonVariableTerm rename(Substitution<Variable> renaming, NonVariableTerm t) {
               return internalApplyToTerm(renaming, t);
            }

            private NonVariableTerm internalApplyToTerm(Substitution<?> renaming, NonVariableTerm t) {
                if (t instanceof Constant)
                    return t;

                if (t instanceof ImmutableFunctionalTerm)
                    return immutableTermsSubstitutionOperations.apply(renaming, (ImmutableFunctionalTerm) t);

                throw new IllegalArgumentException("Unexpected kind of term: " + t.getClass());
            }
        };
    }

    @Override
    public SubstitutionBasicOperations<ImmutableFunctionalTerm> onImmutableFunctionalTerms() {
        return new AbstractSubstitutionBasicOperations<>(termFactory) {
            @Override
            public ImmutableFunctionalTerm applyToTerm(Substitution<? extends ImmutableFunctionalTerm> substitution, ImmutableFunctionalTerm t) {
                return immutableTermsSubstitutionOperations.apply(substitution, t);
            }

            @Override
            public ImmutableFunctionalTerm rename(Substitution<Variable> renaming, ImmutableFunctionalTerm t) {
                return immutableTermsSubstitutionOperations.apply(renaming, t);
            }
        };
    }
}
