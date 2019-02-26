package it.unibz.inf.ontop.iq.tools.impl;

import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import javax.annotation.Nullable;
import java.util.Optional;

public class ProjectionDecompositionImpl implements ProjectionDecomposer.ProjectionDecomposition {

    @Nullable
    private final ImmutableSubstitution<ImmutableTerm> topSubstitution;
    @Nullable
    private final ImmutableSubstitution<ImmutableTerm> subSubstitution;

    private ProjectionDecompositionImpl(@Nullable ImmutableSubstitution<ImmutableTerm> topSubstitution,
                                        @Nullable ImmutableSubstitution<ImmutableTerm> subSubstitution) {
        this.topSubstitution = topSubstitution;
        this.subSubstitution = subSubstitution;
    }

    @Override
    public Optional<ImmutableSubstitution<ImmutableTerm>> getTopSubstitution() {
        return Optional.ofNullable(topSubstitution);
    }

    @Override
    public Optional<ImmutableSubstitution<ImmutableTerm>> getSubSubstitution() {
        return Optional.ofNullable(subSubstitution);
    }

    protected static ProjectionDecomposer.ProjectionDecomposition createTopSubstitutionDecomposition(
            ImmutableSubstitution<ImmutableTerm> topSubstitution) {
        return new ProjectionDecompositionImpl(topSubstitution, null);
    }

    protected static ProjectionDecomposer.ProjectionDecomposition createSubSubstitutionDecomposition(
            ImmutableSubstitution<ImmutableTerm> subSubstitution) {
        return new ProjectionDecompositionImpl(null, subSubstitution);
    }

    protected static ProjectionDecomposer.ProjectionDecomposition createDecomposition(
            ImmutableSubstitution<ImmutableTerm> topSubstitution, ImmutableSubstitution<ImmutableTerm> subSubstitution) {
        return new ProjectionDecompositionImpl(topSubstitution, subSubstitution);
    }

}
