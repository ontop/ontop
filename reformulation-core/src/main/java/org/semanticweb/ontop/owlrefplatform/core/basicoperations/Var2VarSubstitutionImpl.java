package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.data.TreeMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable { Variable --> Variable } substitution.
 */
public class Var2VarSubstitutionImpl extends AbstractImmutableSubstitutionImpl implements Var2VarSubstitution {

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

    /**
     * Extracts the sub-set of the substitution entries that are var-to-var mappings.
     */
    public static Var2VarSubstitution extractVar2VarSubstitution(Substitution substitution) {
        /**
         * Saves an unnecessary computation.
         */
        if (substitution instanceof Var2VarSubstitution)
            return (Var2VarSubstitution) substitution;

        ImmutableMap.Builder<VariableImpl, VariableImpl> substitutionMapBuilder = ImmutableMap.builder();

        for (Map.Entry<VariableImpl, Term> entry : substitution.getMap().entrySet()) {
            Term target = entry.getValue();
            if (target instanceof VariableImpl) {
                substitutionMapBuilder.put(entry.getKey(), (VariableImpl) target);
            }
        }
        return new Var2VarSubstitutionImpl(substitutionMapBuilder.build());
    }


    @Override
    public ImmutableMap<VariableImpl, VariableImpl> getVar2VarMap() {
        return map;
    }

    @Override
    public VariableImpl applyToVariable(VariableImpl variable) {
        if (map.containsKey(variable))
            return map.get(variable);
        return variable;
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
    public ImmutableMap<VariableImpl, ImmutableTerm> getImmutableMap() {
        return (ImmutableMap<VariableImpl, ImmutableTerm>)(ImmutableMap<VariableImpl, ?>)map;
    }

    @Override
    public boolean isDefining(VariableImpl variable) {
        return map.containsKey(variable);
    }
}
