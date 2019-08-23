package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Optional;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    private final RDFDatatype integerDatatype;
    private final RDFDatatype decimalDatatype;

    public NonIntegerNumericInferenceRule(TypeFactory typeFactory) {
        integerDatatype = typeFactory.getXsdIntegerDatatype();
        decimalDatatype = typeFactory.getXsdDecimalDatatype();
    }

    @Override
    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        return optionalTermType
                .map(t -> t.equals(integerDatatype) ? decimalDatatype : t);
    }
}
