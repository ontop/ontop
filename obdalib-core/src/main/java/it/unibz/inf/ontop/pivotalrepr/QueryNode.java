package it.unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

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
            throws QueryNodeTransformationException, NotNeededNodeException;

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
     * Set of variables mentioned in the node.
     */
    ImmutableSet<Variable> getVariables();

    /**
     * Applies a substitution coming from below
     */
    SubstitutionResults<? extends QueryNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query)
            throws QueryNodeSubstitutionException;

    /**
     * Applies a substitution coming from above
     */
    SubstitutionResults<? extends QueryNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query)
            throws QueryNodeSubstitutionException;

    /**
     * TODO: explain
     */
    boolean isSyntacticallyEquivalentTo(QueryNode node);
}
