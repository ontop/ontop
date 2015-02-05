package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;

import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.applyTypeProposal;

/**
 * Base class
 */
public abstract class TypeProposalImpl implements TypeProposal {

    private final Function proposedAtom;

    protected TypeProposalImpl(Function proposedAtom) {
        this.proposedAtom = proposedAtom;
    }

    protected Function getProposedAtom() {
        return proposedAtom;
    }

    @Override
    public List<CQIE> applyType(final List<CQIE> initialRules) throws TypeApplicationError {
        final TypeProposal thisProposal = this;

        return initialRules.map(new F<CQIE, CQIE>() {
            @Override
            public CQIE f(CQIE initialRule) {
                Function currentHead = initialRule.getHead();
                try {
                    Function newHead = applyTypeProposal(currentHead, thisProposal);

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

    @Override
    public Predicate getPredicate() {
        return proposedAtom.getFunctionSymbol();
    }

}
