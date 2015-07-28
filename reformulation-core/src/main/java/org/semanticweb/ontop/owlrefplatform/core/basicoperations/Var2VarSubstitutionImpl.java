package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import fj.data.TreeMap;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.*;

import java.util.Map;
import java.util.Set;

/**
 * Immutable { Variable --> Variable } substitution.
 */
public class Var2VarSubstitutionImpl implements Var2VarSubstitution {

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
    public ImmutableMap<Variable, Variable> getVar2VarMap() {
        return map;
    }


    @Override
    public Term get(Variable var) {
        return map.get(var);
    }

    @Override
    public Map<Variable, Term> getMap() {
        return (Map<Variable, Term>)(Map<Variable, ?>)map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Deprecated
    public Set<Variable> keySet() {
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

    @Override
    public void put(Variable var, Term term) {

    }
}
