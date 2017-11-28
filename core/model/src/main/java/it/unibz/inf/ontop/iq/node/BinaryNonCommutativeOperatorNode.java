package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Operator QueryNode which are binary and whose operands ordering is semantically meaningful.
 *
 * For instance: Left Join.
 */
public interface BinaryNonCommutativeOperatorNode extends BinaryOrderedOperatorNode {

    IQTree liftBinding(IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator);
}
