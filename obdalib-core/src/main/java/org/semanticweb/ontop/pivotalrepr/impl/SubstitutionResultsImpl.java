package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.SubstitutionResults;

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
        this.optionalSubstitution = Optional.absent();
    }

    /**
     * When the node is not needed anymore.
     * May happen for instance for a GroupNode.
     */
    public SubstitutionResultsImpl(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {
        this.optionalNewNode = Optional.absent();
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
