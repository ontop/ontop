package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.proposal.PushUpBooleanExpressionProposal;
import it.unibz.inf.ontop.iq.proposal.impl.PushUpBooleanExpressionProposalImpl;
import it.unibz.inf.ontop.iq.proposal.PushUpBooleanExpressionResults;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.optimizer.PushUpBooleanExpressionOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;


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
 * - a union node u (resp. the root node r): e is not propagated further,
 * and if the child n of u (resp. of r) is a Filter or InnerJoinNode,
 * then it becomes the recipient of e.
 * Otherwise a fresh FilterNode is inserted between u (resp. r) and n to support e.
 * <p>
 * Note that some projections may need to be extended.
 * More exactly, each node projecting variables on the path from the provider to the recipient node will see its set of projected
 * variables extended with all variables appearing in e.
 * <p>
 * <p>
 * As a first exception to this default behavior, an expression e may be propagated up though a UnionNode u iff the
 * following is satisfied
 * (TODO: generalize)
 * - each operand subtree of u contains a filter or inner join node which is only separated from u by construction
 * nodes, and
 * - e is an (explicit) filtering (sub)expression for each of them.
 * <p>
 * As a second exception,
 * if the propagation of e is blocked by a union node u (resp. the root r),
 * take the longest sequence n_1, .., n_m of construction nodes and query modifiers below u (resp. r).
 * Then the recipient of e cannot be above n_m.
 * <p>
 */
@Singleton
public class PushUpBooleanExpressionOptimizerImpl implements PushUpBooleanExpressionOptimizer {


    private final boolean pushAboveUnions;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private PushUpBooleanExpressionOptimizerImpl(ImmutabilityTools immutabilityTools) {
        this(false, immutabilityTools);
    }

    public PushUpBooleanExpressionOptimizerImpl(boolean pushAboveUnions, ImmutabilityTools immutabilityTools) {
        this.pushAboveUnions = pushAboveUnions;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {
        try {
            query = pushUpFromSubtree(query.getRootNode(), query);
            return pushAboveUnions ?
                    pushAboveUnions(query) :
                    query;
        } catch (EmptyQueryException e) {
            throw new IllegalStateException("This optimizer should not empty the query");
        }
    }

    private IntermediateQuery pushUpFromSubtree(QueryNode subtreeRoot, IntermediateQuery query) throws EmptyQueryException {

        if (subtreeRoot instanceof CommutativeJoinOrFilterNode) {
            Optional<PushUpBooleanExpressionProposal> proposal = makeNodeCentricProposal((CommutativeJoinOrFilterNode) subtreeRoot, query);
            if (proposal.isPresent()) {
                PushUpBooleanExpressionResults optimizationResults = (PushUpBooleanExpressionResults) query.applyProposal(proposal.get());
                query = optimizationResults.getResultingQuery();
                QueryNode nextNode = optimizationResults.getExpressionProviderReplacingNodes().iterator().next();
                return pushUpFromSubtree(nextNode, query);
            }
        }
        Optional<QueryNode> optNextNode = QueryNodeNavigationTools.getDepthFirstNextNode(query, subtreeRoot);
        return optNextNode.isPresent() ?
                pushUpFromSubtree(optNextNode.get(), query) :
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
                        query = ((PushUpBooleanExpressionResults) query.applyProposal(proposal.get())).getResultingQuery();
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
            return makeNodeCentricProposal(
                    providerNode,
                    providerNode.getOptionalFilterCondition().get(),
                    Optional.empty(),
                    query,
                    false
            );
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
        ImmutableSet.Builder<ExplicitVariableProjectionNode> inbetweenProjectorsBuilder = ImmutableSet.builder();

        QueryNode currentChildNode;
        QueryNode currentParentNode = providerNode;

        do {
            currentChildNode = currentParentNode;
            currentParentNode = query.getParent(currentParentNode)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("This node must have a parent node"));

            if (currentParentNode == query.getRootNode()) {
                break;
            }
            if (currentParentNode instanceof ConstructionNode) {
                /* keep track of Construction nodes on the path between provider and recipient */
                inbetweenProjectorsBuilder.add((ConstructionNode) currentParentNode);
                continue;
            }
            if (currentParentNode instanceof UnionNode) {
                /* optionally propagate the expression through the first encountered UnionNode */
                if (propagateThroughNextUnionNodeAncestor) {
                    propagateThroughNextUnionNodeAncestor = false;
                    /* keep track of it as an inbetween projector */
                    inbetweenProjectorsBuilder.add((ExplicitVariableProjectionNode) currentParentNode);
                    continue;
                }
                break;
            }
            if (currentParentNode instanceof LeftJoinNode &&
                    (query.getOptionalPosition(currentChildNode)
                            .orElseThrow(() -> new InvalidIntermediateQueryException("The child of a LeftJoin node must have a position"))
                            == RIGHT)) {
                /*
                  Stop propagation when reaching a LeftJoinNode from its right branch,
                  and select the leftJoinNode as recipient
                 */
                return Optional.of(
                        new PushUpBooleanExpressionProposalImpl(
                                propagatedExpression,
                                ImmutableMap.of(providerNode, nonPropagatedExpression),
                                currentChildNode,
                                Optional.of((JoinOrFilterNode) currentParentNode),
                                inbetweenProjectorsBuilder.build()
                        ));
            }
        }
        while (true);


        // If no effective propagation
        if (currentChildNode == providerNode) {
            return Optional.empty();
        }

        // if we reach this point, the upward propagation up must have been blocked by a union or by the root

        recipient = currentChildNode instanceof CommutativeJoinOrFilterNode ?
                Optional.of((CommutativeJoinOrFilterNode) currentChildNode) :
                Optional.empty();


        PushUpBooleanExpressionProposal proposal = new PushUpBooleanExpressionProposalImpl(
                propagatedExpression,
                ImmutableMap.of(providerNode, nonPropagatedExpression),
                currentChildNode,
                recipient,
                inbetweenProjectorsBuilder.build()
        );

        // Possibly adjust the proposal, to enforce that the second exception (see the class comments) holds
        return adjustProposal(proposal, query);
    }

    private Optional<PushUpBooleanExpressionProposal> makeProposalForUnionNode(UnionNode unionNode, IntermediateQuery query) {


        ImmutableSet<CommutativeJoinOrFilterNode> providers = getProviders(unionNode, query);

        if (providers.isEmpty()) {
            return Optional.empty();
        }
        /* get the boolean conjuncts to propagate */
        ImmutableSet<ImmutableExpression> propagatedExpressions = getExpressionsToPropagateAboveUnion(providers);

        if (propagatedExpressions.isEmpty()) {
            return Optional.empty();
        }

        /* conjunction of all conjuncts to propagate */
        ImmutableExpression conjunction = immutabilityTools.foldBooleanExpressions(propagatedExpressions.stream())
                .orElseThrow(() -> new IllegalStateException("The conjunction should be present"));

        Optional<Optional<PushUpBooleanExpressionProposal>> merge = providers.stream()
                .map(n -> makeNodeCentricProposal(
                        n,
                        conjunction,
                        getRetainedSubExpression(propagatedExpressions, n),
                        query,
                        true
                ))
                .reduce(this::mergeProposals);
        return merge.isPresent() ?
                merge.get() :
                Optional.empty();
    }

    private Optional<PushUpBooleanExpressionProposal> mergeProposals(Optional<PushUpBooleanExpressionProposal> optProposal1,
                                                                     Optional<PushUpBooleanExpressionProposal> optProposal2) {

        if (optProposal1.isPresent() && optProposal2.isPresent()) {
            PushUpBooleanExpressionProposal p1 = optProposal1.get();
            PushUpBooleanExpressionProposal p2 = optProposal2.get();

            ImmutableSet<ExplicitVariableProjectionNode> inBetweenProjectors =
                    ImmutableSet.<ExplicitVariableProjectionNode>builder()
                            .addAll(p1.getInbetweenProjectors())
                            .addAll(p2.getInbetweenProjectors())
                            .build();

            ImmutableMap<CommutativeJoinOrFilterNode, Optional<ImmutableExpression>> provider2retainedExpression =
                    ImmutableMap.<CommutativeJoinOrFilterNode, Optional<ImmutableExpression>>builder()
                            .putAll(p1.getProvider2NonPropagatedExpressionMap())
                            .putAll(p2.getProvider2NonPropagatedExpressionMap())
                            .build();

            return Optional.of(
                    new PushUpBooleanExpressionProposalImpl(
                            p1.getPropagatedExpression(),
                            provider2retainedExpression,
                            p1.getUpMostPropagatingNode(),
                            p1.getRecipientNode(),
                            inBetweenProjectors)
            );
        }
        return Optional.empty();

    }

    private ImmutableSet<CommutativeJoinOrFilterNode> getProviders(UnionNode unionNode, IntermediateQuery query) {

        ImmutableList<Optional<CommutativeJoinOrFilterNode>> optProviders = query.getChildren(unionNode).stream()
                .map(n -> getCandidateProvider(n, query))
                .collect(ImmutableCollectors.toList());

        ImmutableSet.Builder<CommutativeJoinOrFilterNode> providers = ImmutableSet.builder();
        for (Optional<CommutativeJoinOrFilterNode> provider : optProviders) {
            if (provider.isPresent()) {
                providers.add(provider.get());
            } else {
                return ImmutableSet.of();
            }
        }
        return providers.build();
    }

    /**
     * Recursive
     */
    private Optional<CommutativeJoinOrFilterNode> getCandidateProvider(QueryNode subtreeRoot, IntermediateQuery query) {

        if (subtreeRoot instanceof ConstructionNode) {
            Optional<QueryNode> optChild = query.getFirstChild(subtreeRoot);
            return optChild.isPresent() ?
                    getCandidateProvider(optChild.get(), query) :
                    Optional.empty();
        }
        if (subtreeRoot instanceof CommutativeJoinOrFilterNode) {
            CommutativeJoinOrFilterNode castNode = (CommutativeJoinOrFilterNode) subtreeRoot;
            if (castNode.getOptionalFilterCondition().isPresent()) {
                return Optional.of(castNode);
            }
        }
        return Optional.empty();
    }


    private Optional<ImmutableExpression> getRetainedSubExpression
            (ImmutableSet<ImmutableExpression> propagatedExpressions,
             CommutativeJoinOrFilterNode provider) {

        ImmutableExpression fullBooleanExpression = provider.getOptionalFilterCondition()
                .orElseThrow(() -> new IllegalStateException("The provider is expected to have a filtering condition"));

        // conjuncts which will not be propagated up from this child
        return immutabilityTools.foldBooleanExpressions(
                fullBooleanExpression.flattenAND().stream()
                        .filter(e -> !propagatedExpressions.contains(e))
        );
    }

    /**
     * Returns the boolean conjuncts shared by all providers.
     */
    private ImmutableSet<ImmutableExpression> getExpressionsToPropagateAboveUnion(ImmutableSet<CommutativeJoinOrFilterNode> providers) {
        return providers.stream()
                .map(n -> n.getOptionalFilterCondition().get().flattenAND())
                .reduce(this::computeIntersection).get();
    }

    // If the expression was blocked by a union or the root of the query
    private Optional<PushUpBooleanExpressionProposal> adjustProposal(PushUpBooleanExpressionProposal proposal,
                                                            IntermediateQuery query) {

        QueryNode currentNode = proposal.getUpMostPropagatingNode();
        Optional<QueryNode> optChild;
        // Will be removed from the list of inbetween projectors
        ImmutableSet.Builder<ExplicitVariableProjectionNode> removedProjectors = ImmutableSet.builder();

        while ((optChild = query.getFirstChild(currentNode)).isPresent()) {
            if (currentNode instanceof ConstructionNode || currentNode instanceof  QueryModifierNode) {
                if(currentNode instanceof ConstructionNode) {
                    removedProjectors.add((ConstructionNode) currentNode);
                }
                currentNode = optChild.get();
                continue;
            }
            // Note that the iteration is stopped (among other) by any binary operator
            break;
        }
        // If we went back to the provider node
        if(proposal.getProvider2NonPropagatedExpressionMap().keySet().contains(currentNode)){
            return Optional.empty();
        }
        // adjust the upmost propagating node
        QueryNode upMostPropagatingNode = currentNode;
        // if it it a filter or join, use it as a recipient for the expression
        Optional<JoinOrFilterNode> recipient = currentNode instanceof CommutativeJoinOrFilterNode ?
                Optional.of((JoinOrFilterNode) currentNode) :
                Optional.empty();
        // update inbetween projectors
        ImmutableSet<ExplicitVariableProjectionNode> inbetweenProjectors = computeDifference(
                proposal.getInbetweenProjectors(),
                removedProjectors.build()
        );

        return Optional.of(
                new PushUpBooleanExpressionProposalImpl(
                    proposal.getPropagatedExpression(),
                    proposal.getProvider2NonPropagatedExpressionMap(),
                    upMostPropagatingNode,
                    recipient,
                    inbetweenProjectors
                ));
    }

    private <T> ImmutableSet<T> computeIntersection(ImmutableSet<T> s1, ImmutableSet<T> s2) {
        return s1.stream()
                .filter(s2::contains)
                .collect(ImmutableCollectors.toSet());
    }

    private <T> ImmutableSet<T> computeDifference(ImmutableSet<T> s1, ImmutableSet<T> s2) {
        return s1.stream()
                .filter(e -> !s2.contains(e))
                .collect(ImmutableCollectors.toSet());
    }
}
