package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
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

    @Override
    IntensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    IntensionalDataNode newAtom(DataAtom<AtomPredicate> newAtom);
}
