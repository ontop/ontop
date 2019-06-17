package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

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

    @Override
    default Optional<TermTypeInference> inferType() {
        FunctionSymbol functionSymbol = getFunctionSymbol();
        return functionSymbol.inferType(getTerms());
    }

    /**
     * Returns true if it can be post-processed modulo some decomposition
     * (i.e. some sub-terms may not post-processed, but the top function symbol yes)
     */
    boolean canBePostProcessed();

    /**
     * Returns an empty optional when no decomposition is possible
     *
     * In the decomposition, the liftable term is injective.
     *
     */
    Optional<FunctionalTermDecomposition> analyzeInjectivity(ImmutableSet<Variable> nonFreeVariables,
                                                             VariableNullability variableNullability,
                                                             VariableGenerator variableGenerator);

    /**
     * Returns some variables are required to non-null for the functional term to be non-null.
     *
     * The stream is NOT guaranteed to be COMPLETE
     *
     * TODO: find a better name
     */
    Stream<Variable> proposeProvenanceVariables();

    interface FunctionalTermDecomposition {

        /**
         * Part of the functional that is liftable (e.g. injective)
         */
        ImmutableFunctionalTerm getLiftableTerm();

        /**
         * Contains the sub-terms that are not liftable.
         * For each of them, a fresh variable has been created.
         */
        Optional<ImmutableMap<Variable, ImmutableFunctionalTerm>> getSubTermSubstitutionMap();
    }
}
