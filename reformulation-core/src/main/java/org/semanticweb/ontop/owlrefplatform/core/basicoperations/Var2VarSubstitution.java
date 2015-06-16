package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Substitution where variables are only mapped to variables
 */
public interface Var2VarSubstitution extends ImmutableSubstitution<VariableImpl> {

    @Override
    VariableImpl applyToVariable(VariableImpl variable);
}
