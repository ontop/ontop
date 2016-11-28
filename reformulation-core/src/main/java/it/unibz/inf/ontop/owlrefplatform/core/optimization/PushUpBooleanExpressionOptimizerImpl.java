package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushUpBooleanExpressionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PushUpBooleanExpressionProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PushUpBooleanExpressionResults;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * Tries to push (explicit) boolean expressions as high as possible in the algebraic tree.
 * <p>
 * Note that is may be desirable to make implicit boolean expressions (variable equalities) explicit beforehand,
 * using the dedicated optimizer.
 * <p>
 * Only InnerJoin and FilterNodes may provide expressions to be propagated up.
 * <p>
 * The default rules for propagating up are the following.
 * An expression e is always propagated up, until we reach:
 * - a left join node j from its right subtree: j becomes the new recipient,
 * and e is not propagated further up.
 * - a union node u (resp. the root construction node r): e is not propagated further,
 * and if the child n of u (resp. of r) is a Filter or InnerJoinNode,
 * then it becomes the recipient of e.
 * Otherwise a fresh FilterNode is inserted between u (resp. r) and n to support e.
 * <p>
 * As an exception to this default behavior, an expression e may be propagated up though a UnionNode u
 * iff:
 * - all children of u are InnerJoin or FilterNodes, and
 * - e is an (explicit) filtering (sub)expression for each of them.
 * <p>
 * Note that some projections may need to be extended.
 * More exactly, each node projecting variables on the path from the provider to the recipient node will see its set of projected
 * variables extended with all variables appearing in e.
 * <p>
 */

public class PushUpBooleanExpressionOptimizerImpl implements PushUpBooleanExpressionOptimizer {

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        return pushAboveUnions(pushUpFromSubtree(query.getRootConstructionNode(), query));
    }

    private IntermediateQuery pushUpFromSubtree(QueryNode subtreeRoot, IntermediateQuery query) throws EmptyQueryException {
        Optional<QueryNode> optionalNextNode;
        Optional<PushUpBooleanExpressionProposal> optionalProposal = Optional.empty();

        if (subtreeRoot instanceof CommutativeJoinOrFilterNode) {
            optionalProposal = makeNodeCentricProposal((CommutativeJoinOrFilterNode) subtreeRoot, query);
        }
        if (optionalProposal.isPresent()) {
            PushUpBooleanExpressionResults optimizationResults = (PushUpBooleanExpressionResults) query.applyProposal(optionalProposal.get());
            query = optimizationResults.getResultingQuery();
            optionalNextNode = Optional.of(optimizationResults.getExpressionProviderReplacingNodes().iterator().next());
        } else {
            optionalNextNode = QueryNodeNavigationTools.getDepthFirstNextNode(query, subtreeRoot);
        }
        return (optionalNextNode.isPresent()) ?
                pushUpFromSubtree(optionalNextNode.get(), query) :
                query;
    }

    /**
     * Can be optimized by reducing after each iteration the set of union nodes to be reviewed
     */
    private IntermediateQuery pushAboveUnions(IntermediateQuery query) throws EmptyQueryException {
        boolean fixPointReached;
        do {
            fixPointReached = true;
            for (QueryNode node : query.getNodesInTopDownOrder()) {
                if (node instanceof UnionNode) {
                    Optional<PushUpBooleanExpressionProposal> proposal = makeProposalForUnionNode((UnionNode) node, query);
                    if (proposal.isPresent()) {
                        query = query.applyProposal(proposal.get()).getResultingQuery();
                        fixPointReached = false;
                    }
                }
            }
        }
        while (!fixPointReached);
        return query;
    }


    private Optional<PushUpBooleanExpressionProposal> makeNodeCentricProposal(CommutativeJoinOrFilterNode providerNode,
                                                                              IntermediateQuery query) {
        if (providerNode.getOptionalFilterCondition().isPresent()) {
            return makeNodeCentricProposal(providerNode, providerNode.getOptionalFilterCondition().get(),
                    Optional.empty(), query, false);
        }
        return Optional.empty();
    }


    /**
     * Explores the tree upwards from the node providing the expression, looking for a recipient node,
     * and optionally keeping track of projections to extend on the path from provider to recipient
     * <p>
     * May optionally force propagation through the first encountered UnionNode ancestor.
     */
    private Optional<PushUpBooleanExpressionProposal> makeNodeCentricProposal(CommutativeJoinOrFilterNode providerNode,
                                                                              ImmutableExpression propagatedExpression,
                                                                              Optional<ImmutableExpression> nonPropagatedExpression,
                                                                              IntermediateQuery query,
                                                                              boolean propagateThroughNextUnionNodeAncestor) {

        Optional<JoinOrFilterNode> recipient;
        ImmutableList.Builder<ExplicitVariableProjectionNode> inbetweenProjectorsBuilder = ImmutableList.builder();

        boolean stopPropagation = false;
        QueryNode currentChildNode;
        QueryNode currentParentNode = providerNode;

        do {
            currentChildNode = currentParentNode;
            currentParentNode = query.getParent(currentParentNode)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("This node must have a parent node"));

            if (currentParentNode instanceof ConstructionNode) {
                if (currentParentNode != query.getRootConstructionNode()) {
                    // Keep track of Construction nodes on the path between provider and recipient
                    inbetweenProjectorsBuilder.add((ConstructionNode) currentParentNode);
                } else {
                    stopPropagation = true;
                }

            } else if (currentParentNode instanceof UnionNode) {
                // Optionally propagate the expression through the first encountered UnionNode
                if (propagateThroughNextUnionNodeAncestor) {
                    propagateThroughNextUnionNodeAncestor = false;
                    // Keep track of it as an inbetween projector
                    inbetweenProjectorsBuilder.add((ExplicitVariableProjectionNode) currentParentNode);
                } else {
                    stopPropagation = true;
                }

            } else if (currentParentNode instanceof LeftJoinNode && (query.getOptionalPosition(currentChildNode)
                    .orElseThrow(() -> new IllegalStateException("The child of a LeftJoin node must have a position"))
                    == RIGHT)) {
                /**
                 * Stop propagation when reaching a LeftJoinNode from its right branch,
                 * and select the leftJoinNode as recipient
                 */
                return Optional.of(
                        new PushUpBooleanExpressionProposalImpl(propagatedExpression,
                                ImmutableMap.of(providerNode, nonPropagatedExpression), currentChildNode,
                                Optional.of((JoinOrFilterNode) currentParentNode), inbetweenProjectorsBuilder.build()));
            }
        }
        while (!stopPropagation);

        // If no effective propagation
        if (currentChildNode == providerNode) {
            return Optional.empty();
        }

        recipient = currentChildNode instanceof CommutativeJoinOrFilterNode ?
                Optional.of((CommutativeJoinOrFilterNode) currentChildNode) :
                Optional.empty();

        return Optional.of(new PushUpBooleanExpressionProposalImpl(propagatedExpression,
                ImmutableMap.of(providerNode, nonPropagatedExpression),
                currentChildNode, recipient, inbetweenProjectorsBuilder.build()));
    }

    private Optional<PushUpBooleanExpressionProposal> makeProposalForUnionNode(UnionNode unionNode,
                                                                               IntermediateQuery query) {
        // Get the boolean conjuncts to propagate
        ImmutableSet<ImmutableExpression> propagatedExpressions = getExpressionsToPropagateAboveUnion(unionNode, query);

        // The conjunction of all conjuncts to propagate (Optional.empty() if there is none of them)
        Optional<ImmutableExpression> propagatedExpressionsConjunction = ImmutabilityTools
                .foldBooleanExpressions(propagatedExpressions.stream());

        // If there is something to propagate
        if (propagatedExpressionsConjunction.isPresent()) {

            // A propagation proposal for the first child of the UnionNode
            Optional<PushUpBooleanExpressionProposal> firstChildProposal = Optional.empty();
            // Keeps track of the possibly non propagated subexpression for each child of the UnionNode
            ImmutableMap.Builder<CommutativeJoinOrFilterNode,
                    Optional<ImmutableExpression>> childToRetainedExpressionBuilder = ImmutableMap.builder();

            for (QueryNode child : query.getChildren(unionNode)) {
                ImmutableExpression fullBooleanExpression = ((CommutativeJoinOrFilterNode) child).getOptionalFilterCondition()
                        .orElseThrow(() -> new IllegalStateException("All children must have a filtering condition"));

                // The conjuncts which will not be propagated up from this child
                Optional<ImmutableExpression> nonPropagatedExpressionsConjunction = ImmutabilityTools.foldBooleanExpressions(
                        fullBooleanExpression.flattenAND().stream()
                                .filter(e -> !propagatedExpressions.contains(e)));

                childToRetainedExpressionBuilder.put((CommutativeJoinOrFilterNode) child,
                        nonPropagatedExpressionsConjunction);

                // Make a propagation proposal for the first child only
                if (!firstChildProposal.isPresent()) {
                    firstChildProposal = Optional.of(makeNodeCentricProposal((CommutativeJoinOrFilterNode) child,
                            propagatedExpressionsConjunction.get(), nonPropagatedExpressionsConjunction, query, true)
                            .orElseThrow(() -> new IllegalStateException("This proposal cannot be empty")));
                }
            }
            // Extend the proposal made for the first child with all the non propagated expressions
            return Optional.of(mergeProposals(firstChildProposal.get(), childToRetainedExpressionBuilder.build()));
        }
        return Optional.empty();
    }

    private PushUpBooleanExpressionProposal mergeProposals(PushUpBooleanExpressionProposal partialProposal,
                                                           ImmutableMap<CommutativeJoinOrFilterNode,
                                                                   Optional<ImmutableExpression>> providerToRetainedExpression) {
        return new PushUpBooleanExpressionProposalImpl(partialProposal.getPropagatedExpression(),
                providerToRetainedExpression,
                partialProposal.getUpMostPropagatingNode(),
                partialProposal.getRecipientNode(),
                partialProposal.getInbetweenProjectors());
    }


    /**
     * Takes a UnionNode u as input.
     * If all children of u are InnerJoin of FilterNodes,
     * returns the boolean conjuncts shared by all of them as filter conditions.
     * <p>
     * Otherwise returns the empty set.
     */
    private ImmutableSet<ImmutableExpression> getExpressionsToPropagateAboveUnion(UnionNode unionNode,
                                                                                  IntermediateQuery query) {
        // Temporary optional object
        Optional<ImmutableSet<ImmutableExpression>> optionalSharedExpressions = Optional.empty();
        for (QueryNode child : query.getChildren(unionNode)) {
            if (!(child instanceof CommutativeJoinOrFilterNode &&
                    ((CommutativeJoinOrFilterNode) child).getOptionalFilterCondition().isPresent())) {
                return ImmutableSet.of();
            }
            ImmutableSet<ImmutableExpression> childBooleanExpressions =
                    ((CommutativeJoinOrFilterNode) child).getOptionalFilterCondition().get().flattenAND();

            if (optionalSharedExpressions.isPresent()) {
                optionalSharedExpressions = Optional.of(optionalSharedExpressions.get().stream()
                        .filter(e -> childBooleanExpressions.contains(e))
                        .collect(ImmutableCollectors.toSet()));
            } else {
                optionalSharedExpressions = Optional.of(childBooleanExpressions);
            }
        }
        /**
         *  At this stage, the temporary optional cannot be empty anymore (although its value may be the empty set),
         *  unless the UnionNode has no child
         */
        return optionalSharedExpressions.
                orElseThrow(() -> new InvalidIntermediateQueryException("A UnionNode must have children"));
    }
}
