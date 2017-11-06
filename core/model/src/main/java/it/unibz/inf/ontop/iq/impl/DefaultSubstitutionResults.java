package it.unibz.inf.ontop.iq.impl;

import java.util.Optional;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.SubstitutionResults;

import static it.unibz.inf.ontop.iq.node.SubstitutionResults.LocalAction.*;

public class DefaultSubstitutionResults<T extends QueryNode> implements SubstitutionResults<T> {
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
    private DefaultSubstitutionResults(LocalAction localAction,
                                       ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        switch (localAction) {
            case NO_CHANGE:
            case REPLACE_BY_CHILD:
                break;
            case NEW_NODE:
            case INSERT_CONSTRUCTION_NODE:
            case DECLARE_AS_EMPTY:
            case DECLARE_AS_TRUE:
                throw new IllegalArgumentException("Wrong construction for " + localAction);
        }
        this.localAction = localAction;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.of(substitution).filter(s -> !s.isEmpty());
        this.optionalReplacingChildPosition = Optional.empty();
        this.optionalNewParentOfChildNode = Optional.empty();
        this.optionalDescendantNode = Optional.empty();
    }

    /**
     * No change, replace by unique child or true/emptiness declaration.
     *
     * No substitution.
     *
     */
    private DefaultSubstitutionResults(LocalAction localAction) {
        switch (localAction) {
            case NO_CHANGE:
            case REPLACE_BY_CHILD:
            case DECLARE_AS_TRUE:
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
    private DefaultSubstitutionResults(T newNode,
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
    private DefaultSubstitutionResults(T newNode) {
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
    private DefaultSubstitutionResults(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                       ArgumentPosition replacingChildPosition) {
        this.localAction = REPLACE_BY_CHILD;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.of(substitution);
        this.optionalReplacingChildPosition = Optional.of(replacingChildPosition);
        this.optionalNewParentOfChildNode = Optional.empty();
        this.optionalDescendantNode = Optional.empty();
    }

    /**
     * Proposes to add a Construction Node between the child node and the focus node.
     */
    private DefaultSubstitutionResults(ConstructionNode newParentOfChildNode, QueryNode descendantNode) {
        this.localAction = INSERT_CONSTRUCTION_NODE;
        this.optionalNewNode = Optional.empty();
        this.optionalSubstitution = Optional.empty();
        this.optionalReplacingChildPosition = Optional.empty();
        this.optionalNewParentOfChildNode = Optional.of(newParentOfChildNode);
        this.optionalDescendantNode = Optional.of(descendantNode);
    }

    /**
     * Replace by unique child
     */
    public static <T extends QueryNode> SubstitutionResults<T> replaceByUniqueChild(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        return new DefaultSubstitutionResults<>(REPLACE_BY_CHILD, substitution);
    }

    /**
     * Replace by unique child
     */
    public static <T extends QueryNode> SubstitutionResults<T> replaceByChild(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, ArgumentPosition replacingChildPosition) {
        return new DefaultSubstitutionResults<>(substitution, replacingChildPosition);
    }

    /**
     * Declare as empty
     */
    public static <T extends QueryNode> SubstitutionResults<T> declareAsEmpty() {
        return new DefaultSubstitutionResults<>(DECLARE_AS_EMPTY);
    }

    /**
     * Declare as true
     */
    public static <T extends QueryNode> SubstitutionResults<T> declareAsTrue() {
        return new DefaultSubstitutionResults<>(DECLARE_AS_TRUE);
    }

    /**
     * No change and stop
     */
    public static <T extends QueryNode> SubstitutionResults<T> noChange() {
        return new DefaultSubstitutionResults<>(NO_CHANGE);
    }

    /**
     * No local change but continue the propagation
     */
    public static <T extends QueryNode> SubstitutionResults<T> noChange(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        return new DefaultSubstitutionResults<>(NO_CHANGE, substitution);
    }

    /**
     * New node
     */
    public static <T extends QueryNode> SubstitutionResults<T> newNode(T newNode) {
        return new DefaultSubstitutionResults<>(newNode);
    }

    /**
     * New node and continue the substitution
     */
    public static <T extends QueryNode> SubstitutionResults<T> newNode(
            T newNode, ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        return new DefaultSubstitutionResults<>(newNode, substitution);
    }

    /**
     * Proposes to add a Construction Node between the child node and the focus node.
     */
    public static <T extends QueryNode> SubstitutionResults<T> insertConstructionNode(
            ConstructionNode newParentOfChildNode, QueryNode childNode) {
        return new DefaultSubstitutionResults<>(newParentOfChildNode, childNode);
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
