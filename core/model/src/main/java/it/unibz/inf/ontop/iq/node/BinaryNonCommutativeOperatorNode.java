package it.unibz.inf.ontop.iq.node;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Operator QueryNode which are binary and whose operands ordering is semantically meaningful.
 *
 * For instance: Left Join.
 */
public interface BinaryNonCommutativeOperatorNode extends BinaryOrderedOperatorNode {

    BinaryNonCommutativeOperatorNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
