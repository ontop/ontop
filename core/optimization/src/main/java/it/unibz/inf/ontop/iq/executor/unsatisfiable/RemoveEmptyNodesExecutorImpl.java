package it.unibz.inf.ontop.iq.executor.unsatisfiable;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.NodeTransformationProposal;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.iq.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeTrackingResultsImpl;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.executor.substitution.AscendingPropagationTools.propagateSubstitutionUp;

/**
 * TODO: explain
 */
@Singleton
public class RemoveEmptyNodesExecutorImpl implements RemoveEmptyNodesExecutor {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableSubstitutionTools substitutionTools;

    @Inject
    private RemoveEmptyNodesExecutorImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                         ImmutableSubstitutionTools substitutionTools) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.substitutionTools = substitutionTools;
    }

    /**
     * TODO: explain
     */
    @Override
    public NodeTrackingResults<EmptyNode> apply(RemoveEmptyNodeProposal proposal, IntermediateQuery query,
                                                QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        EmptyNode originalFocusNode = proposal.getFocusNode();

        return reactToEmptyChildNode(query, originalFocusNode, treeComponent, proposal.getOptionalTracker(query));
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private NodeTrackingResults<EmptyNode> reactToEmptyChildNode(IntermediateQuery query, EmptyNode emptyNode,
                                                                        QueryTreeComponent treeComponent,
                                                                        Optional<NodeTracker> optionalTracker)
            throws EmptyQueryException {

        QueryNode originalParentNode = query.getParent(emptyNode)
                // It is expected that the root has only one child, so if it is unsatisfiable,
                // this query will return empty results.
                .orElseThrow(EmptyQueryException::new);

        Optional<QueryNode> optionalOriginalNextSibling = query.getNextSibling(emptyNode);

        NodeTransformationProposal transformationProposal = originalParentNode.reactToEmptyChild(query, emptyNode);


        /**
         *  Node that propagates the null variables.
         *  Note that the substitution is NOT applied to this node.
         */
        QueryNode propagatingNode;
        Optional<QueryNode> optionalClosestAncestorNode;

        switch (transformationProposal.getState()) {
            case NO_LOCAL_CHANGE:
                treeComponent.removeSubTree(emptyNode);
                optionalClosestAncestorNode = Optional.of(originalParentNode);
                propagatingNode = originalParentNode;
                break;
            case REPLACE_BY_UNIQUE_NON_EMPTY_CHILD:
                // Propagates the null variables from the replacing child
                propagatingNode = transformationProposal.getOptionalNewNodeOrReplacingChild().get();
                optionalTracker
                        .ifPresent(tr -> tr.recordUpcomingReplacementByChild(query, originalParentNode, propagatingNode));
                optionalClosestAncestorNode = applyReplacementProposal(originalParentNode, treeComponent, transformationProposal,
                        emptyNode, true);
                break;

            case REPLACE_BY_NEW_NODE:
                optionalClosestAncestorNode = applyReplacementProposal(originalParentNode, treeComponent, transformationProposal,
                        emptyNode, false);
                propagatingNode = optionalClosestAncestorNode.get();
                optionalTracker
                        .ifPresent(tr -> tr.recordReplacement(originalParentNode, propagatingNode));
                break;

            case DECLARE_AS_EMPTY:
                optionalTracker.ifPresent(tr -> tr.recordUpcomingRemoval(query, originalParentNode));
                EmptyNode newEmptyNode = iqFactory.createEmptyNode(transformationProposal.getNullVariables());
                treeComponent.replaceSubTree(originalParentNode, newEmptyNode);

                /**
                 * Tail-recursive (cascade)
                 */
                return reactToEmptyChildNode(query, newEmptyNode, treeComponent, optionalTracker);

            default:
                throw new RuntimeException("Unexpected state: " + transformationProposal.getState());
        }

        Optional<QueryNode> optionalNewNextSibling = optionalOriginalNextSibling
                /**
                 * In the case the next sibling has also been removed (should be exceptional)
                 */
                .filter(treeComponent::contains);

        if (optionalClosestAncestorNode.isPresent()) {
            /**
             * After removing the empty node(s), second phase: propagates the null variables
             */
            return propagateNullVariables(query, optionalClosestAncestorNode.get(), optionalNewNextSibling, treeComponent,
                    transformationProposal.getNullVariables(), propagatingNode, iqFactory, optionalTracker);
        }
        /**
         * Special case: the promoted child is now the root the query
         */
        else {
            return new NodeTrackingResultsImpl<>(query,
                    /**
                     * Next sibling (of the empty node or of the lastly removed ancestor)
                     */
                    optionalNewNextSibling,
                    /**
                     * First ancestor to remain (may have be updated)
                     */
                    optionalClosestAncestorNode, optionalTracker);
        }
    }


    /**
     * Returns the newly created parent node or the parent of the promoted child.
     */
    private static Optional<QueryNode> applyReplacementProposal(QueryNode parentNode,
                                                      QueryTreeComponent treeComponent,
                                                      NodeTransformationProposal transformationProposal,
                                                      EmptyNode emptyNode, boolean isReplacedByUniqueChild)
            throws EmptyQueryException {

        QueryNode newReplacingNodeOrPromotedChild = transformationProposal.getOptionalNewNodeOrReplacingChild()
                .orElseThrow(() -> new InvalidQueryOptimizationProposalException(
                        "Inconsistent transformation proposal: a replacing node must be given"));

        if (isReplacedByUniqueChild) {
            treeComponent.removeSubTree(emptyNode);
            treeComponent.removeOrReplaceNodeByUniqueChild(parentNode);
        }
        else {
            treeComponent.replaceSubTree(parentNode, newReplacingNodeOrPromotedChild);
        }
        return isReplacedByUniqueChild
        ? treeComponent.getParent(newReplacingNodeOrPromotedChild)
        : Optional.of(newReplacingNodeOrPromotedChild);
    }

    /**
     * Second phase: propagates the null variables.
     *
     * Some ancestors may be removed in that process.
     *
     * Keeps track of the closest ancestor and the next sibling of the original focus (empty) node.
     *
     */
    private NodeTrackingResults<EmptyNode> propagateNullVariables(IntermediateQuery query,
                                                                         QueryNode ancestorNode,
                                                                         Optional<QueryNode> optionalNextSiblingOfFocusNode,
                                                                         QueryTreeComponent treeComponent,
                                                                         ImmutableSet<Variable> nullVariables,
                                                                         QueryNode propagatingNode,
                                                                         IntermediateQueryFactory iqFactory,
                                                                         Optional<NodeTracker> optionalAncestorTracker)
            throws EmptyQueryException {

        if (nullVariables.isEmpty()) {
            return new NodeTrackingResultsImpl<>(query, optionalNextSiblingOfFocusNode,
                    Optional.of(ancestorNode), optionalAncestorTracker);
        }

        ImmutableSubstitution<Constant> ascendingSubstitution = substitutionTools.computeNullSubstitution(nullVariables);

        NodeTrackingResults<QueryNode> propagationResults =
                propagateSubstitutionUp(propagatingNode, ascendingSubstitution, query, treeComponent, iqFactory,
                        substitutionFactory, optionalAncestorTracker);

        QueryNode closestRemainingAncestor = propagationResults.getOptionalNewNode()
                /**
                 * Deals with the case where the propagating node is not an ancestor
                 * of the original focus node.
                 *
                 * However, the parent of the propagating node (if existing) is expected
                 * to be an ancestor of the original focus node.
                 *
                 */
                .filter(n -> n != propagatingNode || n == ancestorNode)
                .orElseGet(() -> propagationResults.getOptionalClosestAncestor()
                        .orElseThrow(() -> new IllegalStateException(
                                "If no ancestor remains, an EmptyQueryException should have been thrown")));

        Optional<QueryNode> optionalNewNextSibling = optionalNextSiblingOfFocusNode
                .filter(treeComponent::contains)
                .map(Optional::of)
                // Uses the next sibling of the latest "removed" ancestor
                // if some ancestors are removed
                .orElseGet(propagationResults::getOptionalNextSibling);


        return new NodeTrackingResultsImpl<>(query, optionalNewNextSibling, Optional.of(closestRemainingAncestor),
                propagationResults.getOptionalTracker());

    }
}
