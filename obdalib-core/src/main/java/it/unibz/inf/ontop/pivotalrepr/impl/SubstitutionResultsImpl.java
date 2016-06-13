package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.SubstitutionResults;

public class SubstitutionResultsImpl<T extends QueryNode> implements SubstitutionResults<T> {
    private final Optional<T> optionalNewNode;
    private final Optional<? extends ImmutableSubstitution<? extends VariableOrGroundTerm>> optionalSubstitution;

    public SubstitutionResultsImpl(T newNode, ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.of(substitution);
    }

    /**
     * No substitution to propagate
     */
    public SubstitutionResultsImpl(T newNode) {
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.empty();
    }

    /**
     * When the node is not needed anymore.
     * May happen for instance for a GroupNode.
     */
    public SubstitutionResultsImpl(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.of(substitution);
    }

    @Override
    public Optional<T> getOptionalNewNode() {
        return optionalNewNode;
    }

    @Override
    public Optional<? extends ImmutableSubstitution<? extends VariableOrGroundTerm>> getSubstitutionToPropagate() {
        return optionalSubstitution;
    }
}
