package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.Multimap;
import fj.*;
import fj.data.*;
import fj.data.HashMap;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.removeHeadTypes;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.updateMultiTypedFunctionSymbolIndex;

/**
 * Type lifting consists in:
 *   1. typing high-level variables of a query (represented as a Datalog program).
 *   2. removing types from intermediate if possible.
 *
 * Here by "type" we mean:
 *   - Integer, double, etc. as usual
 *   - URI templates
 *
 * This implementation uses tree zippers for navigating and "updating" persistent trees.
 * It is based on the FunctionalJava library and thus adopts a functional programming style.
 *
 * Assumptions/restrictions:
 *   - Queries are Union of Conjunctive Queries (if some Left Joins are hidden in the Datalog-like data structure,
 *        they will not be considered).
 *   - Rule bodies are composed of data and filter atoms.
 *   - Type lifting is locally stopped if a sub-goal predicate is found to be multi-typed.
 *
 *
 * Accidental complexity of this implementation: multi-variate URI template support implies to change arity
 * of data atoms, create new variables, etc. Note that this complications are due to the Datalog data structure.
 * In the future, some of these manipulations could vanish with a better data structure for intermediate queries.
 *
 */
public class TypeLift {

    private static Logger LOGGER = LoggerFactory.getLogger(TypeLift.class);

    /**
     * Type lifting implementation based on tree zippers (persistent data structures).
     *
     * @param inputRules Original rules.
     * @param multiTypedFunctionSymbolIndex  Index indicating which predicates are known as multi-typed.
     * @return New list of rules.
     */
    public static java.util.List<CQIE> liftTypes(final java.util.List<CQIE> inputRules,
                                       Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {
        /**
         * Yes, some tests try to lift types while there is no rule...
         */
        if (inputRules.isEmpty()) {
            return inputRules;
        }

        /**
         * Builds a tree zipper from the input Datalog rules.
         */
        TreeBasedDatalogProgram initialDatalogProgram = TreeBasedDatalogProgram.fromRules(inputRules);
        TreeZipper<TypeLiftNode> initialRootZipper = TreeZipper.fromTree(initialDatalogProgram.getTypeLiftTree());

        /**
         * Navigates into the tree until reaching the leftmost leaf.
         */
        TreeZipper<TypeLiftNode> leftmostTreeZipper = navigateToLeftmostLeaf(initialRootZipper);


        /**
         * Makes sure the multi-typed predicate index is complete.
         * (This step could be disabled in the future once the previous unfolding will be safe enough).
         */
        multiTypedFunctionSymbolIndex = updateMultiTypedFunctionSymbolIndex(initialDatalogProgram, multiTypedFunctionSymbolIndex);

        /**
         * Computes a new Datalog program by applying type lifting.
         */
        TreeZipper<TypeLiftNode> newTreeZipper = liftTypesOnTreeZipper(leftmostTreeZipper, multiTypedFunctionSymbolIndex);
        TreeBasedDatalogProgram newDatalogProgram = TreeBasedDatalogProgram.fromTypeLiftTree(newTreeZipper.toTree());

        LOGGER.debug(newDatalogProgram.toString());

        java.util.List<CQIE> newRules = new ArrayList<>(newDatalogProgram.getRules().toCollection());
        return newRules;
    }

    /**
     * Navigates into the zipper until reaching the leftmost leaf.
     *
     * Tail-recursive function
     *  (even if not optimized by the JVM, should not be too profound (tree depth)).
     *
     */
    private static TreeZipper<TypeLiftNode> navigateToLeftmostLeaf(TreeZipper<TypeLiftNode> currentZipper) {

        Option<TreeZipper<TypeLiftNode>> optionalFirstChild = currentZipper.firstChild();
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

    /**
     * We use here an imperative loop instead of a function
     * because:
     *   (i) the tail-recursion optimization is apparently still not supported in Java 8.
     *   (ii) the recursion is too profound (equal to the number of predicates).
     */
    private static TreeZipper<TypeLiftNode> liftTypesOnTreeZipper(
            final TreeZipper<TypeLiftNode> initialTreeZipper,
            final Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {

        /**
         * Non-final variable (will be re-assigned) multiple times.
         */
        TreeZipper<TypeLiftNode> currentZipper = initialTreeZipper;
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
            currentZipper = updateSubTree(currentZipper, multiTypedFunctionSymbolIndex);

            /**
             * Moves to the leftmost leaf of the right sibling if possible.
             */
            final Option<TreeZipper<TypeLiftNode>> optionalRightSibling = currentZipper.right();
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
                final Option<TreeZipper<TypeLiftNode>> optionalParent = currentZipper.parent();
                rootHasBeenEvaluated = optionalParent.isNone();
                if (!rootHasBeenEvaluated) {
                    currentZipper = currentZipper.parent().some();
                }
            }
        }
        return currentZipper;
    }

    /**
     * Updates the current node and its children.
     *
     * Type lifting is forbidden if the current predicate is
     * already multi-typed or if the child proposals would
     * make it multi-typed.
     *
     * Returns the updated treeZipper at the same position.
     */
    private static TreeZipper<TypeLiftNode> updateSubTree(final TreeZipper<TypeLiftNode> currentZipper,
                                                          final Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

        final Predicate currentPredicate = currentZipper.getLabel().getPredicate();

        /**
         * If there is no multi-typing problem, tries to lift the type from the children.
         */
        final boolean isMultiTyped = multiTypedFunctionSymbolIndex.containsKey(currentPredicate);
        if (!isMultiTyped) {
            try {
                final TreeZipper<TypeLiftNode> newTreeZipper = liftTypeFromChildrenToParent(currentZipper);
                return newTreeZipper;
            }
            /**
             * Multi-typing conflict detected during type lifting.
             * The latter operation is thus rejected (and has produced no side-effect).
             */
            catch(TypeLiftTools.MultiTypeException ex) {
            }
        }
        /**
         * Fallback strategy in reaction to multi-typing (of the current predicate).
         *
         * --> Returns the unmodified zipper.
         *
         */
        return currentZipper;
    }

    /**
     * Lifts types from the children to the current parent node.
     *
     * This operation fails if the children proposals indicate that the parent predicate is multi-typed.
     * In such a case, a MultiTypeException is thrown.
     *
     * Returns an updated tree zipper at the same position.
     */
    private static TreeZipper<TypeLiftNode> liftTypeFromChildrenToParent(
            final TreeZipper<TypeLiftNode> parentZipper) throws TypeLiftTools.MultiTypeException {

        /**
         * Main operations:
         *  (i) makes a type proposal for the current (parent) predicate,
         *  (ii) updates body and head atoms (compatible with the future untyped child heads).
         * May throw a MultiTypeException.
         *
         * Note that this data structure is partially inconsistent (needs the child heads to be updated).
         */
        final TreeZipper<TypeLiftNode> partiallyUpdatedTreeZipper = proposeTypeAndUpdateBodies(parentZipper);

        /**
         * Removes child types.
         */
        final TreeZipper<TypeLiftNode> untypedChildrenZipper = applyToChildren(removeTypeFunction, partiallyUpdatedTreeZipper);

        /**
         * Now the tree zipper is consistent :)
         */
        return untypedChildrenZipper;
    }


    /**
     * Makes a PredicateLevelProposal for the current (parent) predicate and updates the current node
     * according to it.
     *
     * If the multi-typing problem is detected, throws a MultiTypeException.
     *
     * Note that the children nodes are NOT UPDATED so the returned tree zipper IS NOT FULLY CONSISTENT.
     *
     */
    private static TreeZipper<TypeLiftNode> proposeTypeAndUpdateBodies(final TreeZipper<TypeLiftNode> parentZipper)
            throws TypeLiftTools.MultiTypeException {

        /**
         * Children proposals. At most one type proposal per child predicate.
         */
        final HashMap<Predicate, PredicateLevelProposal> childProposalIndex = retrieveChildrenProposals(parentZipper);

        /**
         * Aggregates all these proposals according to the rules defining the parent predicate
         * into a PredicateLevelProposal.
         *
         * If such aggregation is not possible, a MultiTypeException will be thrown.
         *
         */
        final TypeLiftNode parentLabel = parentZipper.getLabel();
        final PredicateLevelProposal proposal = makeProposal(parentLabel.getDefinitionRules(), childProposalIndex);

        /**
         * Updated rules: type is applied to these rules (heads and bodies).
         */
        final List<CQIE> updatedParentRules = proposal.getTypedRules();


        /**
         * Returns a new zipper with the updated label.
         * Note that this tree zipper is not fully consistent (child heads not yet updated).
         */
        return parentZipper.setLabel(parentLabel.newRulesAndProposal(updatedParentRules, proposal));
    }

    /**
     * Indexes the proposals of the children of the current parent node according to their predicate.
     *
     * Returns the index.
     */
    private static HashMap<Predicate, PredicateLevelProposal> retrieveChildrenProposals(final TreeZipper<TypeLiftNode> parentZipper) {
        /**
         * Child forest.
         */
        Stream<Tree<TypeLiftNode>> subForest = parentZipper.focus().subForest()._1();
        /**
         * No child: returns an empty map.
         */
        if (subForest.isEmpty()) {
            return HashMap.from(Stream.<P2<Predicate, PredicateLevelProposal>>nil());
        }

        /**
         * Children labels (roots of the child forest).
         */
        Stream<TypeLiftNode> childrenLabels =  subForest.map(
                Tree.<TypeLiftNode>root_());

        Stream<Option<PredicateLevelProposal>> proposals = childrenLabels.map(new F<TypeLiftNode, Option<PredicateLevelProposal>>() {
            @Override
            public Option<PredicateLevelProposal> f(TypeLiftNode typeLiftNode) {
                return typeLiftNode.getOptionalProposal();
            }
        });

        /**
         * Only positive proposals.
         */
        List<PredicateLevelProposal> positiveProposals = Option.somes(proposals).toList();

        /**
         * Computes equivalent predicate index (generic method).
         *
         */
        HashMap<Predicate, List<PredicateLevelProposal>> predicateIndex = buildPredicateIndex(positiveProposals);

        /**
         * Because only one proposal can be made per predicate (child),
         * the structure of this predicate index can be simplified.
         *
         * Returns this simplified index.
         */
        HashMap<Predicate, PredicateLevelProposal> simplifiedPredicateIndex = predicateIndex.map(new F<P2<Predicate, List<PredicateLevelProposal>>, P2<Predicate, PredicateLevelProposal>>() {
            @Override
            public P2<Predicate, PredicateLevelProposal> f(P2<Predicate, List<PredicateLevelProposal>> mapEntry) {
                List<PredicateLevelProposal> proposals = mapEntry._2();
                if (proposals.length() != 1) {
                    // Code inconsistency
                    throw new InternalError("According to the tree, only one proposal can be made per predicate." +
                            "If no proposal has been made, should not appear in this map.");
                }
                return P.p(mapEntry._1(), proposals.head());
            }
        });
        return simplifiedPredicateIndex;
    }

    /**
     * Creates a PredicateLevelProposal from the parent rules and the child proposals.
     */
    private static PredicateLevelProposal makeProposal(List<CQIE> parentRules,
                                                       HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        return new PredicateLevelProposalImpl(parentRules, childProposalIndex);
    }

    /**
     * Applies a function to the children.
     *
     * Returns the updated tree zipper at the parent position.
     */
    private static TreeZipper<TypeLiftNode> applyToChildren(
            F<TreeZipper<TypeLiftNode>, TreeZipper<TypeLiftNode>> f,
            TreeZipper<TypeLiftNode> parentZipper) {
        Option<TreeZipper<TypeLiftNode>> optionalFirstChild = parentZipper.firstChild();

        /**
         * No child, nothing to apply
         */
        if (optionalFirstChild.isNone()) {
            return parentZipper;
        }

        /**
         * Applies "applyTypeToRules" to the children.
         *
         * IMPROVEMENT: Find a way to replace this usage by a map only applied to the children of a given parent node.
         */
        TreeZipper<TypeLiftNode> lastChildZipper = applyToNodeAndRightSiblings(f, optionalFirstChild.some());

        /**
         *  Move back to the parent node
         */
        return lastChildZipper.parent().some();
    }

    /**
     * Applies a function to the current zipper and its right siblings.
     *
     * Tail-recursive function.
     */
    private static TreeZipper<TypeLiftNode> applyToNodeAndRightSiblings(
            F<TreeZipper<TypeLiftNode>, TreeZipper<TypeLiftNode>> f,
            TreeZipper<TypeLiftNode> currentZipper) {
        /**
         * Applies f to the current node
         */
        TreeZipper<TypeLiftNode> updatedCurrentZipper = f.f(currentZipper);

        /**
         * Looks for the right sibling
         */
        Option<TreeZipper<TypeLiftNode>> optionalRightSibling = updatedCurrentZipper.right();
        if (optionalRightSibling.isSome()) {
            /**
             * Recursive call
             */
            return applyToNodeAndRightSiblings(f, optionalRightSibling.some());
        }
        /**
         * If if the rightmost sibling, stops recursion.
         */
        return updatedCurrentZipper;
    }

    /**
     * Removes types from the rules of the current node if the latter has made a proposal in the past.
     *
     * If no proposal has been done before, it is maybe because some types should remain local.
     */
    private static F<TreeZipper<TypeLiftNode>, TreeZipper<TypeLiftNode>> removeTypeFunction
            = new F<TreeZipper<TypeLiftNode>, TreeZipper<TypeLiftNode>>() {
        @Override
        public TreeZipper<TypeLiftNode> f(TreeZipper<TypeLiftNode> treeZipper) {
            TypeLiftNode label = treeZipper.getLabel();
            Option<PredicateLevelProposal> optionalProposal = label.getOptionalProposal();
            /**
             * If no previous proposal, no type removal.
             */
            if (optionalProposal.isNone()) {
                return treeZipper;
            }
            /**
             * Otherwise, remove types.
             */
            else {
                List<CQIE> initialRules = label.getDefinitionRules();
                List<CQIE> updatedRules = removeTypesFromRules(initialRules);
                return treeZipper.setLabel(label.newRulesNoProposal(updatedRules));
            }
        }
    };

    /**
     * Removes types from rules.
     *
     * Returns updated rules.
     */
    private static List<CQIE> removeTypesFromRules(final List<CQIE> initialRules) {
        return removeHeadTypes(initialRules);
    }


    /**
     * Generic method that indexes a list of proposals according to their head predicates.
     */
    private static HashMap<Predicate, List<PredicateLevelProposal>> buildPredicateIndex(List<PredicateLevelProposal> atoms) {
        List<P2<Predicate, List<PredicateLevelProposal>>> predicateAtomList = atoms.group(
                /**
                 * Groups by predicate
                 */
                Equal.equal(new F<PredicateLevelProposal, F<PredicateLevelProposal, Boolean>>() {
                    @Override
                    public F<PredicateLevelProposal, Boolean> f(final PredicateLevelProposal proposal) {
                        return new F<PredicateLevelProposal, Boolean>() {
                            @Override
                            public Boolean f(PredicateLevelProposal other) {
                                return other.getPredicate().equals(proposal.getPredicate());
                            }
                        };
                    }
                })).map(
                /**
                 * Transforms it into a P2 list (predicate and list of functions).
                 */
                new F<List<PredicateLevelProposal>, P2<Predicate, List<PredicateLevelProposal>>>() {
                    @Override
                    public P2<Predicate, List<PredicateLevelProposal>> f(List<PredicateLevelProposal> proposals) {
                        return P.p(proposals.head().getPredicate(), proposals);
                    }
                });

        return HashMap.from(predicateAtomList);
    }

    //    /**
//     * Makes a type proposal by looking at the rules defining the current predicate.
//     *
//     * Its current implementation is very basic and could be improved.
//     * It returns the head of the first rule.
//     *
//     * TODO: Several improvements could be done:
//     *  1. Unifying all the rule heads (case where is there is multiple rules).
//     *  2. Detecting if no type is present in the proposal and returning a None in
//     *     this case.
//     *
//     */
//    @Deprecated
//    private static Option<TypeProposal> proposeTypeFromLocalRules(TreeZipper<TypeLiftNode> currentZipper) {
//        List<CQIE> currentRules = currentZipper.getLabel()._2();
//        if (currentRules.isNotEmpty()) {
//
//            // Head of the first rule (cloned because mutable).
//            Function proposedHead = (Function) currentRules.head().getHead().clone();
//
//            TypeProposal typeProposal = constructTypeProposal(proposedHead);
//            return Option.some(typeProposal);
//        }
//        return Option.none();
//    }
}
