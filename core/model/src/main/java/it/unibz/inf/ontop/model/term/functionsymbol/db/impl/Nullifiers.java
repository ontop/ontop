package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * TODO: find a better name
 */
public class Nullifiers {

    /**
     * Recursive
     */
    public static ImmutableTerm nullify(ImmutableTerm term,
                                        ImmutableTerm termToReplaceByNull,
                                        TermFactory termFactory) {
        if (termToReplaceByNull.equals(term))
            return (termToReplaceByNull instanceof ImmutableExpression)
                    ? termFactory.getIsTrue(termFactory.getNullConstant())
                    : termFactory.getNullConstant();
        else if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            return termFactory.getImmutableFunctionalTerm(
                    functionalTerm.getFunctionSymbol(),
                    functionalTerm.getTerms().stream()
                            // Recursive
                            .map(t -> nullify(t, termToReplaceByNull, termFactory))
                            .collect(ImmutableCollectors.toList()));
        }
        else
            return term;
    }


}
