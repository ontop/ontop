package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TypeInference;


public class SecondArgumentTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    @Override
    protected TypeInference reduceInferredTypes(ImmutableList<TypeInference> argumentTypes) {
        if (argumentTypes.size() < 2) {
            throw new IllegalStateException("At least two arguments is required by the SecondArgumentTermTypeReasoner");
        }
        return argumentTypes.get(1);
    }
}
