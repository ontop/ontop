package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.ProjectionShrinkingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.ProjectionShrinkingProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectionShrinkingOptimizer implements IntermediateQueryOptimizer {


    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {

        /*
          Contains all (non discarded) variables projected out by some node previously traversed,
          plus all variables appearing in an (explicit or implicit) condition of some join node already traversed

          Immutable only for safety (updated in practice).
          Question: shall we keep it as immutable ?
         */
        return optimizeSubtree(
                query.getRootNode(),
                query,
                query.getProjectionAtom().getVariables());
    }

    private IntermediateQuery optimizeSubtree(QueryNode focusNode, IntermediateQuery query, ImmutableSet<Variable> retainedVariables) {

        Optional<ProjectionShrinkingProposal> optionalProposal = Optional.empty();

        if (focusNode instanceof UnionNode || focusNode instanceof ConstructionNode) {
            optionalProposal = makeProposal((ExplicitVariableProjectionNode) focusNode, retainedVariables);
        }

        if (focusNode instanceof JoinOrFilterNode) {
            retainedVariables = updateRetainedVariables((JoinOrFilterNode) focusNode, query, retainedVariables);
        } else if (focusNode instanceof ConstructionNode) {
            retainedVariables = updateRetainedVariables((ConstructionNode) focusNode);
        }


        if (optionalProposal.isPresent()) {
            NodeCentricOptimizationResults<ExplicitVariableProjectionNode> optimizationResults;
            try {
                optimizationResults = query.applyProposal(optionalProposal.get());
            } catch (EmptyQueryException e) {
                throw new IllegalStateException("The projection shrinker should not empty the query");
            }

            focusNode = optimizationResults.getNewNodeOrReplacingChild().orElseThrow(
                    () -> new IllegalStateException("A replacing node should be generated"));
        }
        for (QueryNode childNode : query.getChildren(focusNode)) {
            query = optimizeSubtree(childNode, query, retainedVariables);
        }
        return query;
    }

    private Optional<ProjectionShrinkingProposal> makeProposal(ExplicitVariableProjectionNode node, ImmutableSet<Variable> allRetainedVariables) {


        if (node instanceof UnionNode || node instanceof ConstructionNode) {
            Map<Boolean, List<Variable>> splitVariables = node.getVariables().stream()
                    .collect(Collectors.partitioningBy(allRetainedVariables::contains));

            if (splitVariables.get(false).iterator().hasNext()) {
                return Optional.of(
                        new ProjectionShrinkingProposalImpl(
                                node,
                                splitVariables.get(true).stream().collect(ImmutableCollectors.toSet())
                        ));
            }
            return Optional.empty();
        }
        throw new IllegalStateException("A projection shrinking proposal can only be made for a Union or Construction node");

    }


    private ImmutableSet<Variable> updateRetainedVariables(JoinOrFilterNode joinOrFilterNode, IntermediateQuery query,
                                                           ImmutableSet<Variable> allRetainedVariables) {

        /*
          Add all variables encountered in filtering or explicit joining conditions
         */
        Set<Variable> joinOrFilterVariables = new HashSet<>();
        Optional<ImmutableExpression> explicitJoiningCondition = joinOrFilterNode.getOptionalFilterCondition();
        if (explicitJoiningCondition.isPresent()) {
            joinOrFilterVariables.addAll(explicitJoiningCondition.get().getVariables());
        }

        /*
          Add all variables encountered in implicit joining conditions,
          i.e. projected out by at least two children subtrees of a JoinLikeNode
         */
        Set<Variable> repeatedVariables = new HashSet<>();
        if (joinOrFilterNode instanceof JoinLikeNode) {
            Set<Variable> encounteredVariables = new HashSet<>();
            for (QueryNode child : query.getChildren(joinOrFilterNode)) {
                for (Variable v : query.getVariables(child)) {
                    if (encounteredVariables.contains(v)) {
                        repeatedVariables.add(v);
                    }
                    encounteredVariables.add(v);
                }
            }
        }
        joinOrFilterVariables.addAll(repeatedVariables);
        return ImmutableSet.copyOf(Sets.union(allRetainedVariables, joinOrFilterVariables));
    }

    private ImmutableSet<Variable> updateRetainedVariables(ConstructionNode constructionNode) {


        /*
          Retain only:
          - variables required by the substitution
          - variables projected independently of the substitution
         */
        //P: all projected variables
        ImmutableSet<Variable> projectedVariables = constructionNode.getVariables();
        //O: variables corresponding to the substitution's output
        ImmutableSet<Variable> substitutionOutput = constructionNode.getSubstitution().getDomain();
        //P' = P - O
        ImmutableSet<Variable> simpleProjectionVariables = projectedVariables.stream()
                .filter(v -> !substitutionOutput.contains(v))
                .collect(ImmutableCollectors.toSet());
        //R: variables required by the substitution
        ImmutableSet<Variable> variablesRequiredBySubstitution = constructionNode.getSubstitution().getImmutableMap().values().stream()
                .flatMap(ImmutableTerm::getVariableStream).collect(ImmutableCollectors.toSet());

        //return P' + R
        return ImmutableSet.<Variable>builder()
                .addAll(simpleProjectionVariables)
                .addAll(variablesRequiredBySubstitution)
                .build();
    }
}
