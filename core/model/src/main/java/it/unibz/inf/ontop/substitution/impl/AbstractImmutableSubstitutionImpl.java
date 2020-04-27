package it.unibz.inf.ontop.substitution.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

/**
 * Common abstract class for ImmutableSubstitutionImpl and Var2VarSubstitutionImpl
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractImmutableSubstitutionImpl<T  extends ImmutableTerm>
        extends AbstractProtoSubstitution<T> implements ImmutableSubstitution<T> {

    final AtomFactory atomFactory;
    final SubstitutionFactory substitutionFactory;

    protected AbstractImmutableSubstitutionImpl(AtomFactory atomFactory, TermFactory termFactory,
                                                SubstitutionFactory substitutionFactory) {
        super(termFactory);
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
    }


    @Override
    public <P extends AtomPredicate> DataAtom<P> applyToDataAtom(DataAtom<P> atom) throws ConversionException {
        ImmutableList<? extends ImmutableTerm> newArguments = apply(atom.getArguments());

        if (!newArguments.stream().allMatch(t -> t instanceof VariableOrGroundTerm)) {
            throw new ConversionException("The substitution applied to a DataAtom has " +
                    " produced some non-VariableOrGroundTerm arguments " + newArguments);
        }

        return atomFactory.getDataAtom(atom.getPredicate(), (ImmutableList<VariableOrGroundTerm>) newArguments);
    }

    @Override
    public ImmutableMap<Integer, ? extends VariableOrGroundTerm> applyToArgumentMap(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap)
            throws ConversionException {
        ImmutableMap<Integer, ? extends ImmutableTerm> newArgumentMap = argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> apply(e.getValue())));

        if (!newArgumentMap.values().stream().allMatch(t -> t instanceof VariableOrGroundTerm)) {
            throw new ConversionException("The substitution applied to an argument map has " +
                    " produced some non-VariableOrGroundTerm arguments " + newArgumentMap);
        }
        return (ImmutableMap<Integer, ? extends VariableOrGroundTerm>) newArgumentMap;
    }

    @Override
    public DistinctVariableOnlyDataAtom applyToDistinctVariableOnlyDataAtom(DistinctVariableOnlyDataAtom dataAtom)
            throws ConversionException {
        ImmutableList<? extends ImmutableTerm> newArguments = apply(dataAtom.getArguments());

        if (!newArguments.stream().allMatch(t -> t instanceof Variable)) {
            throw new ConversionException("The substitution applied to a DistinctVariableOnlyDataAtom has " +
                    " produced some non-Variable arguments " + newArguments);
        }
        ImmutableList<Variable> variableArguments =  (ImmutableList<Variable>) newArguments;

        if (variableArguments.size() == ImmutableSet.copyOf(variableArguments).size())
            return atomFactory.getDistinctVariableOnlyDataAtom(dataAtom.getPredicate(), variableArguments);
        else {
            throw new ConversionException("The substitution applied a DistinctVariableOnlyDataAtom has introduced" +
                    " redundant variables: " + newArguments);
        }
    }


    /**
     *" "this o f"
     *
     * Equivalent to the function x -> this.apply(f.apply(x))
     *
     * Follows the formal definition of a the composition of two substitutions.
     *
     */
    @Override
    public ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> f) {
        if (isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)f;
        }
        if (f.isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)this;
        }

        Map<Variable, ImmutableTerm> substitutionMap = new HashMap<>();

        /*
         * For all variables in the domain of f
         */

        for (Map.Entry<Variable, ? extends ImmutableTerm> gEntry :  f.getImmutableMap().entrySet()) {
            substitutionMap.put(gEntry.getKey(), apply(gEntry.getValue()));
        }

        /*
         * For the other variables (in the local domain but not in f)
         */
        for (Map.Entry<Variable, ? extends ImmutableTerm> localEntry :  getImmutableMap().entrySet()) {
            Variable localVariable = localEntry.getKey();

            if (substitutionMap.containsKey(localVariable))
                continue;

            substitutionMap.put(localVariable, localEntry.getValue());
        }

        return substitutionFactory.getSubstitution(
                substitutionMap.entrySet().stream()
                        // Clean out entries like t/t
                        .filter(entry -> !entry.getKey().equals(entry.getValue()))
                        .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public ImmutableSubstitution<T> composeWith2(ImmutableSubstitution<? extends T> g) {
        return (ImmutableSubstitution<T>) composeWith(g);
    }

    /**
     * In case some sub-classes wants to add a new unionXX method returning an optional in their own type.
     *
     * TODO: explain
     */
    protected Optional<ImmutableMap<Variable, T>> computeUnionMap(ImmutableSubstitution<T> otherSubstitution) {
        ImmutableMap.Builder<Variable, T> mapBuilder = ImmutableMap.builder();
        mapBuilder.putAll(getImmutableMap());

        ImmutableMap<Variable, T> otherMap = otherSubstitution.getImmutableMap();
        for(Variable otherVariable : otherMap.keySet()) {

            T otherTerm = otherMap.get(otherVariable);

            /*
             * TODO: explain
             */
            if (isDefining(otherVariable) && (!get(otherVariable).equals(otherTerm))) {
                return Optional.empty();
            }

            mapBuilder.put(otherVariable, otherTerm);
        }

        return Optional.of(mapBuilder.build());
    }

    @Override
    public Optional<ImmutableSubstitution<T>> union(ImmutableSubstitution<T> otherSubstitution) {
        if (otherSubstitution.isEmpty())
            return Optional.of(this);
        else if(isEmpty())
            return Optional.of(otherSubstitution);

        Optional<ImmutableMap<Variable, T>> optionalMap = computeUnionMap(otherSubstitution);
        if (optionalMap.isPresent()) {
            ImmutableSubstitution<T> unionSubstitution = substitutionFactory.getSubstitution(optionalMap.get());
            return Optional.of(unionSubstitution);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableSubstitution<? extends ImmutableTerm>> unionHeterogeneous(
            ImmutableSubstitution<? extends ImmutableTerm> otherSubstitution) {
        if (otherSubstitution.isEmpty())
            return Optional.of(this);
        else if(isEmpty())
            return Optional.of(otherSubstitution);

        ImmutableMap<Variable, T> localMap = getImmutableMap();
        ImmutableSet<? extends Map.Entry<Variable, ? extends ImmutableTerm>> otherEntrySet = otherSubstitution.getImmutableMap().entrySet();

        /*
         * Checks for multiple entries of the same variable
         */
        if (otherEntrySet.stream()
                .filter(e -> localMap.containsKey(e.getKey()))
                .anyMatch(e -> !localMap.get(e.getKey()).equals(e.getValue()))) {
            return Optional.empty();
        }
        else {
            ImmutableMap<Variable, ? extends ImmutableTerm> newMap = Stream.concat(localMap.entrySet().stream(),
                    otherEntrySet.stream())
                    .distinct()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue));
            return Optional.of(substitutionFactory.getSubstitution(newMap));
        }
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> applyToTarget(ImmutableSubstitution<? extends ImmutableTerm>
                                                                          otherSubstitution) {
        ImmutableMap.Builder<Variable, ImmutableTerm> mapBuilder = ImmutableMap.builder();

        ImmutableMap<Variable, ? extends ImmutableTerm> otherSubstitutionMap = otherSubstitution.getImmutableMap();
        for (Map.Entry<Variable, ? extends ImmutableTerm> otherEntry : otherSubstitutionMap.entrySet()) {
            ImmutableTerm newValue = apply(otherEntry.getValue());
            if (!otherEntry.getKey().equals(newValue))
                mapBuilder.put(otherEntry.getKey(), newValue);
        }
        return substitutionFactory.getSubstitution(mapBuilder.build());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableSubstitution) {
            return getImmutableMap().equals(((ImmutableSubstitution) other).getImmutableMap());
        }
        return false;
    }

    protected abstract ImmutableSubstitution<T> constructNewSubstitution(ImmutableMap<Variable, T> map);

    @Override
    public ImmutableSubstitution<T> orientate(ImmutableList<Variable> priorityVariables) {
        if (priorityVariables.isEmpty() || isEmpty()) {
            return this;
        }

        ImmutableMap<Variable, T> localMap = getImmutableMap();
        ImmutableSet<Variable> domain = getDomain();

        if (localMap.values().stream()
                .flatMap(ImmutableTerm::getVariableStream)
                .anyMatch(domain::contains)) {
            throw new UnsupportedOperationException("The orientate() method requires the domain and the range to be disjoint");
        }

        ImmutableMap<Variable, Variable> renamingMap = localMap.entrySet().stream()
                // Will produce some results only if T is compatible with Variable
                .filter(e -> e.getValue() instanceof Variable)
                .filter(e -> {
                    int replacedVariableIndex = priorityVariables.indexOf(e.getKey());
                    int targetVariableIndex = priorityVariables.indexOf(e.getValue());
                    return replacedVariableIndex >= 0 && ((targetVariableIndex < 0)
                            || (replacedVariableIndex < targetVariableIndex));
                })
                .collect(ImmutableCollectors.toMap(
                        e -> (Variable) e.getValue(),
                        Map.Entry::getKey,
                        (v1, v2) -> priorityVariables.indexOf(v1) <= priorityVariables.indexOf(v2) ? v1 : v2
                ));



        /**
         * Applies the renaming
         */
        if (renamingMap.isEmpty()) {
            return this;
        }
        else {
            Var2VarSubstitution renamingSubstitution = substitutionFactory.getVar2VarSubstitution(renamingMap);

            ImmutableMap<Variable, T> orientedMap = Stream.concat(
                    localMap.entrySet().stream()
                            /**
                             * Removes entries that will be reversed
                             */
                            .filter(e -> !Optional.ofNullable(renamingMap.get(e.getValue()))
                                    .filter(newValue -> newValue.equals(e.getKey()))
                                    .isPresent()),
                    renamingMap.entrySet().stream()
                            .map(e -> (Map.Entry<Variable, T>) e))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> renamingSubstitution.applyToTerm(e.getValue())
                    ));

            return constructNewSubstitution(orientedMap);
        }
    }

    @Override
    public  Optional<ImmutableExpression> convertIntoBooleanExpression() {
        return convertIntoBooleanExpression(this);
    }

    @Override
    public ImmutableSubstitution<T> reduceDomainToIntersectionWith(ImmutableSet<Variable> restrictingDomain) {
        if (restrictingDomain.containsAll(getDomain()))
            return this;
        return substitutionFactory.getSubstitution(
                this.getImmutableMap().entrySet().stream()
                        .filter(e -> restrictingDomain.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> simplifyValues(VariableNullability variableNullability) {
        return simplifyValues(Optional.of(variableNullability));
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> simplifyValues() {
        return simplifyValues(Optional.empty());
    }

    public ImmutableSubstitution<ImmutableTerm> simplifyValues(Optional<VariableNullability> variableNullability) {
        return substitutionFactory.getSubstitution(getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> variableNullability
                                .map(n -> e.getValue().simplify(n))
                                .orElseGet(() -> e.getValue().simplify()))));
    }

    protected Optional<ImmutableExpression> convertIntoBooleanExpression(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {

        List<ImmutableExpression> equalities = new ArrayList<>();

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution.getImmutableMap().entrySet()) {
            equalities.add(termFactory.getStrictEquality(entry.getKey(), entry.getValue()));
        }

        switch(equalities.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(equalities.get(0));
            default:
                Iterator<ImmutableExpression> equalityIterator = equalities.iterator();
                // Non-final
                ImmutableExpression aggregateExpression = equalityIterator.next();
                while (equalityIterator.hasNext()) {
                    aggregateExpression = termFactory.getConjunction(aggregateExpression, equalityIterator.next());
                }
                return Optional.of(aggregateExpression);
        }
    }

    @Override
    public ImmutableSubstitution<VariableOrGroundTerm> getVariableOrGroundTermFragment() {
        ImmutableMap<Variable, VariableOrGroundTerm> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof VariableOrGroundTerm)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (VariableOrGroundTerm) e.getValue()));

        return substitutionFactory.getSubstitution(newMap);
    }

    @Override
    public ImmutableSubstitution<NonGroundFunctionalTerm> getNonGroundFunctionalTermFragment() {
        ImmutableMap<Variable, NonGroundFunctionalTerm> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof NonGroundFunctionalTerm)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (NonGroundFunctionalTerm) e.getValue()));

        return substitutionFactory.getSubstitution(newMap);
    }

    @Override
    public ImmutableSubstitution<GroundFunctionalTerm> getGroundFunctionalTermFragment() {
        ImmutableMap<Variable, GroundFunctionalTerm> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof GroundFunctionalTerm)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (GroundFunctionalTerm) e.getValue()));

        return substitutionFactory.getSubstitution(newMap);
    }

    @Override
    public ImmutableSubstitution<NonFunctionalTerm> getNonFunctionalTermFragment() {
        ImmutableMap<Variable, NonFunctionalTerm> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof NonFunctionalTerm)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (NonFunctionalTerm) e.getValue()));

        return substitutionFactory.getSubstitution(newMap);
    }

    @Override
    public ImmutableSubstitution<ImmutableFunctionalTerm> getFunctionalTermFragment() {
        ImmutableMap<Variable, ImmutableFunctionalTerm> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof ImmutableFunctionalTerm)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (ImmutableFunctionalTerm) e.getValue()));

        return substitutionFactory.getSubstitution(newMap);
    }

    @Override
    public ImmutableSubstitution<NonVariableTerm> getNonVariableTermFragment() {
        ImmutableMap<Variable, NonVariableTerm> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof NonVariableTerm)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (NonVariableTerm) e.getValue()));

        return substitutionFactory.getSubstitution(newMap);
    }
}
