package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.IndempotentVar2VarSubstitution;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndempotentVar2VarSubstitutionImpl extends Var2VarSubstitutionImpl implements IndempotentVar2VarSubstitution {

    public IndempotentVar2VarSubstitutionImpl(Map<VariableImpl, VariableImpl> substitutionMap) {
        super(substitutionMap);

        if (!isIndempotent(substitutionMap)) {
            throw new IllegalArgumentException("Not indempotent: " + substitutionMap);
        }
    }



    /**
     * Returns true if there is common variables in the domain and the range of the substitution map.
     */
    public static boolean isIndempotent(Map<VariableImpl, VariableImpl> substitutionMap) {
        if (substitutionMap.isEmpty())
            return true;

        Set<VariableImpl> valueSet = new HashSet<>(substitutionMap.values());
        valueSet.retainAll(substitutionMap.entrySet());

        return valueSet.isEmpty();
    }
}
