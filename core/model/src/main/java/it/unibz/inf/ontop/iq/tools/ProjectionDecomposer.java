package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public interface ProjectionDecomposer {

    ProjectionDecomposition decomposeSubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                                  VariableGenerator variableGenerator);

    interface ProjectionDecomposition {
        Optional<ImmutableSubstitution<ImmutableTerm>> getTopSubstitution();
        Optional<ImmutableSubstitution<ImmutableTerm>> getSubSubstitution();
    }
}
