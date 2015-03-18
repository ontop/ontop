package org.semanticweb.ontop.model;

import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO: describe
 */
public class VariableGenerator {

    private int count;
    private final OBDADataFactory dataFactory;
    private final Set<Variable> knownVariables;

    private static String SUFFIX_PREFIX = "f";


    public VariableGenerator(Set<Variable> knownVariables) {
        count = 0;
        dataFactory = OBDADataFactoryImpl.getInstance();
        this.knownVariables = new HashSet<>(knownVariables);
    }

    /**
     * Rule-level variable generator.
     */
    public VariableGenerator(CQIE initialRule) {
        count = 0;
        dataFactory = OBDADataFactoryImpl.getInstance();
        knownVariables = initialRule.getReferencedVariables();
    }

    /**
     * TODO: explain
     */
    public VariableImpl generateNewVariableFromVar(Variable previousVariable) {
        Variable newVariable;
        do {
            newVariable = dataFactory.getVariable(previousVariable.getName() + SUFFIX_PREFIX + (count++));
        } while(knownVariables.contains(newVariable));

        knownVariables.add(newVariable);
        return (VariableImpl)newVariable;
    }

    /**
     * TODO: explain
     */
    public VariableImpl generateNewVariable() {
        Variable newVariable;
        do {
            newVariable = dataFactory.getVariable(SUFFIX_PREFIX + (count++));
        } while(knownVariables.contains(newVariable));

        knownVariables.add(newVariable);
        return (VariableImpl)newVariable;
    }
}
