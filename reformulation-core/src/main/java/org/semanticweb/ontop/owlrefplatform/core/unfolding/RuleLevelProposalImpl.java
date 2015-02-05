package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ImmutableSet;
import fj.F;
import fj.P2;
import fj.data.*;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;

import java.util.ArrayList;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions.union;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.computeTypePropagatingSubstitution;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.extractBodyAtoms;

/**
 * TODO: Name: should we still call it a proposal?
 *
 * See if turns into a substitution.
 *
 */
public class RuleLevelProposalImpl implements RuleLevelProposal {

    private final Unifier typingSubstitution;
    private final CQIE typedRule;

    /**
     * TODO: describe
     * May throw a MultiTypeException
     */
    public RuleLevelProposalImpl(CQIE initialRule, HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {

        /**
         * All body atoms (even these nested in left-joins)
         */
        List<Function> bodyAtoms = extractBodyAtoms(initialRule);

        List<Function> dataAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isDataFunction();
            }
        });
        List<Function> filterAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isBooleanFunction();
            }
        });

        /**
         * TODO: explain
         */
        List<Function> unifiableDataAtoms = computeUnifiableDataAtoms(dataAtoms, childProposalIndex);

        /**
         * TODO: explain
         */
        typingSubstitution = aggregateRuleAndProposals(unifiableDataAtoms, childProposalIndex);

        typedRule = constructTypedRule(initialRule, typingSubstitution, unifiableDataAtoms, filterAtoms);

    }

    /**
     * Converts each data atom into an unifiable atom thanks to its corresponding type proposal.
     * If no type proposal corresponds to a data atom, it means it is already unifiable.
     *
     * Some of these conversions may introduce new non-conflicting variables.
     *
     */
    private static List<Function> computeUnifiableDataAtoms(final List<Function> dataAtoms,
                                                            final HashMap<Predicate, PredicateLevelProposal> childProposalIndex) {

        /**
         * All the variables are supposed to be present in the data atoms (safe Datalog rules).
         *
         * Append-only Set.
         */
        final java.util.Set<Variable> alreadyKnownRuleVariables = extractVariables(dataAtoms);

        return dataAtoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                Option<PredicateLevelProposal> optionalChildPredProposal = childProposalIndex.get(atom.getFunctionSymbol());
                /**
                 * No child proposal --> return the original atom.
                 */
                if (optionalChildPredProposal.isNone())
                    return atom;

                /**
                 * Converts into an unifiable atom thanks to the type proposal.
                 *
                 * If new variables are created, they are added to the tracking set.
                 *
                 */
                TypeProposal childTypeProposal = optionalChildPredProposal.some().getTypeProposal();
                P2<Function, java.util.Set<Variable>> newAtomAndVariables = childTypeProposal.convertIntoUnifiableAtom(atom,
                        ImmutableSet.copyOf(alreadyKnownRuleVariables));

                // Appends new variables
                alreadyKnownRuleVariables.addAll(newAtomAndVariables._2());

                Function unifiableAtom = newAtomAndVariables._1();
                return unifiableAtom;
            }
        });
    }

    @Override
    public Unifier getSubstitution() {
        return typingSubstitution;
    }

    @Override
    public CQIE getTypedRule() {
        return typedRule;
    }


    /**
     * Entry point for the homonym tail-recursive function.
     */
    private static Unifier aggregateRuleAndProposals(final List<Function> unifiableBodyAtoms,
                                                     final HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        return aggregateRuleAndProposals(Option.<Unifier>none(), unifiableBodyAtoms, childProposalIndex);
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
    private static Unifier aggregateRuleAndProposals(final Option<Unifier> optionalSubstitutionFunction,
                                                     final List<Function> remainingBodyAtoms,
                                                     final HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        /**
         * Stop condition (no further body atom).
         */
        if (remainingBodyAtoms.isEmpty()) {
            if (optionalSubstitutionFunction.isNone()) {
                throw new IllegalArgumentException("Do not give a None substitution function with an empty list of rules");
            }
            return optionalSubstitutionFunction.some();
        }

        Function bodyAtom = remainingBodyAtoms.head();
        Option<PredicateLevelProposal> optionalChildProposal = childProposalIndex.get(bodyAtom.getFunctionSymbol());

        Option<Unifier> newOptionalSubstitutionFct;

        /**
         * If there is a child proposal corresponding to the current body atom,
         * computes a substitution function that propagates types.
         *
         * Then, makes the union of this substitution function with the previous one.
         *
         */
        if (optionalChildProposal.isSome()) {
            try {
                Unifier proposedSubstitutionFunction = computeTypePropagatingSubstitution(
                        bodyAtom, optionalChildProposal.some().getTypeProposal());

                if (optionalSubstitutionFunction.isNone()) {
                    newOptionalSubstitutionFct = Option.some(proposedSubstitutionFunction);
                }
                /**
                 * We do NOT consider the composition of the substitution functions (like during unifier)
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
                    newOptionalSubstitutionFct = Option.some(union(optionalSubstitutionFunction.some(),
                            proposedSubstitutionFunction));
                }
            }
            /**
             * Impossible to propagate type.
             * This happens when multiple types are proposed for this predicate.
             */
            catch(Substitutions.SubstitutionException e) {
                throw new TypeLiftTools.MultiTypeException();
            }
        }
        /**
         * Otherwise, keeps the same proposed head.
         */
        else {
            newOptionalSubstitutionFct = optionalSubstitutionFunction;
        }

        /**
         * Tail recursion
         */
        return aggregateRuleAndProposals(newOptionalSubstitutionFct, remainingBodyAtoms.tail(), childProposalIndex);
    }

    /**
     * TODO: describe it
     *
     */
    private static CQIE constructTypedRule(CQIE initialRule, Unifier typingSubstitution, List<Function> unifiableDataAtoms, List<Function> filterAtoms) {
        Function newHead = initialRule.getHead();
        // SIDE-EFFECT: makes the new head typed.
        UnifierUtilities.applyUnifier(newHead, typingSubstitution);

        List<Function> allAtoms = unifiableDataAtoms.append(filterAtoms);

        java.util.List<Function> typedRuleBody = new ArrayList<>(allAtoms.toCollection());
        CQIE typedRule = OBDADataFactoryImpl.getInstance().getCQIE(newHead, typedRuleBody);
        return typedRule;
    }

    /**
     * TODO: describe
     *
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
