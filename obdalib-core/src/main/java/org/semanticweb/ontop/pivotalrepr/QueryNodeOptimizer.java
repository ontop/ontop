package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.proposal.LocalOptimizationProposal;

/**
 * TODO: complete it.
 *
 * Note that not all the optimizers are indeed to be used in an "normal" optimization loop.
 * For instance, some can be goal-oriented.
 *
 * TODO: create a sub-interface for the optimizers declared in the QuestPreferences
 * and executed in a given order.
 *
 */
public interface QueryNodeOptimizer {

    Optional<LocalOptimizationProposal> makeProposal(InnerJoinNode node);

    Optional<LocalOptimizationProposal> makeProposal(LeftJoinNode leftJoinNode);

    Optional<LocalOptimizationProposal> makeProposal(FilterNode filterNode);

    Optional<LocalOptimizationProposal> makeProposal(ConstructionNode constructionNode);

    Optional<LocalOptimizationProposal> makeProposal(UnionNode unionNode);

    Optional<LocalOptimizationProposal> makeProposal(OrdinaryDataNode ordinaryDataNode);

    Optional<LocalOptimizationProposal> makeProposal(TableNode tableNode);

    Optional<LocalOptimizationProposal> makeProposal(GroupNode groupNode);
}
