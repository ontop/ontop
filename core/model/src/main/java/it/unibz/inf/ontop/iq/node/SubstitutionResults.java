package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

/**
 * Object returned after applying a substitution to a QueryNode
 */
public interface SubstitutionResults<T extends QueryNode> {

    /**
     * Does not say if the substitution should be propagated or not.
     */
    enum LocalAction {
        NO_CHANGE,
        NEW_NODE,
        REPLACE_BY_CHILD,
        INSERT_CONSTRUCTION_NODE,
        DECLARE_AS_TRUE,
        DECLARE_AS_EMPTY
    }

    /**
     * Specifies if some QueryNodes must be changed or not.
     */
    LocalAction getLocalAction();

    /**
     * Only present if the focus node has to be updated.
     */
    Optional<T> getOptionalNewNode();

    /**
     * When the node is not needed anymore, specifies (if possibly) the position of the child that should replace it.
     * If no position is given, it is assumed that the node has only child.
     *
     * Useful for LeftJoinNode.
     */
    Optional<BinaryOrderedOperatorNode.ArgumentPosition> getOptionalReplacingChildPosition();

    /**
     * If absent, stop propagating to the parent/children (depending on the propagation direction).
     */
    Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> getSubstitutionToPropagate();

    /**
     * A construction node that needs to be inserted between the focus
     * node and the descendant node.
     *
     * Useful for Union
     *
     */
    Optional<ConstructionNode> getOptionalNewParentOfChildNode();

    /**
     * Descendant node that need to receive a construction node as a parent.
     *
     * Useful for Union
     */
    Optional<QueryNode> getOptionalDowngradedChildNode();
}
