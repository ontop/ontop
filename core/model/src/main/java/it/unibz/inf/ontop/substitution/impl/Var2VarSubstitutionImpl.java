package it.unibz.inf.ontop.substitution.impl;

import com.google.common.base.Joiner;

import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;

/**
 * Immutable {@code  Variable --> Variable } substitution.
 */
public class Var2VarSubstitutionImpl extends AbstractImmutableSubstitutionImpl<Variable> implements Var2VarSubstitution {

    private final ImmutableMap<Variable, Variable> map;

    /**
     * Regular constructor
     */
    protected Var2VarSubstitutionImpl(ImmutableMap<Variable, ? extends Variable> substitutionMap, AtomFactory atomFactory,
                                      TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        super(atomFactory, termFactory, substitutionFactory);
        this.map = (ImmutableMap)substitutionMap;
    }

    @Override
    public Variable applyToVariable(Variable variable) {
        Variable r = map.get(variable);
        return r == null ? variable : r;
    }

    @Override
    public Var2VarSubstitution getVar2VarFragment() {
        return this;
    }

    @Override
    public ImmutableSubstitution<GroundTerm> getGroundTermFragment() {
        return substitutionFactory.getSubstitution();
    }

    private static final class NotASubstitutionException extends RuntimeException  {
    }

    @Override
    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> applyToSubstitution(
            ImmutableSubstitution<T> substitution) {

        if (isEmpty()) {
            return Optional.of(substitution);
        }

        try {
            ImmutableMap<Variable, T> newMap = substitution.getImmutableMap().entrySet().stream()
                    .map(e -> Maps.immutableEntry(applyToVariable(e.getKey()), applyToTerm(e.getValue())))
                    .filter(e -> !e.getKey().equals(e.getValue()))
                    .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (v1, v2) -> {
                                if (!v1.equals(v2))
                                    throw new NotASubstitutionException();
                                return v1;
                            }));

            return Optional.of(substitutionFactory.getSubstitution(newMap));
        }
        catch (NotASubstitutionException e) {
            return Optional.empty();
        }
    }

    @Override
    public Variable get(Variable var) {
        return map.get(var);
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
        return substitutionFactory.getVar2VarSubstitution(map);
    }

    @Override
    public NonGroundTerm applyToNonGroundTerm(NonGroundTerm term) {
        if (term instanceof Variable)
            return applyToVariable((Variable)term);
        else
            return (NonGroundTerm) applyToFunctionalTerm((ImmutableFunctionalTerm) term);
    }

    @Override
    public <T extends ImmutableTerm> T applyToTerm(T term) {
        return (T) super.apply(term);
    }

}
