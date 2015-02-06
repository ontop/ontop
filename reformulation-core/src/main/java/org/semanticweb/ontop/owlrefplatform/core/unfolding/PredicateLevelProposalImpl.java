package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.P;
import fj.P2;
import fj.data.HashMap;
import fj.data.List;
import fj.data.Option;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions.union;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.constructTypeProposal;

/**
 * From a high-level point of view, this proposal is done by looking at (i) the children proposals and (ii) the rules defining the parent predicate.
 *
 * Its implementation relies on RuleLevelProposals.
 *
 */
public class PredicateLevelProposalImpl implements PredicateLevelProposal {

    private final List<RuleLevelProposal> ruleProposals;
    private final TypeProposal typeProposal;

    /**
     * Constructs the RuleLevelProposals and makes a TypeProposal.
     *
     * May throw a MultiTypeException.
     */
    public PredicateLevelProposalImpl(List<CQIE> parentRules, HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        /**
         * Computes the RuleLevelProposals and the global substitution.
         */
        P2<List<RuleLevelProposal>, Unifier> results = computeRuleProposalsAndSubstitution(parentRules, childProposalIndex);
        ruleProposals = results._1();
        Unifier globalSubstitution = results._2();

        /**
         * Derives the type proposal from these RuleLevelProposals and the global substitution.
         */
        typeProposal = makeTypeProposal(ruleProposals, globalSubstitution);
    }

    @Override
    public TypeProposal getTypeProposal() {
        return typeProposal;
    }

    @Override
    public Predicate getPredicate() {
        return getTypeProposal().getPredicate();
    }

    /**
     * Returns the typed rules produced by the RuleLevelProposals.
     */
    @Override
    public List<CQIE> getTypedRules() {
        return ruleProposals.map(new F<RuleLevelProposal, CQIE>() {
            @Override
            public CQIE f(RuleLevelProposal ruleLevelProposal) {
                return ruleLevelProposal.getTypedRule();
            }
        });
    }

    /**
     * Entry point of the homonym recursive function.
     */
    private static P2<List<RuleLevelProposal>, Unifier> computeRuleProposalsAndSubstitution(List<CQIE> parentRules,
                                                                                            HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        return computeRuleProposalsAndSubstitution(Option.<Unifier>none(), parentRules, List.<RuleLevelProposal>nil(),
                childProposalIndex);
    }

    /**
     * Creates RuleLevelProposals and computes the global substitution as the union of the substitutions they propose.
     *
     * Tail-recursive function.
     */
    private static P2<List<RuleLevelProposal>, Unifier> computeRuleProposalsAndSubstitution(Option<Unifier> optionalSubstitution,
                                                                                            List<CQIE> remainingRules,
                                                                                            List<RuleLevelProposal> ruleProposals,
                                                                                            HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        /**
         * Stop condition (no more rule to consider).
         */
        if (remainingRules.isEmpty()) {
            if (optionalSubstitution.isNone()) {
                throw new IllegalArgumentException("Do not give a None substitution with an empty list of rules");
            }
            return P.p(ruleProposals, optionalSubstitution.some());
        }

        /**
         * Makes a RuleLevelProposal out of the current rule.
         */
        CQIE currentRule = remainingRules.head();
        RuleLevelProposal newRuleLevelProposal = new RuleLevelProposalImpl(currentRule, childProposalIndex);

        /**
         * Updates the global substitution by computes the union of it with the substitution proposed by the rule.
         *
         * If the union is impossible (i.e. does not produce a valid substitution), throws a MultiTypedException.
         *
         */
        Option<Unifier> proposedSubstitution;
        if (optionalSubstitution.isNone()) {
            proposedSubstitution = Option.some(newRuleLevelProposal.getTypingSubstitution());
        }
        else {
            try {
                proposedSubstitution = Option.some(union(optionalSubstitution.some(), newRuleLevelProposal.getTypingSubstitution()));
            }
            /**
             * Impossible to compute the union of two substitutions.
             * This happens when multiple types are proposed for this predicate.
             */
            catch(Substitutions.SubstitutionException e) {
                throw new TypeLiftTools.MultiTypeException();
            }
        }

        // Appends the new RuleLevelProposal to the list
        List<RuleLevelProposal> newRuleProposalList = ruleProposals.append(List.cons(newRuleLevelProposal,
                List.<RuleLevelProposal>nil()));

        /**
         * Tail recursion
         */
        return computeRuleProposalsAndSubstitution(proposedSubstitution, remainingRules.tail(),
                newRuleProposalList, childProposalIndex);
    }

    /**
     * Makes a type proposal out of the head of one definition rule.
     */
    private static TypeProposal makeTypeProposal(List<RuleLevelProposal> ruleProposals, Unifier globalSubstitution) {
        /**
         * Makes a TypeProposal by applying the substitution to the head of one rule.
         *
         */
        final Function newFunctionProposal = (Function) ruleProposals.head().getTypedRule().getHead().clone();
        // Side-effect!
        UnifierUtilities.applyUnifier(newFunctionProposal, globalSubstitution);
        final TypeProposal newProposal = constructTypeProposal(newFunctionProposal);

        return newProposal;
    }
}
