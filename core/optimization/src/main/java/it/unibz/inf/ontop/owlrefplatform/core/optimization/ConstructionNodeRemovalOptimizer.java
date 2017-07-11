package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;

public class ConstructionNodeRemovalOptimizer implements IntermediateQueryOptimizer{

    /**
     * Gets rid of unncessary ConstructionNodes.
     *
     * The algorithm searches the query q depth-first.
     * If a ConstructionNode c1 is encountered with a ConstructionNode c2 as child,
     * and if c1 or c2 has a trivial substitution,
     * or a substitution of variables names only,
     * it proceeds as follows:
     * .Let proj(c1) be the variables projected out by c1
     * .Compute the composition s of both substitutions
     * .Restrict s to proj(c1)
     * .Create the construction node c' with substitution s' and projected variables proj(c1)
     *  .In q, replace the subtree rooted in c1 by the subtree rooted in c',
     *  and such that the child subtree of c' is the child subtree of c2.
     */
    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        throw new RuntimeException("TODO: implement");
    }
}
