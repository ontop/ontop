package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;

import java.util.Optional;


/**
 * TODO: explain
 */
public abstract class AbstractTermTypeInferenceRule implements TermTypeInferenceRule {

    @Override
    public Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes)
            throws FatalTypingException {

        return postprocessInferredType(reduceInferredTypes(argumentTypes));
    }

    /**
     * Hook, does nothing by default
     */
    protected Optional<TermTypeInference> postprocessInferredType(Optional<TermTypeInference> typeInference) {
        return typeInference;
    }

    /**
     * TODO: find a better name
     */
    protected abstract Optional<TermTypeInference> reduceInferredTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes);

}
