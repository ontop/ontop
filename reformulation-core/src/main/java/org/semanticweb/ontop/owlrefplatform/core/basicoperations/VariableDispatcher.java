package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.VariableGenerator;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO: explain
 * TODO: find a better name
 *
 */
public class VariableDispatcher {

    private final VariableGenerator variableGenerator;
    /**
     * TODO: explain
     */
    private final Set<VariableImpl> allocatedVariables;

    public VariableDispatcher(CQIE rule) {
        variableGenerator = new VariableGenerator(rule);
        allocatedVariables = new HashSet<>();
    }

    /**
     * TODO: explain
     */
    public synchronized VariableImpl renameDataAtomVariable(VariableImpl previousVariable) {
        /**
         * Makes sure the variable is registered as "allocated". If was not contained, returns it.
         */
        if (allocatedVariables.add(previousVariable))
            return previousVariable;

        /**
         * Otherwise, creates a new variable, registers and returns it.
         */
        VariableImpl newVariable = variableGenerator.generateNewVariableFromVar(previousVariable);
        allocatedVariables.add(newVariable);
        return newVariable;
    }

    /**
     * Just a wrapper of VariableGenerator.generateNewVariable().
     */
    public synchronized VariableImpl generateNewVariable() {
        return variableGenerator.generateNewVariable();
    }

}
