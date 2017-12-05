package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface VariableOrGroundTermSubstitution<T extends VariableOrGroundTerm>
        extends ImmutableSubstitution<T> {

    VariableOrGroundTermSubstitution<VariableOrGroundTerm> composeWith2(
            ImmutableSubstitution<? extends VariableOrGroundTerm> g);
}
