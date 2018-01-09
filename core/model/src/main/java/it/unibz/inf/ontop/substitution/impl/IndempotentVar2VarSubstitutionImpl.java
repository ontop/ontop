package it.unibz.inf.ontop.substitution.impl;

import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.IndempotentVar2VarSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndempotentVar2VarSubstitutionImpl extends Var2VarSubstitutionImpl implements IndempotentVar2VarSubstitution {

    protected IndempotentVar2VarSubstitutionImpl(Map<Variable, Variable> substitutionMap, AtomFactory atomFactory,
                                                 TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        super(substitutionMap, atomFactory, termFactory, substitutionFactory);

        if (!isIndempotent(substitutionMap)) {
            throw new IllegalArgumentException("Not indempotent: " + substitutionMap);
        }
    }



    /**
     * Returns true if there is common variables in the domain and the range of the substitution map.
     */
    public static boolean isIndempotent(Map<Variable, Variable> substitutionMap) {
        if (substitutionMap.isEmpty())
            return true;

        Set<Variable> valueSet = new HashSet<>(substitutionMap.values());
        valueSet.retainAll(substitutionMap.entrySet());

        return valueSet.isEmpty();
    }
}
