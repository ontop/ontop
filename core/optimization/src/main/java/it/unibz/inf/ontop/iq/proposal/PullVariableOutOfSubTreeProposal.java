package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;

/**
 * TODO: explain
 *
 * TODO: make it inherit from NodeCentricProposal?
 */
public interface PullVariableOutOfSubTreeProposal<N extends JoinLikeNode>
         extends QueryOptimizationProposal<PullVariableOutOfSubTreeResults<N>> {

    N getFocusNode();

    InjectiveVar2VarSubstitution getRenamingSubstitution();

    /**
     * Must be a descendent of the focus node.
     *
     * The renaming substitution will be applied to its sub-tree (root including)
     *
     */
    QueryNode getSubTreeRootNode();
}
