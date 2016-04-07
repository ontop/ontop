package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Predicate;
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

    /**
     * Do not use this construction if you know the language tag!
     */
    public PredefinedTermTypeInferenceRule(Predicate.COL_TYPE predefinedColType) {
        this(new TermTypeImpl(predefinedColType));
    }

    @Override
    protected Optional<TermType> deduceType(ImmutableList<Optional<TermType>> argumentTypes) {
        return Optional.of(predefinedType);
    }
}
