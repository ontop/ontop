package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.SubstitutionResults;

public class SubstitutionResultsImpl<T extends QueryNode> implements SubstitutionResults<T> {
    private final Optional<T> optionalNewNode;
    private final Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalSubstitution;
    private final boolean isNodeEmpty;
    private final Optional<ArgumentPosition> optionalReplacingChildPosition;

    public SubstitutionResultsImpl(T newNode, ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        this.isNodeEmpty = false;
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.of(substitution);
        this.optionalReplacingChildPosition = Optional.empty();
    }

    /**
     * No substitution to propagate
     */
    public SubstitutionResultsImpl(T newNode) {
        this.isNodeEmpty = false;
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.empty();
        this.optionalReplacingChildPosition = Optional.empty();
    }

    /**
     * When the node is not needed anymore.
     * May happen for instance for a GroupNode.
     */
    public SubstitutionResultsImpl(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                   Optional<ArgumentPosition> optionalReplacingChildPosition) {
        this.isNodeEmpty = false;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.of(substitution);
        this.optionalReplacingChildPosition = optionalReplacingChildPosition;
    }

    /**
     * Not a default constructor to force people to be aware
     * that is means that the node is empty
     *
     */
    public SubstitutionResultsImpl(boolean isNodeEmpty) {
        if (!isNodeEmpty) {
            throw new IllegalArgumentException("isNodeEmpty must be true");
        }
        this.isNodeEmpty = true;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.empty();
        this.optionalReplacingChildPosition = Optional.empty();
    }

    @Override
    public Optional<T> getOptionalNewNode() {
        return optionalNewNode;
    }

    @Override
    public Optional<ArgumentPosition> getOptionalReplacingChildPosition() {
        return optionalReplacingChildPosition;
    }

    @Override
    public Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> getSubstitutionToPropagate() {
        return optionalSubstitution;
    }

    @Override
    public boolean isNodeEmpty() {
        return isNodeEmpty;
    }
}
