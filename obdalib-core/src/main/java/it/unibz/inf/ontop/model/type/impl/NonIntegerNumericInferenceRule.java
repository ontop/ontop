package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.DECIMAL;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    @Override
    protected Optional<TermType> postprocessDeducedType(Optional<TermType> optionalTermType) {
        if (optionalTermType.isPresent()) {
            switch(optionalTermType.get().getColType()) {
                case INTEGER:
                case NEGATIVE_INTEGER:
                case NON_NEGATIVE_INTEGER:
                case POSITIVE_INTEGER:
                case NON_POSITIVE_INTEGER:
                case INT:
                case UNSIGNED_INT:
                    return Optional.of(new TermTypeImpl(DECIMAL));
                default:
                    return optionalTermType;
            }
        }
        return optionalTermType;
    }
}
