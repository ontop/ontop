package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProjectionShrinkingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ProjectionShrinkingProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectionShrinkingOptimizer implements IntermediateQueryOptimizer {


    public ProjectionShrinkingOptimizer() {
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        /**
         * Contains all (non discarded) variables projected out by some node previously traversed,
         * plus all variables appearing in an (explicit or implicit) condition of some join node already traversed
         *
         * Immutable only for safety (updated in practice).
         * Question: shall we keep it as immutable ?
         */
        ImmutableSet<Variable> allRetainedVariables = query.getProjectionAtom().getVariables();

        return optimizeSubtree(query.getRootConstructionNode(), query, allRetainedVariables);

    }

    private IntermediateQuery optimizeSubtree(QueryNode focusNode, IntermediateQuery query, ImmutableSet<Variable> allRetainedVariables) throws EmptyQueryException {
        Optional<QueryNode> optionalNextNode;
        Optional<ProjectionShrinkingProposal> optionalProposal = Optional.empty();

        if (focusNode instanceof JoinLikeNode) {
            updateEncounteredVariables((JoinLikeNode) focusNode, query, allRetainedVariables);
        } else if (focusNode instanceof UnionNode || focusNode instanceof ConstructionNode) {
            optionalProposal = makeProposal((ExplicitVariableProjectionNode) focusNode, query, allRetainedVariables);
        }
        if (optionalProposal.isPresent()) {
            NodeCentricOptimizationResults<ExplicitVariableProjectionNode> optimizationResults = query.applyProposal(optionalProposal.get());
            QueryNodeNavigationTools.NextNodeAndQuery nextNodeAndQuery = QueryNodeNavigationTools.getNextNodeAndQuery(optimizationResults);
            query = nextNodeAndQuery.getNextQuery();
            optionalNextNode = nextNodeAndQuery.getOptionalNextNode();
        } else {
            optionalNextNode = QueryNodeNavigationTools.getDepthFirstNextNode(query, focusNode);
        }
        return (optionalNextNode.isPresent()) ?
                optimizeSubtree(optionalNextNode.get(), query, allRetainedVariables) :
                query;
    }

    private Optional<ProjectionShrinkingProposal> makeProposal(ExplicitVariableProjectionNode node, IntermediateQuery query, ImmutableSet<Variable> allRetainedVariables) {

        if (!(node instanceof UnionNode || node instanceof ConstructionNode)) {
            throw new IllegalStateException("a projection shrinking proposal can only be made for a Union or Construction node");
        }

        Map<Boolean, List<Variable>> splitVariables = node.getLocalVariables().stream()
                .collect(Collectors.partitioningBy(v -> allRetainedVariables.contains(v)));
        if (splitVariables.get(false).iterator().hasNext()) {
            return Optional.of(new ProjectionShrinkingProposalImpl(query, node,
                    splitVariables.get(true).stream().collect(ImmutableCollectors.toSet())));
        }
        return Optional.empty();
    }


    private ImmutableSet<Variable> updateEncounteredVariables(JoinLikeNode joinLikeNode, IntermediateQuery query,
                                                              ImmutableSet<Variable> allRetainedVariables) {
        ImmutableList.Builder<Variable> newVariablesBuilder = ImmutableList.builder();
        /**
         * Add all variables encountered in explicit joining conditions
         */
        Optional<ImmutableExpression> explicitJoiningCondition = joinLikeNode.getOptionalFilterCondition();
        if (explicitJoiningCondition.isPresent()) {
            newVariablesBuilder.addAll(explicitJoiningCondition.get().getVariables());
        }

        /**
         * Add all variables encountered in implicit joining conditions (i.e. shared by at least two children subtrees)
         */
        Set<Variable> encounteredVariables = new HashSet<>();
        Set<Variable> repeatedVariables = new HashSet<>();
        for (QueryNode child : query.getChildren(joinLikeNode)) {
            for (Variable v : child.getLocalVariables()) {
                if (encounteredVariables.contains(v)) {
                    repeatedVariables.add(v);
                }
                encounteredVariables.add(v);
            }
        }
        return ImmutableSet.copyOf(Sets.union(allRetainedVariables, repeatedVariables));
    }
}
