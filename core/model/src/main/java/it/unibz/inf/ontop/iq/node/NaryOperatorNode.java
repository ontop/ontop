package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Has at least two children
 */
public interface NaryOperatorNode extends QueryNode {

    IQ liftBinding(ImmutableList<IQ> children, VariableGenerator variableGenerator);
}
