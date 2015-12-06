package org.semanticweb.ontop.executor.substitution;

import com.google.common.base.Optional;
import org.semanticweb.ontop.executor.NodeCentricInternalExecutor;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import org.semanticweb.ontop.pivotalrepr.transformer.NewSubstitutionException;
import org.semanticweb.ontop.pivotalrepr.transformer.SubstitutionDownPropagator;
import org.semanticweb.ontop.pivotalrepr.transformer.impl.SubstitutionDownPropagatorImpl;
import org.semanticweb.ontop.pivotalrepr.transformer.impl.SubstitutionUpPropagatorImpl;

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
            throws InvalidQueryOptimizationProposalException {
        try {
            return applySubstitution(proposal, query, treeComponent);
        }
        catch (QueryNodeSubstitutionException e) {
            throw new InvalidQueryOptimizationProposalException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     *
     */
    private NodeCentricOptimizationResults<QueryNode> applySubstitution(SubstitutionPropagationProposal proposal,
                                                                        IntermediateQuery query,
                                                                        QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException {
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

        SubstitutionDownPropagator propagator = new SubstitutionDownPropagatorImpl(substitutionToPropagate);
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

        final SubstitutionDownPropagator propagator = new SubstitutionDownPropagatorImpl(originalSubstitutionToPropagate);

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
            catch (NewSubstitutionException e) {
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
                             IntermediateQuery query, QueryTreeComponent treeComponent) throws QueryNodeSubstitutionException {
        Queue<QueryNode> nodesToVisit = new LinkedList<>();

        Optional<QueryNode> optionalParent = query.getParent(focusNode);
        if (optionalParent.isPresent()) {
            nodesToVisit.add(optionalParent.get());
        }

        while (!nodesToVisit.isEmpty()) {
            QueryNode formerAncestor = nodesToVisit.poll();

            /**
             * Applies the substitution and analyses the results
             */
            SubstitutionResults<? extends QueryNode> substitutionResults = formerAncestor.applyAscendentSubstitution(
                    substitutionToPropagate, focusNode, query);

            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution =
                    substitutionResults.getSubstitutionToPropagate();
            Optional<? extends QueryNode> optionalNewAncestor = substitutionResults.getOptionalNewNode();

            if (optionalNewSubstitution.isPresent()) {
                /**
                 * TODO: refactor so that to remove this assumption
                 */
                if (!substitutionToPropagate.equals(optionalNewSubstitution.get())) {
                    throw new RuntimeException("Updating the substitution is not supported (yet) in" +
                            "the ascendent substitution mode");
                }

                /**
                 * Normal case: replace the ancestor by an updated version
                 */
                if (optionalNewAncestor.isPresent()) {
                    QueryNode newAncestor = optionalNewAncestor.get();

                    nodesToVisit.addAll(query.getChildren(formerAncestor));
                    treeComponent.replaceNode(formerAncestor, newAncestor);
                }
                /**
                 * The ancestor is not needed anymore
                 */
                else {
                    nodesToVisit.addAll(query.getChildren(formerAncestor));
                    try {
                        treeComponent.removeOrReplaceNodeByUniqueChildren(formerAncestor);
                    } catch (IllegalTreeUpdateException e1) {
                        throw new RuntimeException(e1.getMessage());
                    }
                }
            }
            /**
             * Stops the propagation
             */
            else {
                if (optionalNewAncestor.isPresent()) {
                    treeComponent.replaceNode(formerAncestor, optionalNewAncestor.get());
                }
                else {
                    throw new RuntimeException("Unexcepted case where the propagation is stopped and" +
                            "the stopping node not needed anymore. Should we support this case?");
                }
            }
        }
    }
}
