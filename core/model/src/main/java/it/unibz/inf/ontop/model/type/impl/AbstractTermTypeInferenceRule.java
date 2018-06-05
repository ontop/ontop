package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;
import it.unibz.inf.ontop.model.type.TypeInference;


/**
 * TODO: explain
 */
public abstract class AbstractTermTypeInferenceRule implements TermTypeInferenceRule {

    @Override
    public TypeInference inferTypeFromArgumentTypes(ImmutableList<TypeInference> argumentTypes)
            throws FatalTypingException {

        return postprocessInferredType(reduceInferredTypes(argumentTypes));
    }

    /**
     * Hook, does nothing by default
     */
    protected TypeInference postprocessInferredType(TypeInference typeInference) {
        return typeInference;
    }

    /**
     * TODO: find a better name
     */
    protected abstract TypeInference reduceInferredTypes(ImmutableList<TypeInference> argumentTypes);

}
