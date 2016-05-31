package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.*;
import fj.F;
import fj.data.*;
import fj.data.List;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.PartialUnion;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.proposal.BindingTransfer;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ConstructionNodeUpdateImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionLiftProposalImpl;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.ConstructionNodeUpdate;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionLiftProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.BindingTransferImpl;

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
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        /**
         * TODO: determine and verify the conditions in which we can apply
         * this optimization.
         *
         * Informally, this OK if all the tables (data?) nodes are "protected"
         * by CONSTRUCTION nodes.
         */
        return optimizeQuery(query);
    }

    private IntermediateQuery optimizeQuery(IntermediateQuery query) throws EmptyQueryException {
        Tree<ConstructionNodeUpdate> initialConstructionTree = extractConstructionTree(query);

        Tree<ConstructionNodeUpdate> proposedConstructionTree = proposeOptimizedTree(initialConstructionTree,
                query);

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
    private static Tree<ConstructionNodeUpdate> proposeOptimizedTree(Tree<ConstructionNodeUpdate> initialConstructionTree,
                                                                     IntermediateQuery query) {

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
                currentZipper = optimizeCurrentNode(currentZipper, query);
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
    private static TreeZipper<ConstructionNodeUpdate> optimizeCurrentNode(TreeZipper<ConstructionNodeUpdate> currentZipper,
                                                                          IntermediateQuery query) {

        Option<ImmutableSubstitution<ImmutableTerm>> optionalSubstitutionToLift = liftBindings(currentZipper, query);
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
    private static Option<ImmutableSubstitution<ImmutableTerm>> liftBindings(TreeZipper<ConstructionNodeUpdate> currentZipper,
                                                                             IntermediateQuery query) {

        final Option<TreeZipper<ConstructionNodeUpdate>> optionalFirstChildZipper = currentZipper.firstChild();
        if (optionalFirstChildZipper.isNone()) {
            return Option.none();
        }
        final TreeZipper<ConstructionNodeUpdate> firstChild = optionalFirstChildZipper.some();
        final ImmutableSubstitution<ImmutableTerm> firstChildSubstitution = firstChild.getLabel()
                .getMostRecentConstructionNode().getSubstitution();

        // Non-final
        PartialUnion<ImmutableTerm> currentUnion = new PartialUnion<>(firstChildSubstitution, query);
        // Non-final
        Option<TreeZipper<ConstructionNodeUpdate>> optionalChildZipper = firstChild.right();

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


    /**
     * TODO: explain
     */
    private static IntermediateQuery applyProposal(IntermediateQuery query,
                                                   Tree<ConstructionNodeUpdate> proposedConstructionTree) throws EmptyQueryException {

        ImmutableList<BindingTransfer> transfers = ImmutableList.copyOf(extractTransfers(proposedConstructionTree));
        ImmutableList<ConstructionNodeUpdate> nodeUpdates = ImmutableList.copyOf(proposedConstructionTree
                .flatten()
                .filter(new F<ConstructionNodeUpdate, Boolean>() {
                    @Override
                    public Boolean f(ConstructionNodeUpdate update) {
                        return update.getOptionalNewNode().isPresent();
                    }
                }));
        SubstitutionLiftProposal proposal = new SubstitutionLiftProposalImpl(transfers, nodeUpdates);

        return query.applyProposal(proposal).getResultingQuery();
    }

    /**
     * Recursive
     */
    private static List<BindingTransfer> extractTransfers(Tree<ConstructionNodeUpdate> tree) {
        /**
         * Binding transfers to this node
         */

        List<BindingTransfer> localTransfers;
        ConstructionNodeUpdate currentRoot = tree.root();
        if (currentRoot.hasNewBindings()) {
            localTransfers = buildBindingTransfers(currentRoot, tree);
        }
        else {
            localTransfers = List.nil();
        }

        /**
         * Recursive call
         */
        List<BindingTransfer> subForestTransfers = tree.subForest()._1().toList()
                .bind(new F<Tree<ConstructionNodeUpdate>, List<BindingTransfer>>() {
            @Override
            public List<BindingTransfer> f(Tree<ConstructionNodeUpdate> subTree) {
                return extractTransfers(subTree);
            }
        });

        return localTransfers.append(subForestTransfers);
    }

    /**
     * TODO: explain
     */
    private static List<BindingTransfer> buildBindingTransfers(ConstructionNodeUpdate currentRoot,
                                                               Tree<ConstructionNodeUpdate> currentTree) {
        ImmutableList.Builder<BindingTransfer> transferBuilder = ImmutableList.builder();
        ImmutableMap<Variable, ImmutableTerm> newBindingMap = currentRoot.getNewBindings().getImmutableMap();

        for (Variable boundVariable : newBindingMap.keySet()) {
            List<ConstructionNode> sources = findSources(boundVariable, currentTree);

            ImmutableSubstitution<ImmutableTerm> uniqueBinding = new ImmutableSubstitutionImpl<>(
                    ImmutableMap.of(boundVariable, newBindingMap.get(boundVariable)));

            BindingTransfer transfer = new BindingTransferImpl(uniqueBinding, ImmutableList.copyOf(sources),
                    currentRoot.getFormerNode());
            transferBuilder.add(transfer);
        }
        return mergeTransfers(transferBuilder.build());
    }

    /**
     * TODO: explain
     */
    private static List<ConstructionNode> findSources(final Variable boundVariable,
                                                      Tree<ConstructionNodeUpdate> currentTree) {

        ConstructionNode formerRootNode = currentTree.root().getFormerNode();

        if (formerRootNode.getSubstitution().isDefining(boundVariable)) {
            return List.cons(formerRootNode, List.<ConstructionNode>nil());
        }
        /**
         * Recursive
         */
        else {
            return currentTree.subForest()._1().toList().bind(new F<Tree<ConstructionNodeUpdate>, List<ConstructionNode>>() {
                @Override
                public List<ConstructionNode> f(Tree<ConstructionNodeUpdate> subTree) {
                    return findSources(boundVariable, subTree);
                }
            });
        }
    }

    /**
     * TODO: replace this stub by a merging implementation.
     *
     * Merges transfers that have the same sources and the same target.
     */
    private static List<BindingTransfer> mergeTransfers(ImmutableList<BindingTransfer> transfers) {
        return List.iterableList(transfers);
    }
}
