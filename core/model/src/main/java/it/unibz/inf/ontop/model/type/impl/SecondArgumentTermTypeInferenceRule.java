package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;


public class SecondArgumentTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    @Override
    protected Optional<TermTypeInference> reduceInferredTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
        if (argumentTypes.size() < 2) {
            throw new IllegalStateException("At least two arguments is required by the SecondArgumentTermTypeReasoner");
        }
        return argumentTypes.get(1);
    }
}
