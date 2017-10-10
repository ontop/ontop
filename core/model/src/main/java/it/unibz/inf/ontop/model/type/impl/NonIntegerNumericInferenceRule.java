package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.type.COL_TYPE.DECIMAL;
import static it.unibz.inf.ontop.model.type.COL_TYPE.INTEGER;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    @Override
    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        // No need to call super.postprocessInferredType()
        return optionalTermType
                .map(t -> t.equals(TYPE_FACTORY.getTermType(INTEGER)) ? TYPE_FACTORY.getTermType(DECIMAL) : t);
    }
}
