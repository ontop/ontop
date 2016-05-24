package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.SubstitutionResults;

public class SubstitutionResultsImpl<T extends QueryNode> implements SubstitutionResults<T> {
    private final Optional<T> optionalNewNode;
    private final Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalSubstitution;
    private final boolean isEmpty;

    public SubstitutionResultsImpl(T newNode, ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        this.isEmpty = false;
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.of(substitution);
    }

    /**
     * No substitution to propagate
     */
    public SubstitutionResultsImpl(T newNode) {
        this.isEmpty = false;
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.empty();
    }

    /**
     * When the node is not needed anymore.
     * May happen for instance for a GroupNode.
     */
    public SubstitutionResultsImpl(ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        this.isEmpty = false;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.of(substitution);
    }

    /**
     * Not a default constructor to force people to be aware
     * that is means that the node is empty
     *
     */
    public SubstitutionResultsImpl(boolean isEmpty) {
        if (!isEmpty) {
            throw new IllegalArgumentException("isEmpty must be true");
        }
        this.isEmpty = true;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.empty();
    }

    @Override
    public Optional<T> getOptionalNewNode() {
        return optionalNewNode;
    }

    @Override
    public Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> getSubstitutionToPropagate() {
        return optionalSubstitution;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }
}
