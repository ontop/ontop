package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

public interface StrictFlattenNode extends FlattenNode {

    @Override
    StrictFlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer);

    @Override
    StrictFlattenNode clone();
}
