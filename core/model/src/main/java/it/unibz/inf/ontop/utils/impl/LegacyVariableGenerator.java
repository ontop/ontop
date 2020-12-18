package it.unibz.inf.ontop.utils.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Old-style variable generator
 *
 * TODO: remove it!
 */
public class LegacyVariableGenerator implements VariableGenerator {

    private int count;
    private final Set<Variable> knownVariables;

    private static final String SUFFIX_PREFIX = "f";
    private final TermFactory termFactory;


    @AssistedInject
    private LegacyVariableGenerator(@Assisted Collection<Variable> knownVariables, TermFactory termFactory) {
        this.termFactory = termFactory;
        count = 0;
        this.knownVariables = new HashSet<>(knownVariables);
    }

    public void registerAdditionalVariables(Collection<Variable> additionalVariables) {
        knownVariables.addAll(additionalVariables);
    }

    public Variable generateNewVariableFromVar(Variable previousVariable) {
        knownVariables.add(previousVariable);
        Variable newVariable;
        do {
            newVariable = termFactory.getVariable(previousVariable.getName() + SUFFIX_PREFIX + (count++));
        } while(knownVariables.contains(newVariable));

        knownVariables.add(newVariable);
        return newVariable;
    }

    public Variable generateNewVariableIfConflicting(Variable previousVariable) {
        Variable newVariable = previousVariable;
        while(knownVariables.contains(newVariable)) {
            newVariable = termFactory.getVariable(previousVariable.getName() + SUFFIX_PREFIX + (count++));
        }

        knownVariables.add(newVariable);
        return newVariable;
    }

    public Variable generateNewVariable() {
        Variable newVariable;
        do {
            newVariable = termFactory.getVariable(SUFFIX_PREFIX + (count++));
        } while(knownVariables.contains(newVariable));

        knownVariables.add(newVariable);
        return newVariable;
    }

    @Override
    public Variable generateNewVariable(String suggestedString) {
        return generateNewVariableIfConflicting(termFactory.getVariable(suggestedString));
    }

    public ImmutableSet<Variable> getKnownVariables() {
        return ImmutableSet.copyOf(knownVariables);
    }

    @Override
    public VariableGenerator createSnapshot() {
        return new LegacyVariableGenerator(getKnownVariables(), termFactory);
    }
}
