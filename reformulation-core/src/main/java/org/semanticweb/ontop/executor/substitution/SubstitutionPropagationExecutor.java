package org.semanticweb.ontop.executor.substitution;

import com.google.common.base.Optional;
import org.semanticweb.ontop.executor.NodeCentricInternalExecutor;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import org.semanticweb.ontop.pivotalrepr.transformer.SubstitutionDownPropagator;
import org.semanticweb.ontop.pivotalrepr.transformer.SubstitutionUpPropagator;
import org.semanticweb.ontop.pivotalrepr.transformer.SubstitutionUpPropagator.StopPropagationException;

import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO: explain
 */
public class SubstitutionPropagationExecutor
        implements NodeCentricInternalExecutor<QueryNode, SubstitutionPropagationProposal> {
    @Override
    public NodeCentricOptimizationResults<QueryNode> apply(SubstitutionPropagationProposal proposal,
                                                           IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        QueryNode originalFocusNode = proposal.getFocusNode();
        ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate = proposal.getSubstitution();

        propagateUp(originalFocusNode, substitutionToPropagate, query, treeComponent);
        propagateDown(originalFocusNode, substitutionToPropagate, query, treeComponent);


        QueryNode newQueryNode = propagateToFocusNode(originalFocusNode, substitutionToPropagate, treeComponent);

        /**
         * The substitution is supposed
         */
        return new NodeCentricOptimizationResultsImpl<>(query, newQueryNode);

    }

    private static QueryNode propagateToFocusNode(QueryNode originalFocusNode,
                                                  ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate,
                                                  QueryTreeComponent treeComponent) {

        SubstitutionDownPropagator propagator = new SubstitutionDownPropagator(substitutionToPropagate);
        try {
            QueryNode newFocusNode = originalFocusNode.acceptNodeTransformer(propagator);
            treeComponent.replaceNode(originalFocusNode, newFocusNode);
            return newFocusNode;
        }
        /**
         * No exception is expected.
         */
        catch (QueryNodeTransformationException | NotNeededNodeException e) {
            throw new RuntimeException("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * Has-side effect on the tree component
     *
     * TODO: explain
     */
    private static void propagateDown(final QueryNode focusNode, final ImmutableSubstitution<VariableOrGroundTerm> originalSubstitutionToPropagate,
                               final IntermediateQuery query, final QueryTreeComponent treeComponent) {

        final SubstitutionDownPropagator propagator = new SubstitutionDownPropagator(originalSubstitutionToPropagate);

        Queue<QueryNode> nodesToVisit = new LinkedList<>();
        nodesToVisit.addAll(query.getChildren(focusNode));

        while (!nodesToVisit.isEmpty()) {
            QueryNode formerSubNode = nodesToVisit.poll();

            try {
                QueryNode newSubNode = formerSubNode.acceptNodeTransformer(propagator);

                nodesToVisit.addAll(query.getChildren(formerSubNode));
                treeComponent.replaceNode(formerSubNode, newSubNode);
            }
            /**
             * Recursive call for the sub-tree of the sub-node
             */
            catch (SubstitutionDownPropagator.NewSubstitutionException e) {
                QueryNode newSubNode = e.getTransformedNode();
                treeComponent.replaceNode(formerSubNode, newSubNode);

                // Recursive call
                propagateDown(newSubNode, e.getSubstitution(), query, treeComponent);
            }
            catch (NotNeededNodeException e) {
                nodesToVisit.addAll(query.getChildren(formerSubNode));
                try {
                    treeComponent.removeOrReplaceNodeByUniqueChildren(formerSubNode);
                }
                catch (IllegalTreeUpdateException e1) {
                    throw new RuntimeException(e1.getMessage());
                }
            } catch (QueryNodeTransformationException e) {
                throw new RuntimeException("Unexpected exception: " + e.getMessage());
            }
        }
    }

    /**
     * Has-side effect on the tree component
     *
     * TODO: explain
     *
     */
    private static void propagateUp(QueryNode focusNode, ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate,
                             IntermediateQuery query, QueryTreeComponent treeComponent) {

        SubstitutionUpPropagator propagator = new SubstitutionUpPropagator(query, focusNode, substitutionToPropagate);
        Queue<QueryNode> nodesToVisit = new LinkedList<>();

        Optional<QueryNode> optionalParent = query.getParent(focusNode);
        if (optionalParent.isPresent()) {
            nodesToVisit.add(optionalParent.get());
        }

        while (!nodesToVisit.isEmpty()) {
            QueryNode formerAncestor = nodesToVisit.poll();

            try {
                QueryNode newAncestor = formerAncestor.acceptNodeTransformer(propagator);
                nodesToVisit.addAll(query.getChildren(formerAncestor));

                treeComponent.replaceNode(formerAncestor, newAncestor);
            }
            /**
             * TODO: explain
             */
            catch (StopPropagationException e) {
                treeComponent.replaceNode(formerAncestor, e.getStoppingNode());
            }
            /**
             * TODO: explain
             */
            catch (NotNeededNodeException e) {
                nodesToVisit.addAll(query.getChildren(formerAncestor));
                try {
                    treeComponent.removeOrReplaceNodeByUniqueChildren(formerAncestor);
                }
                catch (IllegalTreeUpdateException e1) {
                    throw new RuntimeException(e1.getMessage());
                }
            }
            catch (QueryNodeTransformationException e) {
                throw new RuntimeException("Unexpected exception: " + e.getMessage());
            }

        }
    }
}
