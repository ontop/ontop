package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * Does not look at the terms, always returns the same type.
 */
public class PredefinedTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    private final TermType predefinedType;

    /**
     * Standard constructor
     */
    public PredefinedTermTypeInferenceRule(TermType predefinedType) {
        this.predefinedType = predefinedType;
    }

    @Override
    protected Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes) {
        return Optional.of(predefinedType);
    }
}
