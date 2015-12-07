package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.VariableOrGroundTerm;

/**
 * Immutable.
 *
 * However, needs to be cloned to have multiple copies (distinct nodes) in an query tree.
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

    /**
     * Set of variables mentioned in the node.
     */
    ImmutableSet<Variable> getVariables();

    /**
     * Applies a substitution coming from below
     */
    SubstitutionResults<? extends QueryNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query)
            throws QueryNodeSubstitutionException;

    /**
     * Applies a substitution coming from above
     */
    SubstitutionResults<? extends QueryNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution)
            throws QueryNodeSubstitutionException;
}
