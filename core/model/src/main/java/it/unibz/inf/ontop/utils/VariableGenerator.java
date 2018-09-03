package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Collection;

/**
 * Generates new variables that are guaranteed to not conflict with
 * already variables in a given scope.
 *
 * The typical scope for variables is the body of a rule.
 */
public interface VariableGenerator extends Cloneable {

    void registerAdditionalVariables(Collection<Variable> additionalVariables);

    Variable generateNewVariableFromVar(Variable previousVariable);

    Variable generateNewVariableIfConflicting(Variable previousVariable);

    Variable generateNewVariable();

    ImmutableSet<Variable> getKnownVariables();

    VariableGenerator clone();
}
