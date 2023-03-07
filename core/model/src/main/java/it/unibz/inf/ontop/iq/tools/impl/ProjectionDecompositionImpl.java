package it.unibz.inf.ontop.iq.tools.impl;

import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.Substitution;

import javax.annotation.Nullable;
import java.util.Optional;

public class ProjectionDecompositionImpl implements ProjectionDecomposer.ProjectionDecomposition {

    @Nullable
    private final Substitution<ImmutableTerm> topSubstitution;
    @Nullable
    private final Substitution<ImmutableTerm> subSubstitution;

    private ProjectionDecompositionImpl(@Nullable Substitution<ImmutableTerm> topSubstitution,
                                        @Nullable Substitution<ImmutableTerm> subSubstitution) {
        this.topSubstitution = topSubstitution;
        this.subSubstitution = subSubstitution;
    }

    @Override
    public Optional<Substitution<ImmutableTerm>> getTopSubstitution() {
        return Optional.ofNullable(topSubstitution);
    }

    @Override
    public Optional<Substitution<ImmutableTerm>> getSubSubstitution() {
        return Optional.ofNullable(subSubstitution);
    }

    protected static ProjectionDecomposer.ProjectionDecomposition createTopSubstitutionDecomposition(
            Substitution<ImmutableTerm> topSubstitution) {
        return new ProjectionDecompositionImpl(topSubstitution, null);
    }

    protected static ProjectionDecomposer.ProjectionDecomposition createSubSubstitutionDecomposition(
            Substitution<ImmutableTerm> subSubstitution) {
        return new ProjectionDecompositionImpl(null, subSubstitution);
    }

    protected static ProjectionDecomposer.ProjectionDecomposition createDecomposition(
            Substitution<ImmutableTerm> topSubstitution, Substitution<ImmutableTerm> subSubstitution) {
        return new ProjectionDecompositionImpl(topSubstitution, subSubstitution);
    }

}
