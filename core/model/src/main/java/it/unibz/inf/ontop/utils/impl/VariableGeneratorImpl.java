package it.unibz.inf.ontop.utils.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Beware: not thread-safe!
 *
 * See CoreUtilsFactory for building new instances
 */
public class VariableGeneratorImpl implements VariableGenerator {

    private static final Pattern PREFIX_PATTERN = Pattern.compile("(.+?)\\d*");
    private static final String DEFAULT_PREFIX = "v";
    private final Map<String, Variable> knownVariableMap;
    private final TermFactory termFactory;
    private int count;

    /**
     * TODO: please use as much as possible the assisted inject pattern
     */
    @AssistedInject
    public VariableGeneratorImpl(@Assisted Collection<Variable> knownVariables, TermFactory termFactory) {
        this(knownVariables, termFactory, 0);
    }

    private VariableGeneratorImpl(Collection<Variable> knownVariables, TermFactory termFactory, int count) {
        this.knownVariableMap = knownVariables.stream()
                .collect(Collectors.toMap(
                        Variable::getName,
                        v -> v));
        this.termFactory = termFactory;
        this.count = count;
    }

    @Override
    public void registerAdditionalVariables(Collection<Variable> additionalVariables) {
        additionalVariables
                .forEach(v -> knownVariableMap.put(v.getName(), v));
    }

    @Override
    public Variable generateNewVariableFromVar(Variable previousVariable) {
        knownVariableMap.put(previousVariable.getName(), previousVariable);

        Matcher matcher = PREFIX_PATTERN.matcher(previousVariable.getName());
        String prefix = matcher.matches()
                ? matcher.group(1)
                : DEFAULT_PREFIX;

        return generateNewVariableFromPrefix(prefix);
    }

    @Override
    public Variable generateNewVariableIfConflicting(Variable previousVariable) {
        if (knownVariableMap.containsKey(previousVariable.getName()))
            return generateNewVariableFromVar(previousVariable);

        knownVariableMap.put(previousVariable.getName(), previousVariable);
        return previousVariable;
    }

    @Override
    public Variable generateNewVariable() {
        return generateNewVariableFromPrefix(DEFAULT_PREFIX);
    }

    @Override
    public Variable generateNewVariable(String suggestedString) {
        return generateNewVariableIfConflicting(termFactory.getVariable(suggestedString));
    }

    private Variable generateNewVariableFromPrefix(String prefix) {
        String newVariableName;
        do {
            newVariableName = prefix + (count++);
        } while (knownVariableMap.containsKey(newVariableName));

        Variable newVariable = termFactory.getVariable(newVariableName);
        knownVariableMap.put(newVariableName, newVariable);
        return newVariable;
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return ImmutableSet.copyOf(knownVariableMap.values());
    }

    @Override
    public VariableGenerator createSnapshot() {
        return new VariableGeneratorImpl(knownVariableMap.values(), termFactory, count);
    }
}
