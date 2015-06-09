package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import fj.data.TreeMap;
import org.semanticweb.ontop.model.Function;
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
    public Map<VariableImpl, Term> getMap() {
        return (Map<VariableImpl, Term>)(Map<VariableImpl, ?>)map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @Deprecated
    public Set<VariableImpl> keySet() {
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
        throw new UnsupportedOperationException("Not implemented (yet)!");
    }

    /***
     * Not implemented.
     */
    @Override
    public boolean composeTerms(Term term1, Term term2) {
        throw new UnsupportedOperationException("Not implemented (yet)!");
    }

    /***
     * Not implemented.
     */
    @Override
    public boolean composeFunctions(Function term1, Function term2) {
        throw new UnsupportedOperationException("Not implemented (yet)!");
    }
}
