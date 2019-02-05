package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
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

    /**
     * This set is sorted, useful for instance for using JDBC result sets
     */
    @Override
    ImmutableSortedSet<Variable> getVariables();

    ImmutableMap<Variable, String> getVariableNames();

    String getNativeQueryString();
}
