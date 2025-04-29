package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
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
     * This set is sorted, useful, for instance, for using JDBC result sets
     */
    @Override
    ImmutableSortedSet<Variable> getVariables();

    /**
     * Needed because certain DBs like Oracle impose constraints on the length of a column name,
     * so the column name may differ from the variable name.
     */
    ImmutableMap<Variable, QuotedID> getColumnNames();

    String getNativeQueryString();

    @Override
    default IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitNative(this);
    }

    @Override
    default LeafIQTree acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }
}
