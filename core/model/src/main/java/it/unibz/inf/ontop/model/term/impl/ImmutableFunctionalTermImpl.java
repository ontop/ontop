package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable implementation
 */
public abstract class ImmutableFunctionalTermImpl implements ImmutableFunctionalTerm {

    private final FunctionSymbol functionSymbol;
    private final ImmutableList<? extends ImmutableTerm> terms;
    protected final TermFactory termFactory;

    // LAZY
    @Nullable
    private ImmutableSet<Variable> variables;

    /**
     * Lazy cache for toString()
     */
    private String string;

    protected ImmutableFunctionalTermImpl(FunctionSymbol functor, TermFactory termFactory, ImmutableTerm... terms) {
        this(functor, ImmutableList.copyOf(terms), termFactory);
    }

    protected ImmutableFunctionalTermImpl(FunctionSymbol functionSymbol, ImmutableList<? extends ImmutableTerm> terms,
                                          TermFactory termFactory) {
        this.functionSymbol = functionSymbol;
        // No problem since the list is immutable
        this.terms = terms;
        this.termFactory = termFactory;
        string = null;
        variables = null;

        if (functionSymbol.getArity() != terms.size()) {
            throw new IllegalArgumentException("Arity violation: " + functionSymbol + " was expecting " + functionSymbol.getArity()
            + ", not " + terms.size());
        }
    }

    @Override
    public ImmutableTerm getTerm(int index) {
        return terms.get(index);
    }

    @Override
    public FunctionSymbol getFunctionSymbol() {
        return functionSymbol;
    }

    @Override
    public int getArity() {
        return functionSymbol.getArity();
    }

    @Override
    public ImmutableList<? extends ImmutableTerm> getTerms() {
        return terms;
    }

    @Override
    public synchronized ImmutableSet<Variable> getVariables() {
        if (variables == null) {
            variables = getVariableStream()
                    .collect(ImmutableCollectors.toSet());
        }
        return variables;
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return variables == null
                ? terms.stream()
                .flatMap(ImmutableTerm::getVariableStream)
                : variables.stream();
    }

    /**
     * Cached toString()
     */
    @Override
    public String toString() {
        if (string == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(functionSymbol.toString());
            sb.append("(");

            List<String> argumentStrings = terms.stream()
                    .map(ImmutableTerm::toString)
                    .collect(Collectors.toList());

            sb.append(String.join(",", argumentStrings));
            sb.append(")");
            string = sb.toString();
        }
        return string;
    }

    /**
     * TODO: A bit hacky: only for the functional term
     * that derives from ImmutableFunctionalTermImpl
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableFunctionalTerm) {
            return toString().equals(other.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
        return functionSymbol.evaluateStrictEq(getTerms(), otherTerm, termFactory, variableNullability);
    }

    @Override
    public IncrementalEvaluation evaluateIsNotNull(VariableNullability variableNullability) {
        return functionSymbol.evaluateIsNotNull(getTerms(), termFactory, variableNullability);
    }

    @Override
    public ImmutableTerm simplify(VariableNullability variableNullability) {
        return functionSymbol.simplify(getTerms(), termFactory, variableNullability);
    }

    @Override
    public ImmutableTerm simplify() {
        return functionSymbol.simplify(getTerms(), termFactory,
                termFactory.createDummyVariableNullability(this));
    }

    @Override
    public Optional<FunctionalTermDecomposition> analyzeInjectivity(ImmutableSet<Variable> nonFreeVariables,
                                                                    VariableNullability variableNullability,
                                                                    VariableGenerator variableGenerator) {
        return getFunctionSymbol().analyzeInjectivity(getTerms(), nonFreeVariables, variableNullability, variableGenerator, termFactory);
    }

    @Override
    public Stream<Variable> proposeProvenanceVariables() {
        return functionSymbol.proposeProvenanceVariables(getTerms());
    }

    @Override
    public FunctionalTermSimplification simplifyAsGuaranteedToBeNonNull() {
        return functionSymbol.simplifyAsGuaranteedToBeNonNull(getTerms(), termFactory);
    }

    @Override
    public boolean canBePostProcessed() {
        return functionSymbol.canBePostProcessed(terms);
    }

    @Override
    public boolean isNullable(ImmutableSet<Variable> nullableVariables) {
        ImmutableSet<Integer> nullableIndexes = IntStream.range(0, getArity())
                .filter(i -> getTerm(i).isNullable(nullableVariables))
                .boxed()
                .collect(ImmutableCollectors.toSet());
        return functionSymbol.isNullable(nullableIndexes);
    }
}
