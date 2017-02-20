package it.unibz.inf.ontop.pivotalrepr;

/**
 * The ordering of the operands is meaningful procedurally,
 * and therefore should be preserved.
 *
 * It may not be meaningful semantically though.
 * such that there may be instances of BinaryOrderedOperatorNode which are also instances of
 * CommutativeJoinOrFilterNode.
 */
public interface BinaryOrderedOperatorNode extends QueryNode {

    enum ArgumentPosition {
        LEFT,
        RIGHT
    }
}
