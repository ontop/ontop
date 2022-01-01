package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * Immutable.
 *
 * However, needs to be cloned to have multiple copies (distinct nodes) in an query tree.
 *
 * Only "QueryNode.equals(this)" returns true since multiple clones of a node
 * may appear in the same IntermediateQuery and they must absolutely be distinguished.
 *
 * See IntermediateQueryFactory for creating new instances.
 */
public interface QueryNode {

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     */
    void acceptVisitor(QueryNodeVisitor visitor);

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     * If the transformation cannot be done,
     * throw a QueryNodeTransformationException
     *
     */
    QueryNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    /**
     * Set of variables MENTIONED in the node, INDEPENDENTLY of its sub-tree.
     *
     * See ImmutableQuery.getVariables(QueryNode node) for getting all the variables
     * returned by the QueryNode.
     *
     */
    ImmutableSet<Variable> getLocalVariables();


    /**
     * TODO: explain
     */
    boolean isSyntacticallyEquivalentTo(QueryNode node);

    /**
     * Set of variables that this node, INDEPENDENTLY OF THE REQUIREMENTS OF ITS ANCESTORS,
     * requires to be defined in the sub-tree.
     *
     * Said differently, additional variable requirements may come from its ancestors.
     *
     */
    ImmutableSet<Variable> getLocallyRequiredVariables();

    /**
     * Locally defined variables must not appear in the sub-tree
     */
    ImmutableSet<Variable> getLocallyDefinedVariables();

    /**
     * Is syntactically equivalent
     */
    boolean isEquivalentTo(QueryNode queryNode);
}
