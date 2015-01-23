package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;

import java.util.ArrayList;

import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLift.applyBasicTypeProposal;

/**
 * Normal case.
 */
public class BasicTypeProposal extends TypeProposalImpl {

    public BasicTypeProposal(Function typeProposal) {
        super(typeProposal);
    }

    @Override
    public List<CQIE> applyType(final List<CQIE> initialRules) throws TypeApplicationError {
        final Function proposedFunction = getUnifiableAtom();

        return initialRules.map(new F<CQIE, CQIE>() {
            @Override
            public CQIE f(CQIE initialRule) {
                Function currentHead = initialRule.getHead();
                try {
                    Function newHead = applyBasicTypeProposal(currentHead, proposedFunction);

                    // Mutable object
                    CQIE newRule = initialRule.clone();
                    newRule.updateHead(newHead);
                    return newRule;
                    /**
                     * A SubstitutionException exception should not appear at this level.
                     * There is an inconsistency somewhere.
                     *
                     * Throws a runtime exception (TypeApplicationError)
                     * that should not be expected.
                     */
                } catch(Substitutions.SubstitutionException e) {
                    throw new TypeApplicationError();
                }
            }
        });
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
}
