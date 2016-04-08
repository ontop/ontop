package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.TermType;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.TermTypeImpl;

import java.util.Optional;

/**
 * Does not look at the terms, always returns the same type.
 */
public class PredefinedTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

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
    protected Optional<TermType> deduceType(ImmutableList<Optional<TermType>> argumentTypes) {
        return Optional.of(predefinedType);
    }
}
