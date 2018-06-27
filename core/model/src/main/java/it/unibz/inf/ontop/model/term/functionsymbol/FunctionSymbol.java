package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

/**
 * FunctionSymbols are the functors needed to build ImmutableFunctionalTerms
 */
public interface FunctionSymbol extends Predicate {

    FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                               VariableNullability childNullability);


    Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException;

    ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, boolean isInConstructionNodeInOptimizationPhase,
                           TermFactory termFactory);

    default EvaluationResult evaluateEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm,
                                TermFactory termFactory) {
        throw new RuntimeException("TODO: implement it");
    }


    interface FunctionalTermNullability {

        boolean isNullable();

        /**
         * When the nullability of a functional term is bound to the nullability
         * of a variable
         */
        Optional<Variable> getBoundVariable();
    }


}
