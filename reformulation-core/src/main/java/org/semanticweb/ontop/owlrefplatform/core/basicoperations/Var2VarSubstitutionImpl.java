package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.data.TreeMap;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.Substitution;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.*;

import java.util.Map;
import java.util.Set;

/**
 * Immutable { Variable --> Variable } substitution.
 */
public class Var2VarSubstitutionImpl implements Var2VarSubstitution {

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
    public ImmutableMap<VariableImpl, VariableImpl> getVar2VarMap() {
        return map;
    }


    @Override
    public Term get(VariableImpl var) {
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

    /**
     * Not implemented
     */
    @Override
    public boolean compose(Substitution otherSubstitution) {
        throw new UnsupportedOperationException("Mutable operations are not supported");
    }

    /***
     * Not implemented.
     */
    @Override
    public boolean composeTerms(Term term1, Term term2) {
        throw new UnsupportedOperationException("Mutable operations are not supported");
    }

    /***
     * Not implemented.
     */
    @Override
    public boolean composeFunctions(Function term1, Function term2) {
        throw new UnsupportedOperationException("Mutable operations are not supported");
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
