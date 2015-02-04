package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.HashMap;
import fj.data.List;
import fj.data.Option;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.CQIEImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;

import java.util.ArrayList;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions.union;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.computeTypePropagatingSubstitution;

/**
 * TODO: Name: should we still call it a proposal?
 *
 * See if turns into a substitution.
 *
 */
public class RuleLevelProposalImpl implements RuleLevelProposal {

    private final Unifier typingSubstitution;
    private final CQIE typedRule;

    public RuleLevelProposalImpl(CQIE initialRule, HashMap<Predicate, PredicateLevelProposal> childProposalIndex) throws TypeLift.MultiTypeException {

        List<Function> bodyAtoms = List.iterableList(initialRule.getBody());

        List<Function> unifiableDataAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
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
        typingSubstitution = aggregateRuleAndProposals(Option.<Unifier>none(), unifiableDataAtoms, childProposalIndex);

        typedRule = constructTypedRule(initialRule, typingSubstitution, unifiableDataAtoms, filterAtoms);

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
     * Tail-recursive method that "iterates" over the body atoms of a given rule defining the parent predicate.
     *
     * For a given body atom, tries to make the *union* (NOT composition) of the current substitution function with
     * the one deduced from the child proposal corresponding to the current atom.
     *
     * If some problems with a substitution function occur, throws a MultiTypeException.
     *
     */
    private static Unifier aggregateRuleAndProposals(final Option<Unifier> optionalSubstitutionFunction,
                                                     final List<Function> remainingBodyAtoms,
                                                     final HashMap<Predicate, PredicateLevelProposal> childProposalIndex) throws TypeLift.MultiTypeException {
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
                throw new TypeLift.MultiTypeException();
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
        Function typedHead = initialRule.getHead();
        UnifierUtilities.applyUnifier(typedHead, typingSubstitution);

        List<Function> untypedDataAtoms = untypeDataAtoms(unifiableDataAtoms);

        java.util.List<Function> typedRuleBody = new ArrayList<>(untypedDataAtoms.append(filterAtoms).toCollection());
        CQIE typedRule = OBDADataFactoryImpl.getInstance().getCQIE(typedHead, typedRuleBody);
        return typedRule;
    }

    /**
     * TODO: implement it
     */
    private static List<Function> untypeDataAtoms(List<Function> unifiableDataAtoms) {
        return null;
    }

}
