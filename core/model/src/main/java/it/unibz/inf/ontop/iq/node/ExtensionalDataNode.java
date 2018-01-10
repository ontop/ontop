package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * TODO: explain
 */
public interface ExtensionalDataNode extends DataNode<RelationPredicate> {

    @Override
    ExtensionalDataNode clone();

    @Override
    ExtensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    ExtensionalDataNode newAtom(DataAtom<RelationPredicate> newAtom);
}
