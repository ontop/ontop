package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
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
    ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
