package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;

/**
 * Substitution where variables are only mapped to variablesx
 */
public interface Var2VarSubstitution extends Substitution {

    Map<Variable, Variable> getVar2VarMap();

}
