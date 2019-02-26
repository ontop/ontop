package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

/**
 * FunctionSymbols are the functors needed to build ImmutableFunctionalTerms
 */
public interface FunctionSymbol extends Predicate {

    boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, VariableNullability variableNullability);

    FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                               VariableNullability childNullability);


    Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms);

    ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                           TermFactory termFactory, VariableNullability variableNullability);

    TermType getExpectedBaseType(int index);

    IncrementalEvaluation evaluateStrictEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm,
                                           TermFactory termFactory, VariableNullability variableNullability);

    IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                            VariableNullability variableNullability);

    /**
     * 1. When a functional term simplifies itself in a BOTTOM-UP manner:
     *     Returns true if is guaranteed to "simplify itself" as a Constant when receiving Constants as arguments
     *     (outside the optimization phase) .
     *
     * 2.  When a functional term simplifies itself in a TOP-DOWN manner (e.g. IF-THEN-ELSE functional terms):
     *     The permission for post-processing may depend on the ability of sub-functional terms to be post-processed
     *      or safely evaluated by the DB engine.
     *  (Recall that top-down evaluation allows some arguments not to be evaluated when their pre-conditions are not met.
     *   This is particularly valuable for preventing fatal errors).
     *
     */
    boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments);

    boolean isDeterministic();

    boolean isNullable(ImmutableSet<Integer> nullableIndexes);


    interface FunctionalTermNullability {

        boolean isNullable();

        /**
         * When the nullability of a functional term is bound to the nullability
         * of a variable
         */
        Optional<Variable> getBoundVariable();
    }


}
