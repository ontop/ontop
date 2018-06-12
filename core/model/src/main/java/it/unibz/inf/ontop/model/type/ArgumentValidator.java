package it.unibz.inf.ontop.model.type;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;

import java.util.Optional;

/**
 * TODO: refactor
 */
public interface ArgumentValidator {

    void validate(ImmutableList<Optional<TermTypeInference>> argumentTypes) throws FatalTypingException;

    TermType getExpectedBaseType(int index);

    ImmutableList<TermType> getExpectedBaseArgumentTypes();

    default boolean areCompatible(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
        try {
            validate(argumentTypes);
            return true;
        } catch (FatalTypingException e) {
            return false;
        }
    }

}
