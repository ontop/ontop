package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.TermType;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER_TYPES;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    @Override
    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        // No need to call super.postprocessInferredType()
        return optionalTermType
                .map(t -> INTEGER_TYPES.contains(t.getColType()) ? DATA_FACTORY.getTermType(INTEGER) : t);
    }
}
