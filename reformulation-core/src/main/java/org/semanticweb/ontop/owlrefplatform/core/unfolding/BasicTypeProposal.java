package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.List;
import org.semanticweb.ontop.model.*;

import java.util.ArrayList;

/**
 * Normal case.
 */
public class BasicTypeProposal extends TypeProposalImpl {

    public BasicTypeProposal(Function typeProposal) {
        super(typeProposal);
    }

    @Override
    public Function getUnifiableAtom() {
        return getProposedAtom();
    }

    /**
     * TODO: describe it
     */
    @Override
    public List<CQIE> removeType(List<CQIE> initialRules) {
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
                        return untypeTerm(term);
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
     * NO EFFECT because this type proposal cannot provoke any arity change.
     */
    @Override
    public List<CQIE> propagateChildArityChangeToBodies(List<CQIE> initialRules) {
        return initialRules;
    }

    @Override
    public Function prepareBodyAtomForUnification(Function bodyAtom, java.util.Set<Variable> alreadyKnownRuleVariables) {
        return bodyAtom;
    }
}
