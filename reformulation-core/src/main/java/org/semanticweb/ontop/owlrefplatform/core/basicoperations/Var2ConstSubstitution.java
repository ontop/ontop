package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * All the terms of the range of substitution are constants.
 */
public interface Var2ConstSubstitution extends FunctionFreeSubstitution {

    ImmutableMap<VariableImpl, Constant> getConstantMap();

    @Override
    Constant get(VariableImpl var);
}
