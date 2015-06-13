package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Just an wrapper above a ImmutableMap<VariableImpl, ImmutableTerm> map.
 */
public class ImmutableSubstitutionImpl extends LocallyImmutableSubstitutionImpl implements ImmutableSubstitution {

    private final ImmutableMap<VariableImpl, ImmutableTerm> map;

    public ImmutableSubstitutionImpl(ImmutableMap<VariableImpl, ImmutableTerm> substitutionMap) {
        this.map = substitutionMap;
    }

    @Override
    public Term get(VariableImpl var) {
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
}
