package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;

public class OntopModelTestFunctionSymbol extends PredicateImpl implements FunctionSymbol {

    protected OntopModelTestFunctionSymbol(@Nonnull String name, int arity) {
        super(name, arity);
    }

    @Override
    public Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> analyzeInjectivity(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonFreeVariables, VariableNullability variableNullability, VariableGenerator variableGenerator, TermFactory termFactory) {
        return Optional.empty();
    }

    @Override
    public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments, VariableNullability childNullability, TermFactory termFactory) {
        return null;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        return null;
    }

    @Override
    public TermType getExpectedBaseType(int index) {
        return null;
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm, TermFactory termFactory, VariableNullability variableNullability) {
        return null;
    }

    @Override
    public IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        return null;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isDeterministic() {
        return false;
    }

    @Override
    public boolean isAggregation() {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return false;
    }

    @Override
    public Stream<Variable> proposeProvenanceVariables(ImmutableList<? extends ImmutableTerm> terms) {
        return null;
    }

    @Override
    public FunctionalTermSimplification simplifyAsGuaranteedToBeNonNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory) {
        return null;
    }

    @Override
    public boolean shouldBeDecomposedInUnion() {
        return true;
    }
}
