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
public class Var2VarSubstitutionImpl extends AbstractImmutableSubstitutionImpl<VariableImpl> implements Var2VarSubstitution {

    private final ImmutableMap<VariableImpl, VariableImpl> map;

    /**
     * Regular constructor
     */
    public Var2VarSubstitutionImpl(Map<VariableImpl, VariableImpl> substitutionMap) {
        this.map = ImmutableMap.copyOf(substitutionMap);
    }

    /**
     * Functional Java constructor
     */
    public Var2VarSubstitutionImpl(TreeMap<VariableImpl, VariableImpl> substitutionMap) {
        this.map = ImmutableMap.copyOf(substitutionMap.toMutableMap());
    }

    @Override
    public VariableImpl applyToVariable(VariableImpl variable) {
        if (map.containsKey(variable))
            return map.get(variable);
        return variable;
    }

    @Override
    public VariableOrGroundTerm applyToVariableOrGroundTerm(VariableOrGroundTerm term) {
        if (term instanceof Variable) {
            return applyToVariable((VariableImpl)term);
        }

        return term;
    }

    @Override
    public NonGroundTerm applyToNonGroundTerm(NonGroundTerm term) {
        if (term instanceof VariableImpl) {
            return applyToVariable((VariableImpl) term);
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
            VariableImpl newVariable = applyToVariable((VariableImpl) orderCondition.getVariable());
            orderConditionBuilder.add(orderCondition.newVariable(newVariable));
        }
        return immutableQueryModifiers.newSortConditions(orderConditionBuilder.build());
    }

    @Override
    public VariableImpl get(VariableImpl var) {
        return map.get(var);
    }

    @Override
    public ImmutableMap<VariableImpl, Term> getMap() {
        return (ImmutableMap<VariableImpl, Term>)(ImmutableMap<VariableImpl, ?>)map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @Deprecated
    public ImmutableSet<VariableImpl> keySet() {
        return map.keySet();
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }

    @Override
    public ImmutableMap<VariableImpl, VariableImpl> getImmutableMap() {
        return map;
    }

    @Override
    public boolean isDefining(VariableImpl variable) {
        return map.containsKey(variable);
    }
}
