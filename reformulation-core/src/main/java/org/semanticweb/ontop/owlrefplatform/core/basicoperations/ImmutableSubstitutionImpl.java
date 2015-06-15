package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Wrapper above a ImmutableMap<VariableImpl, ImmutableTerm> map.
 */
public class ImmutableSubstitutionImpl extends AbstractImmutableSubstitutionImpl {

    private final ImmutableMap<VariableImpl, ImmutableTerm> map;

    public ImmutableSubstitutionImpl(ImmutableMap<VariableImpl, ImmutableTerm> substitutionMap) {
        this.map = substitutionMap;
    }

    @Override
    public ImmutableTerm get(VariableImpl var) {
        return map.get(var);
    }

    @Override
    public ImmutableMap<VariableImpl, ImmutableTerm> getImmutableMap() {
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
}
