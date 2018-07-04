package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Represents a serialized query that can be executed by the DB engine
 */
public interface NativeNode extends LeafIQTree {

    String getNativeQueryString();

    ImmutableList<Variable> getOrderedVariables();
}
