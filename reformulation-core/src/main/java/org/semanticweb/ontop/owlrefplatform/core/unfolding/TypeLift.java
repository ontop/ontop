package org.semanticweb.ontop.owlrefplatform.core.unfolding;


import com.google.common.collect.Multimap;
import fj.F;
import fj.P;
import fj.P3;
import fj.data.*;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.TreeBasedDatalogProgram;
import org.semanticweb.ontop.model.Predicate;

import java.util.ArrayList;

/**
 * TODO: describe it
 *
 * Here by Type we mean:
 *   - Integer, double, etc. as usual
 *   - URI templates
 *
 */
public class TypeLift {

    private class MultiTypeException extends Exception {
    };

    /**
     * Type lifting implementation based on tree zippers (persistent data structures).
     *
     * @param inputRules
     * @param multiTypedFunctionSymbolIndex
     * @return
     */
    public static java.util.List<CQIE> liftTypes(java.util.List<CQIE> inputRules,
                                       Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {
        /**
         * Builds a tree zipper from the input Datalog rules.
         */
        TreeBasedDatalogProgram initialDatalogProgram = TreeBasedDatalogProgram.fromRules(inputRules);
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> initialRootZipper = TreeZipper.fromTree(
                initialDatalogProgram.computeRuleTree());

        /**
         * Navigates into the tree until reaching the leftmost leaf.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> leftmostTreeZipper =
                navigateToLeftmostLeaf(initialRootZipper);

        /**
         * Computes a new Datalog program by applying type lifting
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> newTreeZipper = liftTypesOnTreeZipper(
                leftmostTreeZipper, multiTypedFunctionSymbolIndex);
        TreeBasedDatalogProgram newDatalogProgram = TreeBasedDatalogProgram.fromP3RuleTree(newTreeZipper.toTree());

        return new ArrayList<>(newDatalogProgram.getRules().toCollection());
    }

    /**
     * Navigates into the zipper until reaching the leftmost leaf.
     *
     * Tail-recursive function
     *  (even if not optimized by the JVM, should not be too profound (tree depth)).
     *
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> navigateToLeftmostLeaf(
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper) {

        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalFirstChild = currentZipper.firstChild();
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
     * because the tail-recursion optimization is apparently still not supported in Java 7.
     *
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> liftTypesOnTreeZipper(
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> initialTreeZipper,
            final Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {

        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper = initialTreeZipper;
        while (true) {
            currentZipper = updateSubTree(currentZipper, multiTypedFunctionSymbolIndex);

            /**
             * Moves to the leftmost leaf of the right sibling if possible.
             */
            Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalRightSibling = currentZipper.right();
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
                Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalParent = currentZipper.parent();
                if (optionalParent.isSome()) {
                    currentZipper = currentZipper.parent().some();
                }
                /**
                 * The root has been reached. Applies its proposal and breaks
                 * the loop.
                 */
                else {
                    currentZipper = applyTypeFunction.f(currentZipper);
                    break;
                }
            }
        }
        return currentZipper;
    }

    /**
     * Updates the current and its children nodes.
     *
     * Type lifting is forbidden if the current predicate is
     * already multi-typed or if the child proposals would
     * make it multi-typed.
     *
     * Returns the updated treeZipper at the same position.
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> updateSubTree(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper,
            Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

        P3<Predicate, List<CQIE>, Option<Function>> currentLabel = currentZipper.getLabel();
        Predicate currentPredicate = currentLabel._1();

        boolean isMultiTyped = multiTypedFunctionSymbolIndex.containsKey(currentPredicate);

        if (!isMultiTyped) {
            try {
                return liftTypeFromChildrenToParent(currentZipper);
            }
            /**
             * Type lifting rejected because it would make the predicate
             * be multi-typed.
             */
            catch(MultiTypeException ex) {
            }
        }
        return applyToChildren(applyTypeFunction, currentZipper);
    }

    /**
     *
     * If the children proposals are compatible, throws a MultiTypeException
     *
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> liftTypeFromChildrenToParent(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> parentZipper) throws MultiTypeException {

        Option<Function> parentProposal = buildProposal(parentZipper);
        /**
         * If no type has been proposed by the children nor the node itself,
         * no need to remove types from the children rules.
         */
        if (parentProposal.isNone()) {
            return parentZipper;
        }

        /**
         * Removes types from the children rules
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> cleanedZipper = applyToChildren(removeTypeFunction, parentZipper);

        /**
         * Sets the proposal to the parent node
         */
        P3<Predicate, List<CQIE>, Option<Function>> parentLabel = cleanedZipper.getLabel();
        return cleanedZipper.setLabel(P.p(parentLabel._1(), parentLabel._2(), parentProposal));
    }

    /**
     * If the children proposals are compatible, throws a MultiTypeException
     */
    private static Option<Function> buildProposal(final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper)
            throws MultiTypeException {

        List<Function> childrenProposals = retrieveChildrenProposals(currentZipper);
        Option<Function> currentProposal = proposeType(currentZipper);

        if (childrenProposals.isEmpty()) {
            return currentProposal;
        }

        //TODO: analyze the proposals

        // Extract the node proposal
        return null;
    }

    private static List<CQIE> applyTypeToRules(List<CQIE> initialRules, Function typeProposal) {
        //TODO: implement it
        return null;
    }

    private static List<CQIE> removeTypesFromRules(List<CQIE> initialRules) {
        //TODO: implement it
        return null;
    }

    private static Option<Function> proposeType(TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper) {
        //TODO: implement
        return null;
    }

    private static List<Function> retrieveChildrenProposals(final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> parentZipper) {
        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalFirstChild = parentZipper.firstChild();
        if (optionalFirstChild.isNone()) {
            return List.nil();
        }
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> leftmostChildZipper = optionalFirstChild.some();

        /**
         * Children labels (roots of the child forest)
         */
        Stream<P3<Predicate, List<CQIE>, Option<Function>>> childrenLabels =  leftmostChildZipper.toForest().map(
                Tree.<P3<Predicate, List<CQIE>, Option<Function>>>root_());

        Stream<Option<Function>> proposals = childrenLabels.map(P3.<Predicate, List<CQIE>, Option<Function>>__3());

        /**
         * Only returns positive proposals
         */
        return Option.somes(proposals).toList();
    }

    /**
     * Applies a function to the children.
     *
     * Returns the updated tree zipper at the parent position.
     */
    private static TreeZipper<P3<Predicate,List<CQIE>,Option<Function>>> applyToChildren(
            F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> f,
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> parentZipper) {
        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalFirstChild = parentZipper.firstChild();

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
        TreeZipper<P3<Predicate,List<CQIE>,Option<Function>>> lastChildZipper = applyToNodeAndRightSiblings(f, optionalFirstChild.some());

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
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> applyToNodeAndRightSiblings(
            F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> f,
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper) {
        /**
         * Applies f to the current node
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> updatedCurrentZipper = f.f(currentZipper);

        /**
         * Looks for the right sibling
         */
        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalRightSibling = updatedCurrentZipper.right();
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
     * Low-level. Applies the type to the rules of the current predicate.
     *
     * Returns the updated zipper at the same location.
     */
    private static F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> applyTypeFunction
            = new F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>>() {
        @Override
        public TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> f(TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> treeZipper) {
            /**
             * Extracts values from the node
             */
            P3<Predicate, List<CQIE>, Option<Function>> label = treeZipper.getLabel();
            List<CQIE> initialRules = label._2();
            Option<Function> optionalNewTypeAtom = label._3();

            /**
             * No type atom proposed, nothing to change.
             */
            if (optionalNewTypeAtom.isNone())
                return treeZipper;
            /**
             * Otherwise, applies the proposed types
             * and returns the updated tree zipper.
             */
            else {
                List<CQIE> newRules = applyTypeToRules(initialRules, optionalNewTypeAtom.some());
                return treeZipper.setLabel(P.p(label._1(), newRules, Option.<Function>none()));
            }
        }
    };

    /**
     * Removes types from the rules of the current node.
     */
    private static F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> removeTypeFunction
            = new F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>>() {
        @Override
        public TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> f(TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> treeZipper) {
            P3<Predicate, List<CQIE>, Option<Function>> label = treeZipper.getLabel();
            List<CQIE> initialRules = label._2();
            List<CQIE> updatedRules = removeTypesFromRules(initialRules);
            return treeZipper.setLabel(P.p(label._1(), updatedRules, Option.<Function>none()));
        }
    };


}
