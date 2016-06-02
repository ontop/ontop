package it.unibz.inf.ontop.pivotalrepr;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Object returned after applying a substitution to a QueryNode
 */
public interface SubstitutionResults<T extends QueryNode> {

    /**
     * When is absent, it means that node is not needed anymore.
     *
     * May happen with GroupNode and LeftJoinNode.
     */
    Optional<T> getOptionalNewNode();

    /**
     * When the node is not needed anymore, specifies (if possibly) the position of the child that should replace it.
     * If no position is given, it is assumed that the node has only child.
     *
     * Useful for LeftJoinNode.
     */
    Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalReplacingChildPosition();

    /**
     * If absent, stop propagating to the parent/children (depending on the propagation direction).
     */
    Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> getSubstitutionToPropagate();

    boolean isNodeEmpty();

    /**
     * A construction node that needs to be inserted between the focus
     * node and the descendant node.
     *
     * Useful for Union
     *
     */
    Optional<ConstructionNode> getOptionalNewParentOfDescendantNode();

    /**
     * Descendant node that need to receive a construction node as a parent.
     *
     * Useful for Union
     */
    Optional<QueryNode> getOptionalDescendantNode();
}
