package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;

import java.util.ArrayList;

import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLift.applyTypeProposal;

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

    /**
     * Removes the type for a given term.
     * This method also deals with special cases that should not be untyped.
     *
     * Note that type removal only concern functional terms.
     *
     * If the returned value is null, the term must be eliminated.
     *
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
         * Special case: URI templates --> to be removed.
         *
         */
        boolean isURI = functionSymbol.getName().equals(OBDAVocabulary.QUEST_URI);
        if (isURI) {
            return null;
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

    /**
     * TODO: describe it
     */
    @Override
    public List<CQIE> removeHeadTypes(List<CQIE> initialRules) {
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
                }).filter(new F<Term, Boolean>() {
                    @Override
                    public Boolean f(Term term) {
                        return term != null;
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

}
