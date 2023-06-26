package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public interface ProjectionDecomposer {

    ProjectionDecomposition decomposeSubstitution(Substitution<? extends ImmutableTerm> substitution,
                                                  VariableGenerator variableGenerator);

    interface ProjectionDecomposition {
        Optional<Substitution<ImmutableTerm>> getTopSubstitution();
        Optional<Substitution<ImmutableTerm>> getSubSubstitution();
    }
}
