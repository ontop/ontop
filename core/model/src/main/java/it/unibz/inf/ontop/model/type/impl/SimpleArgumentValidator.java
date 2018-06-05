package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.AbstractTermTypeException;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.type.ArgumentValidator;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeInference;

import java.util.Optional;
import java.util.stream.IntStream;


public class SimpleArgumentValidator implements ArgumentValidator {

    private final ImmutableList<TermType> expectedBaseTypes;

    public SimpleArgumentValidator(ImmutableList<TermType> expectedBaseTypes) {
        this.expectedBaseTypes = expectedBaseTypes;
    }

    @Override
    public void validate(ImmutableList<TypeInference> argumentTypes) throws FatalTypingException {

        if (expectedBaseTypes.size() != argumentTypes.size()) {
            throw new IllegalArgumentException("Arity mismatch between " + argumentTypes + " and " + expectedBaseTypes);
        }

        /*
         * Checks the argument types
         */
        IntStream.range(0, argumentTypes.size())
                .forEach(i -> argumentTypes.get(i)
                        .getTermType()
                        .ifPresent(t -> checkTypes(expectedBaseTypes.get(i), t)));
    }

    /**
     * Can be overloaded
     */
    protected void checkTypes(TermType expectedBaseType, TermType argumentType) {
        if (argumentType.isAbstract())
            throw new AbstractTermTypeException(argumentType);

        if (!argumentType.isA(expectedBaseType)) {
            throw new FatalTypingException(expectedBaseType, argumentType);
        }
    }

    @Override
    public TermType getExpectedBaseType(int index) {
        return expectedBaseTypes.get(index);
    }

    @Override
    public ImmutableList<TermType> getExpectedBaseArgumentTypes() {
        return expectedBaseTypes;
    }
}
