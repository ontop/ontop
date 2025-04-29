package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.Substitution;

/**
 * Head node an IntermediateQuery
 *
 * TODO: further explain
 *
 * See {@link IntermediateQueryFactory#createConstructionNode} for creating a new instance.
 *
 */
public interface ConstructionNode extends ExtendedProjectionNode {

    @Override
    Substitution<ImmutableTerm> getSubstitution();

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformConstruction(tree,this, child);
    }

    @Override
    default <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformConstruction(tree,this, child, context);
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitConstruction(this, child);
    }

    @Override
    default ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }
}
