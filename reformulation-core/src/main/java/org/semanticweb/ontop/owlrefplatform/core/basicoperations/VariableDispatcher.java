package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.VariableGenerator;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * Renames the variables in the scope of a rule.
 * For any given variable, always generates a different renaming.
 *
 * Note that the first renaming of a variable returns the same variable.
 *
 * Also generates new variables that are guaranteed not to conflict
 * with already existing variables in the body of the rule.
 *
 */
public class VariableDispatcher {

    private final VariableGenerator variableGenerator;
    /**
     * Keeps track of allocated variables to not allocate them twice.
     */
    private final Set<VariableImpl> allocatedVariables;

    /**
     * For a datalog rule
     */
    public VariableDispatcher(CQIE rule) {
        variableGenerator = new VariableGenerator(rule);
        allocatedVariables = new HashSet<>();
    }

    /**
     * Please make sure you cannot use the other constructors!
     */
    public VariableDispatcher() {
        variableGenerator = new VariableGenerator(new HashSet<Variable>());
        allocatedVariables = new HashSet<>();
    }

    /**
     * Always generates a different renaming for a given variable.
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
        VariableImpl newVariable = variableGenerator.generateNewVariable();
        allocatedVariables.add(newVariable);
        return newVariable;
    }

}
