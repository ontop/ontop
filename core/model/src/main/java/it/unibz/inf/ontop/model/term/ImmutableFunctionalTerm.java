package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.EvaluationResultImpl;
import it.unibz.inf.ontop.model.type.TypeInference;

import java.util.Optional;

/**
 * Functional term that is declared as immutable.
 *
 */
public interface ImmutableFunctionalTerm extends NonVariableTerm, NonConstantTerm {

    ImmutableList<? extends ImmutableTerm> getTerms();

    ImmutableTerm getTerm(int index);

    FunctionSymbol getFunctionSymbol();

    int getArity();

    ImmutableSet<Variable> getVariables();

    default TypeInference inferType() throws FatalTypingException {
        FunctionSymbol functionSymbol = getFunctionSymbol();
        return functionSymbol.inferType(getTerms());
    }


    interface EvaluationResult {

        enum Status {
            SAME_EXPRESSION,
            SIMPLIFIED_EXPRESSION,
            IS_NULL,
            IS_FALSE,
            IS_TRUE
        }

        /**
         * Only when getStatus() == SIMPLIFIED_EXPRESSION
         */
        Optional<ImmutableExpression> getSimplifiedExpression();

        Status getStatus();


        static EvaluationResult declareSimplifiedExpression(ImmutableExpression simplifiedExpression) {
            return EvaluationResultImpl.declareSimplifiedExpression(simplifiedExpression);
        }

        static EvaluationResult declareSameExpression() {
            return EvaluationResultImpl.declareSameExpression();
        }

        static EvaluationResult declareIsNull() {
            return EvaluationResultImpl.declareIsNull();
        }

        static EvaluationResult declareIsFalse() {
            return EvaluationResultImpl.declareIsFalse();
        }

        static EvaluationResult declareIsTrue() {
            return EvaluationResultImpl.declareIsTrue();
        }
    }
}
