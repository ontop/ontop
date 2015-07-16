package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.*;
import fj.F;
import fj.data.*;
import fj.data.List;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.PartialUnion;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.ConstructionNodeUpdate;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.ConstructionNodeUpdateImpl;

import java.util.*;
import java.util.HashMap;

/**
 * TODO: explain
 *
 * Like the original TypeLift, does not unfold but only lift types
 * between construction nodes.
 *
 * TODO: find a more precise name
 *
 */
public class BasicTypeLiftOptimizer implements IntermediateQueryOptimizer {

    /**
     * High-level method
     */
    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {

        Tree<ConstructionNodeUpdate> initialConstructionTree = extractConstructionTree(query);
        Tree<ConstructionNodeUpdate> proposedConstructionTree = proposeOptimizedTree(initialConstructionTree);

        return applyProposal(query, proposedConstructionTree);
    }

    /**
     * TODO: explain
     *
     * Non-recursive implementation
     *
     */
    private static Tree<ConstructionNodeUpdate> extractConstructionTree(IntermediateQuery query) {
        final Map<ConstructionNode, Tree<ConstructionNodeUpdate>> proposalTreeMap = new HashMap<>();

        // Parent construction node --> children
        Multimap<ConstructionNode, ConstructionNode> constructionSuperiorMap = HashMultimap.create();

        for (QueryNode node : query.getNodesInBottomUpOrder()) {
            if (node instanceof ConstructionNode) {
                ConstructionNode currentConstructionNode = (ConstructionNode) node;

                Option<ConstructionNode> optionalSuperior = findSuperiorConstructionNode(query, currentConstructionNode);
                if (optionalSuperior.isSome()) {
                    constructionSuperiorMap.put(optionalSuperior.some(), currentConstructionNode);
                }

                List<ConstructionNode> childrenNodes = findChildConstructionNodes(currentConstructionNode,
                        constructionSuperiorMap);

                List<Tree<ConstructionNodeUpdate>> childForest = childrenNodes.map(new F<ConstructionNode, Tree<ConstructionNodeUpdate>>() {
                            @Override
                            public Tree<ConstructionNodeUpdate> f(ConstructionNode childNode) {
                                if (!proposalTreeMap.containsKey(childNode)) {
                                    throw new RuntimeException("Internal error: missing tree for a child node");
                                }
                                return proposalTreeMap.get(childNode);
                            }
                        });

                ConstructionNodeUpdate currentProposal = new ConstructionNodeUpdateImpl(currentConstructionNode);
                Tree<ConstructionNodeUpdate> currentTree = Tree.node(currentProposal, childForest);
                proposalTreeMap.put(currentConstructionNode, currentTree);
            }
        }

        ConstructionNode rootNode = query.getRootConstructionNode();
        if (!proposalTreeMap.containsKey(rootNode)) {
            throw new RuntimeException("Internal error: missing tree for the root");
        }

        return proposalTreeMap.get(rootNode);
    }

    private static List<ConstructionNode> findChildConstructionNodes(ConstructionNode currentConstructionNode,
                                                                             Multimap<ConstructionNode, ConstructionNode> constructionParentMap) {
        return List.iterableList(constructionParentMap.get(currentConstructionNode));
    }

    /**
     * TODO: explain
     */
    private static Option<ConstructionNode> findSuperiorConstructionNode(IntermediateQuery query,
                                                                         ConstructionNode childNode) {
        for (QueryNode ancestor : query.getAncestors(childNode)) {
            if (ancestor instanceof ConstructionNode) {
                return Option.some((ConstructionNode) ancestor);
            }
        }
        return Option.none();
    }

    /**
     * TODO: explain
     *
     */
    private static Tree<ConstructionNodeUpdate> proposeOptimizedTree(Tree<ConstructionNodeUpdate> initialConstructionTree) {

        /**
         * Non-final variable (will be re-assigned) multiple times.
         *
         * starts at the bottom-left
         */
        TreeZipper<ConstructionNodeUpdate> currentZipper = navigateToLeftmostLeaf(TreeZipper.fromTree(initialConstructionTree));


        /**
         * Iterates over all the predicates (exactly one time for each predicate)
         * in a topological order so that no parent is evaluated before its children.
         *
         * According this order, the last node to be evaluated is the root.
         * This loop breaks after the evaluation of the latter.
         *
         */
        boolean rootHasBeenEvaluated = false;
        while (!rootHasBeenEvaluated) {
            /**
             * Main operation: updates the current node and its children.
             */
            if (currentZipper.hasChildren()) {
                currentZipper = optimizeCurrentNode(currentZipper);
            }

            /**
             * Moves to the leftmost leaf of the right sibling if possible.
             */
            final Option<TreeZipper<ConstructionNodeUpdate>> optionalRightSibling = currentZipper.right();
            if (optionalRightSibling.isSome()) {
                /**
                 * If the right sibling is not a leaf, reaches the leftmost leaf of its sub-tree.
                 */
                currentZipper = navigateToLeftmostLeaf(optionalRightSibling.some());
            }
            /**
             * Otherwise, tries to move to the parent.
             * If already at the root, terminates.
             */
            else {
                final Option<TreeZipper<ConstructionNodeUpdate>> optionalParent = currentZipper.parent();
                rootHasBeenEvaluated = optionalParent.isNone();
                if (!rootHasBeenEvaluated) {
                    currentZipper = currentZipper.parent().some();
                }
            }
        }

        return currentZipper.toTree();
    }

    /**
     * TODO: explain
     *
     */
    private static TreeZipper<ConstructionNodeUpdate> optimizeCurrentNode(TreeZipper<ConstructionNodeUpdate> currentZipper) {

        Option<ImmutableSubstitution<ImmutableTerm>> optionalSubstitutionToLift = liftBindings(currentZipper);
        if (optionalSubstitutionToLift.isNone()) {
            return currentZipper;
        }

        ImmutableSubstitution<ImmutableTerm> substitutionToLift = optionalSubstitutionToLift.some();

        ConstructionNodeUpdate newCurrentProposal = propagateSubstitutionToParent(substitutionToLift, currentZipper);
        TreeZipper<ConstructionNodeUpdate> updatedChildrenZipper = updateChildren(currentZipper, substitutionToLift);

        return updatedChildrenZipper.setLabel(newCurrentProposal);
    }

    /**
     * TODO: explain
     */
    private static Option<ImmutableSubstitution<ImmutableTerm>> liftBindings(TreeZipper<ConstructionNodeUpdate> currentZipper) {

        final Option<TreeZipper<ConstructionNodeUpdate>> optionalFirstChildZipper = currentZipper.firstChild();
        if (optionalFirstChildZipper.isNone()) {
            return Option.none();
        }
        final ImmutableSubstitution<ImmutableTerm> firstChildSubstitution = optionalFirstChildZipper.some().getLabel()
                .getMostRecentConstructionNode().getSubstitution();

        // Non-final
        PartialUnion<ImmutableTerm> currentUnion = new PartialUnion<>(firstChildSubstitution);
        // Non-final
        Option<TreeZipper<ConstructionNodeUpdate>> optionalChildZipper = currentZipper.firstChild();

        /**
         * Computes a partial union with the other children.
         */
        while(optionalChildZipper.isSome()) {
            TreeZipper<ConstructionNodeUpdate> currentChildZipper = optionalChildZipper.some();
            ImmutableSubstitution<ImmutableTerm> currentChildSubstitution = currentChildZipper.getLabel()
                    .getMostRecentConstructionNode().getSubstitution();

            currentUnion = currentUnion.newPartialUnion(currentChildSubstitution);

            optionalChildZipper = currentChildZipper.right();
        }

        /**
         * Returns the partial union if not empty
         */
        ImmutableSubstitution<ImmutableTerm> proposedSubstitution = currentUnion.getPartialUnionSubstitution();
        if (proposedSubstitution.isEmpty()) {
            return Option.none();
        }
        else {
            return Option.some(proposedSubstitution);
        }
    }


    /**
     * TODO: explain
     *
     */
    private static TreeZipper<ConstructionNodeUpdate> updateChildren(TreeZipper<ConstructionNodeUpdate> parentZipper,
                                                                       ImmutableSubstitution<ImmutableTerm> substitutionToLift) {

        if (substitutionToLift.isEmpty())
            return parentZipper;

        // Non-final
        Option<TreeZipper<ConstructionNodeUpdate>> nextOptionalChild = parentZipper.firstChild();
        // Non-final
        TreeZipper<ConstructionNodeUpdate> currentChildUpdatedZipper = null;
        while (nextOptionalChild.isSome()) {
            TreeZipper<ConstructionNodeUpdate> currentChildZipper = nextOptionalChild.some();

            /**
             * TODO: explain
             */
            ConstructionNodeUpdate newProposal = currentChildZipper.getLabel().removeSomeBindings(substitutionToLift);

            currentChildUpdatedZipper = currentChildZipper.setLabel(newProposal);
            nextOptionalChild = currentChildUpdatedZipper.right();
        }

        /**
         * If no child
         */
        if (currentChildUpdatedZipper == null) {
            return parentZipper;
        }
        else {
            return currentChildUpdatedZipper.parent().some();
        }
    }

    /**
     * TODO: explain
     *
     */
    private static ConstructionNodeUpdate propagateSubstitutionToParent(ImmutableSubstitution<ImmutableTerm> substitutionToLift,
                                                                          TreeZipper<ConstructionNodeUpdate> currentZipper) {
        return currentZipper.getLabel().addBindings(substitutionToLift);
    }


    /**
     * Navigates into the zipper until reaching the leftmost leaf.
     */
    private static TreeZipper<ConstructionNodeUpdate> navigateToLeftmostLeaf(final TreeZipper<ConstructionNodeUpdate> initialZipper) {

        // Non-final
        Option<TreeZipper<ConstructionNodeUpdate>> optionalChild = initialZipper.firstChild();
        // Non-final
        TreeZipper<ConstructionNodeUpdate> currentZipper = initialZipper;

        /**
         * Goes to its left child
         */
        while(optionalChild.isSome()) {
            currentZipper = optionalChild.some();
            optionalChild = currentZipper.firstChild();
        }
        /**
         * No more children --> is the leftmost leaf.
         */
        return currentZipper;
    }


    private static IntermediateQuery applyProposal(IntermediateQuery query,
                                                   Tree<ConstructionNodeUpdate> proposedConstructionTree) {
        throw new RuntimeException("TODO: implement it!");
    }
}
