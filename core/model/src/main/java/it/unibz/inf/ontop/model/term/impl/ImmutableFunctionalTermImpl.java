package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

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
    private final TermFactory termFactory;

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
    public ImmutableSet<Variable> getVariables() {
        return getVariableStream()
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return terms.stream()
                .flatMap(ImmutableTerm::getVariableStream);
    }

    @Override
    public ImmutableFunctionalTermImpl clone() {
        return this;
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
     * A bit hacky: only for the functional term
     * that derives from ImmutableFunctionalTermImpl
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableFunctionalTerm) {
            return toString().equals(other.toString());
        }
        else {
            return false;
        }
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
