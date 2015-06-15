package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper above a ImmutableMap<VariableImpl, ImmutableTerm> map.
 */
public class ImmutableSubstitutionImpl extends AbstractImmutableSubstitutionImpl {

    private final ImmutableMap<VariableImpl, ImmutableTerm> map;

    public ImmutableSubstitutionImpl(ImmutableMap<VariableImpl, ? extends ImmutableTerm> substitutionMap) {
        this.map = (ImmutableMap<VariableImpl, ImmutableTerm>) substitutionMap;
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

    /**
     *" "this o g"
     *
     * Equivalent to the function x -> this.apply(g.apply(x))
     *
     * Follows the formal definition of a the composition of two substitutions.
     *
     */
    @Override
    public ImmutableSubstitution composeWith(ImmutableSubstitution g) {

        Map<VariableImpl, ImmutableTerm> substitutionMap = new HashMap<>();

        /**
         * For all variables in the domain of g
         */
        for (Map.Entry<VariableImpl, ImmutableTerm> gEntry :  g.getImmutableMap().entrySet()) {
            substitutionMap.put(gEntry.getKey(), apply(gEntry.getValue()));
        }

        /**
         * For the other variables (in the local domain but not in g)
         */
        for (Map.Entry<VariableImpl, ImmutableTerm> localEntry :  getImmutableMap().entrySet()) {
            VariableImpl localVariable = localEntry.getKey();

            if (substitutionMap.containsKey(localVariable))
                continue;

            substitutionMap.put(localVariable, localEntry.getValue());
        }


        return new ImmutableSubstitutionImpl(ImmutableMap.copyOf(substitutionMap));
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }
}
