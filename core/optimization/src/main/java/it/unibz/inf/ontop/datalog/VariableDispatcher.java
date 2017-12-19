package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

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
    private final Set<Variable> allocatedVariables;

    /**
     * For a datalog rule
     */
    public VariableDispatcher(CQIE rule, TermFactory termFactory) {
        variableGenerator = new VariableGenerator(rule, termFactory);
        allocatedVariables = new HashSet<>();
    }

    /**
     * Please make sure you cannot use the other constructors!
     */
    public VariableDispatcher(TermFactory termFactory) {
        variableGenerator = new VariableGenerator(new HashSet<Variable>(), termFactory);
        allocatedVariables = new HashSet<>();
    }

    /**
     * Always generates a different renaming for a given variable.
     */
    public synchronized Variable renameDataAtomVariable(Variable previousVariable) {
        /**
         * Makes sure the variable is registered as "allocated". If was not contained, returns it.
         */
        if (allocatedVariables.add(previousVariable))
            return previousVariable;

        /**
         * Otherwise, creates a new variable, registers and returns it.
         */
        Variable newVariable = variableGenerator.generateNewVariableFromVar(previousVariable);
        allocatedVariables.add(newVariable);
        return newVariable;
    }

    /**
     * Just a wrapper of VariableGenerator.generateNewVariable().
     */
    public synchronized Variable generateNewVariable() {
        return variableGenerator.generateNewVariable();
    }

}
