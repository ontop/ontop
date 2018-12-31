package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * Immutable.
 *
 * However, needs to be cloned to have multiple copies (distinct nodes) in an query tree.
 *
 * Only "QueryNode.equals(this)" returns true since multiple clones of a node
 * may appear in the same IntermediateQuery and they must absolutely be distinguished.
 */
public interface QueryNode extends Cloneable {

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     */
    void acceptVisitor(QueryNodeVisitor visitor);

    /**
     * Cloning is needed for having multiple copies
     * in the same intermediate query tree.
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
    QueryNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

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

    /**
     * Set of variables MENTIONED in the node, INDEPENDENTLY of its sub-tree.
     *
     * See ImmutableQuery.getVariables(QueryNode node) for getting all the variables
     * returned by the QueryNode.
     *
     */
    ImmutableSet<Variable> getLocalVariables();

    /**
     * Returns true if it cannot guarantee the projected variable to be non-null
     *
     * Throws an IllegalArgumentException if the variable is not projected by the node
     */
    boolean isVariableNullable(IntermediateQuery query, Variable variable);


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
     * Set of variables which, individually, must be provided by at least one child,
     * INDEPENDENTLY OF THE REQUIREMENTS OF THIS NODE'S ANCESTORS.
     * This may extend locally required variables.
     * For instance, for a join node, this includes variables used in implicit joining conditions
     */
    ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query);

    /**
     * Locally defined variables must not appear in the sub-tree
     */
    ImmutableSet<Variable> getLocallyDefinedVariables();

    /**
     * Is syntactically equivalent
     */
    boolean isEquivalentTo(QueryNode queryNode);
}
