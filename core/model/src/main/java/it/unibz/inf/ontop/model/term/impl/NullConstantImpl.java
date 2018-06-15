package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.stream.Stream;

public class NullConstantImpl implements Constant {

    private static final String NULL_STRING = "null";

    protected NullConstantImpl() {
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public String getValue() {
        return NULL_STRING;
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
    public Optional<TermTypeInference> inferType() throws FatalTypingException {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return NULL_STRING;
    }

    @Override
    public EvaluationResult evaluateEq(ImmutableTerm otherTerm) {
        return EvaluationResult.declareIsFalse();
    }

    /**
     * TODO: get rid of this method and stop this cloning practice
     */
    @Override
    public Term clone() {
        return this;
    }
}
