package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.LeafIQTree;

/**
 * Represents a serialized query that can be executed by the DB engine
 */
public interface NativeNode extends LeafIQTree {

    String getNativeQueryString();
}
