package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * See {@link IntermediateQueryFactory#createTrueNode()} for creating a new instance.
 */
public interface TrueNode extends LeafIQTree {

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.transformTrue(this);
    }

}
