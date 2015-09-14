package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.data.TreeMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.*;
import org.semanticweb.ontop.pivotalrepr.ImmutableQueryModifiers;
import org.semanticweb.ontop.pivotalrepr.impl.ImmutableQueryModifiersImpl;

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
    protected ImmutableSubstitution<Variable> constructNewSubstitution(ImmutableMap<Variable, Variable> map) {
        return new Var2VarSubstitutionImpl(map);
    }
}
