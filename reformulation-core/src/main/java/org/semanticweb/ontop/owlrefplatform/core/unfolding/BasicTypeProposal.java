package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.List;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.TypeProposal;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;

import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLift.applyBasicTypeProposal;

/**
 * Normal case.
 */
public class BasicTypeProposal implements TypeProposal {

    private final Function proposedFunction;

    public BasicTypeProposal(Function typeProposal) {
        this.proposedFunction = typeProposal;
    }

    /**
     * TODO: remove it when possible
     */
    @Override
    public Function getProposedHead() {
        return proposedFunction;
    }

    @Override
    public Predicate getPredicate() {
        return proposedFunction.getFunctionSymbol();
    }

    @Override
    public List<CQIE> applyType(List<CQIE> initialRules) throws TypeApplicationError {
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
     * TODO: implement it
     * @param initialRules
     * @return
     */
    @Override
    public List<CQIE> removeType(List<CQIE> initialRules) {
        return null;
    }
}
