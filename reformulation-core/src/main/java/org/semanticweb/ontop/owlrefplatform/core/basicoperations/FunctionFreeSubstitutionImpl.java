package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.NonFunctionalTerm;
import org.semanticweb.ontop.model.impl.VariableImpl;

public class FunctionFreeSubstitutionImpl extends ImmutableSubstitutionImpl implements FunctionFreeSubstitution {

    public FunctionFreeSubstitutionImpl(ImmutableMap<VariableImpl, ? extends NonFunctionalTerm> substitutionMap) {
        super(substitutionMap);
    }


    @Override
    public ImmutableMap<VariableImpl, NonFunctionalTerm> getFunctionFreeMap() {
        return (ImmutableMap<VariableImpl, NonFunctionalTerm>)(ImmutableMap<VariableImpl, ?>) getImmutableMap();
    }

    @Override
    public NonFunctionalTerm get(VariableImpl var) {
        return (NonFunctionalTerm) super.get(var);
    }
}
