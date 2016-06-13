package it.unibz.inf.ontop.pivotalrepr;

/**
 * For operator QueryNode that are binary and that care about
 * the ordering of their children.
 *
 * For instance: Left Join.
 */
public interface NonCommutativeOperatorNode extends QueryNode {

    public static enum ArgumentPosition {
        LEFT,
        childPosition, RIGHT
    }
}
