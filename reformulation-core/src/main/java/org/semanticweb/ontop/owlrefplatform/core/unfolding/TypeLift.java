package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fj.*;
import fj.data.*;
import fj.data.HashMap;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.HashSet;
import java.util.Set;

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
 */
public class TypeLift {

    /**
     * Happens when unification of proposals and rules
     * is not possible.
     */
    private static class UnificationException extends Exception {
    }

    /**
     * Thrown after receiving an UnificationException.
     * This indicates that the predicate for which the unification
     * has been tried should be considered as multi-typed.
     */
    private static class MultiTypeException extends Exception {
    }

    /**
     * Thrown when an UnificationException happens when trying
     * to apply a type proposal to a set of rules.
     *
     * This error should not be expected (as such).
     */
    private static class TypeApplicationError extends RuntimeException {
    }

    private static Logger LOGGER = LoggerFactory.getLogger(TypeLift.class);

    /**
     * Type lifting implementation based on tree zippers (persistent data structures).
     *
     * @param inputRules Original rules.
     * @param multiTypedFunctionSymbolIndex  Index indicating which predicates are known as multi-typed.
     * @return New list of rules.
     */
    public static java.util.List<CQIE> liftTypes(java.util.List<CQIE> inputRules,
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
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> initialRootZipper = TreeZipper.fromTree(
                initialDatalogProgram.getP3RuleTree());

        /**
         * Navigates into the tree until reaching the leftmost leaf.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> leftmostTreeZipper =
                navigateToLeftmostLeaf(initialRootZipper);

        /**
         * Computes a new Datalog program by applying type lifting.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> newTreeZipper = liftTypesOnTreeZipper(
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
     * because:
     *   (i) the tail-recursion optimization is apparently still not supported in Java 8.
     *   (ii) the recursion is too profound (equal to the number of predicates).
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> liftTypesOnTreeZipper(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> initialTreeZipper,
            final Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {

        /**
         * Non-final variable (will be re-assigned) multiple times.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper = initialTreeZipper;
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
            final Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalRightSibling = currentZipper.right();
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
                final Option<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> optionalParent = currentZipper.parent();
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
     * Updates the current node and its children.
     *
     * Type lifting is forbidden if the current predicate is
     * already multi-typed or if the child proposals would
     * make it multi-typed.
     *
     * Returns the updated treeZipper at the same position.
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> updateSubTree(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper,
            final Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

        Predicate currentPredicate = currentZipper.getLabel()._1();

        /**
         * If there is no multi-typing problem, tries to lift the type from the children.
         */
        boolean isMultiTyped = multiTypedFunctionSymbolIndex.containsKey(currentPredicate);
        if (!isMultiTyped) {
            try {
                TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> newTreeZipper = liftTypeFromChildrenToParent(currentZipper);
                return newTreeZipper;
            }
            /**
             * Multi-typing conflict detected during type lifting.
             * The latter operation is thus rejected (and has produced no side-effect).
             */
            catch(MultiTypeException ex) {
            }
        }
        /**
         * Fallback strategy in reaction to multi-typing (of the current predicate).
         *
         * No new type should be given to the current node.
         * Children must apply their type proposals to themselves.
         */
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> newTreeZipper = applyToChildren(applyTypeFunction, currentZipper);
        return newTreeZipper;
    }

    /**
     * Lifts types from the children to the current parent node.
     *
     * This operation fails if the children proposals indicate that the parent predicate is multi-typed.
     * In such a case, a MultiTypeException is thrown.
     *
     * Returns an updated tree zipper at the same position.
     */
    private static TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> liftTypeFromChildrenToParent(
            final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> parentZipper) throws MultiTypeException {
        /**
         * Main operation: makes a type proposal for the current (parent) predicate.
         * May throw a MultiTypeException.
         */
        Option<Function> parentProposal = proposeType(parentZipper);

        /**
         * If no type has been proposed by the children nor by the node itself,
         * no need to remove types from the children rules.
         */
        if (parentProposal.isNone()) {
            return parentZipper;
        }


        /**
         * Removes types from the children rules (as much as possible).
         */
        final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> cleanedZipper = applyToChildren(removeTypeFunction, parentZipper);

        /**
         * Sets the proposal to the parent node.
         */
        final P3<Predicate, List<CQIE>, Option<Function>> parentLabel = cleanedZipper.getLabel();
        // Non final (may be re-affected in a special case).
        TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> newTreeZipper =
                cleanedZipper.setLabel(P.p(parentLabel._1(), parentLabel._2(), parentProposal));

        /**
         * Special case: if the proposal is not supported for type lifting, applies it directly to the parent node.
         * It will then not appear as a proposal to the grand-parent node.
         *
         * TODO: discuss about it to make sure it is sound.
         */
        if (!isSupported(parentProposal.some())) {
            newTreeZipper = applyTypeFunction.f(newTreeZipper);
        }

        return newTreeZipper;
    }

    /**
     * Proposes a typed atom for the current (parent) predicate.
     *
     * This proposal is done by looking at (i) the children proposals
     * and (ii) the rules defining the parent predicate.
     *
     * If the multi-typing problem is detected, throws a MultiTypeException.
     *
     * Returns the type proposal.
     */
    private static Option<Function> proposeType(final TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> parentZipper)
            throws MultiTypeException {

        /**
         * Children proposals. At most one type proposal per child predicate.
         */
        final HashMap<Predicate, Function> childProposalIndex = retrieveChildrenProposals(parentZipper);

        /**
         * If there is no child proposal, no need to unify.
         * Builds and returns a proposal just by looking at the rules defining the parent predicate.
         */
        if (childProposalIndex.isEmpty()) {
            return proposeTypeFromLocalRules(parentZipper);
        }

        /**
         * Unifies all these proposals according to the rules defining the parent predicate.
         *
         * If such unification is not possible, a MultiTypeException will be thrown.
         *
         * Returns the unified proposal.
         */
        final List<CQIE> parentRules = parentZipper.getLabel()._2();
        final Function newProposal = unifyChildrenProposalsAndRules(Option.<Function>none(), parentRules, childProposalIndex);

        return Option.some(newProposal);
    }

    /**
     * Tail-recursive method "iterating" over the rules defining the parent predicate.
     * In most case, there is just one of these rules.
     *
     * Returns a type proposal by unifying:
     *   - the current rule,
     *   - the children proposals,
     *   - the proposal coming from the "iteration" over the previous rules.
     */
    private static Function unifyChildrenProposalsAndRules(Option<Function> optionalProposal, List<CQIE> remainingRules,
                                                           HashMap<Predicate, Function> childProposalIndex)
            throws MultiTypeException {
        /**
         * Stop condition (no more rule to consider).
         */
        if (remainingRules.isEmpty()) {
            if (optionalProposal.isNone()) {
                throw new IllegalArgumentException("Do not give a None head with an empty list of rules");
            }
            /**
             * Returns the proposal obtained from the previous rules.
             */
            return optionalProposal.some();
        }

        /**
         * Main operation: builds a proposal from the current rule and the children proposals.
         * May throw a MultipleTypeException.
         */
        CQIE rule = remainingRules.head();
        Function proposedHead = unifyRule(rule.getHead(), extractBodyAtoms(rule), childProposalIndex);

        /**
         * Checks if this fresh proposal should be unified with the proposal from the previous rules
         * or not.
         */
        Function newHead;

        /**
         * Not already existing proposal (no previous rule). Most common case.
         */
        if (optionalProposal.isNone()) {
            newHead = proposedHead;
        }
        /**
         * Otherwise, tries to unify with the previous proposal.
         */
        else {
            Function currentHead = optionalProposal.some();
            try {
                newHead = unifyTypes(currentHead, currentHead, proposedHead);
            }
            /**
             * Impossibility to unify here denotes multi-typing.
             *
             * Throws an exception.
             */
            catch(UnificationException e) {
                throw new MultiTypeException();
            }
        }

        /**
         * Tail recursion.
         */
        return unifyChildrenProposalsAndRules(Option.some(newHead), remainingRules.tail(), childProposalIndex);
    }

    /**
     * Tail-recursive method that "iterates" over the body atoms of a given rule defining the parent predicate.
     *
     * For a given body atom, tries to extract a type by unification with the corresponding child proposal.
     * The unifier is then used to update the current proposed head.
     *
     * If unification is impossible, throws a MultiTypeException.
     *
     */
    private static Function unifyRule(final Function currentProposedHead, final List<Function> remainingBodyAtoms,
                                      final HashMap<Predicate,Function> childProposalIndex) throws MultiTypeException {
        /**
         * Stop condition (no further body atom).
         */
        if (remainingBodyAtoms.isEmpty()) {
            return currentProposedHead;
        }

        Function bodyAtom = remainingBodyAtoms.head();
        Option<Function> optionalChildProposal = childProposalIndex.get(bodyAtom.getFunctionSymbol());

        Function newHead;
        /**
         * If there is a child proposal corresponding to the current body atom,
         * use these atoms to update the current proposed head.
         */
        if (optionalChildProposal.isSome()) {
            try {
                newHead = unifyTypes(currentProposedHead, bodyAtom, optionalChildProposal.some());
            }
            /**
             * Impossible to unify.
             * This happens when multiple types are proposed for this predicate.
             */
            catch(UnificationException e) {
                throw new MultiTypeException();
            }
        }
        /**
         * Otherwise, keeps the same proposed head.
         */
        else {
            newHead = currentProposedHead;
        }

        /**
         * Tail recursion
         */
        return unifyRule(newHead, remainingBodyAtoms.tail(), childProposalIndex);
    }

    /**
     * Low-level function.
     *
     * The goal to transfer proposed types (given by the proposedAtom)
     * to the localHead.
     *
     * Like the localHead, the localAtom belongs to the local rule.
     * It should have the same predicate than the proposedAtom (which usually
     * differs from the one of the localHead).
     *
     * One sensitive constraint here is to propagate types without changing the
     * variable names.
     *
     * If the unification could not be achieved, throws a UnificationException.
     */
    private static Function unifyTypes(Function localHead, Function localAtom, Function proposedAtom)
            throws UnificationException{
        /**
         * Most General Unifier between the proposedAtom and the localAtom.
         */
        Map<Variable, Term> directMGU = Unifier.getMGU(proposedAtom, localAtom, true, ImmutableMultimap.<Predicate,Integer>of());

        /**
         * Impossible to unify the multiple types proposed for this predicate.
         */
        if (directMGU == null) {
            throw new UnificationException();
        }

        /**
         * The current MGU may change variable names because they were not the same in the two atoms.
         *
         * Here, we are just interested in the types but we do not want to change the variable names.
         * Thus, we force variable reuse.
         */
        //TODO: re-implement this method without side effect.
        Map<Variable, Term> typingMGU = forceVariableReuse(directMGU);

        //Mutable!!
        Function newHead = (Function)localHead.clone();
        // Side-effect (newHead is updated)
        Unifier.applySelectiveUnifier(newHead, typingMGU);

        return newHead;
    }

    /**
     * Applies the type proposal to the rule heads.
     *
     * Returns updated rules.
     */
    private static List<CQIE> applyTypeToRules(List<CQIE> initialRules, final Function typeProposal)
            throws TypeApplicationError{
        return initialRules.map(new F<CQIE, CQIE>() {
            @Override
            public CQIE f(CQIE initialRule) {
                Function currentHead = initialRule.getHead();
                try {
                    Function newHead = unifyTypes(currentHead, currentHead, typeProposal);
                    // Mutable object
                    CQIE newRule = initialRule.clone();
                    newRule.updateHead(newHead);
                    return newRule;
                    /**
                     * Unification exception should not appear at this level.
                     * There is an inconsistency somewhere.
                     *
                     * Throws a runtime exception (TypeApplicationError)
                     * that should not be expected.
                     */
                } catch(UnificationException e) {
                    throw new TypeApplicationError();
                }
            }
        });
    }

    /**
     * Removes types from rules.
     *
     * Reuses the DatalogUnfolder.getUntypedArgumentFromTerm() static method.
     *
     * Returns updated rules.
     */
    private static List<CQIE> removeTypesFromRules(List<CQIE> initialRules) {
        return initialRules.map(new F<CQIE, CQIE>() {
            @Override
            public CQIE f(CQIE initialRule) {
                Function initialHead = initialRule.getHead();
                List<Term> initialHeadTerms =  List.iterableList(initialHead.getTerms());

                /**
                 * Computes untyped arguments for the head predicate.
                 */
                List<Term> newHeadTerms = initialHeadTerms.map(new F<Term, Term>() {
                    @Override
                    public Term f(Term term) {
                        // TODO: clean the called method
                        return getUntypedArgumentFromTerm(term).get(0);
                    }
                });

                /**
                 * Builds a new rule.
                 * TODO: modernize the CQIE API (make it immutable).
                 */
                CQIE newRule = initialRule.clone();
                Function newHead = (Function)initialHead.clone();
                newHead.updateTerms(new ArrayList<>(newHeadTerms.toCollection()));
                newRule.updateHead(newHead);
                return newRule;
            }
        });
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
    private static Option<Function> proposeTypeFromLocalRules(TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> currentZipper) {
        List<CQIE> currentRules = currentZipper.getLabel()._2();
        if (currentRules.isNotEmpty()) {
            // Head of the first rule (cloned because mutable).
            Function typeProposal = (Function) currentRules.head().getHead().clone();

            if (isSupported(typeProposal)) {
                return Option.some(typeProposal);
            }
        }
        return Option.none();
    }

    /**
     * Some types cannot be lifted for the moment.
     * If such a type is detected in a type proposal, returns false.
     *
     * Unsupported "types":
     *   - URI templates using more than one variable.
     *      Reason: cannot be replaced directly by one variable.
     *
     */
    private static boolean isSupported(Function typeProposal) {
        List<Term> terms = List.iterableList(typeProposal.getTerms());
        /**
         * Makes sure all the terms are supported.
         */
        return terms.forall(new F<Term, Boolean>() {
            /**
             * Support test (for a given term)
             */
            @Override
            public Boolean f(Term term) {
                if (term instanceof Function) {
                    Function functionalTerm = (Function) term;

                    /**
                     * Uri-templates using more than one variable are not supported.
                     */
                    if (functionalTerm.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI)) {
                        return functionalTerm.getTerms().size() <= 2;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Indexes the proposals of the children of the current parent node according to their predicate.
     *
     * Returns the index.
     */
    private static HashMap<Predicate, Function> retrieveChildrenProposals(final TreeZipper<P3<Predicate, List<CQIE>,
            Option<Function>>> parentZipper) {
        /**
         * Child forest.
         */
        Stream<Tree<P3<Predicate, List<CQIE>, Option<Function>>>> subForest = parentZipper.focus().subForest()._1();
        /**
         * No child: returns an empty map.
         */
        if (subForest.isEmpty()) {
            return HashMap.from(Stream.<P2<Predicate, Function>>nil());
        }

        /**
         * Children labels (roots of the child forest).
         */
        Stream<P3<Predicate, List<CQIE>, Option<Function>>> childrenLabels =  subForest.map(
                Tree.<P3<Predicate, List<CQIE>, Option<Function>>>root_());

        Stream<Option<Function>> proposals = childrenLabels.map(P3.<Predicate, List<CQIE>, Option<Function>>__3());

        /**
         * Only positive proposals.
         */
        List<Function> proposedHeads = Option.somes(proposals).toList();

        /**
         * Computes equivalent predicate index (generic method).
         *
         */
        HashMap<Predicate, List<Function>> predicateIndex = buildPredicateIndex(proposedHeads);

        /**
         * Because only one proposal can be made per predicate (child),
         * the structure of this predicate index can be simplified.
         *
         * Returns this simplified index.
         */
        HashMap<Predicate, Function> simplifiedPredicateIndex = predicateIndex.map(new F<P2<Predicate, List<Function>>, P2<Predicate, Function>>() {
            @Override
            public P2<Predicate, Function> f(P2<Predicate, List<Function>> mapEntry) {
                List<Function> proposals = mapEntry._2();
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
     * Removes types from the rules of the current node if the latter has made a proposal in the past.
     *
     * If no proposal has been done before, it is maybe because some types should remain local.
     */
    private static F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>> removeTypeFunction
            = new F<TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>, TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>>>() {
        @Override
        public TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> f(TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> treeZipper) {
            P3<Predicate, List<CQIE>, Option<Function>> label = treeZipper.getLabel();
            Option<Function> typeProposal = label._3();
            /**
             * If no previous proposal, no type removal.
             */
            if (typeProposal.isNone()) {
                return treeZipper;
            }
            /**
             * Otherwise, remove types.
             */
            else {
                List<CQIE> initialRules = label._2();
                List<CQIE> updatedRules = removeTypesFromRules(initialRules);
                return treeZipper.setLabel(P.p(label._1(), updatedRules, Option.<Function>none()));
            }
        }
    };


    /**
     * Generic method that indexes a list of atoms according to their predicates.
     */
    private static HashMap<Predicate, List<Function>> buildPredicateIndex(List<Function> atoms) {
        List<P2<Predicate, List<Function>>> predicateAtomList = atoms.group(
                /**
                 * Groups by predicate
                 */
                Equal.equal(new F<Function, F<Function, Boolean>>() {
                    @Override
                    public F<Function, Boolean> f(final Function atom) {
                        return new F<Function, Boolean>() {
                            @Override
                            public Boolean f(Function other) {
                                return other.getFunctionSymbol().equals(atom.getFunctionSymbol());
                            }
                        };
                    }
                })).map(
                /**
                 * Transforms it into a P2 list (predicate and list of functions).
                 */
                new F<List<Function>, P2<Predicate, List<Function>>>() {
                    @Override
                    public P2<Predicate, List<Function>> f(List<Function> atoms) {
                        return P.p(atoms.head().getFunctionSymbol(), atoms);
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
     * Moved from the previous implementation of lift types (DatalogUnfolder).
     * TODO: improve it
     *
     * Adapts the most general unifier so that it does not change variable names.
     */
    private static Map<Variable, Term> forceVariableReuse(Map<Variable, Term> initialMGU) {
        Map<Variable, Term> mgu = new java.util.HashMap<>(initialMGU);

        Set<Map.Entry<Variable, Term>> entrySet = mgu.entrySet();

        Set<Map.Entry<Variable, Term>> entrySetClone = new HashSet<>();
        for (Map.Entry<Variable, Term> a: entrySet){
            entrySetClone.add(a);
        }

        Iterator<Map.Entry<Variable, Term>> vars = entrySetClone.iterator();
        while (vars.hasNext()) {
            Map.Entry<Variable, Term> pairs = vars.next();

            Variable key = pairs.getKey();
            Term value = pairs.getValue();

            if (value instanceof Function){

                Set<Variable> varset =  value.getReferencedVariables();
                Variable mvar;
                Iterator<Variable> iterator = varset.iterator();
                if (!varset.isEmpty()){
                    Map<Variable, Term> minimgu = new java.util.HashMap<>();
                    mvar = iterator.next();
                    if (varset.size() == 1) {
                        minimgu.put(mvar, key);
                        Unifier.applyUnifier((Function) value, minimgu, false);
                    } else {

                        LOGGER.debug("Multiple vars in Function: "+ varset.toString() + "Type not pushed!-Complete!");

                        mgu.remove(key);
                    }
                } else{ //no variables!
                    LOGGER.debug("This Function has no variables: "+ value + "Type not pushed!-Complete!");

                    mgu.remove(key);
                }
            } else {
                LOGGER.debug("value: "+value.toString());
            }
        }
        return mgu;
    }

    /**
     Moved from the previous implementation of lift types (DatalogUnfolder).
     * TODO: improve it
     *
     * Takes a Term of the form Type(x) and returns the list [x]
     */
    private static java.util.List<Term> getUntypedArgumentFromTerm(Term t) {

        java.util.List<Term> untypedArguments = new ArrayList<>();

        /**
         * Does not untyped aggregations
         */
        if (DatalogUnfolder.detectAggregateInArgument(t)) {
            untypedArguments.add(t);
        }

        else if (t instanceof Function){
            //if it is a function, we add the inner variables and values
            java.util.List<Term> functionArguments = ((Function) t).getTerms();

            Predicate functionSymbol = ((Function) t).getFunctionSymbol();
            boolean isURI = functionSymbol.getName().equals(OBDAVocabulary.QUEST_URI);
            if (isURI && functionArguments.size() >1){
                //I need to remove the URI part and add the rest, usually the variables
                functionArguments.remove(0);
            }
            untypedArguments.addAll(functionArguments);

        }else if(t instanceof Variable){
            untypedArguments.add(t);
        }else if (t instanceof Constant){
            untypedArguments.add(t);
        }
        return untypedArguments;
    }
}
