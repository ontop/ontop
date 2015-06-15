package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.Substitution;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;

/**
 * Substitution where variables are only mapped to variablesx
 */
public interface Var2VarSubstitution extends ImmutableSubstitution {

    Map<VariableImpl, VariableImpl> getVar2VarMap();

    @Override
    VariableImpl get(VariableImpl var);

    @Override
    VariableImpl applyToVariable(VariableImpl variable);

}
