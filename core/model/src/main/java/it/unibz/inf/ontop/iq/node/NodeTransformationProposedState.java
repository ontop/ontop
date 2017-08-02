package it.unibz.inf.ontop.iq.node;

/**
 * Transformation to the local node.
 *
 * This state is orthogonal to the propagation of "null" variables.
 *
 */
public enum NodeTransformationProposedState {
    NO_LOCAL_CHANGE,
    REPLACE_BY_UNIQUE_NON_EMPTY_CHILD,
    REPLACE_BY_NEW_NODE,
    DECLARE_AS_EMPTY,
    DECLARE_AS_TRUE
}
