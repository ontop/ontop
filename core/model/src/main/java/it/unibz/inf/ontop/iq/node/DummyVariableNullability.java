package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

public interface DummyVariableNullability extends VariableNullability {

    @Override
    @Deprecated
    VariableNullability update(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                               ImmutableSet<Variable> projectedVariables);

    @Override
    @Deprecated
    VariableNullability applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution);
}
