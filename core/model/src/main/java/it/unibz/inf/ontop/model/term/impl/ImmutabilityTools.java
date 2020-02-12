package it.unibz.inf.ontop.model.term.impl;

import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
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
        } else if (isGroundTerm(term)) {
            return castIntoGroundTerm(term, termFactory);
        } else {
            throw new IllegalArgumentException("Not a variable nor a ground term: " + term);
        }
    }

    public static class NonGroundTermException extends RuntimeException {
        protected  NonGroundTermException(String message) {
            super(message);
        }
    }


    private static GroundTerm castIntoGroundTerm(Term term, TermFactory termFactory) throws NonGroundTermException{
        if (term instanceof GroundTerm)
            return (GroundTerm) term;

        if (term instanceof Function) {
            Function functionalTerm = (Function) term;
            // Recursive
            FunctionSymbol functionSymbol = Optional.of(functionalTerm.getFunctionSymbol())
                    .filter(p -> p instanceof FunctionSymbol)
                    .map(p -> (FunctionSymbol)p)
                    .orElseThrow(() -> new NonGroundTermException(term + "is not using a function symbol but a"
                            + functionalTerm.getFunctionSymbol().getClass()));

            ImmutableList.Builder<GroundTerm> termBuilder = ImmutableList.builder();
            for (Term t : functionalTerm.getTerms()) {
                termBuilder.add(castIntoGroundTerm(t, termFactory));
            }

            return new GroundFunctionalTermImpl(termBuilder.build(), functionSymbol, termFactory);
        }

        throw new NonGroundTermException(term + " is not a ground term");
    }

    /**
     * Returns true if is a ground term (even if it is not explicitly typed as such).
     */
    private static boolean isGroundTerm(Term term) {
        if (term instanceof Function) {
            Set<Variable> variables = new HashSet<>();
            addReferencedVariablesTo(variables, term);
            return variables.isEmpty();
        }
        return term instanceof Constant;
    }

    private static void addReferencedVariablesTo(Collection<Variable> vars, Term term) {
        if (term instanceof Function) {
            for (Term t : ((Function)term).getTerms())
                addReferencedVariablesTo(vars, t);
        }
        else if (term instanceof Variable) {
            vars.add((Variable)term);
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

    public Function convertToMutableFunction(Predicate predicateOrFunctionSymbol,
                                             ImmutableList<? extends ImmutableTerm> terms) {
        return termFactory.getFunction(predicateOrFunctionSymbol, convertToMutableTerms(terms));
    }

    private List<Term> convertToMutableTerms(ImmutableList<? extends ImmutableTerm> terms) {
        List<Term> mutableList = new ArrayList<>(terms.size());
        for (ImmutableTerm nextTerm : terms) {
            if (nextTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm term2Change = (ImmutableFunctionalTerm) nextTerm;
                Function newTerm = convertToMutableFunction(term2Change.getFunctionSymbol(), term2Change.getTerms());
                mutableList.add(newTerm);
            }
            else {
                // Variables and constants are Term-instances
                mutableList.add((Term) nextTerm);
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
