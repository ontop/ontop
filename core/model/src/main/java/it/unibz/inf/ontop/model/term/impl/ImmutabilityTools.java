package it.unibz.inf.ontop.model.term.impl;

import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.*;

public class ImmutabilityTools {

    private final TermFactory termFactory;

    /**
     * TODO: make it private
     */
    @Inject
    public ImmutabilityTools(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * In case the term is functional, creates an immutable copy of it.
     */
    public ImmutableTerm convertIntoImmutableTerm(Term term) {
        if (term instanceof Function) {
            Function functionalTerm = (Function) term;
            Predicate functor = functionalTerm.getFunctionSymbol();
            if (functor instanceof BooleanFunctionSymbol) {
                return termFactory.getImmutableExpression(
                        (BooleanFunctionSymbol)functor,
                        convertTerms(functionalTerm));
            }
            else if (functor instanceof FunctionSymbol)
                return termFactory.getImmutableFunctionalTerm(
                        (FunctionSymbol) functionalTerm.getFunctionSymbol(),
                        convertTerms(functionalTerm));
            else
                throw new NotAFunctionSymbolException(term + " is not using a FunctionSymbol but a "
                        + functionalTerm.getFunctionSymbol().getClass());
        }
        /*
         * Other terms (constant and variable) are immutable.
         */
        return (ImmutableTerm) term;
    }

    public VariableOrGroundTerm convertIntoVariableOrGroundTerm(Term term) {
        if (term instanceof Variable) {
            return (Variable) term;
        } else if (GroundTermTools.isGroundTerm(term)) {
            return GroundTermTools.castIntoGroundTerm(term, termFactory);
        } else {
            throw new IllegalArgumentException("Not a variable nor a ground term: " + term);
        }
    }

    public static VariableOrGroundTerm convertIntoVariableOrGroundTerm(ImmutableTerm term) {
        if (term instanceof Variable) {
            return (Variable) term;
        } else if (term.isGround()) {
            return (GroundTerm) term;
        } else {
            throw new IllegalArgumentException("Not a variable nor a ground term: " + term);
        }
    }

    /**
     * This method takes a immutable term and convert it into an old mutable function.
     */
    public Function convertToMutableFunction(ImmutableFunctionalTerm functionalTerm) {
        return convertToMutableFunction(functionalTerm.getFunctionSymbol(),
                functionalTerm.getTerms());
    }

    public Function convertToMutableFunction(DataAtom dataAtom) {
        return convertToMutableFunction(dataAtom.getPredicate(), dataAtom.getArguments());
    }

    public Function convertToMutableFunction(Predicate predicateOrFunctionSymbol,
                                             ImmutableList<? extends ImmutableTerm> terms) {
        return termFactory.getFunction(predicateOrFunctionSymbol, convertToMutableTerms(terms));
    }

    private List<Term> convertToMutableTerms(ImmutableList<? extends ImmutableTerm> terms) {
        List<Term> mutableList = new ArrayList<>();
        Iterator<? extends ImmutableTerm> iterator = terms.iterator();
        while (iterator.hasNext()) {

            ImmutableTerm nextTerm = iterator.next();
            if (nextTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm term2Change = (ImmutableFunctionalTerm) nextTerm;
                Function newTerm = convertToMutableFunction(term2Change);
                mutableList.add(newTerm);
            } else {
                // Variables and constants are Term-instances
                mutableList.add((Term)nextTerm);
            }

        }
        return mutableList;
    }

    private ImmutableList<ImmutableTerm> convertTerms(Function functionalTermToClone) {
        ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
        for (Term term : functionalTermToClone.getTerms()) {
            builder.add(convertIntoImmutableTerm(term));
        }
        return builder.build();
    }

    private static class NotAFunctionSymbolException extends MinorOntopInternalBugException {
        private NotAFunctionSymbolException(String message) {
            super(message);
        }
    }
}
