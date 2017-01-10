package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER_TYPES;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    @Override
    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        // No need to call super.postprocessInferredType()
        return optionalTermType
                .map(t -> INTEGER_TYPES.contains(t.getColType()) ? DATA_FACTORY.getTermType(INTEGER) : t);
    }
}
