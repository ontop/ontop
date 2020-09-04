package it.unibz.inf.ontop.iq.node;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.Constant;

/**
 * TODO: explain
 */
public interface ValuesNode extends LeafIQTree {


    @Override
    ValuesNode clone();

    ImmutableList<ImmutableList<Constant>> getValues();

    @Override
    ValuesNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
