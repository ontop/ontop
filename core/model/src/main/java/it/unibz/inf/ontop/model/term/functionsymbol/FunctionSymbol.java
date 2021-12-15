package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * FunctionSymbols are the functors needed to build ImmutableFunctionalTerms
 */
public interface FunctionSymbol extends Predicate {

    Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> analyzeInjectivity(
            ImmutableList<? extends ImmutableTerm> arguments,
            ImmutableSet<Variable> nonFreeVariables,
            VariableNullability variableNullability,
            VariableGenerator variableGenerator, TermFactory termFactory);

    FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                               VariableNullability childNullability, TermFactory termFactory);


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

    /**
     * Returns true for SUM, AVG, etc.
     */
    boolean isAggregation();

    boolean isNullable(ImmutableSet<Integer> nullableIndexes);

    /**
     * Returns some variables are required to non-null for the functional term to be non-null.
     *
     * The stream is NOT guaranteed to be COMPLETE
     *
     * TODO: find a better name
     */
    Stream<Variable> proposeProvenanceVariables(ImmutableList<? extends ImmutableTerm> terms);

    /**
     * See ImmutableFunctionalTerm.simplifyAsGuaranteedToBeNonNull() for an explanation
     *
     */
    FunctionalTermSimplification simplifyAsGuaranteedToBeNonNull(ImmutableList<? extends ImmutableTerm> terms,
                                                                 TermFactory termFactory);

    /**
     * Returns true if it can be decomposed to be lifted above a UNION.
     *
     * One good reason for not decomposed is when some arguments may have very different types
     * (avoiding strong-typing issues).
     */
    boolean shouldBeDecomposedInUnion();


    interface FunctionalTermNullability {

        boolean isNullable();

        /**
         * When the nullability of a functional term is bound to the nullability
         * of a variable
         */
        Optional<Variable> getBoundVariable();
    }

}
