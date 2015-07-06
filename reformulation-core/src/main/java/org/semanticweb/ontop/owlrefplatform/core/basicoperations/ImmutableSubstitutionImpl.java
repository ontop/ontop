package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper above a ImmutableMap<VariableImpl, ImmutableTerm> map.
 */
public class ImmutableSubstitutionImpl<T extends ImmutableTerm> extends AbstractImmutableSubstitutionImpl<T> {

    private final ImmutableMap<VariableImpl, T> map;

    public ImmutableSubstitutionImpl(ImmutableMap<VariableImpl, ? extends T> substitutionMap) {
        this.map = (ImmutableMap<VariableImpl, T>) substitutionMap;
    }

    @Override
    public T get(VariableImpl var) {
        return map.get(var);
    }

    @Override
    public ImmutableMap<VariableImpl, T> getImmutableMap() {
        return map;
    }

    @Override
    public boolean isDefining(VariableImpl variable) {
        return map.containsKey(variable);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public ImmutableSet<VariableImpl> keySet() {
        return map.keySet();
    }

    @Override
    public final ImmutableMap<VariableImpl, Term> getMap() {
        return (ImmutableMap<VariableImpl, Term>)(ImmutableMap<VariableImpl, ?>) map;
    }

    @Override
    public ImmutableTerm applyToVariable(VariableImpl variable) {
        if (map.containsKey(variable))
            return map.get(variable);
        return variable;
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }
}
