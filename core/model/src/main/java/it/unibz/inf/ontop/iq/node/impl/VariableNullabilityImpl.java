package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VariableNullabilityImpl implements VariableNullability {

    private final ImmutableSet<ImmutableSet<Variable>> nullableGroups;
    private final ImmutableSet<Variable> scope;

    private final CoreUtilsFactory coreUtilsFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    // Lazy
    @Nullable
    private ImmutableSet<Variable> nullableVariables;
    @Nullable
    private ImmutableMap<Variable, Integer> variableMap;

    @AssistedInject
    private VariableNullabilityImpl(@Assisted("nullableGroups") ImmutableSet<ImmutableSet<Variable>> nullableGroups,
                                    @Assisted("scope") ImmutableSet<Variable> scope,
                                    CoreUtilsFactory coreUtilsFactory, TermFactory termFactory,
                                    SubstitutionFactory substitutionFactory) {
        this.nullableGroups = nullableGroups;
        this.scope = scope;
        this.coreUtilsFactory = coreUtilsFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Empty
     */
    @AssistedInject
    private VariableNullabilityImpl(@Assisted ImmutableSet<Variable> scope, CoreUtilsFactory coreUtilsFactory,
                                    TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this(ImmutableSet.of(), scope, coreUtilsFactory, termFactory, substitutionFactory);
    }

    /**
     *
     * Treats all the variables of the functional term as independently nullable
     */
    @AssistedInject
    private VariableNullabilityImpl(@Assisted ImmutableFunctionalTerm functionalTerm,
                                         CoreUtilsFactory coreUtilsFactory, TermFactory termFactory,
                                         SubstitutionFactory substitutionFactory) {
        this(functionalTerm.getVariableStream(), coreUtilsFactory, termFactory, substitutionFactory);
    }

    /**
     * Treats all the variables as independently nullable
     */
    @AssistedInject
    protected VariableNullabilityImpl(@Assisted Stream<Variable> variableStream, CoreUtilsFactory coreUtilsFactory,
                                      TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        scope = variableStream.collect(ImmutableCollectors.toSet());
        nullableGroups = scope.stream()
                .map(ImmutableSet::of)
                .collect(ImmutableCollectors.toSet());
        this.coreUtilsFactory = coreUtilsFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.nullableVariables = scope;
    }

    /**
     * Non-projected variables ("external") are considered as nullable.
     *
     * Relevant when propagating down constraints
     *
     */
    @Override
    public boolean isPossiblyNullable(Variable variable) {
        return (!scope.contains(variable)) || getNullableVariables().contains(variable);
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables() {
        if (nullableVariables == null)
            nullableVariables = nullableGroups.stream()
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toSet());
        return nullableVariables;
    }

    @Override
    public boolean canPossiblyBeNullSeparately(ImmutableSet<Variable> variables) {
        if (variableMap == null)
            variableMap = extractVariableMap(ImmutableList.copyOf(nullableGroups));

        return variables.stream()
                .filter(variableMap::containsKey)
                .map(variableMap::get)
                .distinct()
                .count() > 1;
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> getNullableGroups() {
        return nullableGroups;
    }

    /**
     * Creates a new (immutable) VariableNullability
     *
     * For each entry (k,v) where k is a novel variable,
     *  - if (k == v): create a new nullable group for k
     *
     *  - else k is bound to v and therefore is added to the nullable group of v.
     *
     *  Invalid input entry:
     *    - k is already in a nullable group
     *    - v != k is not already in a nullable group
     *
     *
     */
    private VariableNullabilityImpl appendNewVariables(ImmutableMap<Variable, Variable> nullabilityBindings,
                                                       ImmutableSet<Variable> newScope) {
        ImmutableList<ImmutableSet<Variable>> groupList = ImmutableList.copyOf(nullableGroups);
        ImmutableMap<Variable, Integer> originalVariableMap = extractVariableMap(groupList);

        AtomicInteger groupCount = new AtomicInteger(groupList.size());

        ImmutableMultimap<Integer, Variable> newVariableMultimap = nullabilityBindings.entrySet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        e -> e.getKey().equals(e.getValue())
                                ? groupCount.getAndIncrement()
                                : originalVariableMap.get(e.getValue()),
                        Map.Entry::getKey));

        ImmutableSet<ImmutableSet<Variable>> newNullableGroups = IntStream.range(0, groupCount.get())
                .mapToObj(i -> i < groupList.size()
                        ? Sets.union(groupList.get(i), ImmutableSet.copyOf(newVariableMultimap.get(i))).immutableCopy()
                        : ImmutableSet.copyOf(newVariableMultimap.get(i)))
                .collect(ImmutableCollectors.toSet());

        return new VariableNullabilityImpl(newNullableGroups, newScope, coreUtilsFactory, termFactory, substitutionFactory);
    }

    @Override
    public VariableNullability update(Substitution<? extends ImmutableTerm> substitution,
                                      ImmutableSet<Variable> newScope) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(substitution.getDomain(), getNullableVariables()).immutableCopy());
        return update(substitution, newScope, variableGenerator);
    }

    @Override
    public VariableNullability applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        ImmutableSet<Variable> newScope = substitutionFactory.apply(freshRenamingSubstitution, scope);

        ImmutableSet<ImmutableSet<Variable>> newNullableGroups = nullableGroups.stream()
                .map(s -> substitutionFactory.apply(freshRenamingSubstitution, s))
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(newNullableGroups, newScope);
    }

    @Override
    public VariableNullability extendToExternalVariables(Stream<Variable> possiblyExternalVariables) {
        ImmutableSet<Variable> externalVariables = possiblyExternalVariables
                .filter(v -> !scope.contains(v))
                .collect(ImmutableCollectors.toSet());

        if (externalVariables.isEmpty())
            return this;

        ImmutableSet<Variable> newScope = Sets.union(scope, externalVariables).immutableCopy();
        ImmutableSet<ImmutableSet<Variable>> newNullableGroups = Stream.concat(
                externalVariables.stream()
                        .map(ImmutableSet::of),
                nullableGroups.stream())
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(newNullableGroups, newScope);
    }

    private VariableNullability update(Substitution<?> substitution,
                                       ImmutableSet<Variable> newScope,
                                       VariableGenerator variableGenerator) {
        /*
         * The substitutions are split by nesting level
         */
        VariableNullability nullabilityBeforeProjectingOut = splitSubstitution(substitution, variableGenerator)
                .reduce( this,
                        this::updateVariableNullability,
                        (n1, n2) -> {
                            throw new MinorOntopInternalBugException("vns are not expected to be combined");
                        });
        /*
         * Projects away irrelevant variables
         */
        ImmutableSet<ImmutableSet<Variable>> nullableGroups = nullabilityBeforeProjectingOut.getNullableGroups().stream()
                .map(g -> Sets.intersection(g, newScope).immutableCopy())
                .filter(g -> !g.isEmpty())
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(nullableGroups, newScope);
    }

    /*
     * TODO: explain
     */
    private Stream<Substitution<? extends ImmutableTerm>> splitSubstitution(
            Substitution<? extends ImmutableTerm> substitution, VariableGenerator variableGenerator) {

        ImmutableMap<Variable, SplitImmutableFunctionalTerm> subTermNames = substitution.builder()
                .restrictRangeTo(ImmutableFunctionalTerm.class)
                .toMap((v, t) -> new SplitImmutableFunctionalTerm(t, variableGenerator));

        if (subTermNames.values().stream().allMatch(SplitImmutableFunctionalTerm::isEmpty))
            return Stream.of(substitution);

        Substitution<ImmutableTerm> parentSubstitution = substitution.builder()
                .<ImmutableTerm>restrictRangeTo(ImmutableFunctionalTerm.class)
                .transformOrRetain(subTermNames::get, (t, split) -> split.getSplitTerm())
                .build();

        Substitution<ImmutableTerm> childSubstitution = subTermNames.values().stream()
                        .map(SplitImmutableFunctionalTerm::getSubstitution)
                        .reduce(substitutionFactory.getSubstitution(), substitutionFactory::union);

        Stream<Substitution<NonFunctionalTerm>> nonFunctionalSubstitution = Optional.of(substitution.restrictRangeTo(NonFunctionalTerm.class))
                .filter(s -> !s.isEmpty())
                .stream();



        return Stream.concat(
                // Recursive
                splitSubstitution(childSubstitution, variableGenerator),
                Stream.concat(Stream.of(parentSubstitution), nonFunctionalSubstitution));
    }

    private final class SplitImmutableFunctionalTerm {
        final ImmutableFunctionalTerm term;
        final ArgumentSubstitution<ImmutableTerm> map;

        SplitImmutableFunctionalTerm(ImmutableFunctionalTerm term, VariableGenerator variableGenerator) {
            this.term = term;

            ImmutableMap<Integer, Variable> m = IntStream.range(0, term.getArity())
                    .filter(i -> term.getTerm(i) instanceof ImmutableFunctionalTerm)
                    .mapToObj(i -> Maps.immutableEntry(i, variableGenerator.generateNewVariable()))
                    .collect(ImmutableCollectors.toMap());

            this.map = new ArgumentSubstitution<>(m, Optional::ofNullable);
        }

        ImmutableFunctionalTerm getSplitTerm() {
            if (map.isEmpty())
                return term;

            return termFactory.getImmutableFunctionalTerm(term.getFunctionSymbol(), map.replaceTerms(term.getTerms()));
        }

        Substitution<ImmutableTerm> getSubstitution() {
            return map.getSubstitution(substitutionFactory, term.getTerms());
        }

        boolean isEmpty() {
            return map.isEmpty();
        }
    }


    private VariableNullabilityImpl updateVariableNullability(VariableNullabilityImpl childNullability,
            Substitution<? extends ImmutableTerm> nonNestedSubstitution) {

        // TODO: find a better name
        ImmutableMap<Variable, Variable> nullabilityBindings = nonNestedSubstitution.builder()
                .toMapIgnoreOptional((v, t) -> evaluateTermNullability(t, childNullability, v));

        ImmutableSet<Variable> newScope = Sets.union(childNullability.scope, nonNestedSubstitution.getDomain()).immutableCopy();

        return childNullability.appendNewVariables(nullabilityBindings, newScope);
    }

    private Optional<Variable> evaluateTermNullability(ImmutableTerm term, VariableNullability childNullability, Variable key) {

        if (term instanceof Constant) {
            return term.isNull()
                    ? Optional.of(key)
                    : Optional.empty();
        }
        else if (term instanceof Variable)
            return Optional.of((Variable) term)
                    .filter(childNullability::isPossiblyNullable);
        else {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            FunctionSymbol.FunctionalTermNullability results = functionalTerm.getFunctionSymbol().evaluateNullability(
                    (ImmutableList<NonFunctionalTerm>) functionalTerm.getTerms(),
                    childNullability, termFactory);

            return results.isNullable()
                    ? Optional.of(results.getBoundVariable().orElse(key))
                    : Optional.empty();
        }
    }


    private static ImmutableMap<Variable, Integer> extractVariableMap(ImmutableList<ImmutableSet<Variable>> groupList) {
        return IntStream.range(0, groupList.size())
                .boxed()
                .flatMap(i -> groupList.get(i).stream()
                        .map(v -> Maps.immutableEntry(v, i)))
                .collect(ImmutableCollectors.toMap());
    }

    @Override
    public boolean canPossiblyBeNullSeparately(ImmutableList<? extends ImmutableTerm> terms) {

        ImmutableSet<Variable> variables = terms.stream()
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable) t)
                .collect(ImmutableCollectors.toSet());

        if (terms.stream().allMatch(t -> t instanceof Variable)) {
            return canPossiblyBeNullSeparately(variables);
        }

        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Stream.concat(
                        terms.stream().flatMap(ImmutableTerm::getVariableStream),
                        getNullableVariables().stream())
                        .collect(ImmutableCollectors.toSet()));

        Substitution<?> substitution = terms.stream()
                .filter(t -> t instanceof NonVariableTerm)
                .collect(substitutionFactory.toSubstitution(
                        t -> variableGenerator.generateNewVariable(),
                        t -> t));

        VariableNullability newVariableNullability = update(substitution, substitution.getDomain(), variableGenerator);

        ImmutableSet<Variable> newVariables = Sets.union(variables, substitution.getDomain()).immutableCopy();

        return newVariableNullability.canPossiblyBeNullSeparately(newVariables);
    }
}
