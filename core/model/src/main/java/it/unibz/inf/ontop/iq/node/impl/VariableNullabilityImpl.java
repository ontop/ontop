package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
            variableMap = extractVariableMap(nullableGroups);

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
                        ? Sets.union(groupList.get(i), ImmutableSet.copyOf(newVariableMultimap.get(i)))
                            .immutableCopy()
                        : ImmutableSet.copyOf(newVariableMultimap.get(i)))
                .collect(ImmutableCollectors.toSet());

        return new VariableNullabilityImpl(newNullableGroups, newScope, coreUtilsFactory, termFactory, substitutionFactory);
    }

    @Override
    public VariableNullability update(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                      ImmutableSet<Variable> newScope) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(substitution.getDomain(), getNullableVariables()).immutableCopy());
        return update(substitution, newScope, variableGenerator);
    }

    @Override
    public VariableNullability applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        ImmutableSet<Variable> newScope = scope.stream()
                .map(freshRenamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<ImmutableSet<Variable>> newNullableGroups = nullableGroups.stream()
                .map(g -> g.stream()
                        .map(freshRenamingSubstitution::applyToVariable)
                        .collect(ImmutableCollectors.toSet()))
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

    private VariableNullability update(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                       ImmutableSet<Variable> newScope,
                                       VariableGenerator variableGenerator) {
        /*
         * The substitutions are split by nesting level
         */
        VariableNullability nullabilityBeforeProjectingOut = splitSubstitution(substitution, variableGenerator)
                .reduce( this,
                        (n, s) -> updateVariableNullability(s, n),
                        (n1, n2) -> {
                            throw new MinorOntopInternalBugException("vns are not expected to be combined");
                        });
        /*
         * Projects away irrelevant variables
         */
        ImmutableSet<ImmutableSet<Variable>> nullableGroups = nullabilityBeforeProjectingOut.getNullableGroups().stream()
                .map(g -> g.stream()
                        .filter(newScope::contains)
                        .collect(ImmutableCollectors.toSet()))
                .filter(g -> !g.isEmpty())
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(nullableGroups, newScope);
    }

    /*
     * TODO: explain
     */
    private Stream<ImmutableSubstitution<? extends ImmutableTerm>> splitSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, VariableGenerator variableGenerator) {

        ImmutableMultimap<Variable, Integer> functionSubTermMultimap = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof ImmutableFunctionalTerm)
                .flatMap(e -> {
                    ImmutableList<? extends ImmutableTerm> subTerms = ((ImmutableFunctionalTerm) e.getValue()).getTerms();
                    return IntStream.range(0, subTerms.size())
                            .filter(i -> subTerms.get(i) instanceof ImmutableFunctionalTerm)
                            .mapToObj(i -> Maps.immutableEntry(e.getKey(), i));
                })
                .collect(ImmutableCollectors.toMultimap());

        if (functionSubTermMultimap.isEmpty())
            return Stream.of(substitution);

        ImmutableTable<Variable, Integer, Variable> subTermNames = functionSubTermMultimap.entries().stream()
                .map(e -> Tables.immutableCell(e.getKey(), e.getValue(), variableGenerator.generateNewVariable()))
                .collect(ImmutableCollectors.toTable());

        ImmutableMap<Variable, ImmutableTerm> parentSubstitutionMap = substitution.getImmutableMap().entrySet().stream()
                .map(e ->
                        Optional.of(functionSubTermMultimap.get(e.getKey()))
                                .filter(indexes -> !indexes.isEmpty())
                                .map(indexes -> {
                                    Variable v = e.getKey();
                                    ImmutableFunctionalTerm def = (ImmutableFunctionalTerm) substitution.get(v);
                                    ImmutableList<ImmutableTerm> newArgs = IntStream.range(0, def.getArity())
                                            .mapToObj(i -> Optional.ofNullable((ImmutableTerm) subTermNames.get(v, i))
                                                    .orElseGet(() -> def.getTerm(i)))
                                            .collect(ImmutableCollectors.toList());

                                    ImmutableTerm newDef = termFactory.getImmutableFunctionalTerm(
                                            def.getFunctionSymbol(), newArgs);
                                    return Maps.immutableEntry(v, newDef);
                                })
                                .orElse((Map.Entry<Variable, ImmutableTerm>)e))
                .collect(ImmutableCollectors.toMap());

        ImmutableSubstitution<ImmutableTerm> parentSubstitution = substitutionFactory.getSubstitution(parentSubstitutionMap);


        ImmutableSubstitution<ImmutableTerm> childSubstitution = substitutionFactory.getSubstitution(
                subTermNames.cellSet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Table.Cell::getValue,
                                c -> ((ImmutableFunctionalTerm) substitution.get(c.getRowKey())).getTerm(c.getColumnKey()))));

        return Stream.concat(
                // Recursive
                splitSubstitution(childSubstitution, variableGenerator),
                Stream.of(parentSubstitution));
    }


    private VariableNullabilityImpl updateVariableNullability(
            ImmutableSubstitution<? extends ImmutableTerm> nonNestedSubstitution, VariableNullabilityImpl childNullability) {

        // TODO: find a better name
        ImmutableMap<Variable, Variable> nullabilityBindings = nonNestedSubstitution.getImmutableMap().entrySet().stream()
                .flatMap(e -> evaluateTermNullability(e.getValue(), childNullability, e.getKey())
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(ImmutableCollectors.toMap());

        ImmutableSet<Variable> newScope = Sets.union(childNullability.scope, nonNestedSubstitution.getDomain()).immutableCopy();

        return childNullability.appendNewVariables(nullabilityBindings, newScope);
    }

    private Optional<Map.Entry<Variable, Variable>> evaluateTermNullability(
            ImmutableTerm term, VariableNullability childNullability, Variable key) {
        if (term instanceof Constant) {
            return term.isNull()
                    ? Optional.of(Maps.immutableEntry(key, key))
                    : Optional.empty();
        }
        else if (term instanceof Variable)
            return Optional.of((Variable) term)
                    .filter(childNullability::isPossiblyNullable)
                    .map(v -> Maps.immutableEntry(key, v));
        else {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            FunctionSymbol.FunctionalTermNullability results = functionalTerm.getFunctionSymbol().evaluateNullability(
                    (ImmutableList<NonFunctionalTerm>) functionalTerm.getTerms(),
                    childNullability, termFactory);

            return results.isNullable()
                    ? Optional.of(results.getBoundVariable()
                    .map(v -> Maps.immutableEntry(key, v))
                    .orElseGet(() -> Maps.immutableEntry(key, key)))
                    : Optional.empty();
        }
    }


    private static ImmutableMap<Variable, Integer> extractVariableMap(
            ImmutableCollection<ImmutableSet<Variable>> nullableGroups) {

        ImmutableList<ImmutableSet<Variable>> groupList = ImmutableList.copyOf(nullableGroups);
        return IntStream.range(0, groupList.size())
                .boxed()
                .flatMap(i -> groupList.get(i).stream()
                        .map(v -> Maps.immutableEntry(v, i)))
                .collect(ImmutableCollectors.toMap());
    }

    @Override
    public boolean canPossiblyBeNullSeparately(ImmutableList<? extends ImmutableTerm> terms) {
        if (terms.stream().allMatch(t -> t instanceof Variable)) {
            return canPossiblyBeNullSeparately(ImmutableSet.copyOf((ImmutableList<Variable>) terms));
        }

        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Stream.concat(
                        terms.stream().flatMap(ImmutableTerm::getVariableStream),
                        getNullableVariables().stream())
                        .collect(ImmutableCollectors.toSet()));

        ImmutableSubstitution<? extends ImmutableTerm> substitution = substitutionFactory.getSubstitution(
                terms.stream()
                        .filter(t -> t instanceof NonVariableTerm)
                        .collect(ImmutableCollectors.toMap(
                                t -> variableGenerator.generateNewVariable(),
                                t -> t)));

        VariableNullability newVariableNullability = update(substitution, substitution.getDomain(), variableGenerator);

        ImmutableSet<Variable> variables = Sets.union(
                terms.stream()
                        .filter(t -> t instanceof Variable)
                        .map(t -> (Variable) t)
                        .collect(Collectors.toSet()),
                substitution.getDomain()).immutableCopy();

        return newVariableNullability.canPossiblyBeNullSeparately(variables);
    }
}
