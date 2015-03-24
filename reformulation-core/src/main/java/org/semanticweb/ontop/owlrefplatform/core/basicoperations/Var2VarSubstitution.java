package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;

/**
 * Substitution where variables are only mapped to variablesx
 */
public interface Var2VarSubstitution extends Substitution {

    Map<VariableImpl, VariableImpl> getVar2VarMap();

}
