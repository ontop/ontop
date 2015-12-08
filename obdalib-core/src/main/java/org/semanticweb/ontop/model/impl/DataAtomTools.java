package org.semanticweb.ontop.model.impl;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.VariableOrGroundTerm;

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
