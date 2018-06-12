package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface TermTypeInferenceRule {


    /**
     * TODO: explain
     *
     */
    Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> actualArgumentTypes)
            throws FatalTypingException;
}
