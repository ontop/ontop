package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ImmutableSet;
import fj.F;
import fj.P2;
import fj.data.*;
import fj.data.HashMap;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.NeutralSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities;

import java.util.*;
import java.util.Set;

import static org.semanticweb.ontop.model.DatalogTools.constructNewFunction;
import static org.semanticweb.ontop.model.DatalogTools.isDataOrLeftJoinOrJoinAtom;
import static org.semanticweb.ontop.model.DatalogTools.isLeftJoinOrJoinAtom;
import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities.union;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.*;

/**
 * Left-join aware implementation.
 *
 */
public class RuleLevelProposalImpl implements RuleLevelProposal {

    private final Substitution typingSubstitution;
    private final CQIE typedRule;
    private final TypeProposal typeProposal;

    /**
     * Computes the substitution and the typed rule.
     *
     * May throw a MultiTypeException
     */
    public RuleLevelProposalImpl(CQIE initialRule, HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {

        List<Function> bodyAtoms = List.iterableList(initialRule.getBody());

        List<Function> dataAndCompositeAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return isDataOrLeftJoinOrJoinAtom(atom);
            }
        });
        List<Function> otherAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return !isDataOrLeftJoinOrJoinAtom(atom);
            }
        });

        /**
         * Extends the body data atoms (in a possible complex structure made of Joins and LJs)
         *     so that are compatible with the typed child head for computing a type propagation substitution.
         */
        List<Function> extendedBodyDataAndCompositeAtoms = computeExtendedBodyDataAtoms(dataAndCompositeAtoms,
                childProposalIndex);

        /**
         * Computes the type propagating substitution.
         */
        typingSubstitution = aggregateRuleAndProposals(extendedBodyDataAndCompositeAtoms, childProposalIndex);

        typedRule = constructTypedRule(initialRule, typingSubstitution, extendedBodyDataAndCompositeAtoms, otherAtoms);

        /**
         * Derives the type proposal
         */
        typeProposal = constructTypeProposal(typedRule.getHead());

    }

    /**
     * Converts each data atom into an unifiable atom thanks to its corresponding type proposal.
     * If no type proposal corresponds to a data atom, it means it is already unifiable.
     *
     * Some of these conversions may introduce new non-conflicting variables.
     *
     */
    private static List<Function> computeExtendedBodyDataAtoms(final List<Function> dataAndCompositeAtoms,
                                                               final HashMap<Predicate, PredicateLevelProposal> childProposalIndex) {

        /**
         * All the variables are supposed to be present in the data atoms (safe Datalog rules).
         *
         * Append-only Set.
         */
        final java.util.Set<Variable> alreadyKnownRuleVariables = extractVariables(dataAndCompositeAtoms);

        return dataAndCompositeAtoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                /**
                 * New variables may be added to alreadyKnownRuleVariables (mutable).
                 */
                return computeExtendedAtom(atom, childProposalIndex, alreadyKnownRuleVariables);
            }
        });
    }

    /**
     * TODO: explain
     *
     * Beware, alreadyKnownRuleVariables is mutable
     */
    private static Function computeExtendedAtom(final Function dataOrCompositeAtom,
                                                final HashMap<Predicate, PredicateLevelProposal> childProposalIndex,
                                                final java.util.Set<Variable> alreadyKnownRuleVariables) {
        if (dataOrCompositeAtom.isDataFunction())
            return computeExtendedDataAtom(dataOrCompositeAtom, childProposalIndex, alreadyKnownRuleVariables);
        else
            return computeExtendedCompositeAtom(dataOrCompositeAtom, childProposalIndex, alreadyKnownRuleVariables);
    }

    /**
     * TODO: explain
     */
    private static Function computeExtendedDataAtom(Function dataAtom,
                                                    HashMap<Predicate, PredicateLevelProposal> childProposalIndex,
                                                    Set<Variable> alreadyKnownRuleVariables) {
        Option<PredicateLevelProposal> optionalChildPredProposal = childProposalIndex.get(dataAtom.getFunctionSymbol());
        /**
         * No child proposal --> return the original atom.
         */
        if (optionalChildPredProposal.isNone())
            return dataAtom;

        /**
         * Converts into an unifiable atom thanks to the type proposal.
         *
         * If new variables are created, they are added to the tracking set.
         *
         */
        TypeProposal childTypeProposal = optionalChildPredProposal.some().getTypeProposal();
        P2<Function, java.util.Set<Variable>> newAtomAndVariables = childTypeProposal.convertIntoExtendedAtom(dataAtom,
                ImmutableSet.copyOf(alreadyKnownRuleVariables));

        // Appends new variables
        alreadyKnownRuleVariables.addAll(newAtomAndVariables._2());

        Function unifiableAtom = newAtomAndVariables._1();
        return unifiableAtom;
    }

    /**
     * TODO: explain
     */
    private static Function computeExtendedCompositeAtom(Function compositeAtom,
                                                         final HashMap<Predicate, PredicateLevelProposal> childProposalIndex,
                                                         final Set<Variable> alreadyKnownRuleVariables) {
        List<Function> subAtoms = List.iterableList((java.util.List<Function>) (java.util.List<?>) compositeAtom.getTerms());
        List<Term> extendedSubAtoms = subAtoms.map(new F<Function, Term>() {
            @Override
            public Term f(Function subAtom) {
                if (isDataOrLeftJoinOrJoinAtom(subAtom))
                    return computeExtendedAtom(subAtom, childProposalIndex, alreadyKnownRuleVariables);
                return subAtom;
            }
        });

        return constructNewFunction(compositeAtom.getFunctionSymbol(), extendedSubAtoms);
    }




    @Override
    public Substitution getTypingSubstitution() {
        return typingSubstitution;
    }

    @Override
    public CQIE getTypedRule() {
        return typedRule;
    }

    @Override
    public CQIE getDetypedRule() {
        Function extendedTypedHead =  typeProposal.getExtendedTypedAtom();
        Function detypedHead = removeTypeFromAtom(extendedTypedHead);

        CQIE detypedRule = typedRule.clone();
        detypedRule.updateHead(detypedHead);
        return detypedRule;
    }


    /**
     * Entry point for the homonym tail-recursive function.
     */
    private static Substitution aggregateRuleAndProposals(final List<Function> extendedBodyDataAtoms,
                                                     final HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        return aggregateRuleAndProposals(Option.<Substitution>none(), extendedBodyDataAtoms, childProposalIndex);
    }


    /**
     * Tail-recursive function that "iterates" over the body atoms of a given rule defining the parent predicate.
     *
     * For a given body atom, tries to make the *union* (NOT composition) of the current substitution function with
     * the one deduced from the child proposal corresponding to the current atom.
     *
     * If some problems with a substitution function occur, throws a MultiTypeException.
     *
     */
    private static Substitution aggregateRuleAndProposals(final Option<Substitution> optionalSubstitution,
                                                     final List<Function> remainingBodyDataAtoms,
                                                     final HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        /**
         * Stop condition (no further body data atom).
         */
        if (remainingBodyDataAtoms.isEmpty()) {
            /**
             * If no child proposal corresponds to the body atoms, no substitution is created.
             * --> Returns an empty substitution.
             */
            if (optionalSubstitution.isNone()) {
                // Empty substitution
                return new NeutralSubstitution();
            }
            return optionalSubstitution.some();
        }

        Function bodyAtom = remainingBodyDataAtoms.head();
        Option<PredicateLevelProposal> optionalChildProposal = childProposalIndex.get(bodyAtom.getFunctionSymbol());

        Option<Substitution> newOptionalSubstitution;

        /**
         * If there is a child proposal corresponding to the current body atom,
         * computes a substitution function that propagates types.
         *
         * Then, makes the union of this substitution function with the previous one.
         *
         */
        if (optionalChildProposal.isSome()) {
            try {
                Function sourceAtom = optionalChildProposal.some().getTypeProposal().getExtendedTypedAtom();
                Substitution proposedSubstitution = computeTypePropagatingSubstitution(sourceAtom, bodyAtom);

                if (optionalSubstitution.isNone()) {
                    newOptionalSubstitution = Option.some(proposedSubstitution);
                }
                /**
                 * We do NOT consider the composition of the substitution functions (like during unification)
                 * BUT THEIR UNION.
                 *
                 * Why? Because we want to apply a type only once, not multiple times.
                 *
                 * By composition "{ x/int(x) } o { x/int(x) } = { x/int(int(x) }" which is not what we want.
                 * With unions, "{ x/int(x) } U { x/int(x) } = { x/int(x) }".
                 *
                 * Throw a type propagation exception if the substitutions are conflicting.
                 * For example, this "union" does not a produce a function.
                 *
                 * " {x/int(x) } U { x/str(x) } "
                 *
                 */
                else {
                    newOptionalSubstitution = Option.some(union(optionalSubstitution.some(),
                            proposedSubstitution));
                }
            }
            /**
             * Impossible to propagate type.
             * This happens when multiple types are proposed for this predicate.
             */
            catch(SubstitutionUtilities.SubstitutionException e) {
                throw new TypeLiftTools.MultiTypeException();
            }
        }
        /**
         * Otherwise, keeps the same proposed head.
         */
        else {
            newOptionalSubstitution = optionalSubstitution;
        }

        /**
         * Tail recursion
         */
        return aggregateRuleAndProposals(newOptionalSubstitution, remainingBodyDataAtoms.tail(), childProposalIndex);
    }

    /**
     * Rebuilds a Datalog rule from the data and filter atoms and the typing substitution.
     *
     * Note that it only constructs Conjunctive Queries!
     *
     * TODO: make sure it works for LJs!
     *
     */
    private static CQIE constructTypedRule(CQIE initialRule, Substitution typingSubstitution, List<Function> extendedDataAndCompositeAtoms, List<Function> untypedOtherAtoms) {

        /**
         * Derives a typed head by applying the substitution
         */
        Function typedHead = (Function) initialRule.getHead().clone();
        //SIDE-EFFECT!!!
        SubstitutionUtilities.applySubstitution(typedHead, typingSubstitution);

        /**
         * Types filter and non-composite algebra atoms
         */
        List<Function> typedBodyAtoms = typeAtoms(typingSubstitution, untypedOtherAtoms);

        /**
         * Types filter and non-composite algebra sub-atoms INSIDE composite atoms
         */
        List<Function> fullyTypedExtendedDataAndCompositeAtoms = typeRelevantNestedAtoms(extendedDataAndCompositeAtoms,
                typingSubstitution);

        /**
         * Removes the variables that correspond to URI templates from data atoms ONLY (not boolean conditions)
         */
        List<Function> newUntypedDataAtoms = removeURITemplatesFromDataAtoms(fullyTypedExtendedDataAndCompositeAtoms,
                typingSubstitution);

        /**
         * Concats the three types of body atoms
         */
        List<Function> allBodyAtoms = newUntypedDataAtoms.append(typedBodyAtoms);
        java.util.List<Function> typedRuleBody = new ArrayList<>(allBodyAtoms.toCollection());


        CQIE typedRule = OBDADataFactoryImpl.getInstance().getCQIE(typedHead, typedRuleBody);
        return typedRule;
    }

    /**
     * TODO: explain
     */
    private static List<Function> typeRelevantNestedAtoms(List<Function> atoms, final Substitution typingSubstitution) {
        return atoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                if (!isDataOrLeftJoinOrJoinAtom(atom)) {
                    return typeAtom(typingSubstitution, atom);
                }
                else if (isLeftJoinOrJoinAtom(atom)) {
                    List<Function> subAtoms = List.iterableList((java.util.List<Function>)(java.util.List<?>)atom.getTerms());

                    List<Function> updatedSubAtoms = typeRelevantNestedAtoms(subAtoms, typingSubstitution);
                    return constructNewFunction(updatedSubAtoms, atom.getFunctionSymbol());
                }
                /**
                 * If is a data atom, nothing to do.
                 */
                else
                    return atom;
            }
        });
    }

    /**
     * Applies the typing substitution to a list of atoms.
     */
    private static List<Function> typeAtoms(final Substitution typingSubstitution, final List<Function> atoms) {
        return atoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                return typeAtom(typingSubstitution, atom);
            }
        });
    }

    /**
     * TODO: look if this method does not exist somewhere else
     */
    private static Function typeAtom(final Substitution typingSubstitution, final Function atom) {
        Function newAtom = (Function) atom.clone();
        // SIDE-EFFECT: makes the new head typed.
        SubstitutionUtilities.applySubstitution(newAtom, typingSubstitution);
        return newAtom;
    }

    /**
     * Composite atoms may also contain some boolean sub-atoms; URI templates in the latter should NOT be removed.
     */
    private static List<Function> removeURITemplatesFromDataAtoms(List<Function> atoms,
                                                                  final Substitution typingSubstitution) {
        return atoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                if (atom.isDataFunction()) {
                    return removeURITemplatesFromDataAtom(atom, typingSubstitution);
                }
                /**
                 * Indirect rec
                 */
                else if (isLeftJoinOrJoinAtom(atom)) {
                    return removeURITemplatesFromCompositeAtom(atom, typingSubstitution);
                }
                /**
                 * Does not alter the other atoms.
                 */
                else
                    return atom;
            }
        });
    }

    /**
     * TODO: explain
     */
    private static Function removeURITemplatesFromDataAtom(Function dataAtom, final Substitution typingSubstitution) {
        Function typedAtom = (Function) dataAtom.clone();
        SubstitutionUtilities.applySubstitution(typedAtom, typingSubstitution);

        /**
         * Untyping removes URI template terms.
         */
        Function untypedAtom = TypeLiftTools.removeTypeFromAtom(typedAtom);
        return untypedAtom;
    }

    /**
     * TODO: explain
     */
    private static Function removeURITemplatesFromCompositeAtom(Function compositeAtom, Substitution typingSubstitution) {
        List<Function> subAtoms = List.iterableList((java.util.List<Function>)(java.util.List<?>)compositeAtom.getTerms());

        List<Function> cleanedSubAtoms = removeURITemplatesFromDataAtoms(subAtoms, typingSubstitution);
        return constructNewFunction(cleanedSubAtoms, compositeAtom.getFunctionSymbol());
    }

    /**
     * Returns the set of variables found in the atoms.
     */
    private static java.util.Set<Variable> extractVariables(List<Function> atoms) {
        List<Variable> variableList = atoms.bind(new F<Function, List<Variable>>() {
            @Override
            public List<Variable> f(Function atom) {
                return List.iterableList(atom.getVariables());
            }
        });

        return new java.util.HashSet(variableList.toCollection());
    }

}
