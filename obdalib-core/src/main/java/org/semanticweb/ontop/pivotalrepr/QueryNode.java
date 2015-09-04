package org.semanticweb.ontop.pivotalrepr;

/**
 * Mutable BUT ONLY WHEN APPLYING LocalOptimizationProposal forwarded by the IntermediateQuery.
 *
 * --> Mutations under control.
 *
 * Golden rule: after mutation, the node must be semantically equivalent (for instance, not specialized).
 *
 */
public interface QueryNode extends Cloneable {

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     */
    void acceptVisitor(QueryNodeVisitor visitor);

    boolean isRejected();

    /**
     * Since a QueryNode is mutable (under some control however),
     * cloning is needed (at a limited number of places).
     */
    QueryNode clone();


    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     * If the transformation cannot be done,
     * throw a QueryNodeTransformationException
     *
     */
    QueryNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException, NotNeededNodeException;

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     * If the transformation cannot be done,
     * throw a QueryNodeTransformationException
     *
     */
    NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer);
}
