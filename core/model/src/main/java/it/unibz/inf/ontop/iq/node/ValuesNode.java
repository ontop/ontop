package it.unibz.inf.ontop.iq.node;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.stream.Stream;

/**
 * TODO: explain
 */
public interface ValuesNode extends LeafIQTree {


    @Override
    ValuesNode clone();

    ImmutableList<Variable> getOrderedVariables();

    ImmutableList<ImmutableList<Constant>> getValues();

    Stream<Constant> getValueStream(Variable variable);

    @Override
    ValuesNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

}
