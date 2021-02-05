package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.stream.Stream;

public class NullConstantImpl extends AbstractNonFunctionalTerm implements Constant {

    private final String nullLexicalValue;

    protected NullConstantImpl(String nullLexicalValue) {
        this.nullLexicalValue = nullLexicalValue;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public String getValue() {
        return nullLexicalValue;
    }

    @Override
    public Optional<TermType> getOptionalType() {
        return Optional.empty();
    }

    @Override
    public boolean isGround() {
        return true;
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return Stream.empty();
    }

    @Override
    public Optional<TermTypeInference> inferType() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return nullLexicalValue;
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
        return IncrementalEvaluation.declareIsNull();
    }

    @Override
    public IncrementalEvaluation evaluateIsNotNull(VariableNullability variableNullability) {
        return IncrementalEvaluation.declareIsFalse();
    }

    @Override
    public boolean isNullable(ImmutableSet<Variable> nullableVariables) {
        return true;
    }
}
