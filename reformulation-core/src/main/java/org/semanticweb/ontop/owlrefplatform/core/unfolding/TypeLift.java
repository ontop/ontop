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

    /**
     * Type lifting implementation based on tree zippers (persistent data structures).
     *
     * @param inputRules
     * @param multiTypedFunctionSymbolIndex
     * @return
     */
    public static java.util.List<CQIE> liftTypes(java.util.List<CQIE> inputRules,
                                       Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {
        /*
         * Builds a tree zipper from the input Datalog rules.
         */
        TreeBasedDatalogProgram initialDatalogProgram = new TreeBasedDatalogProgram(inputRules);
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> initialRootZipper = TreeZipper.fromTree(
                initialDatalogProgram.computeRuleTree());

        /*
         * Navigates into the zipper until reaching a leaf (that is not the root).
         */
        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalLeafZipper =
                initialRootZipper.findChild(isLeafFunction);

        //TODO: go to the leftmost sibling of this rule (and its leaf if necessary).

        /**
         * Computes a new Datalog program by applying type lifting if possible
         */
        TreeBasedDatalogProgram newDatalogProgram;
        /*
         * Tree with only one node --> no modification
         */
        if (optionalLeafZipper.isNone()) {
            newDatalogProgram = initialDatalogProgram;
        }
        /*
         * Otherwise, applies type lifting and creates a new Datalog program
         */
        else {
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> newTreeZipper = liftTypesOnTreeZipper(
                    optionalLeafZipper.some(), multiTypedFunctionSymbolIndex);
            newDatalogProgram = new TreeBasedDatalogProgram(newTreeZipper);
        }

        return new ArrayList<>(newDatalogProgram.getRules().toCollection());
    }

    /**
     * We use here an imperative loop instead of a function
     * because the tail-recursion optimization is apparently still not supported in Java 7.
     *
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> liftTypesOnTreeZipper(
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> initialTreeZipper,
            Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {

        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper = initialTreeZipper;
        while (true) {
            currentZipper = updateSubTree(currentZipper, multiTypedFunctionSymbolIndex);

            // / Move to the right if possible or to the parent otherwise
            Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalRightSibling = currentZipper.right();
            if (optionalRightSibling.isSome()) {
                currentZipper = optionalRightSibling.some();
                /**
                 * If the right sibling is not a leaf, reaches its first child leaf.
                 */
                if (!currentZipper.isLeaf()) {
                    currentZipper = currentZipper.findChild(isLeafFunction).some();
                }
            }
            else {
                Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalParent = currentZipper.parent();
                if (optionalParent.isSome()) {
                    currentZipper = currentZipper.parent().some();
                }
                /**
                 * The root has been reached.
                 */
                else {
                    //TODO: apply the proposal to the root.
                    break;
                }
            }
        }
        return currentZipper;
    }

    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> updateSubTree(
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper,
            Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

        P3<Predicate, List<CQIE>, Option<Function>> currentLabel = currentZipper.getLabel();
        Predicate currentPredicate = currentLabel._1();

        boolean isMultiTyped = multiTypedFunctionSymbolIndex.containsKey(currentPredicate);

        if (isMultiTyped) {
            currentZipper = applyTypeToRules(currentZipper);
        }
        else {
            currentZipper = liftTypeFromChildrenToParent(currentZipper);
        }
        return null;
    }

    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> liftTypeFromChildrenToParent(
            TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper) {
        return null;
    }

    /**
     * Low-level. Applies the type to the rules of the current predicate.
     * @param treeZipper
     * @return The updated zipper at the location.
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> applyTypeToRules(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> treeZipper) {

        /*
         * Extract
         */
        P3<Predicate, List<CQIE>, Option<Function>> label = treeZipper.getLabel();
        Predicate predicate = label._1();
        List<CQIE> initialRules = label._2();
        Option<Function> optionalNewTypeAtom = label._3();

        /**
         * No type atom proposed, nothing to change
         */
        if (optionalNewTypeAtom.isNone())
            return treeZipper;
        /**
         * Otherwise, applies the proposed types
         * and returns the updated tree zipper.
         */
        else {
            List<CQIE> newRules = applyTypeToRules(initialRules, optionalNewTypeAtom.some());
            return treeZipper.setLabel(P.p(predicate, newRules, Option.<Function>none()));
        }
    }

    private static List<CQIE> applyTypeToRules(List<CQIE> initialRules, Function some) {
        //TODO: implement it
        return null;
    }

    private static List<CQIE> unTypeRules(List<CQIE> initialRules) {
        //TODO: implement it
        return null;
    }

    /**
     * TODO: implement
     */
    private static Option<Function> proposeHead(TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper) {
        return null;
    }

    /**
     * May not be an efficient search (if no a Deep First Search)
     * TODO: investigate
     */
    private static F<Tree<P3<Predicate, List<CQIE>, Option<Function>>>, Boolean> isLeafFunction =
            new F<Tree<P3<Predicate, List<CQIE>, Option<Function>>>, Boolean>() {
        @Override
        public Boolean f(Tree<P3<Predicate, List<CQIE>, Option<Function>>> tree) {
            return tree.subForest()._1().isEmpty();
        }
    };
}
