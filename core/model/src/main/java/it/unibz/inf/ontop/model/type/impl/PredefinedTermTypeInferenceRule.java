package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

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
        this(DATA_FACTORY.getTermType(predefinedColType));
    }

    @Override
    protected Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes) {
        return Optional.of(predefinedType);
    }
}
