package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * Represents a serialized query that can be executed by the DB engine
 *
 * See {@link IntermediateQueryFactory#createNativeNode} for creating a new instance.
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

    /**
     * Needed because certain DBs like Oracle impose constraints on the length of a column name,
     * so the column name may differ from the variable name.
     */
    ImmutableMap<Variable, QuotedID> getColumnNames();

    String getNativeQueryString();
}
