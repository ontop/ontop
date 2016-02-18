package unibz.inf.ontop.executor.substitution;


import java.util.Optional;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.ImmutableTerm;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import unibz.inf.ontop.pivotalrepr.QueryNode;
import unibz.inf.ontop.pivotalrepr.QueryNodeSubstitutionException;
import unibz.inf.ontop.pivotalrepr.SubstitutionResults;
import unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * These methods are only accessible by InternalProposalExecutors (requires access to the QueryTreeComponent).
 */
public class SubstitutionPropagationTools {

    /**
     * Propagates the substitution to the descendants of the focus node.
     *
     * THE SUBSTITUTION IS NOT APPLIED TO THE FOCUS NODE.
     *
     * Has-side effect on the tree component.
     * Returns the updated tree component
     *
     */
    public static QueryTreeComponent propagateSubstitutionDown(final QueryNode focusNode,
                                                               final ImmutableSubstitution<? extends VariableOrGroundTerm> currentSubstitutionToPropagate,
                                                               final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException {

        Queue<QueryNode> nodesToVisit = new LinkedList<>();
        nodesToVisit.addAll(treeComponent.getCurrentSubNodesOf(focusNode));

        while (!nodesToVisit.isEmpty()) {
            QueryNode formerSubNode = nodesToVisit.poll();

            SubstitutionResults<? extends QueryNode> substitutionResults =
                    formerSubNode.applyDescendentSubstitution(currentSubstitutionToPropagate);

            Optional<? extends QueryNode> optionalNewSubNode = substitutionResults.getOptionalNewNode();
            Optional<? extends ImmutableSubstitution<? extends VariableOrGroundTerm>> optionalNewSubstitution
                    = substitutionResults.getSubstitutionToPropagate();

            /**
             * Still a substitution to propagate
             */
            if (optionalNewSubstitution.isPresent()) {
                ImmutableSubstitution<? extends VariableOrGroundTerm> newSubstitution = optionalNewSubstitution.get();

                /**
                 * No substitution change
                 */
                if (newSubstitution.equals(currentSubstitutionToPropagate)) {
                    /**
                     * Normal case: applies the same substitution to the children
                     */
                    if (optionalNewSubNode.isPresent()) {
                        QueryNode newSubNode = optionalNewSubNode.get();

                        nodesToVisit.addAll(treeComponent.getCurrentSubNodesOf(formerSubNode));
                        treeComponent.replaceNode(formerSubNode, newSubNode);
                    }
                    /**
                     * The sub-node is not needed anymore
                     */
                    else {
                        nodesToVisit.addAll(treeComponent.getCurrentSubNodesOf(formerSubNode));
                        treeComponent.removeOrReplaceNodeByUniqueChildren(formerSubNode);
                    }
                }
                /**
                 * New substitution: applies it to the children
                 */
                else if (optionalNewSubNode.isPresent())  {

                    QueryNode newSubNode = optionalNewSubNode.get();
                    treeComponent.replaceNode(formerSubNode, newSubNode);

                    // Recursive call
                    propagateSubstitutionDown(newSubNode, newSubstitution, treeComponent);
                }
                /**
                 * Unhandled case: new substitution to apply to the children of
                 * a not-needed node.
                 *
                 * TODO: should we handle this case
                 */
                else {
                    throw new RuntimeException("Unhandled case: new substitution to apply to the children of " +
                            "a not-needed node.");
                }
            }
            /**
             * Stops the propagation
             */
            else {
                if (optionalNewSubNode.isPresent()) {
                    QueryNode newSubNode = optionalNewSubNode.get();
                    treeComponent.replaceNode(formerSubNode, newSubNode);
                }
                else {
                    throw new RuntimeException("Unhandled case: the stopping node for the propagation" +
                            "is not needed anymore. Should we support it?");
                }
            }
        }
        return treeComponent;
    }

    /**
     * Propagates the substitution to the ancestors of the focus node.
     *
     * THE SUBSTITUTION IS NOT APPLIED TO THE FOCUS NODE.
     *
     * Has-side effect on the tree component.
     * Returns the updated tree component
     *
     */
    public static QueryTreeComponent propagateSubstitutionUp(QueryNode focusNode, ImmutableSubstitution<? extends VariableOrGroundTerm> substitutionToPropagate,
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
                    treeComponent.removeOrReplaceNodeByUniqueChildren(formerAncestor);
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
                    throw new RuntimeException("Unexpected case where the propagation is stopped and" +
                            "the stopping node not needed anymore. Should we support this case?");
                }
            }
        }

        return treeComponent;
    }
}
