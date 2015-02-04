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
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.containsURITemplate;

/**
 * TODO: describe
 */
public class PredicateLevelProposalImpl implements PredicateLevelProposal {

    /**
     * TODO: describe
     */
    private final List<RuleLevelProposal> ruleProposals;
    private final TypeProposal typeProposal;

    public PredicateLevelProposalImpl(List<CQIE> parentRules, HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLift.MultiTypeException {
        P2<List<RuleLevelProposal>, Unifier> results = computeRuleProposals(Option.<Unifier>none(), parentRules,
                List.<RuleLevelProposal>nil(), childProposalIndex);

        ruleProposals = results._1();
        Unifier globalSubstitution = results._2();

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
     * TODO: describe it!
     *
     */
    private static P2<List<RuleLevelProposal>, Unifier> computeRuleProposals(Option<Unifier> optionalSubstitution,
                                                                             List<CQIE> remainingRules,
                                                                             List<RuleLevelProposal> ruleSubstitutions,
                                                                             HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLift.MultiTypeException {
        /**
         * Stop condition (no more rule to consider).
         */
        if (remainingRules.isEmpty()) {
            if (optionalSubstitution.isNone())
                throw new IllegalArgumentException("Do not give a None substitution with an empty list of rules");
            return P.p(ruleSubstitutions, optionalSubstitution.some());
        }

        CQIE rule = remainingRules.head();

        /**
         * TODO: describe
         */
        RuleLevelProposal newRuleLevelSubstitution = new RuleLevelProposalImpl(rule, childProposalIndex);

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
            catch(Substitutions.SubstitutionException e) {
                throw new TypeLift.MultiTypeException();
            }
        }

        List<RuleLevelProposal> newRuleSubstitutionList =  ruleSubstitutions.append(List.cons(newRuleLevelSubstitution,
                List.<RuleLevelProposal>nil()));

        /**
         * Tail recursion
         */
        return computeRuleProposals(proposedSubstitution, remainingRules.tail(),
                newRuleSubstitutionList, childProposalIndex);
    }

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

    /**
     * Constructs a TypeProposal of the proper type.
     */
    private static TypeProposal constructTypeProposal(Function functionalProposal) {
        /**
         * Special case: multi-variate URI template.
         */
        if (containsURITemplate(functionalProposal)) {
            return new UriTemplateTypeProposal(functionalProposal);
        }
        /**
         * Default case
         */
        return new BasicTypeProposal(functionalProposal);
    }

}
