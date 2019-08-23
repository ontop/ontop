package it.unibz.inf.ontop.model.atom.impl;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

import java.util.HashSet;
import java.util.Set;

public class DataAtomTools {

    /**
     * TODO: explain
     */
    public static boolean areVariablesDistinct(ImmutableList<? extends VariableOrGroundTerm> arguments) {
        Set<Variable> encounteredVariables = new HashSet<>();

        for (VariableOrGroundTerm argument : arguments) {
            if (argument instanceof Variable) {
                if (!encounteredVariables.add((Variable)argument)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isVariableOnly(ImmutableCollection<? extends VariableOrGroundTerm> arguments) {
        for (VariableOrGroundTerm argument : arguments) {
            if (argument.isGround()) {
                return false;
            }
        }
        return true;
    }
}
