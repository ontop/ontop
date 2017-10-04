package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

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
    public PredefinedTermTypeInferenceRule(COL_TYPE predefinedColType) {
        this(TYPE_FACTORY.getTermType(predefinedColType));
    }

    @Override
    protected Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes) {
        return Optional.of(predefinedType);
    }
}
