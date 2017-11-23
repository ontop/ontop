package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Has at least two children
 */
public interface NaryOperatorNode extends QueryNode {

    IQTree liftBinding(ImmutableList<IQTree> children, VariableGenerator variableGenerator);
}
