package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * TODO: explain
 * TODO: find a better name
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface IntensionalDataNode extends LeafIQTree {

    DataAtom<AtomPredicate> getProjectionAtom();

    IntensionalDataNode newAtom(DataAtom<AtomPredicate> newAtom);

    @Override
    default IntensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    default IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformIntensionalData(this);
    }

    @Override
    default <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return transformer.transformIntensionalData(this, context);
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitIntensionalData(this);
    }
}
