package org.semanticweb.ontop.pivotalrepr;

/**
 * Non-standard query node: extensions of SPARQL.
 *
 * This interface is expected to be derived by sub-interfaces.
 *
 */
public interface NonStandardNode extends QueryNode {

    /**
     * To be overwritten by sub-interfaces (for having concrete types)
     */
    @Override
    NonStandardNode clone();

    /**
     * To be overwritten by sub-classes
     */
    @Override
    NonStandardNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException;
}
