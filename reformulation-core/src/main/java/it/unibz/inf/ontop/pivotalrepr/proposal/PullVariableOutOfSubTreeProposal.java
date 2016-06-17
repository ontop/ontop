package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

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
