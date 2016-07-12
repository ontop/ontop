package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.SubstitutionResults;

import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.*;

public class SubstitutionResultsImpl<T extends QueryNode> implements SubstitutionResults<T> {
    private final LocalAction localAction;
    private final Optional<T> optionalNewNode;
    private final Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalSubstitution;
    private final Optional<ArgumentPosition> optionalReplacingChildPosition;
    private final Optional<ConstructionNode> optionalNewParentOfChildNode;
    // Not new (already in the graph)
    private final Optional<QueryNode> optionalDescendantNode;

    /**
     * No change or replace by unique child
     */
    public SubstitutionResultsImpl(LocalAction localAction,
                                   Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubstitution) {
        switch (localAction) {
            case NO_CHANGE:
            case REPLACE_BY_CHILD:
                break;
            case NEW_NODE:
            case INSERT_CONSTRUCTION_NODE:
            case DECLARE_AS_EMPTY:
                throw new IllegalArgumentException("Wrong construction for " + localAction);
        }
        this.localAction = localAction;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = optionalSubstitution.filter(s -> !s.isEmpty());
        this.optionalReplacingChildPosition = Optional.empty();
        this.optionalNewParentOfChildNode = Optional.empty();
        this.optionalDescendantNode = Optional.empty();
    }

    /**
     * No change, replace by unique child or emptiness declaration.
     *
     * No substitution.
     *
     */
    public SubstitutionResultsImpl(LocalAction localAction) {
        switch (localAction) {
            case NO_CHANGE:
            case REPLACE_BY_CHILD:
            case DECLARE_AS_EMPTY:
                break;
            case NEW_NODE:
            case INSERT_CONSTRUCTION_NODE:
                throw new IllegalArgumentException("Wrong construction for " + localAction);
        }
        this.localAction = localAction;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.empty();
        this.optionalReplacingChildPosition = Optional.empty();
        this.optionalNewParentOfChildNode = Optional.empty();
        this.optionalDescendantNode = Optional.empty();
    }

    /**
     * NEW_NODE and substitution to propagate
     */
    public SubstitutionResultsImpl(T newNode,
                                   ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        this.localAction = NEW_NODE;
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.of(substitution).filter(s -> !s.isEmpty());
        this.optionalReplacingChildPosition = Optional.empty();
        this.optionalNewParentOfChildNode = Optional.empty();
        this.optionalDescendantNode = Optional.empty();
    }

    /**
     * NEW_NODE and no substitution to propagate
     */
    public SubstitutionResultsImpl(T newNode) {
        this.localAction = NEW_NODE;
        this.optionalNewNode = Optional.of(newNode);
        this.optionalSubstitution = Optional.empty();
        this.optionalReplacingChildPosition = Optional.empty();
        this.optionalNewParentOfChildNode = Optional.empty();
        this.optionalDescendantNode = Optional.empty();
    }

    /**
     * When the node is not needed anymore.
     * May happen for instance for a GroupNode.
     */
    public SubstitutionResultsImpl(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                   Optional<ArgumentPosition> optionalReplacingChildPosition) {
        this.localAction = REPLACE_BY_CHILD;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.of(substitution);
        this.optionalReplacingChildPosition = optionalReplacingChildPosition;
        this.optionalNewParentOfChildNode = Optional.empty();
        this.optionalDescendantNode = Optional.empty();
    }

    /**
     * Proposes to add a Construction Node between the child node and the focus node.
     */
    public SubstitutionResultsImpl(ConstructionNode newParentOfChildNode, QueryNode descendantNode) {
        this.localAction = INSERT_CONSTRUCTION_NODE;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.empty();
        this.optionalReplacingChildPosition = Optional.empty();
        this.optionalNewParentOfChildNode = Optional.of(newParentOfChildNode);
        this.optionalDescendantNode = Optional.of(descendantNode);
    }

    @Override
    public LocalAction getLocalAction() {
        return localAction;
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

    /**
     * Note the "child node" is now the grand-child of the focus node
     */
    public Optional<ConstructionNode> getOptionalNewParentOfChildNode() {
        return optionalNewParentOfChildNode;
    }

    @Override
    public Optional<QueryNode> getOptionalDowngradedChildNode() {
        return optionalDescendantNode;
    }
}
