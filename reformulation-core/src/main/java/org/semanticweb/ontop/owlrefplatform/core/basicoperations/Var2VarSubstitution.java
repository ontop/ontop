package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;

/**
 * Substitution where variables are only mapped to variables
 */
public interface Var2VarSubstitution extends FunctionFreeSubstitution {

    Map<VariableImpl, VariableImpl> getVar2VarMap();

    @Override
    VariableImpl get(VariableImpl var);

    @Override
    VariableImpl applyToVariable(VariableImpl variable);
}
