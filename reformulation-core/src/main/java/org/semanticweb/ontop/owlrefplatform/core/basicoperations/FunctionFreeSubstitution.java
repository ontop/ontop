package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.NonFunctionalTerm;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * All the terms of the range of substitution are (immutable) non-functional terms.
 */
public interface FunctionFreeSubstitution extends ImmutableSubstitution {

    ImmutableMap<VariableImpl, NonFunctionalTerm> getFunctionFreeMap();

    @Override
    NonFunctionalTerm get(VariableImpl var);
}
