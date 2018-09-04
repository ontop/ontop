package it.unibz.inf.ontop.utils;

import it.unibz.inf.ontop.model.term.Variable;

import java.util.Collection;

public interface CoreUtilsFactory {

    VariableGenerator createVariableGenerator(Collection<Variable> knownVariables);
}
