package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.DECIMAL;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER_TYPES;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    @Override
    protected Optional<TermType> postprocessDeducedType(Optional<TermType> optionalTermType) {
        // No need to call super.postprocessDeducedType()
        return optionalTermType
                .map(t -> INTEGER_TYPES.contains(t.getColType()) ? TermTypes.DECIMAL_TERM_TYPE : t);
    }
}
