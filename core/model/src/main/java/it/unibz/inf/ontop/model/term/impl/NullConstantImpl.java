package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeInference;

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
    public boolean isGround() {
        return true;
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return Stream.empty();
    }

    @Override
    public TypeInference inferType() throws FatalTypingException {
        return TypeInference.declareNotDetermined();
    }

    @Override
    public EvaluationResult evaluateEq(ImmutableTerm otherTerm) {
        return EvaluationResult.declareIsFalse();
    }

    @Override
    public Term clone() {
        return new NullConstantImpl();
    }
}
