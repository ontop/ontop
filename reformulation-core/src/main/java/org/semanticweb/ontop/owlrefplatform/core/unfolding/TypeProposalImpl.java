package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.TypeProposal;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

/**
 * Base class
 */
public abstract class TypeProposalImpl implements TypeProposal {

    private final Function proposedFunction;

    protected TypeProposalImpl(Function typeProposal) {
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

    /**
     * Removes the type for a given term.
     * This method also deals with special cases that should not be untyped.
     *
     * Note that type removal only concern functional terms.
     */
    protected static Term untypeTerm(Term term) {
        /**
         * Types are assumed to functional terms.
         *
         * Other type of terms are not concerned.
         */
        if (!(term instanceof Function)) {
            return term;
        }

        /**
         * Special case that should not be untyped:
         *   - Aggregates
         */
        if (DatalogUnfolder.detectAggregateInArgument(term))
            return term;

        Function functionalTerm = (Function) term;
        java.util.List<Term> functionArguments = functionalTerm.getTerms();
        Predicate functionSymbol = functionalTerm.getFunctionSymbol();

        /**
         * Special case: URI templates using just one variable
         * (others are not supported).
         */
        boolean isURI = functionSymbol.getName().equals(OBDAVocabulary.QUEST_URI);
        if (isURI && functionArguments.size() == 2) {
            // Returns the first variable, not the regular expression.
            return functionArguments.get(1);
        }

        /**
         * Other functional terms are expected to be type
         * and to have an arity of 1.
         *
         * Raises an exception if it is not the case.
         */
        if (functionArguments.size() != 1) {
            throw new RuntimeException("Removing types of non-unary functional terms is not supported.");
        }
        return functionArguments.get(0);
    }

}
