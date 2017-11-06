package it.unibz.inf.ontop.substitution.impl;

import com.google.common.base.Joiner;

import java.util.AbstractMap;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.OrderCondition;

import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable { Variable --> Variable } substitution.
 */
public class Var2VarSubstitutionImpl extends AbstractImmutableSubstitutionImpl<Variable> implements Var2VarSubstitution {

    private static class NotASubstitutionException extends RuntimeException  {
    }


    private final ImmutableMap<Variable, Variable> map;

    /**
     * Regular constructor
     */
    protected Var2VarSubstitutionImpl(Map<Variable, Variable> substitutionMap, AtomFactory atomFactory,
                                      TermFactory termFactory) {
        super(atomFactory, termFactory);
        this.map = ImmutableMap.copyOf(substitutionMap);
    }

    @Override
    public Variable applyToVariable(Variable variable) {
        if (map.containsKey(variable))
            return map.get(variable);
        return variable;
    }

    @Override
    public Var2VarSubstitution getVar2VarFragment() {
        return this;
    }

    @Override
    public ImmutableSubstitution<GroundTerm> getVar2GroundTermFragment() {
        return new ImmutableSubstitutionImpl<>(ImmutableMap.of(), getAtomFactory(), getTermFactory());
    }

    @Override
    public Optional<ImmutableQueryModifiers> applyToQueryModifiers(ImmutableQueryModifiers immutableQueryModifiers) {
        ImmutableList.Builder<OrderCondition> orderConditionBuilder = ImmutableList.builder();

        for (OrderCondition orderCondition : immutableQueryModifiers.getSortConditions()) {
            Variable newVariable = applyToVariable((Variable) orderCondition.getVariable());
            orderConditionBuilder.add(orderCondition.newVariable(newVariable));
        }
        return immutableQueryModifiers.newSortConditions(orderConditionBuilder.build());
    }

    /**
     * TODO: directly build an ImmutableMap
     */
    @Override
    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> applyToSubstitution(
            ImmutableSubstitution<T> substitution) {

        if (isEmpty()) {
            return Optional.of(new ImmutableSubstitutionImpl<>(substitution.getImmutableMap(), getAtomFactory(),
                    getTermFactory()));
        }

        try {
            ImmutableMap<Variable, T> newMap = ImmutableMap.copyOf(substitution.getImmutableMap().entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(applyToVariable(e.getKey()), applyToTerm(e.getValue())))
                    .distinct()
                    .filter(e -> ! e.getKey().equals(e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (e1, e2) -> {
                                throw new NotASubstitutionException();
                            })));
            return Optional.of(new ImmutableSubstitutionImpl<>(newMap, getAtomFactory(), getTermFactory()));
        } catch (NotASubstitutionException e) {
            return Optional.empty();
        }
    }

    @Override
    public Variable get(Variable var) {
        return map.get(var);
    }

    @Override
    public ImmutableMap<Variable, Term> getMap() {
        return (ImmutableMap<Variable, Term>)(ImmutableMap<Variable, ?>)map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }

    @Override
    public ImmutableMap<Variable, Variable> getImmutableMap() {
        return map;
    }

    @Override
    public boolean isDefining(Variable variable) {
        return map.containsKey(variable);
    }

    @Override
    public ImmutableSet<Variable> getDomain() {
        return map.keySet();
    }

    @Override
    protected ImmutableSubstitution<Variable> constructNewSubstitution(ImmutableMap<Variable, Variable> map) {
        return new Var2VarSubstitutionImpl(map, getAtomFactory(), getTermFactory());
    }

    @Override
    public Var2VarSubstitution composeWithVar2Var(Var2VarSubstitution g) {
        return new Var2VarSubstitutionImpl(composeRenaming(g)
                .collect(ImmutableCollectors.toMap()), getAtomFactory(), getTermFactory());
    }

    @Override
    public <T extends ImmutableTerm> T applyToTerm(T term) {
        return (T) super.apply(term);
    }

    protected Stream<Map.Entry<Variable, Variable>> composeRenaming(Var2VarSubstitution g ) {
        ImmutableSet<Variable> gDomain = g.getDomain();

        Stream<Map.Entry<Variable, Variable>> gEntryStream = g.getImmutableMap().entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), applyToVariable(e.getValue())));

        Stream<Map.Entry<Variable, Variable>> localEntryStream = getImmutableMap().entrySet().stream()
                .filter(e -> !gDomain.contains(e.getKey()));

        return Stream.concat(gEntryStream, localEntryStream)
                .filter(e -> !e.getKey().equals(e.getValue()));
    }
}
