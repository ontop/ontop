package it.unibz.inf.ontop.model.type;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;

/**
 * TODO: refactor
 */
public interface ArgumentValidator {

    void validate(ImmutableList<TypeInference> argumentTypes) throws FatalTypingException;

    TermType getExpectedBaseType(int index);

    ImmutableList<TermType> getExpectedBaseArgumentTypes();

    default boolean areCompatible(ImmutableList<TypeInference> argumentTypes) {
        try {
            validate(argumentTypes);
            return true;
        } catch (FatalTypingException e) {
            return false;
        }
    }

}
