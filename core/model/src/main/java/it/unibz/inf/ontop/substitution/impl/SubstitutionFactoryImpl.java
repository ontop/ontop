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
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SubstitutionFactoryImpl implements SubstitutionFactory {

    private final TermFactory termFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final ImmutableTermsSubstitutionOperations immutableTermsSubstitutionOperations;

    @Inject
    private SubstitutionFactoryImpl(TermFactory termFactory, CoreUtilsFactory coreUtilsFactory) {
        this.termFactory = termFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.immutableTermsSubstitutionOperations = new ImmutableTermsSubstitutionOperations(termFactory);
    }

    <T extends ImmutableTerm> Substitution<T> createSubstitution(ImmutableMap<Variable, T> map) {
        return termFactory.getSubstitution(map);
    }

    @Override
    public <T extends ImmutableTerm, U, E extends Throwable> Substitution<T> getSubstitutionThrowsExceptions(Collection<U> entries, Function<U, Variable> variableProvider, FunctionThrowsExceptions<U,T,E> termProvider) throws E {
        ImmutableMap.Builder<Variable, T> substitutionMapBuilder = ImmutableMap.builder(); // exceptions - no stream
        for (U u : entries) {
            Variable v = variableProvider.apply(u);
            T t = termProvider.apply(u);
            substitutionMapBuilder.put(v, t);
        }
        return createSubstitution(substitutionMapBuilder.build());
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution() {
        return createSubstitution(ImmutableMap.of());
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1) {
        return createSubstitution(ImmutableMap.of(k1, v1));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2) {
        return createSubstitution(ImmutableMap.of(k1, v1, k2, v2));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2, Variable k3, T v3) {
        return createSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2, Variable k3, T v3, Variable k4, T v4) {
        return createSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    @Override
    public <T extends ImmutableTerm> Substitution<T> getSubstitution(ImmutableList<Variable> variables, ImmutableList<? extends T> values) {
        if (variables.size() != values.size())
            throw new IllegalArgumentException("lists of different lengths");

        return IntStream.range(0, variables.size())
                .mapToObj(i -> Maps.immutableEntry(variables.get(i), values.get(i)))
                .collect(toSubstitutionSkippingIdentityEntries());
    }




    @Override
    public InjectiveSubstitution<Variable> extractAnInjectiveVar2VarSubstitutionFromInverseOf(Substitution<Variable> substitution) {
        return createSubstitution(substitution.inverseMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().iterator().next())))
                .injective();
    }


    @Override
    public <T extends ImmutableTerm> Substitution<T> union(Substitution<? extends T> substitution1, Substitution<? extends T> substitution2) {

        if (substitution1.isEmpty())
            return covariantCast(substitution2);

        if (substitution2.isEmpty())
            return covariantCast(substitution1);

        return Stream.of(substitution1, substitution2)
                .flatMap(Substitution::stream)
                .distinct()
                .collect(toSubstitution());
    }


    @Override
    public <T extends ImmutableTerm> Substitution<T> covariantCast(Substitution<? extends T> substitution) {
        return AbstractSubstitutionOperations.covariantCast(substitution);
    }


    @Override
    public <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, Substitution<T>> toSubstitution() {
        return toSubstitution(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public <T extends ImmutableTerm, U> Collector<U, ?, Substitution<T>> toSubstitution(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper) {
        return SubstitutionFactoryImpl.<T, U, Substitution<T>>getCollector(
                variableMapper, termMapper, ImmutableMap.Builder::put, this::createSubstitution);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Map.Entry<Variable, ? extends T>, ?, Substitution<T>> toSubstitutionSkippingIdentityEntries() {
        return toSubstitutionSkippingIdentityEntries(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public <T extends ImmutableTerm, U> Collector<U, ?, Substitution<T>> toSubstitutionSkippingIdentityEntries(Function<U, Variable> variableMapper, Function<U, ? extends T> termMapper) {
        return SubstitutionFactoryImpl.<T, U, Substitution<T>>getCollector(
                variableMapper, termMapper, (b, v, t) -> { if (!v.equals(t)) b.put(v, t); }, this::createSubstitution);
    }

    @Override
    public <T extends ImmutableTerm> Collector<Variable, ?, Substitution<T>> toSubstitution(Function<Variable, ? extends T> termMapper) {
        return toSubstitution(v -> v, termMapper);
    }

    @Override
    public Collector<Variable, ?, InjectiveSubstitution<Variable>> toFreshRenamingSubstitution(VariableGenerator variableGenerator) {
        return SubstitutionFactoryImpl.getCollector(
                v -> v, variableGenerator::generateNewVariableFromVar, ImmutableMap.Builder::put, m -> createSubstitution(m).injective());
    }


    @FunctionalInterface
    private interface TriConsumer<B, V, T> {
        void put(B b, V v, T t);
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
                .collect(toSubstitutionSkippingIdentityEntries(v -> v, v -> generateNonConflictingVariable(v, variableGenerator, variables)))
                .injective();
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
        return new AbstractSubstitutionOperations<>(termFactory, v -> v, s -> s) {
            @Override
            public NonFunctionalTerm applyToTerm(Substitution<? extends NonFunctionalTerm> substitution, NonFunctionalTerm t) {
                return (t instanceof Variable) ? apply(substitution, (Variable) t) : t;
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
        };
    }

    @Override
    public SubstitutionOperations<VariableOrGroundTerm> onVariableOrGroundTerms() {
        return new AbstractSubstitutionOperations<>(termFactory, v -> v, s -> s) {
            @Override
            public VariableOrGroundTerm applyToTerm(Substitution<? extends VariableOrGroundTerm> substitution, VariableOrGroundTerm t) {
                return (t instanceof Variable) ? apply(substitution, (Variable) t) : t;
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
        };
    }

    @Override
    public SubstitutionOperations<Variable> onVariables() {
        return new AbstractSubstitutionOperations<>(termFactory, v -> v, s -> s) {
            @Override
            public Variable applyToTerm(Substitution<? extends Variable> substitution, Variable t) {
                return apply(substitution, t);
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
        };
    }

    @Override
    public SubstitutionBasicOperations<NonGroundTerm> onNonGroundTerms() {
        return new AbstractSubstitutionBasicOperations<>(termFactory) {

            @Override
            public NonGroundTerm applyToTerm(Substitution<? extends NonGroundTerm> substitution, NonGroundTerm t) {
                if (t instanceof Variable)
                    return applyToVariable(substitution, (Variable) t, v -> v);

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
                if (t instanceof Variable)
                    return applyToVariable(substitution, (Variable) t, v -> v);

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
