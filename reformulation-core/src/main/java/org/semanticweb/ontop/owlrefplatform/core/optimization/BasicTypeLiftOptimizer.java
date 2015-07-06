package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import fj.F;
import fj.data.*;
import fj.data.List;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

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
     * Quasi-immutable (except the ConstructionNode)
     */
    private static class ConstructionNodeProposal {

        private final ConstructionNode formerNode;
        private final Option<ConstructionNode> optionalNewNode;

        public ConstructionNodeProposal(ConstructionNode formerConstructionNode) {
            this.formerNode = formerConstructionNode;
            this.optionalNewNode = Option.none();
        }

        public ConstructionNodeProposal(ConstructionNode formerConstructionNode,
                                        ConstructionNode newConstructionNode) {
            this.formerNode = formerConstructionNode;
            this.optionalNewNode = Option.some(newConstructionNode);
        }

        public ConstructionNode getFormerNode() {
            return formerNode;
        }

        public Option<ConstructionNode> getOptionalNewNode() {
            return optionalNewNode;
        }

        public ConstructionNode getMostRecentConstructionNode() {
            if (optionalNewNode.isSome())
                return optionalNewNode.some();
            return formerNode;
        }
    }



    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {

        Tree<ConstructionNodeProposal> initialConstructionTree = extractConstructionTree(query);
        Tree<ConstructionNodeProposal> proposedConstructionTree = proposeOptimizedTree(initialConstructionTree);

        return applyProposal(query, proposedConstructionTree);
    }

    /**
     * TODO: explain
     *
     * Non-recursive implementation
     *
     */
    private static Tree<ConstructionNodeProposal> extractConstructionTree(IntermediateQuery query) {
        final Map<ConstructionNode, Tree<ConstructionNodeProposal>> proposalTreeMap = new HashMap<>();

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

                List<Tree<ConstructionNodeProposal>> childForest = childrenNodes.map(new F<ConstructionNode, Tree<ConstructionNodeProposal>>() {
                            @Override
                            public Tree<ConstructionNodeProposal> f(ConstructionNode childNode) {
                                if (!proposalTreeMap.containsKey(childNode)) {
                                    throw new RuntimeException("Internal error: missing tree for a child node");
                                }
                                return proposalTreeMap.get(childNode);
                            }
                        });

                ConstructionNodeProposal currentProposal = new ConstructionNodeProposal(currentConstructionNode);
                Tree<ConstructionNodeProposal> currentTree = Tree.node(currentProposal, childForest);
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
    private static Tree<ConstructionNodeProposal> proposeOptimizedTree(Tree<ConstructionNodeProposal> initialConstructionTree) {

        /**
         * Non-final variable (will be re-assigned) multiple times.
         *
         * starts at the bottom-left
         */
        TreeZipper<ConstructionNodeProposal> currentZipper = navigateToLeftmostLeaf(TreeZipper.fromTree(initialConstructionTree));


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
            final Option<TreeZipper<ConstructionNodeProposal>> optionalRightSibling = currentZipper.right();
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
                final Option<TreeZipper<ConstructionNodeProposal>> optionalParent = currentZipper.parent();
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
    private static TreeZipper<ConstructionNodeProposal> optimizeCurrentNode(TreeZipper<ConstructionNodeProposal> currentZipper) {

        Option<ImmutableSubstitution<ImmutableTerm>> optionalSubstitutionToLift = liftBindings(currentZipper);
        if (optionalSubstitutionToLift.isNone()) {
            return currentZipper;
        }

        ImmutableSubstitution<ImmutableTerm> substitutionToLift = optionalSubstitutionToLift.some();
        TreeZipper<ConstructionNodeProposal> updatedChildrenZipper = updateChildren(currentZipper, substitutionToLift);

        ConstructionNodeProposal newCurrentProposal = propagateSubstitutionToParent(substitutionToLift, currentZipper);

        return updatedChildrenZipper.setLabel(newCurrentProposal);
    }

    /**
     * TODO: explain
     */
    private static Option<ImmutableSubstitution<ImmutableTerm>> liftBindings(TreeZipper<ConstructionNodeProposal> currentZipper) {


        Option<TreeZipper<ConstructionNodeProposal>> optionalFirstChildZipper = currentZipper.firstChild();
        if (optionalFirstChildZipper.isNone())
            return Option.none();

        TreeZipper<ConstructionNodeProposal> firstChildZipper = optionalFirstChildZipper.some();
        ImmutableSubstitution<ImmutableTerm> firstSubstitution = firstChildZipper.getLabel()
                .getMostRecentConstructionNode().getSubstitution();

        return mergeChildSubstitutions(firstSubstitution, firstChildZipper.right());
    }

    /**
     * TODO: explain
     */
    private static Option<ImmutableSubstitution<ImmutableTerm>> mergeChildSubstitutions(
            ImmutableSubstitution<ImmutableTerm> currentConsensus,
            Option<TreeZipper<ConstructionNodeProposal>> optionalCurrentChild) {
        if (optionalCurrentChild.isNone())
            return Option.some(currentConsensus);

        TreeZipper<ConstructionNodeProposal> currentChild = optionalCurrentChild.some();

        Optional<ImmutableSubstitution<ImmutableTerm>> optionalSubstitution =
                currentConsensus.union(currentChild.getLabel().getMostRecentConstructionNode().getSubstitution());

        if (!optionalSubstitution.isPresent())
            return Option.none();

        // Recursive call
        return mergeChildSubstitutions(optionalSubstitution.get(), currentChild.right());
    }


    /**
     * TODO: explain
     *
     */
    private static TreeZipper<ConstructionNodeProposal> updateChildren(TreeZipper<ConstructionNodeProposal> currentZipper,
                                                                       ImmutableSubstitution<ImmutableTerm> substitutionToLift) {
        throw new RuntimeException("TODO: implement it!");
    }

    /**
     * TODO: explain
     *
     */
    private static ConstructionNodeProposal propagateSubstitutionToParent(ImmutableSubstitution<ImmutableTerm> substitutionToLift,
                                                                          TreeZipper<ConstructionNodeProposal> currentZipper) {
        throw new RuntimeException("TODO: implement it!");
    }


    /**
     * Navigates into the zipper until reaching the leftmost leaf.
     *
     * Tail-recursive function
     *  (even if not optimized by the JVM, should not be too profound (tree depth)).
     *
     */
    private static TreeZipper<ConstructionNodeProposal> navigateToLeftmostLeaf(TreeZipper<ConstructionNodeProposal> currentZipper) {

        Option<TreeZipper<ConstructionNodeProposal>> optionalFirstChild = currentZipper.firstChild();
        /**
         * Goes to its left child
         */
        if (optionalFirstChild.isSome())
            return navigateToLeftmostLeaf(optionalFirstChild.some());
        /**
         * Otherwise, is the leftmost leaf.
         */
        return currentZipper;
    }


    private static IntermediateQuery applyProposal(IntermediateQuery query,
                                                   Tree<ConstructionNodeProposal> proposedConstructionTree) {
        throw new RuntimeException("TODO: implement it!");
    }
}
