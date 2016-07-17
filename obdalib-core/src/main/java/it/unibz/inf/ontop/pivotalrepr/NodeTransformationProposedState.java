package it.unibz.inf.ontop.pivotalrepr;

/**
 * Transformation to the local node.
 *
 * This state is orthogonal to the propagation of "null" variables.
 *
 */
public enum NodeTransformationProposedState {
    NO_LOCAL_CHANGE,
    REPLACE_BY_UNIQUE_CHILD,
    REPLACE_BY_NEW_NODE,
    DECLARE_AS_EMPTY
}
