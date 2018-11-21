package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * Represents a serialized query that can be executed by the DB engine
 */
public interface NativeNode extends LeafIQTree {

    /**
     * Every variable is guaranteed to have a type
     */
    ImmutableMap<Variable, DBTermType> getTypeMap();

    String getNativeQueryString();
}