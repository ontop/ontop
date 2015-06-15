package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.impl.VariableImpl;

public class Var2ConstSubstitutionImpl extends FunctionFreeSubstitutionImpl implements Var2ConstSubstitution {

    public Var2ConstSubstitutionImpl(ImmutableMap<VariableImpl, ? extends Constant> substitutionMap) {
        super(substitutionMap);
    }

    @Override
    public Constant get(VariableImpl variable) {
        return (Constant) super.get(variable);
    }

    @Override
    public ImmutableMap<VariableImpl, Constant> getConstantMap() {
        return (ImmutableMap<VariableImpl, Constant>)(ImmutableMap<VariableImpl, ?>) getImmutableMap() ;
    }
}
