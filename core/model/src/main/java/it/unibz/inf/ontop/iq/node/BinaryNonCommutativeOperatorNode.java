package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * Operator QueryNode which are binary and whose operand ordering is semantically meaningful.
 *
 * For instance: Left Join.
 */
public interface BinaryNonCommutativeOperatorNode extends BinaryOrderedOperatorNode {

}
