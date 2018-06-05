package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;

/**
 * TODO: explain
 */
public interface TermTypeInferenceRule {


    /**
     * TODO: explain
     *
     */
    TypeInference inferTypeFromArgumentTypes(ImmutableList<TypeInference> actualArgumentTypes)
            throws FatalTypingException;
}
