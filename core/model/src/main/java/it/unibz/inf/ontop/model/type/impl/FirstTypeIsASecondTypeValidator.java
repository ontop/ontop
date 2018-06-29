package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;


public class FirstTypeIsASecondTypeValidator extends SimpleArgumentValidator {

    public FirstTypeIsASecondTypeValidator(ImmutableList<TermType> expectedBaseTypes) {
        super(expectedBaseTypes);
        if (expectedBaseTypes.size() < 2)
            throw new IllegalArgumentException("At least two arguments were expected : " + expectedBaseTypes);
    }

    @Override
    public void validate(ImmutableList<Optional<TermTypeInference>> argumentTypes) throws FatalTypingException {
        super.validate(argumentTypes);

        Optional<TermType> optionalFirstType = argumentTypes.get(0)
                .flatMap(TermTypeInference::getTermType);
        Optional<TermType> optionalSecondType = argumentTypes.get(1)
                .flatMap(TermTypeInference::getTermType);

        if (optionalFirstType.isPresent() && optionalSecondType.isPresent()) {

            if (!optionalFirstType.get().isA(optionalSecondType.get()))
                throw new FirstTypeIsNotASecondTypeException(
                        optionalFirstType.get(),
                        optionalSecondType.get());
        }
    }


    private static class FirstTypeIsNotASecondTypeException extends FatalTypingException {

        private FirstTypeIsNotASecondTypeException(TermType firstTermType, TermType secondTermType) {
            super("The first argument is a " + firstTermType + "and therefore is not a " + secondTermType
                    + " like the second argument");
        }
    }
}
