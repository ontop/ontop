package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * TODO: explain
 * TODO: find a better name
 */
public interface IntensionalDataNode extends DataNode<AtomPredicate> {

    @Override
    IntensionalDataNode clone();

    @Override
    IntensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    @Override
    IntensionalDataNode newAtom(DataAtom<AtomPredicate> newAtom);
}
