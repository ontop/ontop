package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fj.*;
import fj.data.*;
import fj.data.HashMap;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.TypePropagatingSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions.*;
import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.TypePropagatingSubstitution.forceVariableReuse;

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

    /**
     * Thrown after receiving an SubstitutionException.
     *
     * This indicates that the predicate for which the type propagation
     * has been tried should be considered as multi-typed.
     */
    protected static class MultiTypeException extends Exception {
    }

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
        TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> initialRootZipper = TreeZipper.fromTree(
                initialDatalogProgram.getP3RuleTree());

        /**
         * Navigates into the tree until reaching the leftmost leaf.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> leftmostTreeZipper =
                navigateToLeftmostLeaf(initialRootZipper);


        /**
         * Makes sure the multi-typed predicate index is complete.
         * (This step could be disabled in the future once the previous unfolding will be safe enough).
         */
        multiTypedFunctionSymbolIndex = updateMultiTypedFunctionSymbolIndex(initialDatalogProgram, multiTypedFunctionSymbolIndex);

        /**
         * Computes a new Datalog program by applying type lifting.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> newTreeZipper = liftTypesOnTreeZipper(
                leftmostTreeZipper, multiTypedFunctionSymbolIndex);
        TreeBasedDatalogProgram newDatalogProgram = TreeBasedDatalogProgram.fromP3RuleTree(newTreeZipper.toTree());

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
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> navigateToLeftmostLeaf(
            TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> currentZipper) {

        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> optionalFirstChild = currentZipper.firstChild();
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
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> liftTypesOnTreeZipper(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> initialTreeZipper,
            final Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {

        /**
         * Non-final variable (will be re-assigned) multiple times.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> currentZipper = initialTreeZipper;
        /**
         * Iterates over all the predicates (exactly one time for each predicate)
         * in a topological order so that no parent is evaluated before its children.
         *
         * According this order, the last node to be evaluated is the root.
         * This loop breaks after the evaluation of the latter.
         *
         */
        while (true) {
            /**
             * Main operation: updates the current node and its children.
             */
            currentZipper = updateSubTree(currentZipper, multiTypedFunctionSymbolIndex);

            /**
             * Moves to the leftmost leaf of the right sibling if possible.
             */
            final Option<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> optionalRightSibling = currentZipper.right();
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
                final Option<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> optionalParent = currentZipper.parent();
                if (optionalParent.isSome()) {
                    currentZipper = currentZipper.parent().some();
                }
                /**
                 * The root has been reached.
                 * Applies its proposal and breaks the loop.
                 * TODO: update this comment.
                 */
                else {
                    //TODO: remove this line! Types are systematically applied.
                    //currentZipper = applyTypeFunction.f(currentZipper);
                    break;
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
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> updateSubTree(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> currentZipper,
            final Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

        Predicate currentPredicate = currentZipper.getLabel()._1();

        /**
         * If there is no multi-typing problem, tries to lift the type from the children.
         */
        boolean isMultiTyped = multiTypedFunctionSymbolIndex.containsKey(currentPredicate);
        if (!isMultiTyped) {
            try {
                TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> newTreeZipper = liftTypeFromChildrenToParent(currentZipper);
                return newTreeZipper;
            }
            /**
             * Multi-typing conflict detected during type lifting.
             * The latter operation is thus rejected (and has produced no side-effect).
             */
            catch(MultiTypeException ex) {
            }
        }
//        /**
//         * Fallback strategy in reaction to multi-typing (of the current predicate).
//         *
//         * No new type should be given to the current node.
//         * Children must apply their type proposals to themselves.
//         */
//        TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> newTreeZipper = applyToChildren(applyTypeFunction, currentZipper);
//        return newTreeZipper;
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
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> liftTypeFromChildrenToParent(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> parentZipper) throws MultiTypeException {

        /**
         * Children proposals. At most one type proposal per child predicate.
         */
        final HashMap<Predicate, TypeProposal> childProposalIndex = retrieveChildrenProposals(parentZipper);

        /**
         * Main operations:
         *  (i) makes a type proposal for the current (parent) predicate,
         *  (ii) updates body atoms (compatible with the future un-typed child heads).
         * May throw a MultiTypeException.
         *
         * Note that this data structure is partially inconsistent (needs the child heads to be updated).
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> partiallyUpdatedTreeZipper =
                proposeTypeAndUpdateBodies(parentZipper, childProposalIndex);

        /**
         * Removes child types.
         */
        final TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> untypedChildrenZipper = applyToChildren(
                removeTypeFunction, partiallyUpdatedTreeZipper);

        /**
         * Now the tree zipper is consistent :)
         */
        return untypedChildrenZipper;
    }


//    /**
//     * Updates the rule bodies of the parent node according to possible arity changes of child heads.
//     *
//     * Typically, arity changes are caused by MultiVariableUriTemplateTypeProposals.
//     *
//     * TODO: REMOVE or completely transform.
//     *
//     */
//    @Deprecated
//    private static TreeZipper<P3<Predicate,List<CQIE>,Option<TypeProposal>>> propagateChildArityChangesToBodies(
//            final TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> parentZipper,
//            final HashMap<Predicate, TypeProposal> childProposalIndex) {
//
//        final P3<Predicate, List<CQIE>, Option<TypeProposal>> parentLabel = parentZipper.getLabel();
//        final List<CQIE> initialParentRules = parentLabel._2();
//
//        /**
//         * For each child proposal, we update the parentRules.
//         * This sub-task is forwarded to the TypeProposal object.
//         *
//         * From a FP point of view, this is a (left) folding operation.
//         */
//        final List<TypeProposal> childProposals = childProposalIndex.values();
//        List<CQIE> updatedRules = childProposals.foldLeft(new F2<List<CQIE>, TypeProposal, List<CQIE>>() {
//            @Override
//            public List<CQIE> f(List<CQIE> parentRules, TypeProposal childProposal) {
//                return childProposal.propagateChildArityChangeToBodies(parentRules);
//            }
//        }, initialParentRules);
//
//        /**
//         * Returns the updated parent zipper with updated definition rules.
//         */
//        return parentZipper.setLabel(P.p(parentLabel._1(), updatedRules, parentLabel._3()));
//    }

    /**
     * Proposes a typed atom for the current (parent) predicate.
     *
     * This proposal is done by looking at (i) the children proposals
     * and (ii) the rules defining the parent predicate.
     *
     * If the multi-typing problem is detected, throws a MultiTypeException.
     *
     * Returns the type proposal.
     *
     * TODO: update this comment
     * Please note that the returned tree zipper IS NOT FULLY CONSISTENT
     *
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> proposeTypeAndUpdateBodies(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> parentZipper,
            final HashMap<Predicate, TypeProposal> childProposalIndex)
            throws MultiTypeException {

        /**
         * Aggregates all these proposals according to the rules defining the parent predicate into a PredicateSubstitution.
         *
         * If such aggregation is not possible, a MultiTypeException will be thrown.
         *
         *
         */
        final P3<Predicate,List<CQIE>,Option<TypeProposal>> parentLabel = parentZipper.getLabel();
        final List<CQIE> parentRules = parentLabel._2();
        final PredicateLevelSubstitution predicateLevelSubstitution = proposeSubstitutionFromRulesAndChildProposals(parentRules, childProposalIndex);

        /**
         * Makes a TypeProposal by applying the substitution to the head of one rule.
         */
        final Function newFunctionProposal = (Function) parentRules.head().getHead().clone();
        // Side-effect!
        UnifierUtilities.applyUnifier(newFunctionProposal, predicateLevelSubstitution.getGlobalSubstitution());
        final TypeProposal newProposal = constructTypeProposal(newFunctionProposal);

        /**
         * Updated rules: type is applied to these rules (heads and bodies).
         */
        final List<CQIE> updatedParentRules = predicateLevelSubstitution.getUpdatedRules();


        /**
         * Returns a new zipper with the updated label.
         * Note that this tree zipper is not fully consistent (child heads not yet updated).
         */
        return parentZipper.setLabel(P.p(parentLabel._1(), updatedParentRules, Option.some(newProposal)));
    }

    /**
     * Constructs a TypeProposal of the proper type.
     */
    private static TypeProposal constructTypeProposal(Function functionalProposal) {
        /**
         * Special case: multi-variate URI template.
         */
        if (containsMultiVariateURITemplate(functionalProposal)) {
            return new MultiVariateUriTemplateTypeProposal(functionalProposal);
        }
        /**
         * Default case
         */
        return new BasicTypeProposal(functionalProposal);
    }

    /**
     * TODO: comment it!
     */
    private static PredicateLevelSubstitution proposeSubstitutionFromRulesAndChildProposals(List<CQIE> parentRules,
                                                                                       HashMap<Predicate, TypeProposal> childProposalIndex)
            throws MultiTypeException {
        return proposeSubstitutionFromRulesAndChildProposals(Option.<Unifier>none(), parentRules, List.<RuleLevelSubstitution>nil(), childProposalIndex);
    }

    /**
     * TODO: describe it!
     *
     */
    private static PredicateLevelSubstitution proposeSubstitutionFromRulesAndChildProposals(Option<Unifier> optionalSubstitution, List<CQIE> remainingRules, List<RuleLevelSubstitution> ruleSubstitutions,
                                                                                       HashMap<Predicate, TypeProposal> childProposalIndex) throws MultiTypeException {
        /**
         * Stop condition (no more rule to consider).
         */
        if (remainingRules.isEmpty()) {
            if (optionalSubstitution.isNone())
                throw new IllegalArgumentException("Do not give a None head with an empty list of rules");
            return new PredicateLevelSubstitution(ruleSubstitutions, optionalSubstitution.some());
        }

        CQIE rule = remainingRules.head();

        /**
         * TODO: describe
         */
        RuleLevelSubstitution newRuleLevelSubstitution = new RuleLevelSubstitution(rule, childProposalIndex);

        /**
         * TODO: describe this part
         *
         * TODO: Analyse the assumption made: the union of the substitution proposed by the rules makes sense.
         *
         */
        Option<Unifier> proposedSubstitution;
        if (optionalSubstitution.isNone()) {
            proposedSubstitution = Option.some(newRuleLevelSubstitution.getSubstitution());
        }
        else {
            try {
                proposedSubstitution = Option.some(union(optionalSubstitution.some(), newRuleLevelSubstitution.getSubstitution()));
            }
            /**
             * Impossible to propagate type.
             * This happens when multiple types are proposed for this predicate.
             */
            catch(SubstitutionException e) {
                throw new MultiTypeException();
            }
        }

        List<RuleLevelSubstitution> newRuleSubstitutionList =  ruleSubstitutions.append(List.cons(newRuleLevelSubstitution,
                List.<RuleLevelSubstitution>nil()));

        /**
         * Tail recursion
         */
        return proposeSubstitutionFromRulesAndChildProposals(proposedSubstitution, remainingRules.tail(),
                newRuleSubstitutionList, childProposalIndex);
    }


    /**
     * Low-level function.
     *
     * The goal is to build a substitution function
     * that would be able to transfer the proposed types (given by the proposedAtom)
     * to the local atom.
     *
     *
     * One sensitive constraint here is to propagate types without changing the
     * variable names.
     *
     * If such a substitution function does not exist, throws a SubstitutionException.
     *
     * TODO: keep it here or move it?
     *
     */
    protected static TypePropagatingSubstitution computeTypePropagatingSubstitution(Function localAtom, TypeProposal proposal)
            throws SubstitutionException {
        /**
         * Type propagating substitution function between the proposedAtom and the localAtom.
         *
         * TODO: make the latter function throw the exception.
         */
        TypePropagatingSubstitution typePropagatingSubstitutionFunction = TypePropagatingSubstitution.createTypePropagatingSubstitution(
                proposal, localAtom, ImmutableMultimap.<Predicate, Integer>of());

        /**
         * Impossible to unify the multiple types proposed for this predicate.
         */
        if (typePropagatingSubstitutionFunction == null) {
            throw new SubstitutionException();
        }

        /**
         * The current substitution function may change variable names because they were not the same in the two atoms.
         *
         * Here, we are just interested in the types but we do not want to change the variable names.
         * Thus, we force variable reuse.
         */
        TypePropagatingSubstitution renamedSubstitutions = forceVariableReuse(typePropagatingSubstitutionFunction);

        return renamedSubstitutions;
    }

    /**
     * Applies the type proposal to the rule heads.
     *
     * Returns updated rules.
     */
    private static List<CQIE> applyTypeToRules(final List<CQIE> initialRules, final TypeProposal typeProposal)
            throws TypeApplicationError{
        return typeProposal.applyType(initialRules);
    }

    /**
     * Propagates type from a typeProposal to one head atom.
     */
    protected static Function applyTypeProposal(Function headAtom, TypeProposal typeProposal) throws SubstitutionException {
        Unifier substitutionFunction = computeTypePropagatingSubstitution(headAtom, typeProposal);

        // Mutable object
        Function newHead = (Function) headAtom.clone();
        // Limited side-effect
        UnifierUtilities.applyUnifier(newHead, substitutionFunction);

        return newHead;
    }

    /**
     * Removes types from rules.
     *
     * Returns updated rules.
     */
    private static List<CQIE> removeTypesFromRules(final List<CQIE> initialRules, final TypeProposal typeProposal) {
        return typeProposal.removeType(initialRules);
    }

    /**
     * Makes a type proposal by looking at the rules defining the current predicate.
     *
     * Its current implementation is very basic and could be improved.
     * It returns the head of the first rule.
     *
     * TODO: Several improvements could be done:
     *  1. Unifying all the rule heads (case where is there is multiple rules).
     *  2. Detecting if no type is present in the proposal and returning a None in
     *     this case.
     */
    @Deprecated
    private static Option<TypeProposal> proposeTypeFromLocalRules(TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> currentZipper) {
        List<CQIE> currentRules = currentZipper.getLabel()._2();
        if (currentRules.isNotEmpty()) {

            // Head of the first rule (cloned because mutable).
            Function proposedHead = (Function) currentRules.head().getHead().clone();

            TypeProposal typeProposal;

            /**
             * Multi-variate URI template case.
             */
            if (containsMultiVariateURITemplate(proposedHead)) {
                //TODO: call the right constructor
                typeProposal = null;
            }
            /**
             * Normal case
             */
            else {
                typeProposal = new BasicTypeProposal(proposedHead);
            }
            return Option.some(typeProposal);
        }
        return Option.none();
    }

    /**
     * TODO: describe
     *
     */
    protected static boolean containsMultiVariateURITemplate(Function atom) {
        for(Term term : atom.getTerms()) {
            if (isMultiVariateURITemplate(term))
                return true;
        }
        return false;
    }



    /**
     * Uri-templates using more than one variable.
     */
    protected static boolean isMultiVariateURITemplate(Term term) {
        if (!(term instanceof Function))
            return false;

        Function functionalTerm = (Function) term;

        if (functionalTerm.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI)) {
            return functionalTerm.getTerms().size() <= 2;
        }
        return false;
    }

    /**
     * Indexes the proposals of the children of the current parent node according to their predicate.
     *
     * Returns the index.
     */
    private static HashMap<Predicate, TypeProposal> retrieveChildrenProposals(final TreeZipper<P3<Predicate, List<CQIE>,
            Option<TypeProposal>>> parentZipper) {
        /**
         * Child forest.
         */
        Stream<Tree<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> subForest = parentZipper.focus().subForest()._1();
        /**
         * No child: returns an empty map.
         */
        if (subForest.isEmpty()) {
            return HashMap.from(Stream.<P2<Predicate, TypeProposal>>nil());
        }

        /**
         * Children labels (roots of the child forest).
         */
        Stream<P3<Predicate, List<CQIE>, Option<TypeProposal>>> childrenLabels =  subForest.map(
                Tree.<P3<Predicate, List<CQIE>, Option<TypeProposal>>>root_());

        Stream<Option<TypeProposal>> proposals = childrenLabels.map(P3.<Predicate, List<CQIE>, Option<TypeProposal>>__3());

        /**
         * Only positive proposals.
         */
        List<TypeProposal> positiveProposals = Option.somes(proposals).toList();

        /**
         * Computes equivalent predicate index (generic method).
         *
         */
        HashMap<Predicate, List<TypeProposal>> predicateIndex = buildPredicateIndex(positiveProposals);

        /**
         * Because only one proposal can be made per predicate (child),
         * the structure of this predicate index can be simplified.
         *
         * Returns this simplified index.
         */
        HashMap<Predicate, TypeProposal> simplifiedPredicateIndex = predicateIndex.map(new F<P2<Predicate, List<TypeProposal>>, P2<Predicate, TypeProposal>>() {
            @Override
            public P2<Predicate, TypeProposal> f(P2<Predicate, List<TypeProposal>> mapEntry) {
                List<TypeProposal> proposals = mapEntry._2();
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
     * Applies a function to the children.
     *
     * Returns the updated tree zipper at the parent position.
     */
    private static TreeZipper<P3<Predicate,List<CQIE>,Option<TypeProposal>>> applyToChildren(
            F<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> f,
            TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> parentZipper) {
        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> optionalFirstChild = parentZipper.firstChild();

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
        TreeZipper<P3<Predicate,List<CQIE>,Option<TypeProposal>>> lastChildZipper = applyToNodeAndRightSiblings(f, optionalFirstChild.some());

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
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> applyToNodeAndRightSiblings(
            F<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> f,
            TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> currentZipper) {
        /**
         * Applies f to the current node
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> updatedCurrentZipper = f.f(currentZipper);

        /**
         * Looks for the right sibling
         */
        Option<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> optionalRightSibling = updatedCurrentZipper.right();
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
    private static F<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> applyTypeFunction
            = new F<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>>() {
        @Override
        public TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> f(TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> treeZipper) {
            /**
             * Extracts values from the node
             */
            P3<Predicate, List<CQIE>, Option<TypeProposal>> label = treeZipper.getLabel();
            List<CQIE> initialRules = label._2();
            Option<TypeProposal> optionalNewTypeAtom = label._3();

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
                return treeZipper.setLabel(P.p(label._1(), newRules, Option.<TypeProposal>none()));
            }
        }
    };

    /**
     * Removes types from the rules of the current node if the latter has made a proposal in the past.
     *
     * If no proposal has been done before, it is maybe because some types should remain local.
     */
    private static F<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>> removeTypeFunction
            = new F<TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>>>() {
        @Override
        public TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> f(TreeZipper<P3<Predicate, List<CQIE>, Option<TypeProposal>>> treeZipper) {
            P3<Predicate, List<CQIE>, Option<TypeProposal>> label = treeZipper.getLabel();
            Option<TypeProposal> optionalTypeProposal = label._3();
            /**
             * If no previous proposal, no type removal.
             */
            if (optionalTypeProposal.isNone()) {
                return treeZipper;
            }
            /**
             * Otherwise, remove types.
             */
            else {
                List<CQIE> initialRules = label._2();
                List<CQIE> updatedRules = removeTypesFromRules(initialRules, optionalTypeProposal.some());
                return treeZipper.setLabel(P.p(label._1(), updatedRules, Option.<TypeProposal>none()));
            }
        }
    };


    /**
     * Generic method that indexes a list of proposals according to their head predicates.
     */
    private static HashMap<Predicate, List<TypeProposal>> buildPredicateIndex(List<TypeProposal> atoms) {
        List<P2<Predicate, List<TypeProposal>>> predicateAtomList = atoms.group(
                /**
                 * Groups by predicate
                 */
                Equal.equal(new F<TypeProposal, F<TypeProposal, Boolean>>() {
                    @Override
                    public F<TypeProposal, Boolean> f(final TypeProposal typeProposal) {
                        return new F<TypeProposal, Boolean>() {
                            @Override
                            public Boolean f(TypeProposal other) {
                                return other.getPredicate().equals(typeProposal.getPredicate());
                            }
                        };
                    }
                })).map(
                /**
                 * Transforms it into a P2 list (predicate and list of functions).
                 */
                new F<List<TypeProposal>, P2<Predicate, List<TypeProposal>>>() {
                    @Override
                    public P2<Predicate, List<TypeProposal>> f(List<TypeProposal> proposals) {
                        return P.p(proposals.head().getPredicate(), proposals);
                    }
                });

        return HashMap.from(predicateAtomList);
    }

    /**
     * Sometimes rule bodies contains algebra functions (e.g. left joins).
     * These should not be considered as atoms.
     *
     * These method makes sure only real (non algebra) atoms are returned.
     * Some of these atoms may be found inside algebra functions.
     *
     */
    private static List<Function> extractBodyAtoms(CQIE rule) {
        List<Function> directBody = List.iterableList(rule.getBody());

        return List.join(directBody.map(new F<Function, List<Function>>() {
            @Override
            public List<Function> f(Function functionalTerm) {
                return extractAtoms(functionalTerm);
            }
        }));
    }

    /**
     * Extracts real atoms from a functional term.
     *
     * If this functional term is not algebra, it is an atom and is
     * thus directly returned.
     *
     * Otherwise, looks for atoms recursively by looking
     * at the functional sub terms for the algebra function.
     *
     * Recursive function.
     */
    private static List<Function> extractAtoms(Function functionalTerm) {
        /**
         * Normal case: not an algebra function (e.g. left join).
         */
        if (!functionalTerm.isAlgebraFunction()) {
            return List.cons(functionalTerm, List.<Function>nil());
        }

        /**
         * Sub-terms that are functional.
         */
        List<Function> subFunctionalTerms = List.iterableList(functionalTerm.getTerms()).filter(new F<Term, Boolean>() {
            @Override
            public Boolean f(Term term) {
                return term instanceof Function;
            }
        }).map(new F<Term, Function>() {
            @Override
            public Function f(Term term) {
                return (Function) term;
            }
        });

        /**
         * Recursive call over these functional sub-terms.
         * The atoms they returned are then joined.
         * Their union is then returned.
         */
        return List.join(subFunctionalTerms.map(new F<Function, List<Function>>() {
            @Override
            public List<Function> f(Function functionalTerm) {
                return extractAtoms(functionalTerm);
            }
        }));

    }


    /**
     * Looks for predicates are not yet declared as multi-typed (while they should).
     *
     * This tests relies on the ability of rules defining one predicate to be unified.
     *
     * This class strongly relies on the assumption that the multi-typed predicate index is complete.
     * This method offers such a protection against non-detections by previous components.
     */
    protected static Multimap<Predicate, Integer> updateMultiTypedFunctionSymbolIndex(final TreeBasedDatalogProgram initialDatalogProgram,
                                                                                      final Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {
        // Mutable index (may be updated)
        final Multimap<Predicate, Integer> newIndex = ArrayListMultimap.create(multiTypedFunctionSymbolIndex);

        final Stream<P2<Predicate, List<CQIE>>> ruleEntries = Stream.iterableStream(initialDatalogProgram.getRuleTree());
        /**
         * Applies the following effect on each rule entry:
         *   If the predicate has not been declared as multi-typed, checks if it really is.
         *
         *   When a false negative is detected, adds it to the index (side-effect).
         */
        ruleEntries.foreach(new Effect<P2<Predicate, List<CQIE>>>() {
            @Override
            public void e(P2<Predicate, List<CQIE>> ruleEntry) {
                Predicate predicate = ruleEntry._1();
                if (multiTypedFunctionSymbolIndex.containsKey(predicate))
                    return;

                List<CQIE> rules = ruleEntry._2();
                if (isMultiTypedPredicate(rules)) {
                    // TODO: Is there some usage for this count?
                    int count = 1;
                    newIndex.put(predicate, count);
                }
            }
        });
        return newIndex;
    }

    /**
     * Tests if the rules defining one predicate cannot be unified
     * because they have different types.
     *
     * Returns true if the predicate is detected as multi-typed.
     */
    private static boolean isMultiTypedPredicate(List<CQIE> predicateDefinitionRules) {
        if (predicateDefinitionRules.length() <= 1)
            return false;

        Function headFirstRule = predicateDefinitionRules.head().getHead();

        return isMultiTypedPredicate(new BasicTypeProposal(headFirstRule), predicateDefinitionRules.tail());
    }

    /**
     * Tail recursive sub-method that "iterates" over the rules.
     */
    private static boolean isMultiTypedPredicate(TypeProposal currentTypeProposal, List<CQIE> remainingRules) {
        if (remainingRules.isEmpty())
            return false;

        Function ruleHead = remainingRules.head().getHead();
        try {
            Function newType = applyTypeProposal(ruleHead, currentTypeProposal);

            // Tail recursion
            return isMultiTypedPredicate(new BasicTypeProposal(newType), remainingRules.tail());
            /**
             * Multi-type problem detected
             */
        } catch (SubstitutionException e) {
            return true;
        }
    }
}
