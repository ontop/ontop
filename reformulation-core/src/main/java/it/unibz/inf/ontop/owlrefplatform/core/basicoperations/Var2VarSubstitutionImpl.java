package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.data.TreeMap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.NonGroundFunctionalTermImpl;
import it.unibz.inf.ontop.pivotalrepr.ImmutableQueryModifiers;

import java.util.Map;

/**
 * Immutable { Variable --> Variable } substitution.
 */
public class Var2VarSubstitutionImpl extends AbstractImmutableSubstitutionImpl<Variable> implements Var2VarSubstitution {

    private final ImmutableMap<Variable, Variable> map;

    /**
     * Regular constructor
     */
    public Var2VarSubstitutionImpl(Map<Variable, Variable> substitutionMap) {
        this.map = ImmutableMap.copyOf(substitutionMap);
    }

    /**
     * Functional Java constructor
     */
    public Var2VarSubstitutionImpl(TreeMap<Variable, Variable> substitutionMap) {
        this.map = ImmutableMap.copyOf(substitutionMap.toMutableMap());
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
        return new ImmutableSubstitutionImpl<>(ImmutableMap.of());
    }

    @Override
    public VariableOrGroundTerm applyToVariableOrGroundTerm(VariableOrGroundTerm term) {
        if (term instanceof Variable) {
            return applyToVariable((Variable)term);
        }

        return term;
    }

    @Override
    public NonGroundTerm applyToNonGroundTerm(NonGroundTerm term) {
        if (term instanceof Variable) {
            return applyToVariable((Variable) term);
        }
        /**
         * If not a variable, is a functional term.
         */
        return new NonGroundFunctionalTermImpl(
                applyToFunctionalTerm((ImmutableFunctionalTerm) term));
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
        return new Var2VarSubstitutionImpl(map);
    }
}
