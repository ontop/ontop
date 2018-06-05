package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.TypeInference;

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
    protected TypeInference postprocessInferredType(TypeInference typeInference) {
        return typeInference
                .getTermType()
                .map(t -> t.equals(integerDatatype) ? decimalDatatype : t)
                .map(TypeInference::declareTermType)
                .orElseGet(TypeInference::declareNotDetermined);
    }
}
